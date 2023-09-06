package whu.edu.cs.transitnet.service;

import com.google.transit.realtime.GtfsRealtime;
import lombok.extern.slf4j.Slf4j;
import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GlobalCoordinates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import whu.edu.cs.transitnet.realtime.RealtimeService;
import whu.edu.cs.transitnet.realtime.Vehicle;
import whu.edu.cs.transitnet.utils.GeoUtil;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class StopSelectionService {

    @Autowired
    RealtimeService realtimeService;

    @Autowired
    GeodeticCalculator geodeticCalculator;

    @Value("${transitnet.realtime.url}")
    private URI vehiclePositionsUri;

    @Value("${transitnet.realtime.agency-name}")
    private String AgencyName = "Agency";

    private final Map<String, String> vehicleIdsByEntityIds = new HashMap<>();

    private final Map<String, Vehicle> vehiclesById = new ConcurrentHashMap<>();

    public void stopSelection() throws MalformedURLException {

        long startTime = System.currentTimeMillis();


        // 1、用户指定要搭乘的公交的行驶路线的id-routeId以及路线方向direction（0/1）
        String routeId = "Q06";
        int direction = 1;
        // 用户的位置 - lat lon
        ArrayList<Double> userLatAndLon = new ArrayList<>();
        // 用户的步行速度 - m/min 平均5km/h -> 83.333m/min
        Double walkSpeed = 83.0;

        // Attention:
        // (One-to-one)  route_id + direction_id ==> a series of ordered stop_ids
        // (One-to-many) route_id + direction_id ==> a series of trip_ids
        // (Many-to-one) Every trip_id for the specific route_id-direction_id pair has the same sequence of stop_ids

        // stop_id lat lon order
        // all stop_ids
        ArrayList<String> allStopIds = new ArrayList<>();
        // stop_id - [stop_lat, stop_lon]
        HashMap<String, ArrayList<Double>> stopsWithLatAndLon = new HashMap<>();
        // stop_id - its order in a trip
        HashMap<String, Integer> stopsWithOrder = new HashMap<>();

        // all trip_ids
        ArrayList<String> allTripIds = new ArrayList<>();

        // 2、根据routeId查找该路线经过的所有公交站点和相应的经纬度坐标
        // 连接sqlite数据库
        Connection c = null;
        // Statement stmt = null;
        // 查询所有stop_ids
        PreparedStatement pstmt = null;
        // 查询每个stop_id的stop_lat和stop_lon
        PreparedStatement pstmt1 = null;
        // 查询route_id和direction下的trip_id，用于实时公交判断
        PreparedStatement pstmt2 = null;
        try {
            Class.forName("org.sqlite.JDBC");
            // 打开数据库；这一行要使用我们搭建SQLite时的url
            c = DriverManager.getConnection("jdbc:sqlite:C://Windows//System32//gtfsdb//bin//gtfs.db");
            System.out.println("Opened database successfully");

            // 这里执行查询语句
            pstmt = c.prepareStatement("SELECT stop_id FROM route_stops WHERE route_id = ? AND direction_id = ?;");
            pstmt.setString(1, routeId);
            pstmt.setInt(2, direction);

            ResultSet rs = pstmt.executeQuery();

            pstmt1 = c.prepareStatement("SELECT stop_lat, stop_lon FROM stops WHERE stop_id = ?;");

            int order = 0;
            while (rs.next()) {
                order++; // 站点经过的顺序

                String s = rs.getString("stop_id");
                System.out.println(s);

                // 存储所有stop_id
                allStopIds.add(s);

                // 存储每个stop_id对应的顺序
                stopsWithOrder.put(s, order);

                // 查询每个stop_id的经纬度
                pstmt1.setString(1, s);
                ResultSet rs1 = pstmt1.executeQuery();
                Double lat = rs1.getDouble("stop_lat");
                Double lon = rs1.getDouble("stop_lon");

                // 把stop_id - [lat, lon]存到哈希表里
                ArrayList<Double> latAndLon = new ArrayList<>();
                latAndLon.add(lat);
                latAndLon.add(lon);
                stopsWithLatAndLon.put(s, latAndLon);

                // 打印哈希表看看
                System.out.println(stopsWithLatAndLon.get(s));
            }

            pstmt2 = c.prepareStatement("SELECT trip_id FROM trips WHERE route_id = ? AND direction_id = ?;");
            pstmt2.setString(1, routeId);
            pstmt2.setInt(2, direction);

            ResultSet rs2 = pstmt2.executeQuery();
            while (rs2.next()) {
                String s = rs2.getString("trip_id");
                allTripIds.add(s);
            }

        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Operation done successfully");

        // 3、找到用户左右两侧最近的公交站点
        // 目前版本：找到离用户最近的两个公交站点，没有区分左右
        // 但是按照常识，应该确实位于左右，再查资料看看
        // 暴力算法：计算所有公交站点和用户位置的距离
        // TODO: 算法优化（剪枝？）
        Double userLat = 40.680000;
        Double userLon = -73.790000;
        userLatAndLon.add(userLat);
        userLatAndLon.add(userLon);

        // 求出所有公交站点离用户的距离并存到哈希表里
        HashMap<String, Double> stopsWithDistance = new HashMap<>();
        for (String id : allStopIds) {
            ArrayList<Double> itsLatAndLon = stopsWithLatAndLon.get(id);
            // 调用计算两点距离的方法
            Double dis = GeoUtil.distance(userLatAndLon.get(0), itsLatAndLon.get(0), userLatAndLon.get(1), itsLatAndLon.get(1));
            stopsWithDistance.put(id, dis);
        }

        // 将哈希表按照距离进行从小到大的排序
        //     1) 将entrySet放入List集合中
        ArrayList<Map.Entry<String, Double>> arrayList = new ArrayList<>(stopsWithDistance.entrySet());
        //     2) 对哈希表的值进行排序
        //        对list进行排序，并通过Comparator传入自定义的排序规则
        Collections.sort(arrayList, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                Double t = o1.getValue() - o2.getValue();
                int flag = 1;
                if (t <= 0) {
                    flag = -1;
                }
                return flag;
            }
        });

        // 取出离用户最近的两个站点
        HashMap<String, Double> top2 = new HashMap<>();
        String key1 = arrayList.get(0).getKey();
        String key2 = arrayList.get(1).getKey();
        top2.put(key1, arrayList.get(0).getValue());
        top2.put(key2, arrayList.get(1).getValue());
        System.out.println(top2);

        // 判断两个站点的先后顺序
        String stopReachedFirst;
        String stopReachedSecond;
        if (stopsWithOrder.get(key1) < stopsWithOrder.get(key2)) {
            stopReachedFirst = key1;
            stopReachedSecond = key2;
        } else {
            stopReachedFirst = key2;
            stopReachedSecond = key1;
        }

        // 4、获取该routeId下的所有公交实时位置
        //   站点A -> 站点B
        //   找到位于这AB站点中间的公交和将要到达站点A的公交
        //   DONE: 实时数据获取和解析

        // 获取到的所有车辆实体
        List<Vehicle> vehicles = new ArrayList<>();
        vehicles = getRealtimeInfo(allTripIds);

        // 在路上的vehicle到达两个站点的时间
        HashMap<Vehicle, Double> stopRFBusTime = new HashMap<>();
        HashMap<Vehicle, Double> stopRSBusTime = new HashMap<>();

        // 要判断vehicle是否经过了这两个站点
        // 目前的粗略版本：计算距离；前提是采样频率较高
        // TODO: 精确判断

        // 两个站点之间的直线距离
        Double strLineDistBtwnStops = GeoUtil.distance(stopsWithLatAndLon.get(stopReachedFirst).get(0), stopsWithLatAndLon.get(stopReachedSecond).get(0),
                stopsWithLatAndLon.get(stopReachedFirst).get(1), stopsWithLatAndLon.get(stopReachedSecond).get(1));
        // 直线距离的平方
        Double btwn2 = Math.pow(strLineDistBtwnStops, 2);


        // TODO: 如果车辆位置和站点重合，需要加一个判断语句
        //       车速怎么确定？
        for (Vehicle v: vehicles) {
            Double vLat = v.getLat();
            Double vLon = v.getLon();
            Double strLineDistToFirstStop = GeoUtil.distance(vLat, stopsWithLatAndLon.get(stopReachedFirst).get(0), vLon, stopsWithLatAndLon.get(stopReachedFirst).get(1));
            Double strLineDistToSecondStop = GeoUtil.distance(vLat, stopsWithLatAndLon.get(stopReachedSecond).get(0), vLon, stopsWithLatAndLon.get(stopReachedSecond).get(1));

            // 判断两个底角是否是钝角
            Double first2 = Math.pow(strLineDistToFirstStop, 2);
            Double second2 = Math.pow(strLineDistToSecondStop, 2);
            Double cosAngleFirst = (btwn2 + first2 - second2) / (2 * strLineDistBtwnStops * strLineDistToFirstStop);
            Double cosAngleSecond = (btwn2 + second2 - first2) / (2 * strLineDistBtwnStops * strLineDistToSecondStop);
            if (cosAngleFirst < 0) {
                // 公交还没到第一个站点
                // 到第一个站点的时间
                Double vToFirst = strLineDistToFirstStop / v.getSpeed();
                stopRFBusTime.put(v, vToFirst);
                // 到第二个站点的时间
                Double vToSecond = strLineDistToSecondStop / v.getSpeed();
                stopRFBusTime.put(v, vToSecond);
            } else if (cosAngleFirst >= 0 && cosAngleSecond > 0) {
                // 公交在两个站点之间
                // 到第二个站点的时间
                Double vToSecond = strLineDistToSecondStop / v.getSpeed();
                stopRFBusTime.put(v, vToSecond);
            } else if (cosAngleSecond <= 0) {
                // 公交已经过了第二个站点
                // 什么也不做
            }
        }

        // 5、根据设计的算法决定用户应该去往哪个站点
        //    已经获取到一连串direction=?的tripid以及实时位置
        //    首先确定用户步行至两个站点的时间
        //    然后计算出分别最快能到达两个站点的公交id

        // 计算出用户步行至两个站点的时间
        Double stopRFWalkTime = stopsWithDistance.get(stopReachedFirst) / walkSpeed;
        Double stopRSWalkTime = stopsWithDistance.get(stopReachedSecond) / walkSpeed;

        Vehicle selectedBus;
        String selectedStop;
        Double waitingTime;
        Double walkingTime;

        // 公交都过站了
        if (stopRFBusTime.isEmpty() && stopRSBusTime.isEmpty()) {
            System.out.println("公交都过站了");
            selectedBus = null;
            selectedStop = stopRFWalkTime <= stopRSWalkTime ? stopReachedFirst : stopReachedSecond;
            waitingTime = Double.MIN_VALUE;
            walkingTime = stopRFWalkTime <= stopRSWalkTime ? stopRFWalkTime : stopRSWalkTime;

            System.out.println("选择前往站点：" + selectedStop);
            System.out.println("步行至站点时间：" + walkingTime + "分钟");
            System.out.println("下一辆即将到站的公交id：" + selectedBus.getId());
            System.out.println("等待公交到站预计耗时：" + waitingTime / 60 + "分钟");
        } else if (!stopRFBusTime.isEmpty() && stopRSBusTime.isEmpty()) {
            // 公交要到第一个站点了，那么也会到第二个站点，所以这个elseif有问题
            HashMap<Vehicle, Double> stopRFWaitingTime = new HashMap<>();

            Iterator<Map.Entry<Vehicle, Double>> iterator1 = stopRFBusTime.entrySet().iterator();
            while (iterator1.hasNext()) {
                Map.Entry<Vehicle, Double> entry = iterator1.next();
                Double dvalue = entry.getValue() - stopRFWalkTime;
                if (dvalue >= 0) {
                    stopRFWaitingTime.put(entry.getKey(), dvalue);
                }
            }
            // watingtime排序
            List<Map.Entry<Vehicle, Double>> list1 = new ArrayList<>(stopRFWaitingTime.entrySet());
            list1.sort(Map.Entry.comparingByValue());
            Vehicle stopRFBus = list1.get(0).getKey();
            Double stopRFMinTime = list1.get(0).getValue();

            selectedBus = stopRFBus;
            selectedStop = stopReachedFirst;
            waitingTime = stopRFMinTime;
            walkingTime = stopRFWalkTime;

            System.out.println("选择前往站点：" + selectedStop);
            System.out.println("步行至站点时间：" + walkingTime + "分钟");
            System.out.println("下一辆即将到站的公交id：" + selectedBus.getId());
            System.out.println("等待公交到站预计耗时：" + waitingTime / 60 + "分钟");

        } else if (stopRFBusTime.isEmpty() && !stopRSBusTime.isEmpty()) {
            // 有公交要到第二个站点了，但是没有公交要到第一个站点
            HashMap<Vehicle, Double> stopRSWaitingTime = new HashMap<>();

            Iterator<Map.Entry<Vehicle, Double>> iterator2 = stopRSBusTime.entrySet().iterator();
            while (iterator2.hasNext()) {
                Map.Entry<Vehicle, Double> entry = iterator2.next();
                Double dvalue = entry.getValue() - stopRSWalkTime;
                if (dvalue >= 0) {
                    stopRSWaitingTime.put(entry.getKey(), dvalue);
                }
            }
            List<Map.Entry<Vehicle, Double>> list2 = new ArrayList<>(stopRSWaitingTime.entrySet());
            list2.sort(Map.Entry.comparingByValue());
            Vehicle stopRSBus = list2.get(0).getKey();
            Double stopRSMinTime = list2.get(0).getValue();

            selectedBus = stopRSBus;
            selectedStop = stopReachedSecond;
            waitingTime = stopRSMinTime;
            walkingTime = stopRSMinTime;

            System.out.println("选择前往站点：" + selectedStop);
            System.out.println("步行至站点时间：" + walkingTime + "分钟");
            System.out.println("下一辆即将到站的公交id：" + selectedBus.getId());
            System.out.println("等待公交到站预计耗时：" + waitingTime / 60 + "分钟");

        } else {   // 有公交要到第一个站点也有公交要到第二个站点
            // STANDARD: 尽快上车
            // 计算bustime-walktime的差值
            HashMap<Vehicle, Double> stopRFWaitingTime = new HashMap<>();
            HashMap<Vehicle, Double> stopRSWaitingTime = new HashMap<>();

            Iterator<Map.Entry<Vehicle, Double>> iterator1 = stopRFBusTime.entrySet().iterator();
            while (iterator1.hasNext()) {
                Map.Entry<Vehicle, Double> entry = iterator1.next();
                Double dvalue = entry.getValue() - stopRFWalkTime;
                if (dvalue >= 0) {
                    stopRFWaitingTime.put(entry.getKey(), dvalue);
                }
            }
            // waitingtime排序
            List<Map.Entry<Vehicle, Double>> list1 = new ArrayList<>(stopRFWaitingTime.entrySet());
            list1.sort(Map.Entry.comparingByValue());
            Vehicle stopRFBus = list1.get(0).getKey();
            Double stopRFMinTime = list1.get(0).getValue();


            Iterator<Map.Entry<Vehicle, Double>> iterator2 = stopRSBusTime.entrySet().iterator();
            while (iterator2.hasNext()) {
                Map.Entry<Vehicle, Double> entry = iterator2.next();
                Double dvalue = entry.getValue() - stopRSWalkTime;
                if (dvalue >= 0) {
                    stopRSWaitingTime.put(entry.getKey(), dvalue);
                }
            }
            List<Map.Entry<Vehicle, Double>> list2 = new ArrayList<>(stopRSWaitingTime.entrySet());
            list2.sort(Map.Entry.comparingByValue());
            Vehicle stopRSBus = list2.get(0).getKey();
            Double stopRSMinTime = list2.get(0).getValue();


            // 比较最少的时间
            // 先比较等待公交的时间
            // 再比较步行至站点的时间
            if (stopRFMinTime < stopRSMinTime) {
                selectedBus = stopRFBus;
                selectedStop = stopReachedFirst;
                waitingTime = stopRFMinTime;
                walkingTime = stopRFWalkTime;
            } else if (stopRFMinTime > stopRSMinTime) {
                selectedBus = stopRSBus;
                selectedStop = stopReachedSecond;
                waitingTime = stopRSMinTime;
                walkingTime = stopRSWalkTime;
            } else {
                if (stopRFWalkTime < stopRSWalkTime) {
                    selectedBus = stopRFBus;
                    selectedStop = stopReachedFirst;
                    waitingTime = stopRFMinTime;
                    walkingTime = stopRFWalkTime;
                } else if (stopRFWalkTime > stopRSWalkTime) {
                    selectedBus = stopRSBus;
                    selectedStop = stopReachedSecond;
                    waitingTime = stopRSMinTime;
                    walkingTime = stopRSWalkTime;
                } else {
                    // 如果等到公交的时间和步行至站点的时间相等
                    // 那么就去第二个站点，节省“从站点一到站点二”的路上的时间
                    selectedBus = stopRSBus;
                    selectedStop = stopReachedSecond;
                    waitingTime = stopRSMinTime;
                    walkingTime = stopRSWalkTime;
                }
            }

            System.out.println("选择前往站点：" + selectedStop);
            System.out.println("步行至站点时间：" + walkingTime + "分钟");
            System.out.println("下一辆即将到站的公交id：" + selectedBus.getId());
            System.out.println("等待公交到站预计耗时：" + waitingTime / 60 + "分钟");
        }

        long endTime = System.currentTimeMillis();
        System.out.println("程序运行时间：" + (endTime - startTime) / 1000 + "s");
    }

    public List<Vehicle> getRealtimeInfo(List<String> allTripIds) throws MalformedURLException {
        // 通过实时数据获取接口获取feed
        URL url = vehiclePositionsUri.toURL();
//        boolean hadUpdate = false;
        GtfsRealtime.FeedMessage feed = null;
        try {
            feed = GtfsRealtime.FeedMessage.parseFrom(url.openStream());
//            hadUpdate = processDataset(feed);
        } catch (IOException e) {
            // 获取数据失败，继续尝试下一次获取。
//            hadUpdate = false;
            log.error("[executor]error while fetch data.", e);

        }
        // 获取到的所有车辆实体
        List<Vehicle> vehicles = new ArrayList<>();
        // 打印看看获得了多少车辆的信息
        System.out.println("=========================================");
        System.out.println(String.format("get %d vehicles info", feed.getEntityList().size()));

        boolean update = false;

        for (GtfsRealtime.FeedEntity entity : feed.getEntityList()) {
            if (entity.hasIsDeleted() && entity.getIsDeleted()) {
                String vehicleId = vehicleIdsByEntityIds.get(entity.getId());
                if (vehicleId == null) {
                    log.warn("unknown entity id in deletion request: " + entity.getId());
                    continue;
                }
                vehiclesById.remove(vehicleId);
                continue;
            }
            if (!entity.hasVehicle()) {
                continue;
            }
            GtfsRealtime.VehiclePosition vehicle = entity.getVehicle();
            String vehicleId = getVehicleId(vehicle);
            if (vehicleId == null) {
                continue;
            }
            vehicleIdsByEntityIds.put(entity.getId(), vehicleId);
            if (!vehicle.hasPosition()) {
                continue;
            }
            GtfsRealtime.Position position = vehicle.getPosition();
            Vehicle v = new Vehicle();
            v.setRouteID(vehicle.getTrip().getRouteId());
            // TODO direction 是如何计算的？
            v.setDirection("");
            v.setTripID(vehicle.getTrip().getTripId());
            v.setAgencyID(AgencyName);
            v.setOriginStop(v.getOriginStop());
            v.setLat(position.getLatitude());
            v.setLon(position.getLongitude());
            v.setBearing(position.getBearing());
            v.setId(vehicleId);
//            v.setLastUpdate(currentTime);
            v.setNextStop("");
            v.setAimedArrivalTime(0L);

            v.setRecordedTime(vehicle.getTimestamp());
            // 计算速度
            if (position.getSpeed() == 0.0) {
                if (vehiclesById.containsKey(v.getId())) {
                    Vehicle lastPoint = vehiclesById.get(v.getId());
                    GlobalCoordinates source = new GlobalCoordinates(lastPoint.getLat(), lastPoint.getLon());
                    GlobalCoordinates target = new GlobalCoordinates(v.getLat(), v.getLon());
                    // 默认应该都使用 WGS84 坐标系下计算距离
                    double distance = geodeticCalculator.calculateGeodeticCurve(Ellipsoid.WGS84, source, target).getEllipsoidalDistance();
                    long time_spend = vehicle.getTimestamp() - lastPoint.getRecordedTime();
                    double speedByMeter = distance / time_spend;
                    double speedByKilometer = speedByMeter * 3.6;
                    v.setSpeed((float) speedByKilometer);
                } else {
                    // 无法得知速度，只能设置为 0
                    // 呵呵 我要设置成30km/h 约等于8.3m/s
                    v.setSpeed(8.3f);
                }
            } else {
                v.setSpeed(position.getSpeed());
            }

            Vehicle existing = vehiclesById.get(vehicleId);
            if (existing == null || existing.getLat() != v.getLat()
                    || existing.getLon() != v.getLon()) {
                vehiclesById.put(vehicleId, v);
                update = true;
            } else {
                v.setLastUpdate(existing.getLastUpdate());
            }

            if (allTripIds.contains(v.getTripID())) {
                vehicles.add(v);
            }
        }

        System.out.println("=========================================");
        for (Vehicle v:vehicles) {
            System.out.println(v.getTripID());
        }

        System.out.println("=========================================");
        System.out.println("get " + vehicles.size() + " vehicles info that meet the conditions");
        System.out.println("=========================================");
        return vehicles;

    }

    /**
     * @param vehicle 原始传输的车辆数据结构体
     * @return 车辆的 ID
     */
    private String getVehicleId(GtfsRealtime.VehiclePosition vehicle) {
        if (!vehicle.hasVehicle()) {
            return null;
        }
        GtfsRealtime.VehicleDescriptor desc = vehicle.getVehicle();
        if (!desc.hasId()) {
            return null;
        }
        return desc.getId();
    }

}