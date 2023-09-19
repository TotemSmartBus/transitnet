package whu.edu.cs.transitnet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import whu.edu.cs.transitnet.param.QueryKnnHisParam;
import whu.edu.cs.transitnet.service.HistoricalRangeService;
import whu.edu.cs.transitnet.service.RealtimeKNNExpService;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("whu.edu.cs.transitnet.*")
public class HistoricalRangeExpTest {
    @Autowired
    HistoricalRangeService historicalrangeExpService;

    @Test
    public void kNNExpTest() throws InterruptedException, IOException, ParseException {
        double []ps={40.820032,-73.827494,
                40.822107,-73.824386};
        String date="2023-05-20";
        historicalrangeExpService.setup(ps,date);
        historicalrangeExpService.historaical_range_search();
        System.out.println("==============================================================\n");
        System.out.println("============================No Error==========================\n");
        System.out.println("==============================================================\n");
    }
}
