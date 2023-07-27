package whu.edu.cs.transitnet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import whu.edu.cs.transitnet.pojo.RealTimePointEntity;
import whu.edu.cs.transitnet.service.EncodeService;
import whu.edu.cs.transitnet.service.index.CubeId;
import whu.edu.cs.transitnet.service.index.HytraEngineManager;
import whu.edu.cs.transitnet.service.index.TripId;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("whu.edu.cs.transitnet.*")
public class HistoricalTripIndexTest {
    @Autowired
    EncodeService encodeService;

    @Autowired
    HytraEngineManager hytraEngineManager;

    @Test
    public void getTripsOnlyByDateTest() throws ParseException {
        HashMap<TripId, ArrayList<CubeId>> tripCubeList = new HashMap<>();
        HashMap<CubeId, ArrayList<TripId>> cubeTripList = new HashMap<>();
        // 新增 TPList 用于实验
        HashMap<TripId, ArrayList<RealTimePointEntity>> tripPointList = new HashMap<>();

        String date = "2023-05-20";
        File dateTripPointFile = new File("./src/main/" + date + " TPList.txt");

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

        Set<TripId> tripIds = tripPointList.keySet();
        for (TripId tripId : tripIds) {
            ArrayList<RealTimePointEntity> realTimePointEntityList = tripPointList.get(tripId);
            ArrayList<CubeId> cubeIds = new ArrayList<>();

            for (int j = 0; j < realTimePointEntityList.size(); j++) {
//                System.out.println("[HISTORICALTRIPINDEX] number of scanned points: " + (j + 1));

                double lat = realTimePointEntityList.get(j).getLat();
                double lon = realTimePointEntityList.get(j).getLon();

                // TODO:
                String recordedTime = realTimePointEntityList.get(j).getRecordedTime();
                Date parse = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(recordedTime);
                Long time = parse.getTime();

                // 注意 encodecube 里面传的是年月日时分秒

                CubeId cubeId = encodeService.encodeCube(lat, lon, time);
                if(cubeIds.isEmpty() || cubeIds.lastIndexOf(cubeId) != (cubeIds.size() - 1)) cubeIds.add(cubeId);
            }

            // 构造 tripCubeList
            tripCubeList.put(tripId, cubeIds);
        }

        int resolution = hytraEngineManager.getParams().getResolution();
        File dateTripCubeFile = new File("./src/main/" + date + " TCList__" + resolution + ".txt");

        if(!dateTripCubeFile.exists()) {
            System.out.println("=============================");
            System.out.println("[HISTORICALTRIPINDEX] dateTripCubeFile Not Exists... Start serializing TCList...");

            Long startTime1 = System.currentTimeMillis();
            // 构建 CTList
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

    }
}
