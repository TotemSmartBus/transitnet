package whu.edu.cs.transitnet.service.index;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import whu.edu.cs.transitnet.realtime.Vehicle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RealtimeDataIndex {

    /**
     * 查询只能得到 PointID，需要反向找到对应的 vehicle
     */
    private LinkedList<HashMap<Integer, Vehicle>> pointToVehicle = new LinkedList<>();

    @Autowired
    private HytraEngineManager engine;


    public void update(List<Vehicle> list) {
        Thread t = new Thread(new IndexUpdater(list));
        t.start();
    }

    public List<Vehicle> search(double lat, double lon, int k) {
        List<Integer> pidList = engine.searchRealtime(lat, lon, k);
        List<Vehicle> result = new ArrayList<>(pidList.size());
        switch (pointToVehicle.size()) {
            case 0:
                log.warn("Search for kNN, but no data.");
                return new ArrayList<>();
            case 1:
                HashMap<Integer, Vehicle> newestMap = pointToVehicle.getLast();
                for (Integer i : pidList) {
                    if (newestMap.containsKey(i)) {
                        result.add(newestMap.get(i));
                    } else {
                        log.warn("kNN 搜索到的 PID 索引在最新 2 个时间分片上不存在，是否数据已经更新了？");
                    }
                }
                return result;
            default:
                newestMap = pointToVehicle.getLast();
                HashMap<Integer, Vehicle> newestButOneMap = pointToVehicle.get(pointToVehicle.size() - 2);
                for (Integer i : pidList) {
                    if (newestMap.containsKey(i)) {
                        result.add(newestMap.get(i));
                    } else if (newestButOneMap.containsKey(i)) {
                        result.add(newestButOneMap.get(i));
                    } else {
                        log.warn("kNN 搜索到的 PID 索引在最新 2 个时间分片上不存在，是否数据已经更新了？");
                    }
                }
                return result;
        }
    }

    class IndexUpdater implements Runnable {
        private List<edu.whu.hytra.entity.Vehicle> newIndex;

        public IndexUpdater(List<Vehicle> newIndex) {
            // 更新索引时还要维护一下 pid 和原始数据的 map
            HashMap<Integer, Vehicle> pointMap = new HashMap<>();
            this.newIndex = newIndex.stream().map(i -> {
                edu.whu.hytra.entity.Vehicle j = new edu.whu.hytra.entity.Vehicle();
                j.setId(i.getId());
                j.setRecordedTime(i.getRecordedTime());
                j.setLat(i.getLat());
                j.setLon(i.getLon());
                j.setTripID(i.getTripID());
                pointMap.put(j.getPID(), i);
                return j;
            }).collect(Collectors.toList());
            pointToVehicle.add(pointMap);
            // 只保存最新 10 个原始时间分片防止内存占用过多
            if (pointToVehicle.size() > 10) {
                pointToVehicle.poll();
            }
        }

        @Override
        public void run() {
            engine.updateIndex(newIndex);
        }
    }
}
