package whu.edu.cs.transitnet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import whu.edu.cs.transitnet.dao.RealTimeDataDao;
import whu.edu.cs.transitnet.dao.TripsDao;
import whu.edu.cs.transitnet.pojo.RealTimePointEntity;
import whu.edu.cs.transitnet.pojo.TripsEntity;
import whu.edu.cs.transitnet.service.index.CubeId;
import whu.edu.cs.transitnet.service.index.HytraEngineManager;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toMap;

@Service
public class HistoricalMatchForDirectTrip {
    @Autowired
    HytraEngineManager hytraEngineManager;

    @Autowired
    DecodeService decodeService;

    @Autowired
    EncodeService encodeService;

    @Autowired
    TripsDao tripsDao;

    @Autowired
    RealTimeDataDao realTimeDataDao;

    List<TripsEntity> tripsEntityList = new ArrayList<>();
    Map<String, List<RealTimePointEntity>> slicedTripTrajectoryList = new ConcurrentHashMap<>();
    Map<String, List<CubeId>> traCubeList = new ConcurrentHashMap<>();

    public List<TripsEntity> getTripsEntityList() {
        return tripsEntityList;
    }

    public Map<String, List<RealTimePointEntity>> getSlicedTripTrajectoryList() {
        return slicedTripTrajectoryList;
    }

    public Map<String, List<CubeId>> getTraCubeList() {
        return traCubeList;
    }

    /**
     * Get all trip_ids on the specified bus route; specifically, by route_id and direction_id.
     * @param routeId
     * @param directionId
     * @return tripsEntityList
     */
    public List<TripsEntity> getAllTripsByRouteIdAndDirectionId(String routeId, String directionId) {
        return tripsDao.findAllByRouteIdAndDirectionId(routeId, directionId);
    }

