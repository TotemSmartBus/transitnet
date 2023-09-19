package whu.edu.cs.transitnet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import whu.edu.cs.transitnet.dao.TripsDao;
import whu.edu.cs.transitnet.pojo.RealTimePointEntity;
import whu.edu.cs.transitnet.pojo.TripsEntity;
import whu.edu.cs.transitnet.service.index.*;

import java.io.IOException;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HistoricalKNNExpServiceTEST {

    @Autowired
    HytraEngineManager hytraEngineManager;

    @Autowired
    HistoricalTripIndex historicalTripIndex;

    @Autowired
    EncodeService encodeService;

    @Autowired
    DecodeService decodeService;

    @Autowired
    ShapeIndex shapeIndex;

    @Autowired
    ScheduleIndex scheduleIndex;

    // 这个k指的是取前k个shape;15 12 9 6 3
    int kShape = 50;


    HashMap<TripId, ArrayList<RealTimePointEntity>> tripPointList = new HashMap<>();
    // trip_id - [start_time, end_time] 写个类
    private HashMap<TripId, ArrayList<Time>> tripStartEndList = new HashMap<>();
    // trip - cube  map做操作 删掉list
    private HashMap<TripId, ArrayList<CubeId>> tripCubeList = new HashMap<>();

    private HashMap<TripId, ArrayList<CubeId>> allTripCubeList = new HashMap<>();


    @Autowired
    TripsDao tripsDao;

    /**
     * schedule 做筛选；user: [start_time, end_time]
     * @param userTripId
     * @param userGridList
     * @param userStart
     * @param userEnd
     * @return
     */
    public ArrayList<TripId> filterTripList(TripId userTripId, ArrayList<GridId> userGridList, Time userStart, Time userEnd) {
        ArrayList<TripId> filteredTripList = new ArrayList<>();

        // 取出 user tripId 对应的 shapeId
        List<TripsEntity> tripsEntityList = tripsDao.findAllByTripId(userTripId.toString());

        // 如果为空 也就是数据库里面没有这个 tripId
        if (tripsEntityList.isEmpty()) {
            return filteredTripList;
        }

        // 正常步骤往下走
        ShapeId shapeId = new ShapeId(tripsEntityList.get(0).getShapeId());

        // top-k shapes -> trips of top-k shapes
        ArrayList<TripId> tripIds = shapeIndex.getTripsOfTopKShapes(shapeId, userGridList, kShape);

        // 插一嘴，把 usertripid 对应的 shape 的 trips 也加进去
        for (TripsEntity tripsEntity : tripsEntityList) {
            ArrayList<TripId> tripIds1 = shapeIndex.getTripIdsByShapeId(new ShapeId(tripsEntity.getShapeId()));
            if(tripIds1.isEmpty()){
                continue;
            }
            for (TripId tripId : tripIds1) {
                if(!tripIds.contains(tripId)) {
                    tripIds.add(tripId);
                }
            }
        }


        tripStartEndList = scheduleIndex.getTripStartEndList();

        System.out.println("[HISTORICALKNNEXPSERVICE] userTripId scheduled start and end time: " + tripStartEndList.get(userTripId));

        System.out.println("[HISTORICALKNNEXPSERVICE] size of trips of top-k shapes: " + tripIds.size());
        System.out.println("[HISTORICALKNNEXPSERVICE] if the trips of top-k shapes contain usertripid: " + tripIds.contains(userTripId));

        for (TripId tripId : tripIds) {
            ArrayList<Time> times = tripStartEndList.get(tripId);
            if (times != null) {
                Time start = times.get(0);
                Time end = times.get(1);

                Long startToLong = start.getTime();
                Long endToLong = end.getTime();

                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

                Date d1 = new Date();
                // 往前推30分钟
                d1.setTime(startToLong - 30 * 60 * 1000);
                String startForward = sdf.format(d1);

                Date d2 = new Date();
                // 往后推30分钟
                d2.setTime(endToLong + 30 * 60 * 1000);
                String endBackward = sdf.format(d2);

                if (Time.valueOf(startForward).before(userEnd) && Time.valueOf(endBackward).after(userStart)) {
                    filteredTripList.add(tripId);
                }
            }

        }



        return filteredTripList;
    }

    /**
     * 不用 schedule 做筛选；user: [start_time, end_time]
     * @param userTripId
     * @param userGridList
     * @return
     */
    public ArrayList<TripId> filterTripList(TripId userTripId, ArrayList<GridId> userGridList) {
        ArrayList<TripId> filteredTripList = new ArrayList<>();

        // 取出 user tripId 对应的 shapeId
        List<TripsEntity> tripsEntityList = tripsDao.findAllByTripId(userTripId.toString());

        // 如果为空 也就是数据库里面没有这个 tripId
        if (tripsEntityList.isEmpty()) {
            return filteredTripList;
        }

        // 正常步骤往下走
        ShapeId shapeId = new ShapeId(tripsEntityList.get(0).getShapeId());

        // top-k shapes -> trips of top-k shapes
        ArrayList<TripId> tripIds = shapeIndex.getTripsOfTopKShapes(shapeId, userGridList, kShape);

        // 插一嘴，把 usertripid 对应的 shape 的 trips 也加进去
        for (TripsEntity tripsEntity : tripsEntityList) {
            ArrayList<TripId> tripIds1 = shapeIndex.getTripIdsByShapeId(new ShapeId(tripsEntity.getShapeId()));
            for (TripId tripId : tripIds1) {
                if(!tripIds.contains(tripId)) {
                    tripIds.add(tripId);
                }
            }
        }

        System.out.println("[HISTORICALKNNEXPSERVICE] size of trips of top-k shapes: " + tripIds.size());
        System.out.println("[HISTORICALKNNEXPSERVICE] if the trips of top-k shapes contain usertripid: " + tripIds.contains(userTripId));

        return tripIds;
    }

    /**
     * 使用 shape - trip - schedule 两层索引取得所有轨迹
     * @param userTripId
     * @param userGridList
     * @param userStart
     * @param userEnd
     * @throws InterruptedException
     * @throws ParseException
     */
    public void getTripIdCubeList(TripId userTripId, ArrayList<GridId> userGridList, Time userStart, Time userEnd) throws InterruptedException, ParseException {

        tripCubeList = new HashMap<>();

        // 获取所有要判断的tripid
        ArrayList<TripId> filteredTripList = filterTripList(userTripId, userGridList, userStart, userEnd);

        if (filteredTripList.isEmpty()) {
            return;
        }

        System.out.println("[HISTORICALKNNEXPSERVICE] " + "size of filtered trips: " + filteredTripList.size());

        System.out.println("[HISTORICALKNNEXPSERVICE] " + "if the filtered list contains usertripid: " + filteredTripList.contains(userTripId));
        for (TripId tripId : filteredTripList) {
            tripCubeList.put(tripId, allTripCubeList.get(tripId));
        }

    }

    /**
     * 使用 shape - trip 一层索引
     * @param userTripId
     * @param userGridList
     * @throws InterruptedException
     * @throws ParseException
     */
    public void getTripIdCubeList(TripId userTripId, ArrayList<GridId> userGridList) throws InterruptedException, ParseException {
        tripCubeList = new HashMap<>();

        // 获取所有要判断的tripid
        ArrayList<TripId> filteredTripList = filterTripList(userTripId, userGridList);

        if (filteredTripList.isEmpty()) {
            return;
        }

        System.out.println("[HISTORICALKNNEXPSERVICE] " + "size of filtered trips: " + filteredTripList.size());
        System.out.println("[HISTORICALKNNEXPSERVICE] " + "if the filtered list contains usertripid: " + filteredTripList.contains(userTripId));
        for (TripId tripId : filteredTripList) {
            tripCubeList.put(tripId, allTripCubeList.get(tripId));
        }

    }

    /**
     * 无索引
     * @throws InterruptedException
     * @throws ParseException
     */
    public void getTripIdCubeList() throws InterruptedException, ParseException {
        tripCubeList = new HashMap<>(allTripCubeList);

    }


    // trip_id - similarity
    HashMap<TripId, Double> tripSimListLOCS = new HashMap<>();
    HashMap<TripId, Integer> tripSimListLOC = new HashMap<>();

    HashMap<TripId, Double> tripSimListDTW = new HashMap<>();
    HashMap<TripId, Double> tripSimListEDR = new HashMap<>();
    HashMap<TripId, Double> tripSimListERP = new HashMap<>();


    /**
     * 获取 Top-k trip （用户自己指定k，不是前面定义的变量k）
     * @param k
     * @throws IOException
     * @throws InterruptedException
     * @throws ParseException
     */
    public void getTopKTrips(int k) throws IOException, InterruptedException, ParseException {
        allTripCubeList = historicalTripIndex.getTripCubeList();

        // 获取 tripPointList，遍历每个 trip 做实验
        HashMap<TripId, ArrayList<RealTimePointEntity>> partialTripPointList = historicalTripIndex.getTripPointList();
        int quantity  = partialTripPointList.keySet().size();
        // 0.2, 0.4, 0.6, 0.8 ,1.0
        double ratio = 1;
        int partialQuantity = (int)(ratio * quantity);

        int num = 0;
        for(TripId tripId : partialTripPointList.keySet()) {
            if(num > partialQuantity) {
                break;
            }
            ;
            num++;

            tripPointList.put(tripId, partialTripPointList.get(tripId));
        }

        Long partialTime = 0L;
        Long totalTime = 0L;
        int partialNum = 0;
        int totalNum = 0;

        for (TripId tripId : tripPointList.keySet()) {
            // 为空则 continue
            if (tripPointList.get(tripId).isEmpty()) {
                continue;
            }

            TripId userTripId = tripId;
            // vehicle list
            ArrayList<RealTimePointEntity> userPointList = new ArrayList<>();
            userPointList = tripPointList.get(userTripId);

            ArrayList<GridId> userGridList = new ArrayList<>();
            ArrayList<CubeId> userCubeList = new ArrayList<>();

            ArrayList<String> times = new ArrayList<>();
            ArrayList<GridId> grids = new ArrayList<>();
            ArrayList<CubeId> cubes = new ArrayList<>();

            Date d = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));

            for (RealTimePointEntity realTimePointEntity : userPointList) {
                GridId gridId = encodeService.getGridID(realTimePointEntity.getLat(), realTimePointEntity.getLon());
                grids.add(gridId);
                if(userGridList.isEmpty() || userGridList.lastIndexOf(gridId) != (userGridList.size() - 1)) {
                    userGridList.add(gridId);
                }

                String recordedTime = realTimePointEntity.getRecordedTime();
                Date parse = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(recordedTime);
                Long time = parse.getTime();

                CubeId cubeId = encodeService.encodeCube(realTimePointEntity.getLat(), realTimePointEntity.getLon(), time);
                cubes.add(cubeId);
                if(userCubeList.isEmpty() || userCubeList.lastIndexOf(cubeId) != (userCubeList.size() - 1)) {
                    userCubeList.add(cubeId);
                }

                // 这个实体中的 recordedTime 是个字符串
                d.setTime(time);
                String date_hour_min_sec  = sdf.format(d);

                times.add(date_hour_min_sec);
            }

            Time userStart = Time.valueOf(times.get(0));
            Time userEnd = Time.valueOf(times.get(times.size() - 1));

            // 以上不会出现任何问题

            System.out.println("============================================");
            System.out.println("[HISTORICALKNNEXPSERVICE] user raw grids: " + grids);
            System.out.println("[HISTORICALKNNEXPSERVICE] userGridList: " + userGridList);
            System.out.println("[HISTORICALKNNEXPSERVICE] user raw cubes: " + cubes);
            System.out.println("[HISTORICALKNNEXPSERVICE] userCubeList: " + userCubeList);
            System.out.println("[HISTORICALKNNEXPSERVICE] userStartTime: " + userStart);
            System.out.println("[HISTORICALKNNEXPSERVICE] userEndTime: " + userEnd);

            Long startTime = System.currentTimeMillis();

            int choice = 1;

            switch (choice) {
                case 2:
                    // 两层索引
                    getTripIdCubeList(userTripId, userGridList, userStart, userEnd);
                    break;
                case 1:
                    // 一层索引
                    getTripIdCubeList(userTripId, userGridList);
                    break;
                case 0:
                    // 无索引
                    getTripIdCubeList();
                    break;
                default:
                    // 两层索引
                    getTripIdCubeList(userTripId, userGridList, userStart, userEnd);
            }


            System.out.println("[HISTORICALKNNEXPSERVICE] size of tripCubeList: " + tripCubeList.size());

            // tripCubeList 为空则 continue
            if (tripCubeList.isEmpty()) {
                System.out.println("[HISTORICALKNNEXPSERVICE] tripCubeList is empty! Early termination of the procedure!");
                continue;
            }

            Set<TripId> keySet = tripCubeList.keySet();
            for (TripId tripId1 : keySet) {
                if(tripCubeList.get(tripId1)==null) {
                    continue;
                }

                // 请不要删除以下注释代码

                // 利用 LOCS 计算出来的相似度
                // double sim = LongestOverlappedCubeSeries(userCubeList, tripCubeList.get(tripId1), Integer.MAX_VALUE);
                // tripSimListLOCS.put(tripId1, sim);

                // 利用 LOC 计算出来的相似度
                List<CubeId> intersection0 = new ArrayList<>(userCubeList);
                intersection0.retainAll(tripCubeList.get(tripId1));
                List<CubeId> intersection1 = intersection0.stream().distinct().collect(Collectors.toList());
                tripSimListLOC.put(tripId1, intersection1.size());

                // 利用 DTW 计算出来的相似度
                // double sim1 = DynamicTimeWarping(userCubeList, tripCubeList.get(tripId1));
                // tripSimListDTW.put(tripId1, sim1);

                // 利用 EDR 计算出来的相似度
                // double sim2 = EditDistanceonRealSequence(userCubeList, tripCubeList.get(tripId1));
                // tripSimListEDR.put(tripId1, sim2);

                // 利用 EDRP 计算出来的相似度
                // double sim3 = EditDistanceWithRealPenalty(userCubeList, tripCubeList.get(tripId1), new CubeId("0"));
                // tripSimListERP.put(tripId1, sim3);
            }

            // topk LOCS
//            List<TripId> topTripsLOCS = tripSimListLOCS.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
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
//            List<TripId> topkTripsLOCS = new ArrayList<>();
//            if(topTripsLOCS.size() >= k) {
//                topkTripsLOCS = topTripsLOCS.subList(0, k);
//            } else {
//                topkTripsLOCS = topTripsLOCS;
//            }

            // topk LOC
            List<TripId> topTripsLOC = tripSimListLOC.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).map(Map.Entry::getKey).collect(Collectors.toList());
            List<TripId> topkTripsLOC = new ArrayList<>();
            if(topTripsLOC.size() >= k) {
                topkTripsLOC = topTripsLOC.subList(0, k);
            } else {
                topkTripsLOC = topTripsLOC;
            }

            // topk DTW
