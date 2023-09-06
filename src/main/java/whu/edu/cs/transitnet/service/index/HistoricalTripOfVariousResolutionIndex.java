package whu.edu.cs.transitnet.service.index;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import whu.edu.cs.transitnet.dao.RealTimeDataDao;
import whu.edu.cs.transitnet.pojo.RealTimePointEntity;
import whu.edu.cs.transitnet.service.EncodeService;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Component
public class HistoricalTripOfVariousResolutionIndex {

    @Autowired
    RealTimeDataDao realTimeDataDao;

    @Autowired
    EncodeService encodeService;

    @Autowired
    HytraEngineManager hytraEngineManager;

    HashMap<TripId, ArrayList<CubeId>> tripCubeList4 = new HashMap<>();
    HashMap<TripId, ArrayList<CubeId>> tripCubeList5 = new HashMap<>();
    HashMap<TripId, ArrayList<CubeId>> tripCubeList7 = new HashMap<>();
    HashMap<TripId, ArrayList<CubeId>> tripCubeList8 = new HashMap<>();

    HashMap<TripId, ArrayList<CubeId>> tripCubeList6 =  new HashMap<>();

    HashMap<CubeId, ArrayList<TripId>> cubeTripList4 = new HashMap<>();
    HashMap<CubeId, ArrayList<TripId>> cubeTripList5 = new HashMap<>();
    HashMap<CubeId, ArrayList<TripId>> cubeTripList7 = new HashMap<>();
    HashMap<CubeId, ArrayList<TripId>> cubeTripList8 = new HashMap<>();
    HashMap<CubeId, ArrayList<TripId>> cubeTripList6 =  new HashMap<>();


//    @PostConstruct
    public void init() throws ParseException {

        // 不要删除这段代码
        // 是否注释掉等同于是否进行索引构建
//        if(!indexEnable) {
//            System.out.println("[HISTORICALTRIPOFVARIOUSRESOLUTIONINDEX] Index is not enabled, skipped.");
//            return;
//        }

        String startTime = "2023-05-20 00:00:00";
        String endTime = "2023-05-20 23:59:59";
        String date = getDateFromTime(startTime);

        tripCubeListSerializationAndDeserilization(startTime, endTime);
        cubeTripListSerializationAndDeserilization(date);
    }

