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
    int k = 5;


    // TODO hashmap初始化

    public ArrayList<GridId> getUserGridList() {
        return userGridList;
    }

    public ArrayList<CubeId> getUserCubeList() {
        return userCubeList;
    }

    public Time getUserStartTime() {
        return userStartTime;
    }

    public Time getUserEndTime() {
        return userEndTime;
    }

    TripId userTripId;
    // user - grid
    ArrayList<GridId> userGridList = new ArrayList<>();
    // user - cube
    ArrayList<CubeId> userCubeList = new ArrayList<>();
    // user - [start_time, end_time]
    Time userStartTime;
    Time userEndTime;
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

    Map<TripId, ArrayList<Vehicle>> vehiclesByTripId  = new HashMap<>();
    // 模拟用户轨迹
    public void getUserTra() throws InterruptedException {
        Thread.sleep(300000);
        vehiclesByTripId  = realtimeService.get_vehiclesByTripId();
        TripId[] keys = vehiclesByTripId.keySet().toArray(new TripId[0]); //将map里的key值取出，并放进数组里
        int random = (int) (Math.random()*(keys.length)); //生成随机数
        TripId randomKey = keys[random]; //随机取key值

        System.out.println("============================================");
        System.out.println("[USERKNNSERVICE] randomKey: " + randomKey);   //输出随机的key值

        userTripId = randomKey;
//        userTripId = new TripId("EN_B3-Sunday-051800_Q56_456");



        ArrayList<Vehicle> vehicles = vehiclesByTripId.get(userTripId);

//        System.out.println(Arrays.asList(keys).contains("[USERKNNSERVICE] if vehicle set contains usertripid: " + new TripId("QV_B3-Sunday-044200_Q46_601")));
//        ArrayList<Vehicle> vehicles = vehiclesByTripId.get(new TripId("QV_B3-Sunday-044200_Q46_601"));


        ArrayList<String> times = new ArrayList<>();
        ArrayList<GridId> grids = new ArrayList<>();
        ArrayList<CubeId> cubes = new ArrayList<>();

        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));


        for (Vehicle vehicle : vehicles) {
            GridId gridId = encodeService.getGridID(vehicle.getLat(), vehicle.getLon());
            grids.add(gridId);
            if(userGridList.isEmpty() || userGridList.lastIndexOf(gridId) != (userGridList.size() - 1)) {
                userGridList.add(gridId);
            }
            CubeId cubeId = encodeService.encodeCube(vehicle.getLat(), vehicle.getLon(), vehicle.getRecordedTime());
            cubes.add(cubeId);
            if(userCubeList.isEmpty() || userCubeList.lastIndexOf(cubeId) != (userCubeList.size() - 1)) {
                userCubeList.add(cubeId);
            }

            d.setTime(vehicle.getRecordedTime() * 1000);
            String date_hour_min_sec  = sdf.format(d);

            times.add(date_hour_min_sec);
//            String[] hour_min_sec = date_hour_min_sec.split(":");   // 取的是时分秒
//            double t = (double)(Integer.parseInt(hour_min_sec[0]) * 3600 + Integer.parseInt(hour_min_sec[1]) * 60 + Integer.parseInt(hour_min_sec[2])); // 转化成秒
//            times.add(t);
        }

        userStartTime = Time.valueOf(times.get(0));
        userEndTime = Time.valueOf(times.get(times.size() - 1));


        System.out.println("[USERKNNSERVICE] userId(TripId): " + userTripId);
        System.out.println("[USERKNNSERVICE] times: " + times);
        System.out.println("[USERKNNSERVICE] grids: " + grids);
        System.out.println("[USERKNNSERVICE] userGridList: " + userGridList);
        System.out.println("[USERKNNSERVICE] cubes: " + cubes);
        System.out.println("[USERKNNSERVICE] userCubeList: " + userCubeList);
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
    public ArrayList<TripId> filterTripList(ArrayList<GridId> userGridList, Time userStartTime, Time userEndTime) {
        ArrayList<TripId> filteredTripList = new ArrayList<>();

        // 插一嘴，把虚拟的usertripid对应的shape的trips也加进去
        List<TripsEntity> tripsEntityList = tripsDao.findAllByTripId(userTripId.toString());
        ShapeId shapeId = new ShapeId(tripsEntityList.get(0).getShapeId());

        // top-k shapes -> trips of top-k shapes
        ArrayList<TripId> tripIds = shapeIndex.getTripsOfTopKShapes(shapeId, userGridList, k);

        // 插一嘴，把虚拟的usertripid对应的shape的trips也加进去
        for (TripsEntity tripsEntity : tripsEntityList) {
            ArrayList<TripId> tripIds1 = shapeIndex.getTripIdsByShapeId(new ShapeId(tripsEntity.getShapeId()));
            for (TripId tripId : tripIds1) {
                if(!tripIds.contains(tripId)) tripIds.add(tripId);
            }
        }


        tripStartEndList = scheduleIndex.getTripStartEndList();
        System.out.println("[USERKNNSERVICE] userStartTime: " + userStartTime);
        System.out.println("[USERKNNSERVICE] userEndTime: " + userEndTime);
        System.out.println("[USERKNNSERVICE] userTripId start and end time: " + tripStartEndList.get(userTripId));

        System.out.println("[USERKNNSERVICE] size of trips of top-k shapes: " + tripIds.size());
        System.out.println("[USERKNNSERVICE] if the trips of top-k shapes contain usertripid: " + tripIds.contains(userTripId));

        for (TripId tripId : tripIds) {
            ArrayList<Time> times = tripStartEndList.get(tripId);

            if (times != null && times.get(0).before(userEndTime) && times.get(1).after(userStartTime)) {
                filteredTripList.add(tripId);
            }
        }

        return filteredTripList;
    }


    // 取得所有轨迹
