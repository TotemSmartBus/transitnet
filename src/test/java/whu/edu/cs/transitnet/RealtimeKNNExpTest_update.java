package whu.edu.cs.transitnet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import whu.edu.cs.transitnet.param.QueryKnnRtParam;
import whu.edu.cs.transitnet.service.RealtimeKNNExpService;
import whu.edu.cs.transitnet.service.RealtimeKNNExpServiceTEST;
import whu.edu.cs.transitnet.service.index.ShapeIndex;
import whu.edu.cs.transitnet.vo.KnnRtQueryResultVo;

import java.io.IOException;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("whu.edu.cs.transitnet.*")
public class RealtimeKNNExpTest_update {

    @Autowired
    RealtimeKNNExpService realtimeKNNExpService;

    @Autowired
    ShapeIndex shapeIndex;

    @Test
    public void kNNExpTest() throws InterruptedException, IOException {
        int k = 5;
        int backdate=180;
        double []ps={40.68760299682617,-73.97772979736328,
                40.68608474731445,-73.9739761352539,
                40.68581771850586,-73.97328186035156,
                40.681819915771484,-73.95903778076172,
                40.683345794677734,-73.95016479492188};
        QueryKnnRtParam param=new QueryKnnRtParam(ps);
        realtimeKNNExpService.setup(param.getPoints(),k,backdate);
        realtimeKNNExpService.getTopKTrips();
        //KnnRtQueryResultVo result=new KnnRtQueryResultVo(realtimeKNNExpService.get_res());
    }

}
