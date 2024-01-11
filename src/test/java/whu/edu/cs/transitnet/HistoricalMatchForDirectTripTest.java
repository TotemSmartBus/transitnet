package whu.edu.cs.transitnet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import whu.edu.cs.transitnet.service.DecodeService;
import whu.edu.cs.transitnet.service.EncodeService;
import whu.edu.cs.transitnet.service.HistoricalMatchForDirectTrip;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("whu.edu.cs.transitnet.*")
public class HistoricalMatchForDirectTripTest {
    @Autowired
    HistoricalMatchForDirectTrip historicalMatchForDirectTrip;

    @Autowired
    EncodeService encodeService;

    @Autowired
    DecodeService decodeService;

    @Test
    public void getAllPointsOfOneTrip() throws IOException, ClassNotFoundException {
//        String routeId = "Q110";
//        String directionId = "0";
//        historicalMatchForDirectTrip.getAllHistoricalTrajectoriesOnTheSpecifiedRoute(routeId, directionId);
//        historicalMatchForDirectTrip.createTraCubeList();
//
//        Map<String, List<RealTimePointEntity>> slicedTripTrajectoryList = historicalMatchForDirectTrip.getSlicedTripTrajectoryList();
//        Map<String, List<CubeId>> traCubeList = historicalMatchForDirectTrip.getTraCubeList();

        String key = "37566472-BPPD3-BP_D3-Weekday-04-SDon@2023-12-19";
        int k = 1;

        historicalMatchForDirectTrip.getTopK(key, k);
    }
}
