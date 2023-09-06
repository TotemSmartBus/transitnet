package whu.edu.cs.transitnet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import whu.edu.cs.transitnet.service.RealtimeKNNService;
import whu.edu.cs.transitnet.service.index.ShapeIndex;

@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("whu.edu.cs.transitnet.*")
public class RealtimeKNNTest {

    @Autowired
    RealtimeKNNService realtimeKNNService;

    @Autowired
    ShapeIndex shapeIndex;

    @Test
    public void kNNTest() throws InterruptedException {
        int k = 30;
        realtimeKNNService.getTopKTrips(k);
    }

}
