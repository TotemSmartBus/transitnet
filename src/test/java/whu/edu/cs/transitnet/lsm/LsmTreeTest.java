package whu.edu.cs.transitnet.lsm;

import edu.whu.hytra.core.SocketStorageManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
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
public class LsmTreeTest {

    @Autowired
    private SocketStorageManager manager;

    // 简单的读写测试
    @Test
    public void WriteAndReadTest() throws Exception {
        String date = "";
        String key = "123";
        try {
            manager.put(key, "123");
            String result = manager.get(key);
            Assert.assertEquals("123", result);
            manager.put(key, "1234");
            String result2 = manager.get(key);
            String[] results = result2.split(",");
            Assert.assertArrayEquals(new String[]{"123", "1234"}, results);
            String status = manager.status();
            log.info("LSM status is " + status);
        } catch (Exception e) {
            System.out.println(e);
            throw e;
        }

    }

    // 简单的读取状态测试
    @Test
    public void statusTest() throws Exception {
        String status = manager.status();
        System.out.println(status);
    }

}
