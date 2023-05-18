package whu.edu.cs.transitnet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import whu.edu.cs.transitnet.realtime.RealtimeService;
import whu.edu.cs.transitnet.realtime.Vehicle;
import whu.edu.cs.transitnet.service.UserKNNExpService;
import whu.edu.cs.transitnet.service.UserKNNService;
import whu.edu.cs.transitnet.service.index.ShapeIndex;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("whu.edu.cs.transitnet.*")
public class KNNExpTest {

    @Autowired
    UserKNNExpService userKNNExpService;

    @Autowired
    ShapeIndex shapeIndex;

    @Test
    public void kNNExpTest() throws InterruptedException, IOException {
        int k = 20;
        userKNNExpService.getTopKTrips(k);
    }

}