    public void getTripsByDate(String startTime, String endTime) throws ParseException {
        // 1. 先根据 dateTime 筛选出所有 tripId
        List<String> tripIdsByDate = realTimeDataDao.findAllTripsOnlyByDate(startTime, endTime);

        // 2. 再根据 dateTime 和 tripId 筛选出每个 tripId 在 dateTime 当天的所有轨迹点 【按时间升序】
        System.out.println("=============================");
        System.out.println("[HISTORICALTRIPOFVARIOUSRESOLUTIONINDEX] number of tripIds: " + tripIdsByDate.size());
        for (int i = 0; i < tripIdsByDate.size(); i++) {
            System.out.println("[HISTORICALTRIPOFVARIOUSRESOLUTIONINDEX] number of scanned tripIds: " + (i + 1));

            String tripId = tripIdsByDate.get(i);
            ArrayList<CubeId> cubeIds4 = new ArrayList<>();
            ArrayList<CubeId> cubeIds5 = new ArrayList<>();
            ArrayList<CubeId> cubeIds7 = new ArrayList<>();
            ArrayList<CubeId> cubeIds8 = new ArrayList<>();
            ArrayList<CubeId> cubeIds6 = new ArrayList<>();


            List<RealTimePointEntity> realTimePointEntityList = realTimeDataDao.findAllSimplePointsByTripIdByTimeSpan(tripId, startTime, endTime);

            for (int j = 0; j < realTimePointEntityList.size(); j++) {

                double lat = realTimePointEntityList.get(j).getLat();
                double lon = realTimePointEntityList.get(j).getLon();

                String recordedTime = realTimePointEntityList.get(j).getRecordedTime();
                Date parse = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(recordedTime);
                Long time = parse.getTime();

                // 注意 encodecube 里面传的是年月日时分秒
                CubeId cubeId4 = encodeService.encodeCube(lat, lon, time, 4);
                CubeId cubeId5 = encodeService.encodeCube(lat, lon, time, 5);
                CubeId cubeId7 = encodeService.encodeCube(lat, lon, time, 7);
                CubeId cubeId8 = encodeService.encodeCube(lat, lon, time, 8);
                CubeId cubeId6 = encodeService.encodeCube(lat, lon, time, 6);

                if(cubeIds4.isEmpty() || cubeIds4.lastIndexOf(cubeId4) != (cubeIds4.size() - 1)) {
                    cubeIds4.add(cubeId4);
                }
                if(cubeIds5.isEmpty() || cubeIds5.lastIndexOf(cubeId5) != (cubeIds5.size() - 1)) {
                    cubeIds5.add(cubeId5);
                }
                if(cubeIds7.isEmpty() || cubeIds7.lastIndexOf(cubeId7) != (cubeIds7.size() - 1)) {
                    cubeIds7.add(cubeId7);
                }
                if(cubeIds8.isEmpty() || cubeIds8.lastIndexOf(cubeId8) != (cubeIds8.size() - 1)) {
                    cubeIds8.add(cubeId8);
                }
                if(cubeIds6.isEmpty() || cubeIds6.lastIndexOf(cubeId6) != (cubeIds6.size() - 1)) {
                    cubeIds6.add(cubeId6);
                }
            }

            // 构造 tripCubeList
            tripCubeList4.put(new TripId(tripId), cubeIds4);
            tripCubeList5.put(new TripId(tripId), cubeIds5);
            tripCubeList7.put(new TripId(tripId), cubeIds7);
            tripCubeList8.put(new TripId(tripId), cubeIds8);
            tripCubeList6.put(new TripId(tripId), cubeIds6);
        }
    }


