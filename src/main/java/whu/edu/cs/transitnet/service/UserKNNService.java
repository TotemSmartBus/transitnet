package whu.edu.cs.transitnet.service;

import com.graphhopper.Trip;
import edu.whu.hyk.model.Point;
import org.locationtech.jts.triangulate.tri.Tri;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import whu.edu.cs.transitnet.TransitnetApplication;
import whu.edu.cs.transitnet.dao.TripsDao;
import whu.edu.cs.transitnet.pojo.TripsEntity;
import whu.edu.cs.transitnet.realtime.RealtimeService;
import whu.edu.cs.transitnet.realtime.Vehicle;
import whu.edu.cs.transitnet.service.index.*;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class UserKNNService {

    @Autowired
    RealtimeService realtimeService;

    @Autowired
    HytraEngineManager hytraEngineManager;

    @Autowired
    EncodeService encodeService;

    @Autowired
    DecodeService decodeService;

    @Autowired
    ShapeIndex shapeIndex;

    @Autowired
    ScheduleIndex scheduleIndex;
    // 给定用户轨迹 <lat, lon, timestamp>
    // ArrayList<Point> user_tra;
    int k = 5; // 这个k指的是取前k个shape


    // TODO hashmap初始化

//    public ArrayList<GridId> getUserGridList() {
//        return userGridList;
//    }
//
//    public ArrayList<CubeId> getUserCubeList() {
//        return userCubeList;
//    }


    TripId userTripId;
    // vehicle list
    ArrayList<Vehicle> userVehicleList = new ArrayList<>();
    // user - grid
//    ArrayList<GridId> userGridList = new ArrayList<>();
//    // user - cube
//    ArrayList<CubeId> userCubeList = new ArrayList<>();
    // user - [start_time, end_time]
//    Time userStartTime;
//    Time userEndTime;
    // 10 20 30 40 50
//    ArrayList<Time> userStartTime = new ArrayList<>();
//    ArrayList<Time> userEndTimes = new ArrayList<>();
//
//    // shape - grid
//    private HashMap<ShapeId, ArrayList<GridId>> shapeGridList = new HashMap<>();
//    // grid - shape 倒排索引
//    private HashMap<GridId, ArrayList<ShapeId>> gridShapeList = new HashMap<>();
//    // shape_id - trip_id
//    private HashMap<ShapeId, ArrayList<TripId>> shapeTripList = new HashMap<>();
    // trip_id - [start_time, end_time] 写个类
    private HashMap<TripId, ArrayList<Time>> tripStartEndList = new HashMap<>();
    // trip - cube  map做操作 删掉list
    private HashMap<TripId, ArrayList<CubeId>> tripCubeList = new HashMap<>();

    // 存 50 个值
    ConcurrentHashMap<TripId, ArrayList<Vehicle>> vehiclesByTripId  = new ConcurrentHashMap<>();

    // 模拟用户轨迹

    public void getUserTra() throws InterruptedException {
//        Thread.sleep(900000); // 50 * 30 * 1000 ms
//        vehiclesByTripId  = realtimeService.get_vehiclesByTripId();

//        List<TripId> top10Keys = vehiclesByTripId.keySet().stream().limit(10).collect(Collectors.toList());
//        for (TripId tripId : top10Keys) {
//            if (vehiclesByTripId.get(tripId).size() >= 30) {
//                userTripId = tripId;
//                break;
//            }
//        }
//        Random random = new Random();
//        int randomIndex = random.nextInt(top10Keys.size());
//        TripId randomKey = top10Keys.get(randomIndex);



        TripId[] keys = vehiclesByTripId.keySet().toArray(new TripId[0]); //将map里的key值取出，并放进数组里
        int random = (int) (Math.random() * (keys.length)); //生成随机数
        TripId randomKey = keys[random]; //随机取key值

        System.out.println("============================================");
        System.out.println("[USERKNNSERVICE] randomKey: " + randomKey);   //输出随机的key值
//
        userTripId = randomKey;
//        userTripId = new TripId("35672205-BPPB3-BP_B3-Weekday-02-SDon");


        userVehicleList = vehiclesByTripId.get(userTripId);
//        ArrayList<Vehicle> vehicles = vehiclesByTripId.get(userTripId);

//        System.out.println(Arrays.asList(keys).contains("[USERKNNSERVICE] if vehicle set contains usertripid: " + new TripId("QV_B3-Sunday-044200_Q46_601")));
//        ArrayList<Vehicle> vehicles = vehiclesByTripId.get(new TripId("QV_B3-Sunday-044200_Q46_601"));


//        ArrayList<String> times = new ArrayList<>();
//        ArrayList<GridId> grids = new ArrayList<>();
//        ArrayList<CubeId> cubes = new ArrayList<>();
//
//        Date d = new Date();
//        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
//        sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));
//
//
//        for (Vehicle vehicle : userVehicleList) {
//            GridId gridId = encodeService.getGridID(vehicle.getLat(), vehicle.getLon());
//            grids.add(gridId);
//            if(userGridList.isEmpty() || userGridList.lastIndexOf(gridId) != (userGridList.size() - 1)) {
//                userGridList.add(gridId);
//            }
//            CubeId cubeId = encodeService.encodeCube(vehicle.getLat(), vehicle.getLon(), vehicle.getRecordedTime());
//            cubes.add(cubeId);
//            if(userCubeList.isEmpty() || userCubeList.lastIndexOf(cubeId) != (userCubeList.size() - 1)) {
//                userCubeList.add(cubeId);
//            }
//
//            d.setTime(vehicle.getRecordedTime() * 1000);
//            String date_hour_min_sec  = sdf.format(d);
//
//            times.add(date_hour_min_sec);
////            String[] hour_min_sec = date_hour_min_sec.split(":");   // 取的是时分秒
////            double t = (double)(Integer.parseInt(hour_min_sec[0]) * 3600 + Integer.parseInt(hour_min_sec[1]) * 60 + Integer.parseInt(hour_min_sec[2])); // 转化成秒
////            times.add(t);
//        }
//
//        userStartTime = Time.valueOf(times.get(0));
////        userEndTime = Time.valueOf(times.get(times.size() - 1));
//        userEndTimes.add(Time.valueOf(times.get(9)));
//        userEndTimes.add(Time.valueOf(times.get(19)));
//        userEndTimes.add(Time.valueOf(times.get(29)));
//        userEndTimes.add(Time.valueOf(times.get(39)));
//        userEndTimes.add(Time.valueOf(times.get(times.size() - 1)));


        System.out.println("[USERKNNSERVICE] userId(TripId): " + userTripId);
        System.out.println("[USERKNNSERVICE] length of user vehicle list: " + userVehicleList.size());
//        System.out.println("[USERKNNSERVICE] times: " + times);
//        System.out.println("[USERKNNSERVICE] grids: " + grids);
//        System.out.println("[USERKNNSERVICE] userGridList: " + userGridList);
//        System.out.println("[USERKNNSERVICE] cubes: " + cubes);
//        System.out.println("[USERKNNSERVICE] userCubeList: " + userCubeList);
    }

    // 先获取Top-k的shape
//    ArrayList<ShapeIndex.ShapeId> topkShapeList = shapeIndex.getTopKShapes(userGridList, k);
    // 再获取下面的所有trip
//    ArrayList<TripId> tripIdList = new ArrayList<>();

    // 获取所有trip
//    public void getTripList() {
//        ArrayList<ShapeId> topkShapeList = shapeIndex.getTopKShapes(userGridList, k);
//
//        for (ShapeId shapeId : topkShapeList) {
//            ArrayList<TripId> tripIds = shapeTripList.get(shapeId);
//            for (TripId tripId : tripIds) {
//                tripIdList.add(tripId);
//            }
//        }
//    }

    // 再根据schedule找到和用户轨迹时间有重叠的trip


    @Autowired
    TripsDao tripsDao;
    // schedule做筛选
    // user: [start_time, end_time]
    public ArrayList<TripId> filterTripList(ArrayList<GridId> userPartialGridList, Time userStart, Time userEnd) {
        ArrayList<TripId> filteredTripList = new ArrayList<>();

        // 取出 user tripId 对应的 shapeId
        List<TripsEntity> tripsEntityList = tripsDao.findAllByTripId(userTripId.toString());
        ShapeId shapeId = new ShapeId(tripsEntityList.get(0).getShapeId());

        // top-k shapes -> trips of top-k shapes
        ArrayList<TripId> tripIds = shapeIndex.getTripsOfTopKShapes(shapeId, userPartialGridList, k);

        // 插一嘴，把虚拟的usertripid对应的shape的trips也加进去
        for (TripsEntity tripsEntity : tripsEntityList) {
            ArrayList<TripId> tripIds1 = shapeIndex.getTripIdsByShapeId(new ShapeId(tripsEntity.getShapeId()));
            for (TripId tripId : tripIds1) {
                if(!tripIds.contains(tripId)) tripIds.add(tripId);
            }
        }


        tripStartEndList = scheduleIndex.getTripStartEndList();
        System.out.println("[USERKNNSERVICE] userStartTime: " + userStart);
        System.out.println("[USERKNNSERVICE] userEndTime: " + userEnd);
        System.out.println("[USERKNNSERVICE] userTripId start and end time: " + tripStartEndList.get(userTripId));

        System.out.println("[USERKNNSERVICE] size of trips of top-k shapes: " + tripIds.size());
        System.out.println("[USERKNNSERVICE] if the trips of top-k shapes contain usertripid: " + tripIds.contains(userTripId));

        for (TripId tripId : tripIds) {
            ArrayList<Time> times = tripStartEndList.get(tripId);
            if (times != null) {
                Time start = times.get(0);
                Time end = times.get(1);

                Long startToLong = start.getTime();
                Long endToLong = end.getTime();

                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

                Date d1 = new Date();
                d1.setTime(startToLong - 30 * 60 * 1000); // 往前推30分钟
                String startForward = sdf.format(d1);

                Date d2 = new Date();
                d2.setTime(endToLong + 30 * 60 * 1000); // 往后推30分钟
                String endBackward = sdf.format(d2);

                if (Time.valueOf(startForward).before(userEnd) && Time.valueOf(endBackward).after(userStart)) {
                    filteredTripList.add(tripId);
                }
            }

        }

        return filteredTripList;
    }


    // 取得所有轨迹
//    public void getTripIdCubeList(ArrayList<TripId> filteredTripList) {
    public void getTripIdCubeList(ArrayList<GridId> userPartialGridList, Time userStart, Time userEnd, int length) throws InterruptedException {
//        getUserTra(); // 先构建用户轨迹的索引表；获取用户轨迹起始时间和结束时间
        tripCubeList = new HashMap<>();

        ArrayList<TripId> filteredTripList = filterTripList(userPartialGridList, userStart, userEnd); // 获取所有要判断的tripid
        System.out.println("[USERKNNSERVICE] " + "size of filtered trips: " + filteredTripList.size());
//        Map<TripId, ArrayList<Vehicle>> vehiclesByTripId  = realtimeService.get_vehiclesByTripId();
        System.out.println("[USERKNNSERVICE] " + "if the filtered list contains usertripid: " + filteredTripList.contains(userTripId));
        for (TripId tripId : filteredTripList) {
            if (vehiclesByTripId.get(tripId) != null && !vehiclesByTripId.get(tripId).isEmpty()) {
                // 只取前 length 个值
                ArrayList<Vehicle> vehicles = new ArrayList<>();
                vehicles = vehiclesByTripId.get(tripId);
//                if (vehiclesByTripId.get(tripId).size() >= length) {
//                    vehicles.addAll(vehiclesByTripId.get(tripId).subList(0, length));
//                } else {
//                    vehicles.addAll(vehiclesByTripId.get(tripId));
//                }


                ArrayList<CubeId> cubeIds = new ArrayList<>();
                for (Vehicle v : vehicles) {
                    CubeId cubeId = encodeService.encodeCube(v.getLat(), v.getLon(), v.getRecordedTime());
                    if(cubeIds.isEmpty() || cubeIds.lastIndexOf(cubeId) != (cubeIds.size() - 1)) {
                        cubeIds.add(cubeId);
                    }
//                    cubeIds.add(cubeId);
                }
                tripCubeList.put(tripId, cubeIds);
            }
        }


    }

    // tripCubeList 和 userCubeList 做相似度查询

//    int theta = 5;

    // trip_id - similarity
    HashMap<TripId, Double> tripSimListLOCS = new HashMap<>();
    HashMap<TripId, Integer> tripSimListLOC = new HashMap<>();

    HashMap<TripId, Double> tripSimListDTW = new HashMap<>();
    HashMap<TripId, Double> tripSimListEDR = new HashMap<>();
    HashMap<TripId, Double> tripSimListERP = new HashMap<>();


    // 获取 Top-k trip （用户自己指定k，不是前面定义的变量k）
    public void getTopKTrips(int k) throws InterruptedException {

        int length = 30; // 轨迹长度
        int sleepTime = length * 30 * 1000;
        Thread.sleep(sleepTime); // 50 * 30 * 1000 ms
        vehiclesByTripId  = realtimeService.get_vehiclesByTripId();
        System.out.println("============================================");
        System.out.println("[USERKNNSERVICE] " + "number of realtime vehicles: " + vehiclesByTripId.size());
        getUserTra(); // 先构建用户轨迹的索引表；获取用户轨迹起始时间和结束时间

        System.out.println("============================================");
        System.out.println("[USERKNNSERVICE] " + "trajectory length: " + length);
        ArrayList<Vehicle> userPartialVehicleList = new ArrayList<>();
//        userPartialVehicleList.addAll(userVehicleList.subList(0, 30));
        userPartialVehicleList.addAll(userVehicleList);

        ArrayList<GridId> userPartialGridList = new ArrayList<>();
        ArrayList<CubeId> userPartialCubeList = new ArrayList<>();

        ArrayList<String> times = new ArrayList<>();
        ArrayList<GridId> grids = new ArrayList<>();
        ArrayList<CubeId> cubes = new ArrayList<>();

        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));

        for (Vehicle vehicle : userPartialVehicleList) {
            GridId gridId = encodeService.getGridID(vehicle.getLat(), vehicle.getLon());
            grids.add(gridId);
            if(userPartialGridList.isEmpty() || userPartialGridList.lastIndexOf(gridId) != (userPartialGridList.size() - 1)) {
                userPartialGridList.add(gridId);
            }
            CubeId cubeId = encodeService.encodeCube(vehicle.getLat(), vehicle.getLon(), vehicle.getRecordedTime());
            cubes.add(cubeId);
            if(userPartialCubeList.isEmpty() || userPartialCubeList.lastIndexOf(cubeId) != (userPartialCubeList.size() - 1)) {
                userPartialCubeList.add(cubeId);
            }

            d.setTime(vehicle.getRecordedTime() * 1000);
            String date_hour_min_sec  = sdf.format(d);

            times.add(date_hour_min_sec);
//            String[] hour_min_sec = date_hour_min_sec.split(":");   // 取的是时分秒
//            double t = (double)(Integer.parseInt(hour_min_sec[0]) * 3600 + Integer.parseInt(hour_min_sec[1]) * 60 + Integer.parseInt(hour_min_sec[2])); // 转化成秒
//            times.add(t);
        }

        Time userStart = Time.valueOf(times.get(0));
        Time userEnd = Time.valueOf(times.get(times.size() - 1));

        System.out.println("[USERKNNSERVICE] times: " + times);
        System.out.println("[USERKNNSERVICE] grids: " + grids);
        System.out.println("[USERKNNSERVICE] userPartialGridList: " + userPartialGridList);
        System.out.println("[USERKNNSERVICE] cubes: " + cubes);
        System.out.println("[USERKNNSERVICE] userPartialCubeList: " + userPartialCubeList);



