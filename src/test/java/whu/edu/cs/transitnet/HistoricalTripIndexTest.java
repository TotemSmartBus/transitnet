package whu.edu.cs.transitnet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("whu.edu.cs.transitnet.*")
public class HistoricalTripIndexTest {

//    @Autowired
//    HistoricalTripIndex historicalTripIndex;

    @Test
    public void getTripsOnlyByDateTest() throws ParseException {
        String startTime = "2023-05-20 10:00:00";
        String endTime = "2023-05-20 23:59:59";

        Date parse = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(startTime);
        Long time = parse.getTime();


        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        d.setTime(time);
        String date_hour_min_sec  = sdf.format(d);
        System.out.println(parse);
//        List<String> trips = historicalTripIndex.getTripsByDate(startTime, endTime);
//        for (String trip : trips) {
//            System.out.println(trip);
//        }
    }
}