    /**
     * Get all historical trajectories for the specified route.
     * First, get all trips.
     * Second, get all the historical points for each trip.
     * Third, slice the points for each trip by date, and thus get all the trajectories of the specified route.
     * @return slicedTripTrajectoryList: key -> tripId@date, value -> point list
     */
    public void getAllHistoricalTrajectoriesOnTheSpecifiedRoute(String routeId, String directionId) throws IOException, ClassNotFoundException {
        File file = new File("./src/main/" + "slicedTripTrajectoryList.txt");
        if (file.exists()) {
            // read the file
            System.out.println("======================");
            System.out.println("FILE EXISTS...");
            System.out.println("======================");
            System.out.println("Start Deserializing HashMap..");

            Long starttime = System.currentTimeMillis();

            FileInputStream fileInput1 = new FileInputStream(file);
            ObjectInputStream objectInput1 = new ObjectInputStream(fileInput1);
            slicedTripTrajectoryList = (ConcurrentHashMap)objectInput1.readObject();

            objectInput1.close();
            fileInput1.close();

            Long endtime = System.currentTimeMillis();

            System.out.println("======================");
            System.out.println("Deserializing HashMap DONE!");
            System.out.println("Deserializing time: " + (endtime - starttime) / 1000 + "s");
        } else {
            // construct the hashmaps by fetching data from the database
            System.out.println("=============================");
            System.out.println("File Not Exists... Start fetching data from database...");

            tripsEntityList = getAllTripsByRouteIdAndDirectionId(routeId, directionId);

            // scan all the trips and get historical points for each trip
            for (TripsEntity tripsEntity : tripsEntityList) {
                String tripId = tripsEntity.getTripId();
                List<RealTimePointEntity> realTimePointEntityList = realTimeDataDao.findAllSimplePointsByTripId(tripId);
                // slice the trajectories by date
                String loopDate = "";
                String key = "";
                List<RealTimePointEntity> realTimePointEntityList1 = new ArrayList<>();
                for (RealTimePointEntity realTimePointEntity : realTimePointEntityList) {
                    String recorded_time = realTimePointEntity.getRecordedTime();
                    String date = recorded_time.substring(0, 10);
                    String newKey = tripId + "@" + date;
                    if (!date.equals(loopDate)) {
                        loopDate = date;
                        key = newKey;
                        realTimePointEntityList1 = new ArrayList<>();
                        realTimePointEntityList1.add(realTimePointEntity);
                        slicedTripTrajectoryList.put(key, realTimePointEntityList1);
                        continue;
                    }
                    realTimePointEntityList1.add(realTimePointEntity);
                    slicedTripTrajectoryList.put(key, realTimePointEntityList1);
                }
            }

            // filter erroneous trajectories, i.e., too few trajectory points
            for (String key : slicedTripTrajectoryList.keySet()) {
                List<RealTimePointEntity> realTimePointEntityList = slicedTripTrajectoryList.get(key);
                if (realTimePointEntityList.size() <= 15) {
                    slicedTripTrajectoryList.remove(key);
                }
            }

            // then serialize the hashmap
            FileOutputStream myFileOutStream1 = new FileOutputStream(file);
            ObjectOutputStream myObjectOutStream1 = new ObjectOutputStream(myFileOutStream1);
            myObjectOutStream1.writeObject(slicedTripTrajectoryList);

            // close FileOutputStream and ObjectOutputStream
            myObjectOutStream1.close();
            myFileOutStream1.close();
        }


        File file1 = new File("./src/main/" + " routingPointSize.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(file1));

        // print the map sorted by key
        // get the key set
        Set keySet = slicedTripTrajectoryList.keySet();
        // turn the key set into an array
        Object[] arr= keySet.toArray();
        // sort the array using Arrays.sort()
        Arrays.sort(arr);
        int number = 0;
        int size = 0;
        for (Object key : arr){
            number++;
            int newSize = slicedTripTrajectoryList.get(key).size();
            size += newSize;
            writer.write(String.valueOf(newSize));
            writer.newLine();
        }
        writer.close();
        // print the size of the historical trajectories
        System.out.println("Size of trajectories: " + slicedTripTrajectoryList.size());
        // print the average size of trajectory points
        System.out.println("Average size of trajectory points: " + size/number);
    }

    /**
     * Turn each trajectory into a cube list
     * return traCubeList
     */
    public void createTraCubeList() throws IOException, ClassNotFoundException {
        File file = new File("./src/main/" + "traCubeList.txt");

        if (file.exists()) {
            // read the file
            System.out.println("======================");
            System.out.println("FILE EXISTS...");
            System.out.println("======================");
            System.out.println("Start Deserializing HashMap..");

            Long starttime = System.currentTimeMillis();

            FileInputStream fileInput1 = new FileInputStream(file);
            ObjectInputStream objectInput1 = new ObjectInputStream(fileInput1);
            traCubeList = (ConcurrentHashMap)objectInput1.readObject();

            objectInput1.close();
            fileInput1.close();

            Long endtime = System.currentTimeMillis();

            System.out.println("======================");
            System.out.println("Deserializing HashMap DONE!");
            System.out.println("Deserializing time: " + (endtime - starttime) / 1000 + "s");
        } else {
            System.out.println("=============================");
            System.out.println("File Not Exists...");
            // turn each trajectory into a cube list
            for (String key : slicedTripTrajectoryList.keySet()) {
                List<RealTimePointEntity> realTimePointEntityList = slicedTripTrajectoryList.get(key);
                List<CubeId> cubeIdList = new ArrayList<>();
                for (RealTimePointEntity realTimePointEntity : realTimePointEntityList) {
                    Double lat = realTimePointEntity.getLat();
                    Double lon = realTimePointEntity.getLon();
                    String time = realTimePointEntity.getRecordedTime();
                    CubeId cubeId = encodeService.encodeCube(lat, lon, time);
                    if (!cubeIdList.contains(cubeId)) {
                        cubeIdList.add(cubeId);
                    }
                }
                traCubeList.put(key, cubeIdList);
            }
            // then serialize the hashmap
            FileOutputStream myFileOutStream1 = new FileOutputStream(file);
            ObjectOutputStream myObjectOutStream1 = new ObjectOutputStream(myFileOutStream1);
            myObjectOutStream1.writeObject(traCubeList);

            // close FileOutputStream and ObjectOutputStream
            myObjectOutStream1.close();
            myFileOutStream1.close();
        }

        // print the average size of trajectory cubes
        int number = 0;
        int size = 0;
        for (String key : traCubeList.keySet()) {
            number ++;
            size += traCubeList.get(key).size();
        }
        // print the size of the historical trajectories
        System.out.println("Size of trajectories: " + slicedTripTrajectoryList.size());
        // print the average size of trajectory points
        System.out.println("Average size of trajectory cubes: " + size/number);
    }

    /**
     * Get the candidate trajectories by the size of the overlapped cubes
     * @param traId
     */
    public Map<String, Integer> filterByCube(String traId) {
        Map<String, Integer> traSimListLOC = new ConcurrentHashMap<>();
        List<CubeId> cubeIdList0 = traCubeList.get(traId);
        int length0 = cubeIdList0.size();
        System.out.println("Length of the input trajectory's cube list: " + length0);

        // calculate the size of the overlapped cubes
        for (String key : traCubeList.keySet()) {
            List<CubeId> intersection0 = new ArrayList<>(cubeIdList0);
            List<CubeId> cubeIdList = traCubeList.get(key);
            intersection0.retainAll(cubeIdList);
            List<CubeId> intersection1 = intersection0.stream().distinct().collect(Collectors.toList());
            traSimListLOC.put(key, intersection1.size());
        }

        // give a threshold, e.g., k, to filter the trajectories with a very low similarity
        double ratio = 0.3;
        int k = (int) (length0 * ratio);
        for (String key : traSimListLOC.keySet()) {
            int similarity = traSimListLOC.get(key);
            if (similarity <= k) {
                traSimListLOC.remove(key);
            }
        }

        System.out.println("Size of the candidate trajectories after filtering: " + traSimListLOC.size());

        // sort the trajectories by similarity in descending order
        Map<String, Integer> sorted = traSimListLOC
                .entrySet()
                .stream()
                .sorted(Collections.reverseOrder(comparingByValue()))
                .collect(
                        toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                LinkedHashMap::new));

        // print the number of overlapped cubes for each candidate trajectory
//        for (String key : sorted.keySet()) {
//            if (sorted.get(key) >= 11) {
//                System.out.println(key + ": " + sorted.get(key));
//            }
//        }

        // remove the traId itself
        sorted.remove(traId);

        return sorted;
    }

