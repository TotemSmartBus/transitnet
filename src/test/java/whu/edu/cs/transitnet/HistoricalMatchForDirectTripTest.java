package whu.edu.cs.transitnet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import whu.edu.cs.transitnet.pojo.RealTimePointEntity;
import whu.edu.cs.transitnet.service.HistoricalMatchForDirectTrip;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("whu.edu.cs.transitnet.*")
public class HistoricalMatchForDirectTripTest {
    @Autowired
    HistoricalMatchForDirectTrip historicalMatchForDirectTrip;

    @Test
    public void getAllPointsOfOneTrip() throws IOException {
        String routeId = "Q110";
        String directionId = "0";
        historicalMatchForDirectTrip.getAllHistoricalTrajectoriesOnTheSpecifiedRoute(routeId, directionId);
        Map<String, List<RealTimePointEntity>> slicedTripTrajectoryList = historicalMatchForDirectTrip.getSlicedTripTrajectoryList();


        // System.out.println(slicedTripTrajectoryList.keySet());
        // System.out.println(slicedTripTrajectoryList.get("2023-06-23@35671278-BPPB3-BP_B3-Weekday-02-SDon"));
        // System.out.println("Size of a trajectory: " + slicedTripTrajectoryList.get("2023-06-23@35671278-BPPB3-BP_B3-Weekday-02-SDon").size());
        // System.out.println("Size of trajectories: " + slicedTripTrajectoryList.size());
    }
}
