package whu.edu.cs.transitnet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import whu.edu.cs.transitnet.bean.BusRouteMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("whu.edu.cs.transitnet.*")
public class BusRouteMapDeserializationTest {
    @Test
    public void test() {

        System.out.println("===========Bus Route Map Deserialization==========");
        FileInputStream fileIn = null;
        try {
            Long startTime = System.currentTimeMillis();
            File file = new File("./src/main/" + "bus_route_map.txt");
            fileIn = new FileInputStream(file);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            BusRouteMap busRouteMap = (BusRouteMap) in.readObject();
            System.out.println(busRouteMap.getBusStops().size());

            Long endTime = System.currentTimeMillis();
            System.out.println("[Deserialization time] " + (endTime - startTime) / 1000 + " s");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
