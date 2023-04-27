package whu.edu.cs.transitnet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import whu.edu.cs.transitnet.realtime.RealtimeService;
import whu.edu.cs.transitnet.realtime.Vehicle;
//import whu.edu.cs.transitnet.service.UserKNNService;
import whu.edu.cs.transitnet.service.index.ShapeIndex;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("whu.edu.cs.transitnet.*")
public class KNNTest {

//    @Autowired
//    UserKNNService userKNNService;
//
//    @Autowired
//    ShapeIndex shapeIndex;
//
//    @Test
//    public void kNNTest() throws InterruptedException {
//        userKNNService.getTopKTrips(10);
//    }
    public void kTest() {
        System.out.println("t");
    }
}
