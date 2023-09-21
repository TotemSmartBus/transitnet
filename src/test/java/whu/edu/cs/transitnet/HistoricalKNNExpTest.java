package whu.edu.cs.transitnet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import whu.edu.cs.transitnet.service.HistoricalKNNExpServiceTEST;

import java.io.IOException;
import java.text.ParseException;

@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("whu.edu.cs.transitnet.*")
public class HistoricalKNNExpTest {
    @Autowired
    HistoricalKNNExpServiceTEST historicalKNNExpService;

    @Test
    public void kNNExpTest() throws InterruptedException, IOException, ParseException {
        int k = 10;
        historicalKNNExpService.getTopKTrips(k);
    }
}
