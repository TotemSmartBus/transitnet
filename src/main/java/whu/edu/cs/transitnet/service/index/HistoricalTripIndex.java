package whu.edu.cs.transitnet.service.index;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import whu.edu.cs.transitnet.dao.RealTimeDataDao;
import whu.edu.cs.transitnet.pojo.RealTimePointEntity;
import whu.edu.cs.transitnet.service.EncodeService;

import javax.annotation.PostConstruct;
import java.io.*;
import java.text.ParseException;
import java.util.*;

@Component
public class HistoricalTripIndex {
    @Value("${transitnet.historicalindex.enable}")
    private boolean indexEnable;

    @Autowired
    RealTimeDataDao realTimeDataDao;

    @Autowired
    EncodeService encodeService;

    @Autowired
    HytraEngineManager hytraEngineManager;

    public HashMap<TripId, ArrayList<CubeId>> getTripCubeList() {
        return tripCubeList;
    }

    public HashMap<CubeId, ArrayList<TripId>> getCubeTripList() {
        return cubeTripList;
    }

    public HashMap<Integer, TripId> getHashcodeTripList() {
        return hashcodeTripList;
    }

    public HashMap<TripId, ArrayList<RealTimePointEntity>> getTripPointList() {
        return tripPointList;
    }

    HashMap<TripId, ArrayList<CubeId>> tripCubeList = new HashMap<>();
    HashMap<CubeId, ArrayList<TripId>> cubeTripList = new HashMap<>();
    HashMap<Integer, TripId> hashcodeTripList = new HashMap<>();
    // 新增 TPList 用于实验
    HashMap<TripId, ArrayList<RealTimePointEntity>> tripPointList = new HashMap<>();

    @PostConstruct
    public void init() throws ParseException {

        // 不要删除这段代码
        // 是否注释掉等同于是否进行索引构建
//        if(!indexEnable) {
//            System.out.println("[HISTORICALTRIPINDEX] Index is not enabled, skipped.");
//            return;
//        }

        String startTime = "2023-05-20 00:00:00";
        String endTime = "2023-05-20 23:59:59";
        String date = getDateFromTime(startTime);

        tripCubeListSerializationAndDeserilization(startTime, endTime);
        cubeTripListSerializationAndDeserilization(date);
        hashcodeTripListSerializationAndDeserilization(date);

        tripPointListSerializationAndDeserialization(startTime, endTime);
    }

    public void getTripsByDate(String startTime, String endTime) throws ParseException {
        // 1. 先根据 dateTime 筛选出所有 tripId
        List<String> tripIdsByDate = realTimeDataDao.findAllTripsOnlyByDate(startTime, endTime);


        // 2. 再根据 dateTime 和 tripId 筛选出每个 tripId 在 dateTime 当天的所有轨迹点 【按时间升序】
        System.out.println("=============================");
        System.out.println("[HISTORICALTRIPINDEX] number of tripIds: " + tripIdsByDate.size());
        int num = 11;
        for (int i = 0; i < num; i++) {
            System.out.println("[HISTORICALTRIPINDEX] number of scanned tripIds: " + (i + 1));

            String tripId = tripIdsByDate.get(i);
            ArrayList<CubeId> cubeIds = new ArrayList<>();

            List<RealTimePointEntity> realTimePointEntityList = realTimeDataDao.findAllSimplePointsByTripIdByTimeSpan(tripId, startTime, endTime);

            // List 转 ArrayList，构造 tripPointList
            ArrayList<RealTimePointEntity> realTimePointEntityArrayList = new ArrayList<>(realTimePointEntityList);
            tripPointList.put(new TripId(tripId), realTimePointEntityArrayList);
            System.out.println("[HISTORICALTRIPINDEX] number of points: " + realTimePointEntityList.size());

            for (int j = 0; j < realTimePointEntityList.size(); j++) {

                double lat = realTimePointEntityList.get(j).getLat();
                double lon = realTimePointEntityList.get(j).getLon();

                // 数据库里存的轨迹点的时间为纽约时间
                String recordedTime = realTimePointEntityList.get(j).getRecordedTime();

                // 注意 encodecube 里面传的是年月日时分秒
                CubeId cubeId = encodeService.encodeCube(lat, lon, recordedTime);
                if(cubeIds.isEmpty() || cubeIds.lastIndexOf(cubeId) != (cubeIds.size() - 1)) {
                    cubeIds.add(cubeId);
                }
            }

            // 构造 tripCubeList
            tripCubeList.put(new TripId(tripId), cubeIds);
        }
    }

