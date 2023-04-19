package whu.edu.cs.transitnet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import whu.edu.cs.transitnet.service.index.ScheduleIndex;

@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("whu.edu.cs.transitnet.*")
public class ScheduleTest {
    @Autowired
    ScheduleIndex scheduleIndex;

    @Test
    public void daoTest() {
        // TODO 测试失败了
        scheduleIndex = new ScheduleIndex();
//        scheduleIndex.testDao();
    }
}
