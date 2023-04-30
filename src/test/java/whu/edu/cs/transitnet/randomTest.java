package whu.edu.cs.transitnet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import whu.edu.cs.transitnet.realtime.RealtimeService;
import whu.edu.cs.transitnet.service.UserKNNService;
import whu.edu.cs.transitnet.service.index.CubeId;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.sql.Time;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("whu.edu.cs.transitnet.*")
public class randomTest {

    @Autowired
    UserKNNService userKNNService;

    @Test
    public void EDRTest() {
        ArrayList<CubeId> cubeIds = new ArrayList<>();
        cubeIds.add(new CubeId("119785"));
        cubeIds.add(new CubeId("119675"));
        cubeIds.add(new CubeId("119647"));

        ArrayList<CubeId> cubeIds1 = new ArrayList<>();
        cubeIds1.add(new CubeId("119785"));
        cubeIds1.add(new CubeId("119675"));
        cubeIds1.add(new CubeId("119647"));

        Double a = userKNNService.EditDistanceonRealSequence(cubeIds, cubeIds1);

        System.out.println(a);
    }

//    @Test
//    public void sortTest () {
//        List<Double> list = new ArrayList<>();
//        list.add(2.0);
//        list.add(3.4);
//        list.add(2.5);
//        list.add(3.4);
//        list.add(3.4);
//        list.add(6.7);
//        list.add(5.6);
//
//        Collections.sort(list, new Comparator<Double>() {
//            @Override
//            public int compare(Double a, Double b) { // 从小到大
//                Double t = a-b;
//                int flag = -1;
//                if (t <= 0) flag = 1;
//                return flag;
//            }
//        });
//        List<Double> list1= list.subList(0, 5);
//        System.out.println(list);
//        System.out.println(list1);
//    }

//    @Test
//    public void timeCompareTest() {
//        Time a = Time.valueOf("08:02:00");
//        Time b = Time.valueOf("21:00:00");
//        System.out.println(a.before(b));
//        System.out.println(a.after(b));
//    }
//    public void listAddAllTest() {
//                List<Integer> list1 = new ArrayList<>();
//                List<Integer> list2 = new ArrayList<>();
//                list1.add(1);
//                list1.add(2);
//                list1.add(3);
//                list1.add(4);
//                list2.add(3);
//                list2.add(4);
//                list2.add(7);
//                list2.add(8);
//                List<Integer> list = new ArrayList<>();
//                list.addAll(list1);
//                list.addAll(list2);
//                System.out.println(list1);
//                System.out.println(list2);
//                System.out.println(list);
//    }
}


//    public void fileTest() {
//        File file = new File("./src/test/test.txt");
//
//        if (!file.exists()) {
//
//            try {
//
//                file.createNewFile();
//
//            } catch (IOException e) {
//
//                e.printStackTrace();
//
//            }
//
//            System.out.println("文件已创建");
//
//        } else {
//
//            System.out.println("文件已存在");
//
//        }
//    }

//    public void randomTest() throws Exception {
//        Map<String, Double> map = new HashMap();
//        map.put("1", 5.7);
//        map.put("2", 2.4);
//        map.put("3", 1.8);
//        map.put("4", 6.4);
//        map.put("5", 10.6);
//        map.put("6", 6.4);
//        map.put("7", 2.3);
//        List<Map.Entry<String,Double>> list = new ArrayList(map.entrySet());
////        Collections.sort(list, (o1, o2) -> (int) (o2.getValue() - o1.getValue()));
//        list.sort(Map.Entry.comparingByValue());
//        System.out.println(list);
//
//        List<Double> list1 = new ArrayList<>();
//        list1.add(5.7);
//        list1.add(2.4);
//        list1.add(1.8);
//        list1.add(6.4);
//        list1.add(10.6);
//        list1.add(6.4);
//        list1.add(2.3);
//        Collections.sort(list1);
//        System.out.println(list1);
//
//        ArrayList<Map.Entry<String, Double>> arrayList = new ArrayList<>(map.entrySet());
//        //     2) 对哈希表的值进行排序
//        //        对list进行排序，并通过Comparator传入自定义的排序规则
//        Collections.sort(arrayList, new Comparator<Map.Entry<String, Double>>() {
//            @Override
//            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
//                Double t = o1.getValue() - o2.getValue();
//                int flag = 1;
//                if (t <= 0) flag = -1;
//                return flag;
//            }
//        });
//        System.out.println(arrayList);
//    }

