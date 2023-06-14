package whu.edu.cs.transitnet.service.index;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import whu.edu.cs.transitnet.dao.RealTimeDataDao;
import whu.edu.cs.transitnet.pojo.RealTimeDataEntity;
import whu.edu.cs.transitnet.service.EncodeService;

import javax.annotation.PostConstruct;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class HistoricalTripIndex {
    @Value("${transitnet.historicalindex.enable}")
    private boolean indexEnable;

    @Autowired
    RealTimeDataDao realTimeDataDao;

    @Autowired
    EncodeService encodeService;

    public HashMap<TripId, ArrayList<CubeId>> getTripCubeList() {
        return tripCubeList;
    }

    public HashMap<CubeId, ArrayList<TripId>> getCubeTripList() {
        return cubeTripList;
    }

    public HashMap<Integer, TripId> getHashcodeTripList() {
        return hashcodeTripList;
    }

    HashMap<TripId, ArrayList<CubeId>> tripCubeList = new HashMap<>();
    HashMap<CubeId, ArrayList<TripId>> cubeTripList = new HashMap<>();
    HashMap<Integer, TripId> hashcodeTripList = new HashMap<>();

    @PostConstruct
    public void init() throws ParseException {
//
//        if(!indexEnable) {
//            System.out.println("[HISTORICALTRIPINDEX] Index is not enabled, skipped.");
//            return;
//        }

        String startTime = "2023-05-20 10:00:00";
        String endTime = "2023-05-20 23:59:59";
        String date = getDateFromTime(startTime);

        tripCubeListSerializationAndDeserilization(startTime, endTime);
        cubeTripListSerializationAndDeserilization(date);
        hashcodeTripListSerializationAndDeserilization(date);
    }

    public void getTripsByDate(String startTime, String endTime) throws ParseException {
        // 1. 先根据 dateTime 筛选出所有 tripId
        List<String> tripIdsByDate = realTimeDataDao.findAllTripsOnlyByDate(startTime, endTime);

        // 2. 再根据 dateTime 和 tripId 筛选出每个 tripId 在 dateTime 当天的所有轨迹点 【按时间升序】
        System.out.println("=============================");
        System.out.println("[HISTORICALTRIPINDEX] number of tripIds: " + tripIdsByDate.size());
        for (int i = 0; i < tripIdsByDate.size(); i++) {
            System.out.println("[HISTORICALTRIPINDEX] number of scanned tripIds: " + (i + 1));

            String tripId = tripIdsByDate.get(i);
            ArrayList<CubeId> cubeIds = new ArrayList<>();

            List<RealTimeDataEntity> realTimeDataEntityList = realTimeDataDao.findAllPointsByTripIdByTimeSpan(tripId, startTime, endTime);

            System.out.println("[HISTORICALTRIPINDEX] number of points: " + realTimeDataEntityList.size());
            for (int j = 0; j < realTimeDataEntityList.size(); j++) {
//                System.out.println("[HISTORICALTRIPINDEX] number of scanned points: " + (j + 1));

                double lat = realTimeDataEntityList.get(j).getLat();
                double lon = realTimeDataEntityList.get(j).getLon();

                String recordedTime = realTimeDataEntityList.get(j).getRecordedTime();
                Date parse = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(recordedTime);
                Long time = parse.getTime();

                CubeId cubeId = encodeService.encodeCube(lat, lon, time);
                if(cubeIds.isEmpty() || cubeIds.lastIndexOf(cubeId) != (cubeIds.size() - 1)) cubeIds.add(cubeId);
            }

            tripCubeList.put(new TripId(tripId), cubeIds);
        }
    }


    public void tripCubeListSerializationAndDeserilization(String startTime, String endTime) throws ParseException {

        String date = getDateFromTime(startTime);

        File dateTripCubeFile = new File("./src/main/" + date + " TCList.txt");

        if(!dateTripCubeFile.exists()) {
            System.out.println("=============================");
            System.out.println("[HISTORICALTRIPINDEX] File Not Exists... Start serializing TCList...");

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
        System.out.println("[HISTORICALTRIPINDEX] FILE EXISTS...");
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

        //        Set set = tripCubeList.entrySet();
//        Iterator iterator = set.iterator();
//
//        while (iterator.hasNext()) {
//            Map.Entry entry = (Map.Entry)iterator.next();
//
//            System.out.print("key : " + entry.getKey()
//                    + " & Value : ");
//            System.out.println(entry.getValue());
        //        }


    }

    public void cubeTripListSerializationAndDeserilization(String date) throws ParseException {


        File dateCubeTripFile = new File("./src/main/" + date + " CTList.txt");



        if(!dateCubeTripFile.exists()) {
            System.out.println("=============================");
            System.out.println("[HISTORICALTRIPINDEX] File Not Exists... Start serializing CTList...");

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
        System.out.println("[HISTORICALTRIPINDEX] FILE EXISTS...");
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

//            System.out.print("key : " + entry.getKey()
//                    + " & Value : ");

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
        Date parse = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(startTime);
        Long time = parse.getTime();

        Date d = new Date();
        d.setTime(time);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        String date = sdf.format(d);
        return date;
    }
}
