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

import java.util.Calendar;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("whu.edu.cs.transitnet.*")
public class RealtimeServiceTest {

    @Autowired
    RealtimeService realtimeService;

    @Test
    public void timezoneTest() {
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
}
