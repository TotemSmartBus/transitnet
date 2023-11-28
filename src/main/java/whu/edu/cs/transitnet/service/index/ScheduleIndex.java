package whu.edu.cs.transitnet.service.index;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import whu.edu.cs.transitnet.dao.StopTimesDao;
import whu.edu.cs.transitnet.dao.TripsDao;
import whu.edu.cs.transitnet.pojo.TripsEntity;
import whu.edu.cs.transitnet.vo.TripTimesVo;

import javax.annotation.PostConstruct;
import java.io.*;
import java.sql.Time;
import java.util.*;

@Component
public class ScheduleIndex {

    @Value("${transitnet.gridindex.enable}")
    private boolean indexEnable;

    @Autowired
    StopTimesDao stopTimesDao;

    @Autowired
    TripsDao tripsDao;

    @Autowired
    ShapeIndex shapeIndex;

    public HashMap<TripId, ArrayList<Time>> getTripStartEndList() {
        return tripStartEndList;
    }

    // trip_id - start_time - end_time
    private HashMap<TripId, ArrayList<Time>> tripStartEndList;

    public ScheduleIndex() {
        tripStartEndList = new HashMap<>();
    }

    @PostConstruct
    public void init() {

        // 请勿删除
        // 用于控制是否构建索引
//        if(!indexEnable) {
//            System.out.println("[SCHEDULEINDEX] Index is not enabled, skipped.");
//            return;
//        }

        Resource tripScheduleResource = new ClassPathResource("indexFiles/trip_schedule.txt");

        if(tripScheduleResource.exists()) {
            // 读取文件
            System.out.println("======================");
            System.out.println("[SCHEDULEINDEX] FILE EXISTS...");
            System.out.println("[SCHEDULEINDEX] Start Deserializing HashMap..");

            Long starttime = System.currentTimeMillis();


            try {

                InputStream tripScheduleStream = tripScheduleResource.getInputStream();


                ObjectInputStream objectInput1
                        = new ObjectInputStream(tripScheduleStream);

                tripStartEndList = (HashMap)objectInput1.readObject();

                objectInput1.close();
                tripScheduleStream.close();
            }

            catch (IOException obj1) {
                obj1.printStackTrace();
                return;
            }

            catch (ClassNotFoundException obj2) {
                System.out.println("[SCHEDULEINDEX] Class not found");
                obj2.printStackTrace();
                return;
            }

            Long endtime = System.currentTimeMillis();

            System.out.println("[SCHEDULEINDEX] Deserializing HashMap DONE!");
            System.out.println("[SCHEDULEINDEX] Deserializing time: " + (endtime - starttime) / 1000 + "s");

            // Displaying content in "newHashMap.txt" using
            // Iterator
            Set set = tripStartEndList.entrySet();
            Iterator iterator = set.iterator();

            int i = 1;
            while (iterator.hasNext() && i < 4) {
                i++;
                Map.Entry entry = (Map.Entry)iterator.next();

                System.out.print("key : " + entry.getKey()
                        + " & Value : ");
                System.out.println(entry.getValue());
            }
        } else {
            System.out.println("=============================");
            System.out.println("[SCHEDULEINDEX] File Not Exists... Start fetching data from database...");


            Long startTime1 = System.currentTimeMillis();

            // 取出所有 trip_id
            List<TripsEntity> tripsEntities = tripsDao.findAll();

            System.out.println("[SCHEDULEINDEX] Size of Trips: " + tripsEntities.size());

            int num = 0;
            for(TripsEntity tripsEntity : tripsEntities) {
                String trip = tripsEntity.getTripId();
                TripId tripId = new TripId(trip);

                num++;
                //System.out.println("[SCHEDULEINDEX] Number of Scanned Trips: " + num);

                // 取出该 trip_id 下的到站时间序列
                List<TripTimesVo> tripTimesVos = stopTimesDao.findAllByTripId(trip);
                if (tripTimesVos.size() >1) {
                    ArrayList<Time> startEndTime = new ArrayList<>();
                    startEndTime.add(tripTimesVos.get(0).getArrivalTime());
                    startEndTime.add(tripTimesVos.get(tripTimesVos.size() - 1).getArrivalTime());

                    tripStartEndList.put(tripId, startEndTime);
                }
            }

//            try {
//                FileOutputStream myFileOutStream1
//                        = new FileOutputStream(tripScheduleFile);
//
//                ObjectOutputStream myObjectOutStream1
//                        = new ObjectOutputStream(myFileOutStream1);
//
//                myObjectOutStream1.writeObject(tripStartEndList);
//
//                myObjectOutStream1.close();
//                myFileOutStream1.close();
//            }
//            catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

            Long endTime1 = System.currentTimeMillis();
            System.out.println("[SCHEDULEINDEX] index construction and serialization time: " + (endTime1 - startTime1) / 1000 / 60 + "min");
        }


    }

}
