package whu.edu.cs.transitnet.lsm;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import whu.edu.cs.transitnet.service.index.HistoricalTripIndex;

@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("whu.edu.cs.transitnet.*")
@Slf4j
public class LsmTreeHistoricalPutTest {
    @Autowired
    HistoricalTripIndex historicalTripIndex;

    @Test
    public void historicalCTListPutTest() {

    }
}
