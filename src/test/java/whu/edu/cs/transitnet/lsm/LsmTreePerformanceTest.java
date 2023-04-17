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

import java.util.HashMap;
import java.util.Random;

/**
 * This is a test for LSM Tree performance.
 * <p>
 * We use the jar library to connect to the LSM process and insert and get values to calculate the time cost.
 * <p>
 * <p>
 * The LSM Tree is a key-value pair. We decide to insert the key from
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("whu.edu.cs.transitnet.*")
@Slf4j
public class LsmTreePerformanceTest {
    private static char[] dict;

    static {
        dict = new char[26 * 2];
        for (int i = 0; i < 26; i++) {
            dict[i] = ((char) ('A' + i));
        }
        for (int i = 0; i < 26; i++) {
            dict[i + 26] = ((char) ('a' + i));
        }
    }

    private Random rand = new Random();

    @Autowired
    private SocketStorageManager manager;

    /**
     * 生成一个字母开头 + 6 位数字的ID 组合
     * e.p.:"T18284", "a98965"
     *
     * @return
     */
    private String genValue() {
        return String.valueOf(dict[rand.nextInt(26 * 2)]) + (rand.nextInt(90000) + 10000);
    }

    private HashMap<String, String> prepareData(int size) {
        Long start = System.currentTimeMillis();
        HashMap<String, String> result = new HashMap<>();
        for (int i = 0; i < size; i++) {
            result.put("k" + i, genValue());
        }
        Long end = System.currentTimeMillis();
        log.info(String.format("[metrix]prepare data for %.2f s", (end - start) / 1000.0));
        return result;
    }

    private Long insertTest(int size) {
        HashMap<String, String> data = prepareData(size);
        Long start = System.currentTimeMillis();
        data.forEach((k, v) -> {
            try {
                manager.put(k, v);
            } catch (Exception e) {
                log.warn("insert data error", e);
            }
        });
        Long end = System.currentTimeMillis();
        return end - start;
    }

    /**
     * 运行 1000 条 100 次的插入操作测试
     */
    @Test
    public void insert1000For100TimesTest() {
        int count = 1000;
        int round = 500;
        Long times = 0L;
        for (int i = 0; i < round; i++) {
            Long time = insertTest(count);
            log.info(String.format("insert %d for %.2f s.", count, time / 1000.0));
            times += time;
        }
        log.info(String.format("insert %d items for %d times, average time is %.2f s", count, round, times / round / 1000.0));
    }

    @Test
    public void get100000Test() {
        int count = 100000;
        HashMap<String, String> data = prepareData(count);
        Long insertStart = System.currentTimeMillis();
        data.forEach((k, v) -> {
            try {
                manager.put(k, v);
            } catch (Exception e) {
                log.warn("insert data error", e);
            }
        });
        Long insertEnd = System.currentTimeMillis();
        log.info(String.format("[matrix]insert %d items cost %.2f s", count, (insertEnd - insertStart) / 1000.0));
        // query for each
        Long queryStart = System.currentTimeMillis();
        data.forEach((k, v) -> {
            try {
                String res = manager.get(k);
                Assert.assertTrue(res.contains(v));
            } catch (Exception e) {
                log.warn("insert data error", e);
            }
        });
        Long queryEnd = System.currentTimeMillis();
        log.info(String.format("[matrix]query %d items cost %.2f s", count, (queryEnd - queryStart) / 1000.0));
    }
}
