package whu.edu.cs.transitnet.realtime;

import com.google.transit.realtime.GtfsRealtime.*;
import edu.whu.hyk.model.Point;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import lombok.extern.slf4j.Slf4j;
import org.gavaghan.geodesy.Ellipsoid;
import org.gavaghan.geodesy.GeodeticCalculator;
import org.gavaghan.geodesy.GlobalCoordinates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import whu.edu.cs.transitnet.bean.RouteCache;
import whu.edu.cs.transitnet.service.EncodeService;
import whu.edu.cs.transitnet.service.index.GridId;
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
import java.util.stream.Collectors;

@Slf4j
@Service
public class RealtimeService {

    @Value("${transitnet.realtime.url}")
    private URI vehiclePositionsUri;

    @Value("${transitnet.realtime.agency-name}")
    private String AgencyName = "Agency";


    @Value("${transitnet.realtime.timezone}")
    private int timezone = 8;
    @Autowired
    private MeterRegistry meterRegistry;
    private ScheduledExecutorService executor;

    private final Map<String, String> vehicleIdsByEntityIds = new HashMap<>();

    private Map<String, Vehicle> vehiclesById;

    public ConcurrentHashMap<TripId, ArrayList<Vehicle>> getVehiclesByTripId() {
        return vehiclesByTripId;
    }

    // 获取所有轨迹信息；有序
    private final ConcurrentHashMap<TripId, ArrayList<Vehicle>> vehiclesByTripId = new ConcurrentHashMap<>();

    private Map<String, Vehicle> onRouteVehiclesById;

    // 位置信息时间序列
    private final LinkedList<List<Vehicle>> timeSerial = new LinkedList<>();

    private final RefreshTask refreshTask = new RefreshTask();

    /**
     * GTFS 采样的时间间隔，由于原始数据使用了 30s 间隔，这里也用 30s
     **/
    private final int refreshInterval = 30;

    private boolean dynamicRefreshInterval = true;

    private long mostRecentRefresh = -1;

    public HashMap<GridId, HashSet<TripId>> GT_List=new HashMap<>();
    public HashMap<TripId, Point> TlP_List=new HashMap<>();

    @Autowired
    RealtimeDataStore storeService;

    @Autowired
    RealtimeDataIndex indexService;

    @Autowired
    GeodeticCalculator geodeticCalculator;

    @Autowired
    RouteCache routeCache;
    @Autowired
    EncodeService encodeService;

    @PostConstruct
    public void start() {
        vehiclesById = meterRegistry.gaugeMapSize("realtime_vehicle", Tags.of("region", "nyc"), new ConcurrentHashMap<>());
        onRouteVehiclesById = meterRegistry.gaugeMapSize("realtime_vehicle", Tags.of("region", "nyc_filter"), new ConcurrentHashMap<>());
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(refreshTask, 0, TimeUnit.SECONDS);
        log.info("executor is running...");
    }

    public List<Vehicle> getAllVehicles() {
        return new ArrayList<>(timeSerial.getLast());
    }

    public List<Vehicle> getValidVehicles() {
        return timeSerial.getLast().stream().filter(vehicle -> routeCache.routes.contains(vehicle.getRouteID())).collect(Collectors.toList());
    }

    public long getCurrentTimestamp() {
        TimeZone zone = TimeZone.getTimeZone(String.format("GMT%s%d:00", timezone >= 0 ? "+" : "", timezone));
        Calendar can = Calendar.getInstance(zone);
        return can.getTimeInMillis();
    }

    private void refresh() throws IOException {
        log.info("reset GT and TlP");
        GT_List.clear();
        TlP_List.clear();

        log.info("refreshing vehicle positions");

        URL url = vehiclePositionsUri.toURL();
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
            if (dynamicRefreshInterval) {
                updateRefreshInterval();
            }
        }

        executor.schedule(refreshTask, refreshInterval, TimeUnit.SECONDS);
    }

    private boolean processDataset(FeedMessage feed) {
        long currentTime = System.currentTimeMillis();
        List<Vehicle> vehicles = new ArrayList<>();
        boolean update = false;
        log.info(String.format("get %d vehicles info", feed.getEntityList().size()));
        for (FeedEntity entity : feed.getEntityList()) {
            if (entity.hasIsDeleted() && entity.getIsDeleted()) {
                String vehicleId = vehicleIdsByEntityIds.get(entity.getId());
                if (vehicleId == null) {
                    log.warn("unknown entity id in deletion request: " + entity.getId());
                    continue;
                }
                log.debug("Vehicle {} ends trip", vehicleId);
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
            vehicleIdsByEntityIds.put(entity.getId(), vehicleId);
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
            // TODO: 未知字段
            v.setLastUpdate(currentTime);
            v.setNextStop("");
            v.setAimedArrivalTime(0L);
            v.setRecordedTime(vehicle.getTimestamp());

            ArrayList<Vehicle> vs = new ArrayList<>();
            TripId tripId = new TripId(v.getTripID());
            if (vehiclesByTripId.containsKey(tripId)) {
                vs = vehiclesByTripId.get(tripId);
            }
            vs.add(v);
            vehiclesByTripId.put(tripId, vs);

            //更新TlP与GT
            updateTlpAndGt(v);


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
                    v.setSpeed(0.0f);
                }
            } else {
                v.setSpeed(position.getSpeed());
            }
            vehicles.add(v);
        }
        log.info("vehicles updating: " + vehicles.size());
        long t0 = System.currentTimeMillis();
        // update latest map
        vehiclesById.clear();
        onRouteVehiclesById.clear();
        vehicles.stream().forEach(v -> {
            vehiclesById.put(v.getId(), v);
            if (routeCache.routes.contains(v.getRouteID())) {
                onRouteVehiclesById.put(v.getId(), v);
            }
        });

        // update time serial
        updateTimeSerial(vehicles);
        // update indexes
        indexService.update(vehicles);
        // update database storage
        storeService.store(vehicles);
        long t1 = System.currentTimeMillis();
        log.info("vehicles updated, total time cost " + (t1 - t0) + "ms");
        return update;
    }

    private void updateTimeSerial(List<Vehicle> vehicles) {
        if (timeSerial.size() > 50) {
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
        if (mostRecentRefresh != -1) {
            int refreshInterval = (int) ((t - mostRecentRefresh) / 1000);
            log.info("refresh interval: " + refreshInterval + "s");
        }
        mostRecentRefresh = t;
    }

    private void updateTlpAndGt(Vehicle newest){
        TripId tid=new TripId(newest.getTripID());
        double lat=newest.getLat();
        double lng=newest.getLon();

        TlP_List.put(tid,new Point(lat, lng));

        GridId gid=encodeService.getGridID(lat,lng);
        if(!GT_List.containsKey(gid)){
            HashSet<TripId> tid_set=new HashSet<>();
            tid_set.add(tid);
            GT_List.put(gid,tid_set);
        }else{
            GT_List.get(gid).add(tid);
        }

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
