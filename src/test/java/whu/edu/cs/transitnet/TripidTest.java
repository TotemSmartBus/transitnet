package whu.edu.cs.transitnet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import whu.edu.cs.transitnet.dao.TripsDao;
import whu.edu.cs.transitnet.pojo.TripsEntity;
import whu.edu.cs.transitnet.realtime.RealtimeService;
import whu.edu.cs.transitnet.realtime.Vehicle;
import whu.edu.cs.transitnet.service.index.TripId;

import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("whu.edu.cs.transitnet.*")
public class TripidTest {
    @Autowired
    RealtimeService realtimeService;

    @Autowired
    TripsDao tripsDao;

    @Test
    public void tripidTest() {
        Map<TripId, ArrayList<Vehicle>> tripids =  realtimeService.getVehiclesByTripId();
        Set<TripId> tripIds = tripids.keySet();

        Set<TripId> tripIds1 = new HashSet<>();
        List<TripsEntity> tripsEntities = tripsDao.findAll();
        for (TripsEntity tripsEntity : tripsEntities) {
            tripIds1.add(new TripId(tripsEntity.getTripId()));
        }

        Set<TripId> tripIds2 = new HashSet<>();
        tripIds2.addAll(tripIds);
        tripIds2.retainAll(tripIds1);
        System.out.println(tripIds.size());
        System.out.println(tripIds1.size());
        System.out.println(tripIds2.size());
        System.out.println(tripIds2);

        Set<TripId> tripIds3 = new HashSet<>();
        tripIds3.addAll(tripIds);
        tripIds3.removeAll(tripIds2);
        System.out.println(tripIds3);

    }
}
