package whu.edu.cs.transitnet.lsm;

import edu.whu.hytra.core.SocketStorageManager;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import java.net.Socket;

@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("whu.edu.cs.transitnet.*")
public class LsmTreeTest {

    @Autowired
    private SocketStorageManager manager;

    @Test
    public void WriteAndReadTest() throws Exception {
        String key = "123";
        try {
            manager.put(key, "123");
            String result = manager.get(key);
            Assert.assertEquals("123", result);
            manager.put(key, "1234");
            String result2 = manager.get(key);
            String[] results = result2.split(",");
            Assert.assertArrayEquals(new String[]{"123", "1234"}, results);
        } catch (Exception e) {
            System.out.println(e);
            throw e;
        }

    }

}