//    public void getTripIdCubeList(ArrayList<TripId> filteredTripList) {
    public void getTripIdCubeList() throws InterruptedException {
        getUserTra(); // 先构建用户轨迹的索引表；获取用户轨迹起始时间和结束时间
        ArrayList<TripId> filteredTripList = filterTripList(userGridList, userStartTime, userEndTime); // 获取所有要判断的tripid
//        Map<TripId, ArrayList<Vehicle>> vehiclesByTripId  = realtimeService.get_vehiclesByTripId();
        System.out.println("[USERKNNSERVICE] " + "if the filtered list contains usertripid: " +filteredTripList.contains(userTripId));
        for (TripId tripId : filteredTripList) {
            if (vehiclesByTripId.get(tripId) != null && !vehiclesByTripId.get(tripId).isEmpty()) {
                ArrayList<Vehicle> vehicles = vehiclesByTripId.get(tripId);
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

    int theta = 5;

    // trip_id - similarity
    HashMap<TripId, Double> tripSimListDTW = new HashMap<>();
    HashMap<TripId, Double> tripSimListEDR = new HashMap<>();
    HashMap<TripId, Double> tripSimListERP = new HashMap<>();

    // 获取 Top-k trip
    public void getTopKTrips(int k) throws InterruptedException {

        Long startTime = System.currentTimeMillis();
//    public void getTopKTrips(ArrayList<CubeId> userCubeList, HashMap<TripId, ArrayList<CubeId>> tripCubeList, int k) throws InterruptedException {
        getTripIdCubeList();

        Set<TripId> keySet = tripCubeList.keySet();
        for (TripId tripId : keySet) {
            double sim = DynamicTimeWarping(userCubeList, tripCubeList.get(tripId));
            tripSimListDTW.put(tripId, sim);

            double sim1 = EditDistanceonRealSequence(userCubeList, tripCubeList.get(tripId));
            tripSimListEDR.put(tripId, sim1);

            double sim2 = EditDistanceWithRealPenalty(userCubeList, tripCubeList.get(tripId), new CubeId("1"));
            tripSimListERP.put(tripId, sim2);
        }

        // 给 filteredTripList 排序就是最后结果
//        List<TripId> topTripsDTW = tripSimListDTW.entrySet().stream().sorted((a, b) -> a.getValue() <= b.getValue() ? -1 : 1).limit(k).map(Map.Entry::getKey).collect(Collectors.toList());
//        List<TripId> topTripsEDR = tripSimListEDR.entrySet().stream().sorted((a, b) -> a.getValue() <= b.getValue() ? -1 : 1).limit(k).map(Map.Entry::getKey).collect(Collectors.toList());
//        List<TripId> topTripsERP = tripSimListERP.entrySet().stream().sorted((a, b) -> a.getValue() <= b.getValue() ? -1 : 1).limit(k).map(Map.Entry::getKey).collect(Collectors.toList());

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
        System.out.println("[USERKNNSERVICE] Top-k time: " + (endTime - startTime - 300000) / 1000 + "s");

        System.out.println("================================");
        for (TripId tripId : topkTripsDTW) {
            System.out.println("DTW " + tripId + ": " + tripSimListDTW.get(tripId));
        }

        System.out.println("================================");
        for (TripId tripId : topkTripsEDR) {
            System.out.println("EDR " + tripId + ": " + tripSimListEDR.get(tripId));
        }

        System.out.println("================================");
        for (TripId tripId : topkTripsERP) {
            System.out.println("ERP " + tripId + ": " + tripSimListERP.get(tripId));
        }
    }


//    // cube 做相似度计算
//    public double getCubeSimilarity(ArrayList<ShapeIndex.CubeId> cubes1, ArrayList<ShapeIndex.CubeId> cubes2, int theta) {
//
//    }

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
