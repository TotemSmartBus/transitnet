package whu.edu.cs.transitnet.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import whu.edu.cs.transitnet.realtime.RealtimeService;
import whu.edu.cs.transitnet.realtime.Vehicle;
import whu.edu.cs.transitnet.service.index.TripId;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("whu.edu.cs.transitnet.*")
public class RealtimeServiceTest {

    @Autowired
    RealtimeService realtimeService;

    @Test
    public void timezoneTest() {
        // realtimeService.getAllVehicles();
        long timestamp = realtimeService.getCurrentTimestamp();
        Calendar serverTime = Calendar.getInstance();
        serverTime.setTimeInMillis(timestamp);
        Calendar localTime = Calendar.getInstance();
        localTime.setTimeInMillis(System.currentTimeMillis());
        Assert.assertEquals(serverTime.get(Calendar.YEAR), localTime.get(Calendar.YEAR));
        Assert.assertEquals(serverTime.get(Calendar.MONTH), localTime.get(Calendar.MONTH));
        Assert.assertEquals(serverTime.get(Calendar.DATE), localTime.get(Calendar.DATE));
        Assert.assertEquals(serverTime.get(Calendar.HOUR), localTime.get(Calendar.HOUR));
        // try to figure the timezone diff
        // FIXME no way, the diff is strange...
        long diffMs = System.currentTimeMillis() - timestamp;
        long diff = Math.round((double) diffMs / 1000 / 60 / 60);
        double accuracy = 1 - (double) (diff * 60 * 60 * 1000 - diffMs) / diffMs;
        log.info(String.format("I guess the diff of the timezone between server and gtfs server is %d(%.2f).", diff, accuracy));
    }

    @Test
    public void getVehiclesTest() throws InterruptedException {
//        Thread.sleep(200000);
        Thread.sleep(20000);

        Map<TripId, ArrayList<Vehicle>> vehicles =  realtimeService.getVehiclesByTripId();

        Date d = new Date();

        for(TripId tripId : vehicles.keySet()) {
            Long time = vehicles.get(tripId).get(0).getRecordedTime();
            d.setTime(time * 1000);
            System.out.println( time + ": " + new SimpleDateFormat("HH:mm:ss").format(d));
        }


    }
}
