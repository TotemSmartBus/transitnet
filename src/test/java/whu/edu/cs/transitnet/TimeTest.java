package whu.edu.cs.transitnet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import whu.edu.cs.transitnet.realtime.RealtimeService;
import whu.edu.cs.transitnet.realtime.Vehicle;
import whu.edu.cs.transitnet.service.index.ScheduleIndex;
import whu.edu.cs.transitnet.service.index.TripId;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("whu.edu.cs.transitnet.*")
public class TimeTest {

    @Autowired
    RealtimeService realtimeService;

    @Autowired
    ScheduleIndex scheduleIndex;

    // 测试看实时取到的tripid的时间范围和schedule的时间是不是一致的
    @Test
    public void timeTest() throws InterruptedException {
        HashMap<TripId, ArrayList<Time>> tripStartEndList = scheduleIndex.getTripStartEndList();
        Thread.sleep(300000);
        Map<TripId, ArrayList<Vehicle>> vehiclesByTripId = realtimeService.getVehiclesByTripId();


        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));

        Time startTime;
        Time endTime;

        Set<TripId> tripIdSet = vehiclesByTripId.keySet();
        for (TripId tripId : tripIdSet) {
            ArrayList<Vehicle> vehicles = vehiclesByTripId.get(tripId);
            ArrayList<String> times = new ArrayList<>();
            for (Vehicle vehicle : vehicles) {
                d.setTime(vehicle.getRecordedTime() * 1000);
                String date_hour_min_sec  = sdf.format(d);
                times.add(date_hour_min_sec);
            }
            startTime = Time.valueOf(times.get(0));
            endTime = Time.valueOf(times.get(times.size() - 1));
            System.out.println("[TIMETEST] " + tripId + ": [" + startTime + ", " + endTime + "] " + tripStartEndList.get(tripId));
        }

    }
}
