package whu.edu.cs.transitnet.service.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import whu.edu.cs.transitnet.dao.RealTimeDataDao;
import whu.edu.cs.transitnet.pojo.RealTimeDataEntity;
import whu.edu.cs.transitnet.realtime.Vehicle;

import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class RealtimeDataStore {

    @Autowired
    private RealTimeDataDao realTimeDataDao;

    public void store(List<Vehicle> list) {
        Thread t = new Thread(new RealtimeDataStoreRunner(list));
        t.start();
    }

    class RealtimeDataStoreRunner implements Runnable {
        private List<Vehicle> vehicleList;

        public RealtimeDataStoreRunner(List<Vehicle> vehicleList) {
            this.vehicleList = vehicleList;
        }

        @Override
        public void run() {
            log.info(String.format("async saving %d vehicles' information.", vehicleList.size()));

            List<RealTimeDataEntity> list = vehicleList.stream().map((Vehicle v) -> {
                RealTimeDataEntity entity = new RealTimeDataEntity();
                entity.setRouteId(v.getRouteID());
                entity.setDirection(v.getDirection());
                entity.setTripId(v.getTripID());
                entity.setAgencyId(v.getAgencyID());
                entity.setOriginStop(v.getOriginStop());
                entity.setLat(v.getLat());
                entity.setLon(v.getLon());
                entity.setBearing((double) v.getBearing());
                entity.setVehicleId(v.getId());
                entity.setAimedArrivalTime(v.getAimedArrivalTime().toString());
                entity.setDistanceFromOrigin((double)v.getDistanceFromOrigin());
                entity.setPresentableDistance((double)v.getPresentableDistance());
                entity.setDistanceFromNextStop(String.valueOf(v.getDistanceFromNextStop()));
                entity.setNextStop(v.getNextStop());
                entity.setDistanceFromOrigin((double) v.getDistanceFromOrigin());
                entity.setRecordedTime(v.getRecordedTime().toString());
                return entity;
            }).collect(Collectors.toList());
            List<RealTimeDataEntity> result = realTimeDataDao.saveAll(list);
            log.info(String.format("async saved %d vehicles' information.", result.size()));
        }
    }
}
