package whu.edu.cs.transitnet.lsm;

import edu.whu.hytra.core.SocketStorageManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import whu.edu.cs.transitnet.service.index.CubeId;
import whu.edu.cs.transitnet.service.index.HistoricalTripIndex;
import whu.edu.cs.transitnet.service.index.TripId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("whu.edu.cs.transitnet.*")
public class LsmTreeHistoricalGetTest {
    @Autowired
    HistoricalTripIndex historicalTripIndex;

    @Autowired
    private SocketStorageManager manager;

    @Test
    public void getTest() throws Exception {
        HashMap<CubeId, ArrayList<TripId>> CTList = historicalTripIndex.getCubeTripList();

        CubeId[] keys = CTList.keySet().toArray(new CubeId[0]); //将map里的key值取出，并放进数组里

        int random = (int) (Math.random()*(keys.length)); //生成随机数

        CubeId randomKey = new CubeId("76672");
//        CubeId randomKey = keys[random]; //随机取key值
        String key = "2023-05-20@76672@0";


//        manager.put(key, "111");
//        manager.put(key, "222");
//        manager.put(key, "333");

        String result2 = manager.get(key);
        String[] results = result2.split(",");
        System.out.println(Arrays.toString(results));

        ArrayList<TripId> tripIds = CTList.get(randomKey);
        for (TripId tripId : tripIds) {
            System.out.println(tripId.toString().hashCode());
        }

        String status = manager.status();
        System.out.println("LSM status is " + status);
    }
}
