package whu.edu.cs.transitnet.service.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import whu.edu.cs.transitnet.dao.RealTimeDataDao;
import whu.edu.cs.transitnet.pojo.RealTimeDataEntity;
import whu.edu.cs.transitnet.realtime.Vehicle;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class RealtimeDataStore {

    @Value("${transitnet.storage.enable}")
    private boolean storageEnable;

    @Autowired
    private RealTimeDataDao realTimeDataDao;

    public void store(List<Vehicle> list) {
        Thread t = new Thread(new RealtimeDataStoreRunner(list));
        t.start();
    }

    private static final SimpleDateFormat FORMATER = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    class RealtimeDataStoreRunner implements Runnable {
        private List<Vehicle> vehicleList;

        public RealtimeDataStoreRunner(List<Vehicle> vehicleList) {
            this.vehicleList = vehicleList;
        }

        @Override
        public void run() {
            if (!storageEnable) {
                log.info("The storage process is disabled, skip for saving data.");
                return;
            }
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
                entity.setAimedArrivalTime(FORMATER.format(v.getAimedArrivalTime() * 1000));
                entity.setDistanceFromOrigin((double) v.getDistanceFromOrigin());
                entity.setPresentableDistance((double) v.getPresentableDistance());
                entity.setDistanceFromNextStop(String.valueOf(v.getDistanceFromNextStop()));
                entity.setNextStop(v.getNextStop());
                entity.setDistanceFromOrigin((double) v.getDistanceFromOrigin());
                entity.setRecordedTime(FORMATER.format(v.getRecordedTime() * 1000));
                return entity;
            }).collect(Collectors.toList());
            List<RealTimeDataEntity> result = realTimeDataDao.saveAll(list);
            log.info(String.format("async saved %d vehicles' information.", result.size()));
        }
    }
}
