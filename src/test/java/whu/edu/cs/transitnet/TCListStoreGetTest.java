package whu.edu.cs.transitnet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import whu.edu.cs.transitnet.service.index.CubeId;
import whu.edu.cs.transitnet.service.index.HytraSerivce;
import whu.edu.cs.transitnet.service.index.TripId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("whu.edu.cs.transitnet.*")
public class TCListStoreGetTest {
    @Autowired
    HytraSerivce hytraSerivce;
    @Test
    public void Test() throws Exception {
        HashMap<TripId, ArrayList<CubeId>> res=hytraSerivce.getTCListByDate("2024-01-02");
    }
}
