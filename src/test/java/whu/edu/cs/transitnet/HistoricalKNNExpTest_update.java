package whu.edu.cs.transitnet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import whu.edu.cs.transitnet.param.QueryKnnHisParam;
import whu.edu.cs.transitnet.service.HistoricalKNNExpService;
import whu.edu.cs.transitnet.service.RealtimeKNNExpService;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("whu.edu.cs.transitnet.*")
public class HistoricalKNNExpTest_update {
    @Autowired
    HistoricalKNNExpService historicalKNNExpService;

    @Test
    public void kNNExpTest() throws InterruptedException, IOException, ParseException {
        int k = 6;
        double []ps={40.68760299682617,-73.97772979736328,
                40.68608474731445,-73.9739761352539,
                40.68581771850586,-73.97328186035156,
                40.681819915771484,-73.95903778076172,
                40.683345794677734,-73.95016479492188};
        String []ts={"2023-05-20 08:00:00","2023-05-20 08:00:40",
                "2023-05-20 08:01:20","2023-05-20 08:02:00",
                "2023-05-20 08:02:40"};
        String date="2023-05-20";
        List<String> range=new ArrayList<>();
        range.add("2023-09-12T09:00:00.000Z");
        range.add("2023-09-12T17:00:00.000Z");
        QueryKnnHisParam param=new QueryKnnHisParam(ps,ts);
        historicalKNNExpService.setup(param.getPoints(),k);
        historicalKNNExpService.getTopKTrips();
        List<RealtimeKNNExpService.resItem> res = historicalKNNExpService.get_res();
        System.out.println("==============================================================\n");
        System.out.println("Rank |                   TripID                   | Similarity\n");
        for(int i=0;i<res.size();i++){
            System.out.println(res.get(i).getRank()+"        "+res.get(i).getBusId()+"              "+res.get(i).getSim());
        }
        System.out.println("==============================================================\n");
    }
}