//            ArrayList<GridId> userPartialGridList = new ArrayList<>();
//            userPartialGridList.addAll(userGridList.subList(0, length));
//            ArrayList<CubeId> userPartialCubeList = new ArrayList<>();
//            userPartialCubeList.addAll(userCubeList.subList(0, length));
//            Time userStart = userStartTime;
//            Time userEnd = userEndTimes.get((length / 10) - 1);
//            System.out.println("[USERKNNSERVICE] " + "userPartialGridList: " + userPartialGridList);
//            System.out.println("[USERKNNSERVICE] " + "userPartialCubeList: " + userPartialCubeList);
        System.out.println("[USERKNNSERVICE] " + "userStartTime: " + userStart);
        System.out.println("[USERKNNSERVICE] " + "userEnd: " + userEnd);


        Long startTime = System.currentTimeMillis();
//    public void getTopKTrips(ArrayList<CubeId> userCubeList, HashMap<TripId, ArrayList<CubeId>> tripCubeList, int k) throws InterruptedException {
        getTripIdCubeList(userPartialGridList, userStart, userEnd, length);

        Set<TripId> keySet = tripCubeList.keySet();
        for (TripId tripId : keySet) {
            double sim = LongestOverlappedCubeSeries(userPartialCubeList, tripCubeList.get(tripId), Integer.MAX_VALUE);
            tripSimListLOCS.put(tripId, sim);

            List<CubeId> intersection0 = new ArrayList<>(userPartialCubeList);
            intersection0.retainAll(tripCubeList.get(tripId));
            List<CubeId> intersection1 = intersection0.stream().distinct().collect(Collectors.toList());
            tripSimListLOC.put(tripId, intersection1.size());

            double sim1 = DynamicTimeWarping(userPartialCubeList, tripCubeList.get(tripId));
            tripSimListDTW.put(tripId, sim1);

            double sim2 = EditDistanceonRealSequence(userPartialCubeList, tripCubeList.get(tripId));
            tripSimListEDR.put(tripId, sim2);

            double sim3 = EditDistanceWithRealPenalty(userPartialCubeList, tripCubeList.get(tripId), new CubeId("0"));
            tripSimListERP.put(tripId, sim3);


        }

        // 给 filteredTripList 排序就是最后结果
