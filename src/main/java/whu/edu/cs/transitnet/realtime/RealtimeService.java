package whu.edu.cs.transitnet.realtime;

import com.google.transit.realtime.GtfsRealtime.*;
import lombok.extern.slf4j.Slf4j;
import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GlobalCoordinates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import whu.edu.cs.transitnet.service.index.RealtimeDataIndex;
import whu.edu.cs.transitnet.service.index.TripId;
import whu.edu.cs.transitnet.service.storage.RealtimeDataStore;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RealtimeService {

    @Value("${transitnet.realtime.url}")
    private URI _vehiclePositionsUri;

    @Value("${transitnet.realtime.agency-name}")
    private String AgencyName = "Agency";


    @Value("${transitnet.realtime.timezone}")
    private int timezone = 8;
    private ScheduledExecutorService _executor;

    private final Map<String, String> _vehicleIdsByEntityIds = new HashMap<>();

    private final Map<String, Vehicle> _vehiclesById = new ConcurrentHashMap<>();

    public Map<TripId, ArrayList<Vehicle>> get_vehiclesByTripId() {
        return _vehiclesByTripId;
    }

    // 获取所有轨迹信息
    private final Map<TripId, ArrayList<Vehicle>> _vehiclesByTripId = new HashMap<>();

    // 位置信息时间序列
    private final Queue<List<Vehicle>> timeSerial = new LinkedList<>();

    private final RefreshTask _refreshTask = new RefreshTask();

    private final int _refreshInterval = 30;

    private boolean _dynamicRefreshInterval = true;

    private long _mostRecentRefresh = -1;

    @Autowired
    RealtimeDataStore storeService;

    @Autowired
    RealtimeDataIndex indexService;

    @Autowired
    GeodeticCalculator geodeticCalculator;

    @PostConstruct
    public void start() {
        _executor = Executors.newSingleThreadScheduledExecutor();
        _executor.schedule(_refreshTask, 0, TimeUnit.SECONDS);
        log.info("executor is running...");
    }

    public List<Vehicle> getAllVehicles() {
        return new ArrayList<>(_vehiclesById.values());
    }

    public long getCurrentTimestamp() {
        TimeZone zone = TimeZone.getTimeZone(String.format("GMT%s%d:00", timezone>=0?"+":"", timezone));
        Calendar can = Calendar.getInstance(zone);
        return can.getTimeInMillis();
    }
    private void refresh() throws IOException {

        log.info("refreshing vehicle positions");

        URL url = _vehiclePositionsUri.toURL();
        boolean hadUpdate = false;
        try {
            FeedMessage feed = FeedMessage.parseFrom(url.openStream());
            hadUpdate = processDataset(feed);
        } catch (IOException e) {
            // 获取数据失败，继续尝试下一次获取。
            hadUpdate = false;
            log.error("[executor]error while fetch data.", e);

        }
        if (hadUpdate) {
            if (_dynamicRefreshInterval) {
                updateRefreshInterval();
            }
        }

        _executor.schedule(_refreshTask, _refreshInterval, TimeUnit.SECONDS);
    }

    private boolean processDataset(FeedMessage feed) {
        long currentTime = System.currentTimeMillis();
        List<Vehicle> vehicles = new ArrayList<>();
        boolean update = false;
        log.info(String.format("get %d vehicles info", feed.getEntityList().size()));
        for (FeedEntity entity : feed.getEntityList()) {
            if (entity.hasIsDeleted() && entity.getIsDeleted()) {
                String vehicleId = _vehicleIdsByEntityIds.get(entity.getId());
                if (vehicleId == null) {
                    log.warn("unknown entity id in deletion request: " + entity.getId());
                    continue;
                }
                _vehiclesById.remove(vehicleId);
                continue;
            }
            if (!entity.hasVehicle()) {
                continue;
            }
            VehiclePosition vehicle = entity.getVehicle();
            String vehicleId = getVehicleId(vehicle);
            if (vehicleId == null) {
                continue;
            }
            _vehicleIdsByEntityIds.put(entity.getId(), vehicleId);
            if (!vehicle.hasPosition()) {
                continue;
            }
            Position position = vehicle.getPosition();
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
            v.setLastUpdate(currentTime);
            // TODO: 未知字段
            v.setNextStop("");
            v.setAimedArrivalTime(0L);

            v.setRecordedTime(vehicle.getTimestamp());

            ArrayList<Vehicle> vs = new ArrayList<>();
            TripId tripId = new TripId(v.getTripID());
            if(_vehiclesByTripId.containsKey(tripId)) {
                vs = _vehiclesByTripId.get(tripId);
            }
            vs.add(v);
            _vehiclesByTripId.put(tripId, vs);


            // 计算速度
            if (position.getSpeed() == 0.0) {
                if (_vehiclesById.containsKey(v.getId())) {
                    Vehicle lastPoint = _vehiclesById.get(v.getId());
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
                    v.setSpeed(0.0f);
                }
            } else {
                v.setSpeed(position.getSpeed());
            }

            Vehicle existing = _vehiclesById.get(vehicleId);
            if (existing == null || existing.getLat() != v.getLat()
                    || existing.getLon() != v.getLon()) {
                _vehiclesById.put(vehicleId, v);
                update = true;
            } else {
                v.setLastUpdate(existing.getLastUpdate());
            }

            vehicles.add(v);
        }

        if (update) {
            log.info("vehicles updating: " + vehicles.size());
            long t0 = System.currentTimeMillis();
            updateTimeSerial(vehicles);
            indexService.update(vehicles);
            storeService.store(vehicles);
            long t1 = System.currentTimeMillis();
            log.info("vehicles updated, total time cost " + (t1 - t0) + "ms");
        }

        return update;
    }

    private void updateTimeSerial(List<Vehicle> vehicles) {
        if (timeSerial.size() > 10) {
            timeSerial.poll();
        }
        timeSerial.offer(vehicles);
    }

    /**
     * @param vehicle 原始传输的车辆数据结构体
     * @return 车辆的 ID
     */
    private String getVehicleId(VehiclePosition vehicle) {
        if (!vehicle.hasVehicle()) {
            return null;
        }
        VehicleDescriptor desc = vehicle.getVehicle();
        if (!desc.hasId()) {
            return null;
        }
        return desc.getId();
    }

    private void updateRefreshInterval() {
        long t = System.currentTimeMillis();
        if (_mostRecentRefresh != -1) {
            int refreshInterval = (int) ((t - _mostRecentRefresh) / 1000);
            log.info("refresh interval: " + _refreshInterval + "s");
        }
        _mostRecentRefresh = t;
    }

    private class RefreshTask implements Runnable {
        @Override
        public void run() {
            try {
                refresh();
            } catch (Exception ex) {
                log.error("error refreshing GTFS-realtime data", ex);
            }
        }
    }
}
