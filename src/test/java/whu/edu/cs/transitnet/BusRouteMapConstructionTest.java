package whu.edu.cs.transitnet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import whu.edu.cs.transitnet.bean.BusRouteMap;
import whu.edu.cs.transitnet.service.index.BusTripEdge;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("whu.edu.cs.transitnet.*")
public class BusRouteMapConstructionTest {
     @Autowired
     BusRouteMap busRouteMap;

    @Test
    public void test() {

        System.out.println("===========Bus Route Map Construction==========");
        List<BusTripEdge> busTripEdges = busRouteMap.getBusTripEdges();
        System.out.println("The size of busStops: " + busRouteMap.getBusStops().size());
        System.out.println("The size of busTrips: " + busRouteMap.getBusTrips().size());
        System.out.println("The size of busTripsEdges: " + busTripEdges.size());
        // 每条边的 trip 的数量
    }
}