//        List<TripId> topTripsDTW = tripSimListDTW.entrySet().stream().sorted((a, b) -> a.getValue() <= b.getValue() ? -1 : 1).limit(k).map(Map.Entry::getKey).collect(Collectors.toList());
//        List<TripId> topTripsEDR = tripSimListEDR.entrySet().stream().sorted((a, b) -> a.getValue() <= b.getValue() ? -1 : 1).limit(k).map(Map.Entry::getKey).collect(Collectors.toList());
//        List<TripId> topTripsERP = tripSimListERP.entrySet().stream().sorted((a, b) -> a.getValue() <= b.getValue() ? -1 : 1).limit(k).map(Map.Entry::getKey).collect(Collectors.toList());

        // topk LOCS
        List<TripId> topTripsLOCS = tripSimListLOCS.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
        Collections.sort(topTripsLOCS, new Comparator<TripId>() {
            @Override
            public int compare(TripId a, TripId b) { // 从大到小
                Double t = tripSimListLOCS.get(a) - tripSimListLOCS.get(b);
                int flag = -1;
                if (t < 0) flag = 1;
                if (t == 0) flag = 0;
                return flag;
            }
        });
        List<TripId> topkTripsLOCS = new ArrayList<>();
        if(topTripsLOCS.size() >= k) {
            topkTripsLOCS = topTripsLOCS.subList(0, k);
        } else {
            topkTripsLOCS = topTripsLOCS;
        }

        // topk LOC
        List<TripId> topTripsLOC = tripSimListLOC.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).map(Map.Entry::getKey).collect(Collectors.toList());
        List<TripId> topkTripsLOC = new ArrayList<>();
        if(topTripsLOC.size() >= k) {
            topkTripsLOC = topTripsLOC.subList(0, k);
        } else {
            topkTripsLOC = topTripsLOC;
        }

        // topk DTW
        List<TripId> topTripsDTW = tripSimListDTW.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
        Collections.sort(topTripsDTW, new Comparator<TripId>() {
            @Override
            public int compare(TripId a, TripId b) { // 从小到大
                Double t = tripSimListDTW.get(a) - tripSimListDTW.get(b);
                int flag = 1;
                if (t < 0) flag = -1;
                if (t == 0) flag = 0;
                return flag;
            }
        });
        List<TripId> topkTripsDTW = new ArrayList<>();
        if(topTripsDTW.size() >= k) {
            topkTripsDTW = topTripsDTW.subList(0, k);
        } else {
            topkTripsDTW = topTripsDTW;
        }

        // topk EDR
        List<TripId> topTripsEDR = tripSimListEDR.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
        Collections.sort(topTripsEDR, new Comparator<TripId>() {
            @Override
            public int compare(TripId a, TripId b) { // 从小到大
                Double t = tripSimListEDR.get(a) - tripSimListEDR.get(b);
                int flag = 1;
                if (t < 0) flag = -1;
                if (t == 0) flag = 0;
                return flag;
            }
        });
        List<TripId> topkTripsEDR = new ArrayList<>();
        if(topTripsEDR.size() >= k) {
            topkTripsEDR = topTripsEDR.subList(0, k);
        } else {
            topkTripsEDR = topTripsEDR;
        }


        // topk ERP
        List<TripId> topTripsERP = tripSimListERP.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
        Collections.sort(topTripsERP, new Comparator<TripId>() {
            @Override
            public int compare(TripId a, TripId b) { // 从小到大
                Double t = tripSimListERP.get(a) - tripSimListERP.get(b);
                int flag = 1;
                if (t < 0) flag = -1;
                if (t == 0) flag = 0;
                return flag;
            }
        });
        List<TripId> topkTripsERP = new ArrayList<>();
        if(topTripsERP.size() >= k) {
            topkTripsERP = topTripsERP.subList(0, k);
        } else {
            topkTripsERP = topTripsERP;
        }




        Long endTime = System.currentTimeMillis();
        System.out.println("[USERKNNSERVICE] Top-k time: " + (endTime - startTime)+ "ms");

        System.out.println("================================");
        for (TripId tripId : topkTripsLOCS) {
            System.out.println("LOCS " + tripId + ": " + tripSimListLOCS.get(tripId) + " " +tripCubeList.get(tripId));
        }

        System.out.println("================================");
        for (TripId tripId : topkTripsLOC) {
            System.out.println("LOC " + tripId + ": " + tripSimListLOC.get(tripId) + " " +tripCubeList.get(tripId));
        }

        System.out.println("================================");
        for (TripId tripId : topkTripsDTW) {
            System.out.println("DTW " + tripId + ": " + tripSimListDTW.get(tripId) + " " +tripCubeList.get(tripId));
        }

        System.out.println("================================");
        for (TripId tripId : topkTripsEDR) {
            System.out.println("EDR " + tripId + ": " + tripSimListEDR.get(tripId) + " " +tripCubeList.get(tripId));
        }

        System.out.println("================================");
        for (TripId tripId : topkTripsERP) {
            System.out.println("ERP " + tripId + ": " + tripSimListERP.get(tripId) + " " +tripCubeList.get(tripId));
        }


