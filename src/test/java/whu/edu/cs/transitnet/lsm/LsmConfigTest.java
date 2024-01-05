package whu.edu.cs.transitnet.lsm;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import whu.edu.cs.transitnet.service.index.HytraSerivce;


@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("whu.edu.cs.transitnet.*")
public class LsmConfigTest {
    @Autowired
    HytraSerivce hytraSerivce;

    @Test
    public void test() throws Exception {
        hytraSerivce.buildDateLsmConfigAndInsertKV();
    }
}