    // 新增 TPList 的序列化和反序列化
    public void tripPointListSerializationAndDeserialization(String startTime, String endTime) throws ParseException {
        String date = getDateFromTime(startTime);

        File dateTripPointFile = new File("./src/main/" + date + " TPList.txt");

        if(!dateTripPointFile.exists()) {
            System.out.println("=============================");
            System.out.println("[HISTORICALTRIPINDEX] dateTripPointFile Not Exists... Start serializing TCList...");

            Long startTime1 = System.currentTimeMillis();
            // 构建 TPList
            getTripsByDate(startTime, endTime);
            Long endTime1 = System.currentTimeMillis();
            System.out.println("[HISTORICALTRIPINDEX] index construction time: " + (endTime1 - startTime1) / 1000 + "s");


            Long startTime2 = System.currentTimeMillis();
            // try catch block
            try {
                FileOutputStream myFileOutStream
                        = new FileOutputStream(dateTripPointFile);

                ObjectOutputStream myObjectOutStream
                        = new ObjectOutputStream(myFileOutStream);

                myObjectOutStream.writeObject(tripPointList);

                // closing FileOutputStream and
                // ObjectOutputStream
                myObjectOutStream.close();
                myFileOutStream.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            Long endTime2 = System.currentTimeMillis();
            System.out.println("[HISTORICALTRIPINDEX] serialization time: " + (endTime2 - startTime2) / 1000 + "s");

        }

        // 如果文件存在
        // 读取文件
        System.out.println("======================");
        System.out.println("[HISTORICALTRIPINDEX] dateTripPointFile EXISTS...");
        System.out.println("[HISTORICALTRIPINDEX] Start Deserializing HashMap..");

        Long starttime = System.currentTimeMillis();
        try {
            FileInputStream fileInput = new FileInputStream(
                    dateTripPointFile);


            ObjectInputStream objectInput
                    = new ObjectInputStream(fileInput);

            tripPointList = (HashMap)objectInput.readObject();

            objectInput.close();
            fileInput.close();
        }
        catch (IOException obj1) {
            obj1.printStackTrace();
            return;
        }
        catch (ClassNotFoundException obj2) {
            System.out.println("[HISTORICALTRIPINDEX] Class not found");
            obj2.printStackTrace();
            return;
        }

        Long endtime = System.currentTimeMillis();

        System.out.println("======================");
        System.out.println("[HISTORICALTRIPINDEX] Deserializing HashMap DONE!");
        System.out.println("[HISTORICALTRIPINDEX] Deserializing time: " + (endtime - starttime) / 1000 + "s");

    }

    public void tripCubeListSerializationAndDeserilization(String startTime, String endTime) throws ParseException {

        String date = getDateFromTime(startTime);

        int resolution = hytraEngineManager.getParams().getResolution();
        File dateTripCubeFile = new File("./src/main/" + date + " TCList_" + resolution + ".txt");

        if(!dateTripCubeFile.exists()) {
            System.out.println("=============================");
            System.out.println("[HISTORICALTRIPINDEX] dateTripCubeFile Not Exists... Start serializing TCList...");

            Long startTime1 = System.currentTimeMillis();
            // 构建 CTList
            getTripsByDate(startTime, endTime);
            Long endTime1 = System.currentTimeMillis();
            System.out.println("[HISTORICALTRIPINDEX] index construction time: " + (endTime1 - startTime1) / 1000 + "s");


            Long startTime2 = System.currentTimeMillis();
            // try catch block
            try {
                FileOutputStream myFileOutStream
                        = new FileOutputStream(dateTripCubeFile);

                ObjectOutputStream myObjectOutStream
                        = new ObjectOutputStream(myFileOutStream);

                myObjectOutStream.writeObject(tripCubeList);

                // closing FileOutputStream and
                // ObjectOutputStream
                myObjectOutStream.close();
                myFileOutStream.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            Long endTime2 = System.currentTimeMillis();
            System.out.println("[HISTORICALTRIPINDEX] serialization time: " + (endTime2 - startTime2) / 1000 + "s");

        }

        // 如果文件存在
        // 读取文件
        System.out.println("======================");
        System.out.println("[HISTORICALTRIPINDEX] dateTripCubeFile EXISTS...");
        System.out.println("[HISTORICALTRIPINDEX] Start Deserializing HashMap..");

        Long starttime = System.currentTimeMillis();
        try {
            FileInputStream fileInput = new FileInputStream(
                    dateTripCubeFile);


            ObjectInputStream objectInput
                    = new ObjectInputStream(fileInput);

            tripCubeList = (HashMap)objectInput.readObject();

            objectInput.close();
            fileInput.close();
        }
        catch (IOException obj1) {
            obj1.printStackTrace();
            return;
        }
        catch (ClassNotFoundException obj2) {
            System.out.println("[HISTORICALTRIPINDEX] Class not found");
            obj2.printStackTrace();
            return;
        }

        Long endtime = System.currentTimeMillis();

        System.out.println("======================");
        System.out.println("[HISTORICALTRIPINDEX] Deserializing HashMap DONE!");
        System.out.println("[HISTORICALTRIPINDEX] Deserializing time: " + (endtime - starttime) / 1000 + "s");

    }

    // 将 TCList 转为 CTList，因此传参只需要 date，不需要 startTime 和 endTime
    public void cubeTripListSerializationAndDeserilization(String date) throws ParseException {


        int resolution = hytraEngineManager.getParams().getResolution();
        File dateCubeTripFile = new File("./src/main/" + date + " CTList_" + resolution + ".txt");



        if(!dateCubeTripFile.exists()) {
            System.out.println("=============================");
            System.out.println("[HISTORICALTRIPINDEX] dateCubeTripFile Not Exists... Start serializing CTList...");

            // 将 TCList 转为 CTList
            for (TripId tripId : tripCubeList.keySet()) {
                ArrayList<CubeId> cubeIdArrayList = tripCubeList.get(tripId);
                for(int i = 0; i < cubeIdArrayList.size(); i++) {
                    CubeId cubeId = cubeIdArrayList.get(i);

                    ArrayList<TripId> tripIdArrayList = new ArrayList<>();
                    if(!cubeTripList.containsKey(cubeId)) {
                        tripIdArrayList.add(tripId);
                        cubeTripList.put(cubeId, tripIdArrayList);
                    } else if(!cubeTripList.get(cubeId).contains(tripId)) {
                        tripIdArrayList = cubeTripList.get(cubeId);
                        tripIdArrayList.add(tripId);
                        cubeTripList.put(cubeId, tripIdArrayList);
                    }

                }
            }


            Long startTime2 = System.currentTimeMillis();
            // try catch block
            try {
                FileOutputStream myFileOutStream
                        = new FileOutputStream(dateCubeTripFile);

                ObjectOutputStream myObjectOutStream
                        = new ObjectOutputStream(myFileOutStream);

                myObjectOutStream.writeObject(cubeTripList);

                // closing FileOutputStream and
                // ObjectOutputStream
                myObjectOutStream.close();
                myFileOutStream.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            Long endTime2 = System.currentTimeMillis();
            System.out.println("[HISTORICALTRIPINDEX] serialization time: " + (endTime2 - startTime2) / 1000 + "s");

        }

        // 如果文件存在
        // 读取文件
        System.out.println("======================");
        System.out.println("[HISTORICALTRIPINDEX] dateCubeTripFile EXISTS...");
        System.out.println("[HISTORICALTRIPINDEX] Start Deserializing HashMap..");

        Long starttime = System.currentTimeMillis();
        try {
            FileInputStream fileInput = new FileInputStream(
                    dateCubeTripFile);


            ObjectInputStream objectInput
                    = new ObjectInputStream(fileInput);

            cubeTripList = (HashMap)objectInput.readObject();

            objectInput.close();
            fileInput.close();
        }
        catch (IOException obj1) {
            obj1.printStackTrace();
            return;
        }
        catch (ClassNotFoundException obj2) {
            System.out.println("[HISTORICALTRIPINDEX] Class not found");
            obj2.printStackTrace();
            return;
        }

        Long endtime = System.currentTimeMillis();

        System.out.println("======================");
        System.out.println("[HISTORICALTRIPINDEX] Deserializing HashMap DONE!");
        System.out.println("[HISTORICALTRIPINDEX] Deserializing time: " + (endtime - starttime) / 1000 + "s");

        Set set = cubeTripList.entrySet();
        Iterator iterator = set.iterator();

        int n5 = 0, n10 = 0, n20 = 0, n30 = 0, n40 = 0, n = 0;
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry)iterator.next();

            int size = cubeTripList.get(entry.getKey()).size();
            if(size <= 5) {
                n5++;
            } else if(size <= 10) {
                n10++;
            } else if(size <= 20) {
                n20++;
            } else if(size <= 30) {
                n30++;
            } else {
                n40++;
            }

            n++;

        }

        System.out.println("0 <= size <= 5: " + n5 + "; ratio: " + (double)n5/n);
        System.out.println("5 <= size <= 10: " + n10 + "; ratio: " + (double)n10/n);
        System.out.println("10 <= size <= 20: " + n20 + "; ratio: " + (double)n20/n);
        System.out.println("20 <= size <= 30: " + n30 + "; ratio: " + (double)n30/n);
        System.out.println("30 <= size: " + n40 + "; ratio: " + (double)n40/n);
        System.out.println("total number: " + n + "; ratio: " + n/n);
    }

    public void hashcodeTripListSerializationAndDeserilization(String date) throws ParseException {

        File hashcodeTripFile = new File("./src/main/" + date + " hashcodeTripList.txt");

        if(!hashcodeTripFile.exists()) {
            System.out.println("=============================");
            System.out.println("[HISTORICALTRIPINDEX] File Not Exists... Start serializing CTList...");

            for (TripId tripId : tripCubeList.keySet()) {
                hashcodeTripList.put(tripId.hashCode(), tripId);
            }

            Long startTime2 = System.currentTimeMillis();
            // try catch block
            try {
                FileOutputStream myFileOutStream
                        = new FileOutputStream(hashcodeTripFile);

                ObjectOutputStream myObjectOutStream
                        = new ObjectOutputStream(myFileOutStream);

                myObjectOutStream.writeObject(hashcodeTripList);

                // closing FileOutputStream and
                // ObjectOutputStream
                myObjectOutStream.close();
                myFileOutStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Long endTime2 = System.currentTimeMillis();
            System.out.println("[HISTORICALTRIPINDEX] serialization time: " + (endTime2 - startTime2) / 1000 + "s");
        }

        Long starttime = System.currentTimeMillis();
        try {
            FileInputStream fileInput = new FileInputStream(hashcodeTripFile);


            ObjectInputStream objectInput
                    = new ObjectInputStream(fileInput);

            hashcodeTripList = (HashMap)objectInput.readObject();

            objectInput.close();
            fileInput.close();
        }
        catch (IOException obj1) {
            obj1.printStackTrace();
            return;
        }
        catch (ClassNotFoundException obj2) {
            System.out.println("[HISTORICALTRIPINDEX] Class not found");
            obj2.printStackTrace();
            return;
        }

        Long endtime = System.currentTimeMillis();

        System.out.println("======================");
        System.out.println("[HISTORICALTRIPINDEX] Deserializing HashMap DONE!");
        System.out.println("[HISTORICALTRIPINDEX] Deserializing time: " + (endtime - starttime) / 1000 + "s");
    }

    public String getDateFromTime(String startTime) throws ParseException {
        String[] date_time = startTime.split(" ");
        String date = date_time[0];
        return date;
    }
}
