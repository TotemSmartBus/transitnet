package whu.edu.cs.transitnet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import whu.edu.cs.transitnet.bean.BusRouteMap;
import whu.edu.cs.transitnet.dao.StopTimesDao;
import whu.edu.cs.transitnet.dao.StopsDao;
import whu.edu.cs.transitnet.dao.TripsDao;
import whu.edu.cs.transitnet.pojo.StopTimesEntity;
import whu.edu.cs.transitnet.pojo.TripsEntity;
import whu.edu.cs.transitnet.service.index.BusStop;
import whu.edu.cs.transitnet.service.index.BusTrip;
import whu.edu.cs.transitnet.service.index.BusTripEdge;
import whu.edu.cs.transitnet.service.index.TripId;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class BusRouteMapSerializationService {
    @Autowired
    StopsDao stopsDao;

    @Autowired
    TripsDao tripsDao;

    @Autowired
    StopTimesDao stopTimesDao;

    /**
     * 静态图，程序运行起来后直接构建
     */
//    @PostConstruct
    public void init() {
        BusRouteMap busRouteMap = new BusRouteMap();

        Long startTime = System.currentTimeMillis();

        // 1. 获取所有公交站点 [id, lat, lon]
        List<BusStop> busStops = stopsDao.findAllBusStops();
        busRouteMap.setBusStops(busStops);

        int busStopsSize = busStops.size();
        //    构建索引表
        HashMap<Integer, BusStop> orderStopIndex = new HashMap<>();
        HashMap<String, Integer> stopIdOrderIndex = new HashMap<>();
        for (int i = 0; i < busStopsSize; i++) {
            String stopId = busStops.get(i).getStopId();
            orderStopIndex.put(i, busStops.get(i));
            stopIdOrderIndex.put(stopId, i);
        }
        busRouteMap.setOrderStopIndex(orderStopIndex);
        busRouteMap.setStopIdOrderIndex(stopIdOrderIndex);

        // 2. 获取给定时间范围内的所有行程 [TripsEntity] -> 实际只需要用到 TripId 属性
        Date endDate = Date.valueOf("2023-10-01");
        List<TripsEntity> tripsEntityList = tripsDao.findAllTripsByEndDate(endDate);

        System.out.println("[Size of BusTrips, including zero-stop trips] " + tripsEntityList.size());

        int count = 0;
        List<BusTrip> busTrips = new ArrayList<>();
        List<BusTripEdge> busTripEdges = new ArrayList<>();
        HashMap<String, HashMap<String, BusTripEdge>> stopEdgeIndex = new HashMap<>();
        // 3. 利用这些 TripIds 来构建公交网的边
        for(TripsEntity tripsEntity : tripsEntityList) {
            count++;
            System.out.println(count);
            // 获取每个 tripid 经过的站点序列
            TripId tripId = new TripId(tripsEntity.getTripId());
            List<StopTimesEntity> stopTimesEntityList = stopTimesDao.findPassedStopsByTripId(tripId.toString());
            int stopsSize = stopTimesEntityList.size();


            if (stopsSize == 0) continue;


            // 构建 busTrips
            busTrips.add(new BusTrip(tripId, stopTimesEntityList.get(0).getStopId(), stopTimesEntityList.get(stopsSize-1).getStopId(),
                    stopTimesEntityList.get(0).getDepartureTime(), stopTimesEntityList.get(stopsSize - 1).getArrivalTime()));

            // [边界条件]
            // 构建 busTripEdges 和 stopEdgeIndex
            for(int i = 0; i <= stopsSize - 2; i++) {
                StopTimesEntity stopTimesEntity1 = stopTimesEntityList.get(i);
                String stopId1 = stopTimesEntity1.getStopId();
                StopTimesEntity stopTimesEntity2 = stopTimesEntityList.get(i + 1);
                String stopId2 = stopTimesEntity2.getStopId();

                BusTripEdge busTripEdge = new BusTripEdge(stopId1, stopId2);

                if (stopEdgeIndex.containsKey(stopId1)) {
                    // 如果存在边
                    if (stopEdgeIndex.get(stopId1).containsKey(stopId2)) {
                        busTripEdge = stopEdgeIndex.get(stopId1).get(stopId2);
                        busTripEdge.addTripIds(tripId);
                    } else {
                        // 如果只存在第一个点不存在第二个点
                        busTripEdge.addTripIds(tripId);
                        stopEdgeIndex.get(stopId1).put(stopId2, busTripEdge);

                        busTripEdges.add(busTripEdge);
                    }
                } else {
                    // 如果不存在这个边
                    HashMap<String, BusTripEdge> stringBusTripEdgeHashMap = new HashMap<>();
                    busTripEdge.addTripIds(tripId);
                    stringBusTripEdgeHashMap.put(stopId2, busTripEdge);
                    stopEdgeIndex.put(stopId1, stringBusTripEdgeHashMap);

                    busTripEdges.add(busTripEdge);
                }

            }
        }

        busRouteMap.setBusTrips(busTrips);
        busRouteMap.setBusTripEdges(busTripEdges);
        busRouteMap.setStopEdgeIndex(stopEdgeIndex);

        Long endTime = System.currentTimeMillis();
        System.out.println("[Construction time] " + (endTime - startTime) / 1000 / 30 + " min");

        // 序列化
        try {
            File file = new File("./src/main/" + "bus_route_map.txt");
            FileOutputStream fileOut = new FileOutputStream(file);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(busRouteMap);
            out.close();
            fileOut.close();
            System.out.println("序列化后的文件保存在：bus_route_map.txt");
        } catch (Exception i) {
            i.printStackTrace();
        }
    }
}
