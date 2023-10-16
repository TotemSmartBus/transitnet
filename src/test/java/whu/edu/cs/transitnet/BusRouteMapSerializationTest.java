package whu.edu.cs.transitnet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import whu.edu.cs.transitnet.service.BusRouteMapSerializationService;

@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("whu.edu.cs.transitnet.*")
public class BusRouteMapSerializationTest {
    @Autowired
    BusRouteMapSerializationService busRouteMapSerializationService;

    @Test
    public void test() {

        System.out.println("===========Bus Route Map Serialization==========");
        busRouteMapSerializationService.init();
    }
}
