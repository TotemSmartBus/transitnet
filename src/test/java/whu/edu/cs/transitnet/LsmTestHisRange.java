package whu.edu.cs.transitnet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import whu.edu.cs.transitnet.param.QueryKnnHisParam;
import whu.edu.cs.transitnet.pojo.RealTimePointEntity;
import whu.edu.cs.transitnet.service.HistoricalRangeService;
import whu.edu.cs.transitnet.service.RealtimeKNNExpService;
import whu.edu.cs.transitnet.service.index.TripId;
import whu.edu.cs.transitnet.vo.RangeHisQueryResultVo;
import whu.edu.cs.transitnet.vo.tripPoints;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("whu.edu.cs.transitnet.*")
public class LsmTestHisRange {
    @Autowired
    HistoricalRangeService historicalRangeService;

    @Test
    public void Test() throws Exception {

        double []ps={40.60,-74.00, 40.80,-73.80};
        String date="2023-12-27";
        String st=date+" 02:21:00";
        String et=date+" 02:30:00";

        historicalRangeService.setup(ps,date,st,et);
        HashSet<TripId> res = historicalRangeService.historaical_range_search();
        for (TripId tid:res) {
            System.out.println(tid.toString());
        }
    }
}
