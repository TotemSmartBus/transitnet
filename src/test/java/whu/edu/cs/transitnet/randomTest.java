package whu.edu.cs.transitnet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import whu.edu.cs.transitnet.realtime.RealtimeService;

import javax.annotation.Resource;
import java.security.KeyStore;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("whu.edu.cs.transitnet.*")
public class randomTest {

    @Test
    public void randomTest() throws Exception {
        Map<String, Double> map = new HashMap();
        map.put("1", 5.7);
        map.put("2", 2.4);
        map.put("3", 1.8);
        map.put("4", 6.4);
        map.put("5", 10.6);
        map.put("6", 6.4);
        map.put("7", 2.3);
        List<Map.Entry<String,Double>> list = new ArrayList(map.entrySet());
//        Collections.sort(list, (o1, o2) -> (int) (o2.getValue() - o1.getValue()));
        list.sort(Map.Entry.comparingByValue());
        System.out.println(list);

        List<Double> list1 = new ArrayList<>();
        list1.add(5.7);
        list1.add(2.4);
        list1.add(1.8);
        list1.add(6.4);
        list1.add(10.6);
        list1.add(6.4);
        list1.add(2.3);
        Collections.sort(list1);
        System.out.println(list1);

        ArrayList<Map.Entry<String, Double>> arrayList = new ArrayList<>(map.entrySet());
        //     2) 对哈希表的值进行排序
        //        对list进行排序，并通过Comparator传入自定义的排序规则
        Collections.sort(arrayList, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                Double t = o1.getValue() - o2.getValue();
                int flag = 1;
                if (t <= 0) flag = -1;
                return flag;
            }
        });
        System.out.println(arrayList);
    }
}