//        // 这个for循环导致运行时间非常短，因此还是选择一次运行一个length
//        // 调整轨迹长度
//        for(int length = 10; length <= 30; length += 10) {
//            System.out.println("============================================");
//            System.out.println("[USERKNNSERVICE] " + "trajectory length: " + length);
////            ArrayList<Vehicle> userPartialVehicleList = new ArrayList<>();
//            userPartialVehicleList = new ArrayList<>();
//            userPartialVehicleList.addAll(userVehicleList.subList(0, length));
//
////            ArrayList<GridId> userPartialGridList = new ArrayList<>();
////            ArrayList<CubeId> userPartialCubeList = new ArrayList<>();
//            userPartialGridList = new ArrayList<>();
//            userPartialCubeList = new ArrayList<>();
//
////            ArrayList<String> times = new ArrayList<>();
////            ArrayList<GridId> grids = new ArrayList<>();
////            ArrayList<CubeId> cubes = new ArrayList<>();
//            times = new ArrayList<>();
//            grids = new ArrayList<>();
//            cubes = new ArrayList<>();
//
////            Date d = new Date();
////            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
//            d = new Date();
//            sdf = new SimpleDateFormat("HH:mm:ss");
//            sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));
//
//            for (Vehicle vehicle : userPartialVehicleList) {
//                GridId gridId = encodeService.getGridID(vehicle.getLat(), vehicle.getLon());
//                grids.add(gridId);
//                if(userPartialGridList.isEmpty() || userPartialGridList.lastIndexOf(gridId) != (userPartialGridList.size() - 1)) {
//                    userPartialGridList.add(gridId);
//                }
//                CubeId cubeId = encodeService.encodeCube(vehicle.getLat(), vehicle.getLon(), vehicle.getRecordedTime());
//                cubes.add(cubeId);
//                if(userPartialCubeList.isEmpty() || userPartialCubeList.lastIndexOf(cubeId) != (userPartialCubeList.size() - 1)) {
//                    userPartialCubeList.add(cubeId);
//                }
//
//                d.setTime(vehicle.getRecordedTime() * 1000);
//                String date_hour_min_sec  = sdf.format(d);
//
//                times.add(date_hour_min_sec);
////            String[] hour_min_sec = date_hour_min_sec.split(":");   // 取的是时分秒
////            double t = (double)(Integer.parseInt(hour_min_sec[0]) * 3600 + Integer.parseInt(hour_min_sec[1]) * 60 + Integer.parseInt(hour_min_sec[2])); // 转化成秒
////            times.add(t);
//            }
//
////            Time userStart = Time.valueOf(times.get(0));
////            Time userEnd = Time.valueOf(times.get(times.size() - 1));
//            userStart = Time.valueOf(times.get(0));
//            userEnd = Time.valueOf(times.get(times.size() - 1));
//
//            System.out.println("[USERKNNSERVICE] times: " + times);
//            System.out.println("[USERKNNSERVICE] grids: " + grids);
//            System.out.println("[USERKNNSERVICE] userPartialGridList: " + userPartialGridList);
//            System.out.println("[USERKNNSERVICE] cubes: " + cubes);
//            System.out.println("[USERKNNSERVICE] userPartialCubeList: " + userPartialCubeList);
//
//
//
////            ArrayList<GridId> userPartialGridList = new ArrayList<>();
////            userPartialGridList.addAll(userGridList.subList(0, length));
////            ArrayList<CubeId> userPartialCubeList = new ArrayList<>();
////            userPartialCubeList.addAll(userCubeList.subList(0, length));
////            Time userStart = userStartTime;
////            Time userEnd = userEndTimes.get((length / 10) - 1);
////            System.out.println("[USERKNNSERVICE] " + "userPartialGridList: " + userPartialGridList);
////            System.out.println("[USERKNNSERVICE] " + "userPartialCubeList: " + userPartialCubeList);
//            System.out.println("[USERKNNSERVICE] " + "userStartTime: " + userStart);
//            System.out.println("[USERKNNSERVICE] " + "userEnd: " + userEnd);
//
//
////            Long startTime = System.currentTimeMillis();
//            startTime = System.currentTimeMillis();
////    public void getTopKTrips(ArrayList<CubeId> userCubeList, HashMap<TripId, ArrayList<CubeId>> tripCubeList, int k) throws InterruptedException {
//            getTripIdCubeList(userPartialGridList, userStart, userEnd, length);
//
////            Set<TripId> keySet = tripCubeList.keySet();
//            keySet = tripCubeList.keySet();
//            for (TripId tripId : keySet) {
//                double sim = LongestOverlappedCubeSeries(userPartialCubeList, tripCubeList.get(tripId), Integer.MAX_VALUE);
//                tripSimListLOCS.put(tripId, sim);
//
//                List<CubeId> intersection0 = new ArrayList<>(userPartialCubeList);
//                intersection0.retainAll(tripCubeList.get(tripId));
//                List<CubeId> intersection1 = intersection0.stream().distinct().collect(Collectors.toList());
//                tripSimListLOC.put(tripId, intersection1.size());
//
//                double sim1 = DynamicTimeWarping(userPartialCubeList, tripCubeList.get(tripId));
//                tripSimListDTW.put(tripId, sim1);
//
//                double sim2 = EditDistanceonRealSequence(userPartialCubeList, tripCubeList.get(tripId));
//                tripSimListEDR.put(tripId, sim2);
//
//                double sim3 = EditDistanceWithRealPenalty(userPartialCubeList, tripCubeList.get(tripId), new CubeId("0"));
//                tripSimListERP.put(tripId, sim3);
//
//
//            }
//
//            // 给 filteredTripList 排序就是最后结果
////        List<TripId> topTripsDTW = tripSimListDTW.entrySet().stream().sorted((a, b) -> a.getValue() <= b.getValue() ? -1 : 1).limit(k).map(Map.Entry::getKey).collect(Collectors.toList());
////        List<TripId> topTripsEDR = tripSimListEDR.entrySet().stream().sorted((a, b) -> a.getValue() <= b.getValue() ? -1 : 1).limit(k).map(Map.Entry::getKey).collect(Collectors.toList());
////        List<TripId> topTripsERP = tripSimListERP.entrySet().stream().sorted((a, b) -> a.getValue() <= b.getValue() ? -1 : 1).limit(k).map(Map.Entry::getKey).collect(Collectors.toList());
//
//            // topk LOCS
////            List<TripId> topTripsLOCS = tripSimListLOCS.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
//            topTripsLOCS = tripSimListLOCS.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
//            Collections.sort(topTripsLOCS, new Comparator<TripId>() {
//                @Override
//                public int compare(TripId a, TripId b) { // 从大到小
//                    Double t = tripSimListLOCS.get(a) - tripSimListLOCS.get(b);
//                    int flag = -1;
//                    if (t < 0) flag = 1;
//                    if (t == 0) flag = 0;
//                    return flag;
//                }
//            });
////            List<TripId> topkTripsLOCS = new ArrayList<>();
//            topkTripsLOCS = new ArrayList<>();
//            if(topTripsLOCS.size() >= k) {
//                topkTripsLOCS = topTripsLOCS.subList(0, k);
//            } else {
//                topkTripsLOCS = topTripsLOCS;
//            }
//
//            // topk LOC
////            List<TripId> topTripsLOC = tripSimListLOC.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).map(Map.Entry::getKey).collect(Collectors.toList());
////            List<TripId> topkTripsLOC = new ArrayList<>();
//            topTripsLOC = tripSimListLOC.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).map(Map.Entry::getKey).collect(Collectors.toList());
//            topkTripsLOC = new ArrayList<>();
//            if(topTripsLOC.size() >= k) {
//                topkTripsLOC = topTripsLOC.subList(0, k);
//            } else {
//                topkTripsLOC = topTripsLOC;
//            }
//
//            // topk DTW
////            List<TripId> topTripsDTW = tripSimListDTW.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
//            topTripsDTW = tripSimListDTW.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
//            Collections.sort(topTripsDTW, new Comparator<TripId>() {
//                @Override
//                public int compare(TripId a, TripId b) { // 从小到大
//                    Double t = tripSimListDTW.get(a) - tripSimListDTW.get(b);
//                    int flag = 1;
//                    if (t < 0) flag = -1;
//                    if (t == 0) flag = 0;
//                    return flag;
//                }
//            });
////            List<TripId> topkTripsDTW = new ArrayList<>();
//            topkTripsDTW = new ArrayList<>();
//            if(topTripsDTW.size() >= k) {
//                topkTripsDTW = topTripsDTW.subList(0, k);
//            } else {
//                topkTripsDTW = topTripsDTW;
//            }
//
//            // topk EDR
////            List<TripId> topTripsEDR = tripSimListEDR.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
//            topTripsEDR = tripSimListEDR.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
//            Collections.sort(topTripsEDR, new Comparator<TripId>() {
//                @Override
//                public int compare(TripId a, TripId b) { // 从小到大
//                    Double t = tripSimListEDR.get(a) - tripSimListEDR.get(b);
//                    int flag = 1;
//                    if (t < 0) flag = -1;
//                    if (t == 0) flag = 0;
//                    return flag;
//                }
//            });
////            List<TripId> topkTripsEDR = new ArrayList<>();
//            topkTripsEDR = new ArrayList<>();
//            if(topTripsEDR.size() >= k) {
//                topkTripsEDR = topTripsEDR.subList(0, k);
//            } else {
//                topkTripsEDR = topTripsEDR;
//            }
//
//
//            // topk ERP
////            List<TripId> topTripsERP = tripSimListERP.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
//            topTripsERP = tripSimListERP.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
//            Collections.sort(topTripsERP, new Comparator<TripId>() {
//                @Override
//                public int compare(TripId a, TripId b) { // 从小到大
//                    Double t = tripSimListERP.get(a) - tripSimListERP.get(b);
//                    int flag = 1;
//                    if (t < 0) flag = -1;
//                    if (t == 0) flag = 0;
//                    return flag;
//                }
//            });
////            List<TripId> topkTripsERP = new ArrayList<>();
//            topkTripsERP = new ArrayList<>();
//            if(topTripsERP.size() >= k) {
//                topkTripsERP = topTripsERP.subList(0, k);
//            } else {
//                topkTripsERP = topTripsERP;
//            }
//
//
//
//
////            Long endTime = System.currentTimeMillis();
//            endTime = System.currentTimeMillis();
//            System.out.println("[USERKNNSERVICE] Top-k time: " + (endTime - startTime)+ "ms");
//
//            System.out.println("================================");
//            for (TripId tripId : topkTripsLOCS) {
//                System.out.println("LOCS " + tripId + ": " + tripSimListLOCS.get(tripId) + " " +tripCubeList.get(tripId));
//            }
//
//            System.out.println("================================");
//            for (TripId tripId : topkTripsLOC) {
//                System.out.println("LOC " + tripId + ": " + tripSimListLOC.get(tripId) + " " +tripCubeList.get(tripId));
//            }
//
//            System.out.println("================================");
//            for (TripId tripId : topkTripsDTW) {
//                System.out.println("DTW " + tripId + ": " + tripSimListDTW.get(tripId) + " " +tripCubeList.get(tripId));
//            }
//
//            System.out.println("================================");
//            for (TripId tripId : topkTripsEDR) {
//                System.out.println("EDR " + tripId + ": " + tripSimListEDR.get(tripId) + " " +tripCubeList.get(tripId));
//            }
//
//            System.out.println("================================");
//            for (TripId tripId : topkTripsERP) {
//                System.out.println("ERP " + tripId + ": " + tripSimListERP.get(tripId) + " " +tripCubeList.get(tripId));
//            }
//
//        }


    }


