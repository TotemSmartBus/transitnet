package whu.edu.cs.transitnet;

import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class randomTest {

    @Test
    public void TimeTest() throws ParseException {
        String startTime = "2023-04-05 13:44:00";
        Date parse = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(startTime);
        Long time = parse.getTime();

        Date d = new Date();
        d.setTime(time);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String date = sdf.format(d);
        System.out.println(date);
    }

}

