package whu.edu.cs.transitnet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("whu.edu.cs.transitnet.*")
public class HistoricalTripOfVariousResolutionIndexTest {
    @Test
    public void ATest(){
        System.out.println("hello world!");
    }
}