//            List<TripId> topTripsDTW = tripSimListDTW.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
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
//            List<TripId> topkTripsDTW = new ArrayList<>();
//            if(topTripsDTW.size() >= k) {
//                topkTripsDTW = topTripsDTW.subList(0, k);
//            } else {
//                topkTripsDTW = topTripsDTW;
//            }

              // topk EDR
//            List<TripId> topTripsEDR = tripSimListEDR.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
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
//            List<TripId> topkTripsEDR = new ArrayList<>();
//            if(topTripsEDR.size() >= k) {
//                topkTripsEDR = topTripsEDR.subList(0, k);
//            } else {
//                topkTripsEDR = topTripsEDR;
//            }

              // topk ERP
//            List<TripId> topTripsERP = tripSimListERP.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
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
//            List<TripId> topkTripsERP = new ArrayList<>();
//            if(topTripsERP.size() >= k) {
//                topkTripsERP = topTripsERP.subList(0, k);
//            } else {
//                topkTripsERP = topTripsERP;
//            }


            Long endTime = System.currentTimeMillis();
            Long running = endTime - startTime;
            System.out.println("[HISTORICALKNNEXPSERVICE] Top-k time: " + (endTime - startTime)+ "ms");

            if (running >= 10 && running < 100) {

                partialTime += running;
                partialNum++;
            }
            totalTime += running;
            totalNum++;
            System.out.println("[HISTORICALKNNEXPSERVICE] the number of scanned trips: " + totalNum);

        }


        if(totalNum != 0 ) {
            System.out.println("[HISTORICALKNNEXPSERVICE] totalTime | totalNum: " + totalTime / totalNum);
            System.out.println("[HISTORICALKNNEXPSERVICE] partialTime | totalNum: " + partialTime / totalNum);
            System.out.println("[HISTORICALKNNEXPSERVICE] partialTime | partialNum: " + partialTime / partialNum);
        } else {
            System.out.println("totalNum == 0");
        }

    }


    /**
     * LOCS
     * @param T1
     * @param T2
     * @param theta
     * @return
     */
    public double LongestOverlappedCubeSeries(ArrayList<CubeId> T1, ArrayList<CubeId> T2, int theta) {
        if (T1 == null || T2 == null || T1.size() == 0 || T2.size() == 0) {
            return 0;
        }

        // dp数组
        int[][] dp = new int[T1.size()][T2.size()];
        // 相似度
        int maxSimilarity = 0;

        if (T1.get(0).equals(T2.get(0))) {
            dp[0][0] = 1;
        }

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

            }
        }

        maxSimilarity = dp[T1.size() - 1][T2.size() - 1];

        return maxSimilarity;
    }

    /**
     * DTW
     * @param T1
     * @param T2
     * @return
     */
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
                int[] xyz1 = decodeService.decodeZ3(Integer.parseInt(T1.get(i - 1).toString()), 0);
                int[] xyz2 = decodeService.decodeZ3(Integer.parseInt(T2.get(j - 1).toString()), 0);
                dpInts[i][j] = getDistances(xyz1[0], xyz1[2], xyz1[4], xyz2[0], xyz2[2], xyz2[4]) + min(dpInts[i - 1][j - 1], dpInts[i - 1][j], dpInts[i][j - 1]);
            }
        }

        return dpInts[T1.size()][T2.size()];
    }

    /**
     * EDR
     * @param T1
     * @param T2
     * @return
     */
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
                int[] xyz1 = decodeService.decodeZ3(Integer.parseInt(T1.get(i - 1).toString()), 0);
                int[] xyz2 = decodeService.decodeZ3(Integer.parseInt(T2.get(j - 1).toString()), 0);
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

    /**ERP
     *
     * @param T1
     * @param T2
     * @param g cube0
     * @return
     */

    public double EditDistanceWithRealPenalty(List<CubeId> T1, List<CubeId> T2, CubeId g) {
        int resolution = hytraEngineManager.getParams().getResolution();

        int[] xyz = decodeService.decodeZ3(Integer.parseInt(g.toString()), 0);

        if (T1 == null || T1.size() == 0) {
            double res = 0.0;
            if (T2 != null) {
                for (CubeId t : T2) {
                    int[] xyz2 = decodeService.decodeZ3(Integer.parseInt(t.toString()), 0);
                    res += getDistances(xyz2[0], xyz2[2], xyz2[4], xyz[0], xyz[2], xyz[4]);
                }
            }
            return res;
        }

        if (T2 == null || T2.size() == 0) {
            double res = 0.0;
            for (CubeId t : T1) {
                int[] xyz1 = decodeService.decodeZ3(Integer.parseInt(t.toString()), 0);
                res += getDistances(xyz1[0], xyz1[2], xyz1[4], xyz[0], xyz[2], xyz[4]);
            }
            return res;
        }

        double[][] dpInts = new double[T1.size() + 1][T2.size() + 1];

        for (int i = 1; i <= T1.size(); ++i) {
            int[] xyz1 = decodeService.decodeZ3(Integer.parseInt(T1.get(i - 1).toString()), 0);
            dpInts[i][0] = getDistances(xyz1[0], xyz1[2], xyz1[4], xyz[0], xyz[2], xyz[4]) + dpInts[i - 1][0];
        }

        for (int j = 1; j <= T2.size(); ++j) {
            int[] xyz2 = decodeService.decodeZ3(Integer.parseInt(T2.get(j - 1).toString()), 0);
            dpInts[0][j] = getDistances(xyz2[0], xyz2[2], xyz2[4], xyz[0], xyz[2], xyz[4]) + dpInts[0][j - 1];
        }

        for (int i = 1; i <= T1.size(); ++i) {
            for (int j = 1; j <= T2.size(); ++j) {
                int[] xyz1 = decodeService.decodeZ3(Integer.parseInt(T1.get(i - 1).toString()), 0);
                int[] xyz2 = decodeService.decodeZ3(Integer.parseInt(T2.get(j - 1).toString()), 0);

                dpInts[i][j] = min(dpInts[i - 1][j - 1] + getDistances(xyz1[0], xyz1[2], xyz1[4], xyz2[0], xyz2[2], xyz2[4]),
                        dpInts[i - 1][j] + getDistances(xyz1[0], xyz1[2], xyz1[4], xyz[0], xyz[2], xyz[4]),
                        dpInts[i][j - 1] + getDistances(xyz2[0], xyz2[2], xyz2[4], xyz[0], xyz[2], xyz[4]));
            }
        }

        return dpInts[T1.size()][T2.size()] * 1.0 / Math.max(T1.size(), T2.size());
    }

    /**
     * distance between two cubes
     * @param x1
     * @param y1
     * @param z1
     * @param x2
     * @param y2
     * @param z2
     * @return
     */
    public double getDistances(int x1, int y1, int z1, int x2, int y2, int z2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) + Math.pow(z2 - z1, 2));
    }

    public int min(int a, int b, int c) {
        if (a > b) {
            a = b;
        }
        if (a > c) {
            a = c;
        }
        return a;
    }

    public double min(double a, double b, double c) {
        if (a > b) {
            a = b;
        }
        if (a > c) {
            a = c;
        }
        return a;
    }


}