    /**
     * Get top-k trajectories among candidates by DTW/EDR/ERP
     * @param traId
     */
    public List<String> getTopK(String traId, int k) throws IOException, ClassNotFoundException {
        String routeId = "Q110";
        String directionId = "0";
        getAllHistoricalTrajectoriesOnTheSpecifiedRoute(routeId, directionId);
        createTraCubeList();

        // trajectories and their similarity after filtering
        Map<String, Integer> traSimListLOC = filterByCube(traId);
        List<CubeId> cubeIdList0 = traCubeList.get(traId);

        Map<String, Double> traSimListDTW = new HashMap<>();
        Map<String, Double> traSimListEDR = new HashMap<>();
        Map<String, Double> traSimListERP = new HashMap<>();
        for (String key : traSimListLOC.keySet()) {
            double sim1 = DynamicTimeWarping(cubeIdList0, traCubeList.get(key));
            traSimListDTW.put(key, sim1);

            double sim2 = EditDistanceonRealSequence(cubeIdList0, traCubeList.get(key));
            traSimListEDR.put(key, sim2);

            CubeId paramCubeId = traCubeList.get(traId).get(0);
            double sim3 = EditDistanceWithRealPenalty(cubeIdList0, traCubeList.get(key), paramCubeId);
            traSimListERP.put(key, sim3);
        }

//        System.out.println("DTW: " + traSimListDTW.get("35671762-BPPB3-BP_B3-Weekday-02-SDon@2023-05-09"));
//        System.out.println("ERP: " + traSimListERP.get("35671762-BPPB3-BP_B3-Weekday-02-SDon@2023-05-09"));
//        System.out.println("DTW: " + traSimListDTW.get("37566369-BPPD3-BP_D3-Weekday-04-SDon@2023-12-11"));
//        System.out.println("ERP: " + traSimListEDR.get("37566369-BPPD3-BP_D3-Weekday-04-SDon@2023-12-11"));

        // // sort the trajectories by similarity in ascending order
        Map<String, Double> sortedDTW = traSimListDTW
                .entrySet()
                .stream()
                .sorted(comparingByValue())
                .limit(k)
                .collect(
                        toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                LinkedHashMap::new));

        Map<String, Double> sortedEDR = traSimListEDR
                .entrySet()
                .stream()
                .sorted(comparingByValue())
                .limit(k)
                .collect(
                        toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                LinkedHashMap::new));

        Map<String, Double> sortedERP = traSimListERP
                .entrySet()
                .stream()
                .sorted(comparingByValue())
                .limit(k)
                .collect(
                        toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                LinkedHashMap::new));

        Set<String> keySet = sortedDTW.keySet();
        List<String> keyList = new ArrayList<>();
        keyList.addAll(keySet);

        return keyList;
//        for (String key : sortedDTW.keySet()) {
//            System.out.println("DTW - " + key + " - " + sortedDTW.get(key));
//        }
//
//        for (String key : sortedEDR.keySet()) {
//            System.out.println("EDR - " + key + " - " + sortedEDR.get(key));
//        }
//
//        for (String key : sortedERP.keySet()) {
//            System.out.println("ERP - " + key + " - " + sortedERP.get(key));
//        }
    }

    /**
     * DTW
     * @param T1
     * @param T2
     * @return
     */
    public double DynamicTimeWarping(List<CubeId> T1, List<CubeId> T2) {
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
    public double EditDistanceonRealSequence(List<CubeId> T1, List<CubeId> T2) {
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