//    // cube 做相似度计算
//    public double getCubeSimilarity(ArrayList<ShapeIndex.CubeId> cubes1, ArrayList<ShapeIndex.CubeId> cubes2, int theta) {
//
//    }


    // LOCS
    public double LongestOverlappedCubeSeries(ArrayList<CubeId> T1, ArrayList<CubeId> T2, int theta) {
        if (T1 == null || T2 == null || T1.size() == 0 || T2.size() == 0) {
            return 0;
        }

        int[][] dp = new int[T1.size()][T2.size()]; // dp数组
        int maxSimilarity = 0; // 相似度

        if (T1.get(0).equals(T2.get(0))) dp[0][0] = 1;

        for (int i = 1; i < T1.size(); i++) {
            if (T1.get(i).equals(T2.get(0))) {
                dp[i][0] = 1;
            } else {
                dp[i][0] = dp[i-1][0];
            }
        }

        for (int j = 1; j < T2.size(); j++) {
            if (T2.get(j).equals(T1.get(0))) {
                dp[0][j] = 1;
            } else {
                dp[0][j] = dp[0][j-1];
            }
        }

        for (int i = 1; i < T1.size(); i++) {
            for (int j = 1; j < T2.size(); j++) {
                if (Math.abs(i - j) <= theta) {
                    if (T1.get(i).equals(T2.get(j))) {
                        dp[i][j] = 1 + dp[i-1][j-1];
                    } else {
                        dp[i][j] = Math.max(dp[i-1][j], dp[i][j-1]);
                    }
                }

//                if (maxSimilarity < dp[i][j]) {
//                    maxSimilarity = dp[i][j];
//                }
            }
        }

        maxSimilarity = dp[T1.size() - 1][T2.size() - 1];
//        System.out.println("两条轨迹的相似度为：" + maxSimilarity);
        return maxSimilarity;
    }

    // DTW
    public double DynamicTimeWarping(ArrayList<CubeId> T1, ArrayList<CubeId> T2) {
        int resolution = hytraEngineManager.getParams().getResolution();


        if (T1.size() == 0 && T2.size() == 0) {
            return 0;
        }
        if (T1.size() == 0 || T2.size() == 0) {
            return Integer.MAX_VALUE;
        }

        double[][] dpInts = new double[T1.size() + 1][T2.size() + 1];

        for (int i = 1; i <= T1.size(); ++i) {
            dpInts[i][0] = Integer.MAX_VALUE;
        }

        for (int j = 1; j <= T2.size(); ++j) {
            dpInts[0][j] = Integer.MAX_VALUE;
        }

        for (int i = 1; i <= T1.size(); ++i) {
            for (int j = 1; j <= T2.size(); ++j) {
                int xyz1[] = decodeService.decodeZ3(Integer.parseInt(T1.get(i - 1).toString()), 0);
//                System.out.println(getDistances(xyz1[0], xyz1[2], xyz1[4], xyz1[1], xyz1[3], xyz1[5]));
//                System.out.println((xyz1[0]-xyz1[1]) + " " + (xyz1[2] - xyz1[3]) + " " + (xyz1[4] - xyz1[5]));
                int xyz2[] = decodeService.decodeZ3(Integer.parseInt(T2.get(j - 1).toString()), 0);
//                System.out.println(getDistances(xyz2[0], xyz2[2], xyz2[4], xyz2[1], xyz2[3], xyz2[5]));
//                System.out.println((xyz2[0]-xyz2[1]) + " " + (xyz2[2] - xyz2[3]) + " " + (xyz2[4] - xyz2[5]));
//                dpInts[i][j] = distFunc.apply(T1.get(i - 1), T2.get(j - 1)) + min(dpInts[i - 1][j - 1], dpInts[i - 1][j], dpInts[i][j - 1]);
                dpInts[i][j] = getDistances(xyz1[0], xyz1[2], xyz1[4], xyz2[0], xyz2[2], xyz2[4]) + min(dpInts[i - 1][j - 1], dpInts[i - 1][j], dpInts[i][j - 1]);
            }
        }

        return dpInts[T1.size()][T2.size()];
    }

    // EDR
    public double EditDistanceonRealSequence(ArrayList<CubeId> T1, ArrayList<CubeId> T2) {
        int resolution = hytraEngineManager.getParams().getResolution();

        if (T1 == null || T1.size() == 0) {
            if (T2 != null) {
                return T2.size();
            } else {
                return 0;
            }
        }

        if (T2 == null || T2.size() == 0) {
            return T1.size();
        }

        int[][] dpInts = new int[T1.size() + 1][T2.size() + 1];

        for (int i = 1; i <= T1.size(); ++i) {
            dpInts[i][0] = i;
        }

        for (int j = 1; j <= T2.size(); ++j) {
            dpInts[0][j] = j;
        }

        for (int i = 1; i <= T1.size(); ++i) {
            for (int j = 1; j <= T2.size(); ++j) {
                int xyz1[] = decodeService.decodeZ3(Integer.parseInt(T1.get(i - 1).toString()), 0);
                int xyz2[] = decodeService.decodeZ3(Integer.parseInt(T2.get(j - 1).toString()), 0);
                int subCost = 1;
                // TODO 确定阈值 根号3
//                if (getDistances(xyz1[0], xyz1[2], xyz1[4], xyz2[0], xyz2[2], xyz2[4]) <= Double.MAX_VALUE) {
                if (getDistances(xyz1[0], xyz1[2], xyz1[4], xyz2[0], xyz2[2], xyz2[4]) <= Math.sqrt(3.0)) {
                    subCost = 0;
                }
                dpInts[i][j] = min(dpInts[i - 1][j - 1] + subCost, dpInts[i - 1][j] + 1, dpInts[i][j - 1] + 1);
            }
        }

        return dpInts[T1.size()][T2.size()] * 1.0;
    }

    // ERP
    // g: cube0
    public double EditDistanceWithRealPenalty(List<CubeId> T1, List<CubeId> T2, CubeId g) {
        int resolution = hytraEngineManager.getParams().getResolution();

        int xyz[] = decodeService.decodeZ3(Integer.parseInt(g.toString()), 0);

        if (T1 == null || T1.size() == 0) {
            double res = 0.0;
            if (T2 != null) {
                for (CubeId t : T2) {
                    int xyz2[] = decodeService.decodeZ3(Integer.parseInt(t.toString()), 0);
                    res += getDistances(xyz2[0], xyz2[2], xyz2[4], xyz[0], xyz[2], xyz[4]);
                }
            }
            return res;
        }

        if (T2 == null || T2.size() == 0) {
            double res = 0.0;
            for (CubeId t : T1) {
                int xyz1[] = decodeService.decodeZ3(Integer.parseInt(t.toString()), 0);
                res += getDistances(xyz1[0], xyz1[2], xyz1[4], xyz[0], xyz[2], xyz[4]);
            }
            return res;
        }

        double[][] dpInts = new double[T1.size() + 1][T2.size() + 1];

        for (int i = 1; i <= T1.size(); ++i) {
            int xyz1[] = decodeService.decodeZ3(Integer.parseInt(T1.get(i - 1).toString()), 0);
            dpInts[i][0] = getDistances(xyz1[0], xyz1[2], xyz1[4], xyz[0], xyz[2], xyz[4]) + dpInts[i - 1][0];
        }

        for (int j = 1; j <= T2.size(); ++j) {
            int xyz2[] = decodeService.decodeZ3(Integer.parseInt(T2.get(j - 1).toString()), 0);
            dpInts[0][j] = getDistances(xyz2[0], xyz2[2], xyz2[4], xyz[0], xyz[2], xyz[4]) + dpInts[0][j - 1];
        }

        for (int i = 1; i <= T1.size(); ++i) {
            for (int j = 1; j <= T2.size(); ++j) {
                int xyz1[] = decodeService.decodeZ3(Integer.parseInt(T1.get(i - 1).toString()), 0);
                int xyz2[] = decodeService.decodeZ3(Integer.parseInt(T2.get(j - 1).toString()), 0);

                dpInts[i][j] = min(dpInts[i - 1][j - 1] + getDistances(xyz1[0], xyz1[2], xyz1[4], xyz2[0], xyz2[2], xyz2[4]),
                        dpInts[i - 1][j] + getDistances(xyz1[0], xyz1[2], xyz1[4], xyz[0], xyz[2], xyz[4]),
                        dpInts[i][j - 1] + getDistances(xyz2[0], xyz2[2], xyz2[4], xyz[0], xyz[2], xyz[4]));
            }
        }

        return dpInts[T1.size()][T2.size()] * 1.0 / Math.max(T1.size(), T2.size());
    }

    // distance between two cubes
    public double getDistances(int x1, int y1, int z1, int x2, int y2, int z2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) + Math.pow(z2 - z1, 2));
    }

    public int min(int a, int b, int c) {
        if (a > b) a = b;
        if (a > c) a = c;
        return a;
    }

    public double min(double a, double b, double c) {
        if (a > b) a = b;
        if (a > c) a = c;
        return a;
    }


}