    // resolution = 4/5/7/8 时创建 txt 文件的操作
//    @PostConstruct
    public void tripCubeListSerializationAndDeserilization(String startTime, String endTime) throws ParseException {

        String date = getDateFromTime(startTime);


        File dateTripCubeFile4 = new File("./src/main/" + date + " TCList_4.txt");
        File dateTripCubeFile5 = new File("./src/main/" + date + " TCList_5.txt");
        File dateTripCubeFile7 = new File("./src/main/" + date + " TCList_7.txt");
        File dateTripCubeFile8 = new File("./src/main/" + date + " TCList_8.txt");
        File dateTripCubeFile6 = new File("./src/main/" + date + " TCList_6.txt");

        System.out.println("=============================");
        System.out.println("[HISTORICALTRIPOFVARIOUSRESOLUYIONINDEX] dateTripCubeFile Not Exists... Start serializing TCList...");

        Long startTime1 = System.currentTimeMillis();
        // 构建 CTList
        getTripsByDate(startTime, endTime);
        Long endTime1 = System.currentTimeMillis();
        System.out.println("[HISTORICALTRIPOFVARIOUSRESOLUYIONINDEX] index construction time: " + (endTime1 - startTime1) / 1000 + "s");


        Long startTime2 = System.currentTimeMillis();
        // try catch block
        try {
            FileOutputStream myFileOutStream4
                    = new FileOutputStream(dateTripCubeFile4);
            FileOutputStream myFileOutStream5
                    = new FileOutputStream(dateTripCubeFile5);
            FileOutputStream myFileOutStream7
                    = new FileOutputStream(dateTripCubeFile7);
            FileOutputStream myFileOutStream8
                    = new FileOutputStream(dateTripCubeFile8);
            FileOutputStream myFileOutStream6
                    = new FileOutputStream(dateTripCubeFile6);


            ObjectOutputStream myObjectOutStream4
                    = new ObjectOutputStream(myFileOutStream4);
            ObjectOutputStream myObjectOutStream5
                    = new ObjectOutputStream(myFileOutStream5);
            ObjectOutputStream myObjectOutStream7
                    = new ObjectOutputStream(myFileOutStream7);
            ObjectOutputStream myObjectOutStream8
                    = new ObjectOutputStream(myFileOutStream8);
            ObjectOutputStream myObjectOutStream6
                    = new ObjectOutputStream(myFileOutStream6);

            myObjectOutStream4.writeObject(tripCubeList4);
            myObjectOutStream5.writeObject(tripCubeList5);
            myObjectOutStream7.writeObject(tripCubeList7);
            myObjectOutStream8.writeObject(tripCubeList8);
            myObjectOutStream6.writeObject(tripCubeList6);


            // closing FileOutputStream and
            // ObjectOutputStream
            myFileOutStream4.close();
            myObjectOutStream4.close();
            myFileOutStream5.close();
            myObjectOutStream5.close();
            myFileOutStream7.close();
            myObjectOutStream7.close();
            myFileOutStream8.close();
            myObjectOutStream8.close();
            myFileOutStream6.close();
            myObjectOutStream6.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Long endTime2 = System.currentTimeMillis();
        System.out.println("[HISTORICALTRIPOFVARIOUSRESOLUYIONINDEX] serialization time: " + (endTime2 - startTime2) / 1000 + "s");
    }

    public void cubeTripListSerializationAndDeserilization(String date) throws ParseException {


        File dateCubeTripFile4 = new File("./src/main/" + date + " CTList_4.txt");
        File dateCubeTripFile5 = new File("./src/main/" + date + " CTList_5.txt");
        File dateCubeTripFile7 = new File("./src/main/" + date + " CTList_7.txt");
        File dateCubeTripFile8 = new File("./src/main/" + date + " CTList_8.txt");
        File dateCubeTripFile6 = new File("./src/main/" + date + " CTList_6.txt");

        System.out.println("=============================");
        System.out.println("[HISTORICALTRIPOFVARIOUSRESOLUYIONINDEX] dateCubeTripFile Not Exists... Start serializing CTList...");

            // 将 TCList 转为 CTList
        for (TripId tripId : tripCubeList6.keySet()) {
            ArrayList<CubeId> cubeIdArrayList = tripCubeList6.get(tripId);
            for(int i = 0; i < cubeIdArrayList.size(); i++) {
                CubeId cubeId = cubeIdArrayList.get(i);

                ArrayList<TripId> tripIdArrayList = new ArrayList<>();
                if(!cubeTripList6.containsKey(cubeId)) {
                    tripIdArrayList.add(tripId);
                    cubeTripList6.put(cubeId, tripIdArrayList);
                } else if(!cubeTripList6.get(cubeId).contains(tripId)) {
                    tripIdArrayList = cubeTripList6.get(cubeId);
                    tripIdArrayList.add(tripId);
                    cubeTripList6.put(cubeId, tripIdArrayList);
                }

            }
        }

            for (TripId tripId : tripCubeList4.keySet()) {
                ArrayList<CubeId> cubeIdArrayList = tripCubeList4.get(tripId);
                for(int i = 0; i < cubeIdArrayList.size(); i++) {
                    CubeId cubeId = cubeIdArrayList.get(i);

                    ArrayList<TripId> tripIdArrayList = new ArrayList<>();
                    if(!cubeTripList4.containsKey(cubeId)) {
                        tripIdArrayList.add(tripId);
                        cubeTripList4.put(cubeId, tripIdArrayList);
                    } else if(!cubeTripList4.get(cubeId).contains(tripId)) {
                        tripIdArrayList = cubeTripList4.get(cubeId);
                        tripIdArrayList.add(tripId);
                        cubeTripList4.put(cubeId, tripIdArrayList);
                    }

                }
            }

        for (TripId tripId : tripCubeList5.keySet()) {
            ArrayList<CubeId> cubeIdArrayList = tripCubeList5.get(tripId);
            for(int i = 0; i < cubeIdArrayList.size(); i++) {
                CubeId cubeId = cubeIdArrayList.get(i);

                ArrayList<TripId> tripIdArrayList = new ArrayList<>();
                if(!cubeTripList5.containsKey(cubeId)) {
                    tripIdArrayList.add(tripId);
                    cubeTripList5.put(cubeId, tripIdArrayList);
                } else if(!cubeTripList5.get(cubeId).contains(tripId)) {
                    tripIdArrayList = cubeTripList5.get(cubeId);
                    tripIdArrayList.add(tripId);
                    cubeTripList5.put(cubeId, tripIdArrayList);
                }

            }
        }

        for (TripId tripId : tripCubeList7.keySet()) {
            ArrayList<CubeId> cubeIdArrayList = tripCubeList7.get(tripId);
            for(int i = 0; i < cubeIdArrayList.size(); i++) {
                CubeId cubeId = cubeIdArrayList.get(i);

                ArrayList<TripId> tripIdArrayList = new ArrayList<>();
                if(!cubeTripList7.containsKey(cubeId)) {
                    tripIdArrayList.add(tripId);
                    cubeTripList7.put(cubeId, tripIdArrayList);
                } else if(!cubeTripList7.get(cubeId).contains(tripId)) {
                    tripIdArrayList = cubeTripList7.get(cubeId);
                    tripIdArrayList.add(tripId);
                    cubeTripList7.put(cubeId, tripIdArrayList);
                }

            }
        }

        for (TripId tripId : tripCubeList8.keySet()) {
            ArrayList<CubeId> cubeIdArrayList = tripCubeList8.get(tripId);
            for(int i = 0; i < cubeIdArrayList.size(); i++) {
                CubeId cubeId = cubeIdArrayList.get(i);

                ArrayList<TripId> tripIdArrayList = new ArrayList<>();
                if(!cubeTripList8.containsKey(cubeId)) {
                    tripIdArrayList.add(tripId);
                    cubeTripList8.put(cubeId, tripIdArrayList);
                } else if(!cubeTripList8.get(cubeId).contains(tripId)) {
                    tripIdArrayList = cubeTripList8.get(cubeId);
                    tripIdArrayList.add(tripId);
                    cubeTripList8.put(cubeId, tripIdArrayList);
                }

            }
        }


            Long startTime2 = System.currentTimeMillis();
            // try catch block
            try {
                FileOutputStream myFileOutStream4
                        = new FileOutputStream(dateCubeTripFile4);
                FileOutputStream myFileOutStream5
                        = new FileOutputStream(dateCubeTripFile5);
                FileOutputStream myFileOutStream7
                        = new FileOutputStream(dateCubeTripFile7);
                FileOutputStream myFileOutStream8
                        = new FileOutputStream(dateCubeTripFile8);
                FileOutputStream myFileOutStream6
                        = new FileOutputStream(dateCubeTripFile6);

                ObjectOutputStream myObjectOutStream4
                        = new ObjectOutputStream(myFileOutStream4);
                ObjectOutputStream myObjectOutStream5
                        = new ObjectOutputStream(myFileOutStream5);
                ObjectOutputStream myObjectOutStream7
                        = new ObjectOutputStream(myFileOutStream7);
                ObjectOutputStream myObjectOutStream8
                        = new ObjectOutputStream(myFileOutStream8);
                ObjectOutputStream myObjectOutStream6
                        = new ObjectOutputStream(myFileOutStream6);

                myObjectOutStream4.writeObject(cubeTripList4);
                myObjectOutStream5.writeObject(cubeTripList5);
                myObjectOutStream7.writeObject(cubeTripList7);
                myObjectOutStream8.writeObject(cubeTripList8);
                myObjectOutStream6.writeObject(cubeTripList6);

                // closing FileOutputStream and
                // ObjectOutputStream
                myObjectOutStream4.close();
                myFileOutStream4.close();
                myObjectOutStream5.close();
                myFileOutStream5.close();
                myObjectOutStream7.close();
                myFileOutStream7.close();
                myObjectOutStream8.close();
                myFileOutStream8.close();
                myObjectOutStream6.close();
                myFileOutStream6.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }

            Long endTime2 = System.currentTimeMillis();
            System.out.println("[HISTORICALTRIPOFVARIOUSRESOLUTIONINDEX] serialization time: " + (endTime2 - startTime2) / 1000 + "s");

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
