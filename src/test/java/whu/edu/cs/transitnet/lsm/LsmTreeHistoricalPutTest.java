package whu.edu.cs.transitnet.lsm;

import edu.whu.hytra.core.SocketStorageManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("whu.edu.cs.transitnet.*")
@Slf4j
public class LsmTreeHistoricalPutTest {

    @Autowired
    private SocketStorageManager manager;

    @Test
    public void PutTest() throws Exception {
//        String key = "2023-05-20@92344@0"; // 只会存在 disklevel 0
        String key = "2023-05-20@76672@0"; // 会合并到 disklevel 1

        String status0 = manager.status();
        System.out.println("LSM status is " + status0);

        int size1 = 12300;
        for(int i = 1; i <= size1; i++) {
            manager.put(key, String.valueOf(i));
        }



        String result2 = manager.get(key);
        String[] results = result2.split(",");
        System.out.println(results.length);

        String status = manager.status();
        System.out.println("LSM status is " + status);
    }
}
