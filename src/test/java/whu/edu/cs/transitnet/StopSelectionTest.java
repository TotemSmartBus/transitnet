package whu.edu.cs.transitnet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import whu.edu.cs.transitnet.realtime.RealtimeService;
import whu.edu.cs.transitnet.realtime.Vehicle;
import whu.edu.cs.transitnet.service.StopSelectionService;

import javax.annotation.Resource;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("whu.edu.cs.transitnet.*")
public class StopSelectionTest {
    @Resource
    StopSelectionService stopSelectionService;

    List<String> tripIds = new ArrayList<>();
    List<Vehicle> vs = new ArrayList<>();

    @Test
    public void GetRealtimeInfoTest() throws MalformedURLException {
        stopSelectionService.stopSelection();
    }
}
