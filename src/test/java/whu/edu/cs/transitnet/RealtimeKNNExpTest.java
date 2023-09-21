package whu.edu.cs.transitnet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import whu.edu.cs.transitnet.service.RealtimeKNNExpServiceTEST;
import whu.edu.cs.transitnet.service.index.ShapeIndex;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("whu.edu.cs.transitnet.*")
public class RealtimeKNNExpTest {

    @Autowired
    RealtimeKNNExpServiceTEST realtimeKNNExpService;

    @Autowired
    ShapeIndex shapeIndex;

    @Test
    public void kNNExpTest() throws InterruptedException, IOException {
        int k = 20;
        realtimeKNNExpService.getTopKTrips(k);
    }

}
