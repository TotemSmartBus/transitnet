package whu.edu.cs.transitnet.service;

import java.io.IOException;
import java.sql.*;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import whu.edu.cs.transitnet.realtime.RealtimeService;
import whu.edu.cs.transitnet.utils.GeoUtil;
import whu.edu.cs.transitnet.vo.StopsVo;

@Service
public class StopSelectionService {

    @Autowired
    RealtimeService realtimeService;

//    public List<StopsVo> getStops(String routeId, Integer direction) {
//
//    }
    public void stopSelection() {
        // 1、用户指定要搭乘的公交的行驶路线的id-routeId以及路线方向direction（0/1）
        String routeId = "1";
        int direction = 0;
        // 用户的位置 - lat lon
        ArrayList<Double> userLatAndLon = new ArrayList<>();
        // 用户的步行速度 - m/min 平均5km/h -> 83.333m/min
        Double walkSpeed = 83.0;

        // stop_id lat lon order
        ArrayList<String> allStopIds = new ArrayList<>(); // all stop_ids
        HashMap<String, ArrayList<Double>> stopsWithLatAndLon = new HashMap<>(); // stop_id - [stop_lat, stop_lon]
        HashMap<String, Integer> stopsWithOrder = new HashMap<>(); // stop_id - its order in a trip

        // 2、根据routeId查找该路线经过的所有公交站点和相应的经纬度坐标
        // 连接sqlite数据库
        Connection c =null;
        // Statement stmt = null;
        PreparedStatement pstmt = null; // 查询所有stop_ids
        PreparedStatement pstmt1 = null; // 查询每个stop_id的stop_lat和stop_lon
        try {
            Class.forName("org.sqlite.JDBC");
            // 打开数据库；这一行要使用我们搭建SQLite时的url
            c = DriverManager.getConnection("jdbc:sqlite:C://Windows//System32//gtfsdb//bin//gtfs.db");
            System.out.println("Opened database successfully");

            // stmt = c.createStatement();
            // 这里执行查询语句
            // ResultSet rs = stmt.executeQuery( "SELECT stop_id FROM route_stops WHERE route_id = '%s', routeId;");
            pstmt = c.prepareStatement("SELECT stop_id FROM route_stops WHERE route_id = ? AND direction_id = ?;");
            pstmt.setString(1, routeId);
            pstmt.setInt(2, direction);

            ResultSet rs = pstmt.executeQuery();
            pstmt1 = c.prepareStatement("SELECT stop_lat, stop_lon FROM stops WHERE stop_id = ?;");

            int order = 0;
            while(rs.next()) {
                order++; // 站点经过的顺序

                String s = rs.getString("stop_id");
                System.out.println(s);

                // 存储所有stop_id
                allStopIds.add(s);

                // 存储每个stop_id对应的顺序
                stopsWithOrder.put(s, order);

                // 查询每个stop_id的经纬度
                pstmt1.setString(1, s);
                ResultSet rs1 = pstmt1.executeQuery();
                Double lat = rs1.getDouble("stop_lat");
                Double lon = rs1.getDouble("stop_lon");
                // System.out.println(lat);
                // System.out.println(lon);

                // 把stop_id - [lat, lon]存到哈希表里
                ArrayList<Double> latAndLon = new ArrayList<>();
                latAndLon.add(lat);
                latAndLon.add(lon);
                stopsWithLatAndLon.put(s, latAndLon);

                // 打印哈希表看看
                System.out.println(stopsWithLatAndLon.get(s));
            }
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
        System.out.println("Operation done successfully");

        // 3、找到用户左右两侧最近的公交站点
        // 目前版本：找到离用户最近的两个公交站点，没有区分左右
        // 但是按照常识，应该确实位于左右，再查资料看看
        // 暴力算法：计算所有公交站点和用户位置的距离
        // TODO: 算法优化（剪枝？）
        Double userLat = 45.522246;
        Double userLon = -122.676453;
        userLatAndLon.add(userLat);
        userLatAndLon.add(userLon);

        // 求出所有公交站点离用户的距离并存到哈希表里
        HashMap<String, Double> stopsWithDistance = new HashMap<>();
        for (String id: allStopIds) {
            ArrayList<Double> itsLatAndLon = stopsWithLatAndLon.get(id);
            // 调用计算两点距离的方法
            Double dis = GeoUtil.distance(userLatAndLon.get(0), itsLatAndLon.get(0), userLatAndLon.get(1), itsLatAndLon.get(1));
            stopsWithDistance.put(id, dis);
        }

        // 将哈希表按照距离进行从小到大的排序
        //     1) 将entrySet放入List集合中
        ArrayList<Map.Entry<String, Double>> arrayList = new ArrayList<>(stopsWithDistance.entrySet());
        //     2) 对哈希表的值进行排序
        //        对list进行排序，并通过Comparator传入自定义的排序规则
        Collections.sort(arrayList, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                Double t = o1.getValue()-o2.getValue();
                int flag = 1;
                if (t <= 0) flag = -1;
                return flag;
            }
        });
        //     3) 用迭代器对list中的键值对元素进行遍历

//        Iterator<Map.Entry<String, Double>> iterator = arrayList.iterator();
//        while (iterator.hasNext()) {
//            Map.Entry<String, Double> ite = iterator.next();
//            System.out.println(ite.getKey() + ": " + ite.getValue());
//        }

        //        取出离用户最近的两个站点
        HashMap<String, Double> top2 = new HashMap<>();
        String key1 = arrayList.get(0).getKey();
        String key2 = arrayList.get(1).getKey();
        top2.put(key1, arrayList.get(0).getValue());
        top2.put(key2, arrayList.get(1).getValue());
        System.out.println(top2);

        // 判断两个站点的先后顺序
        String stopReachedFirst;
        String stopReachedSecond;
        if (stopsWithOrder.get(key1) < stopsWithOrder.get(key2)) {
            stopReachedFirst = key1;
            stopReachedSecond = key2;
        } else {
            stopReachedFirst = key2;
            stopReachedSecond = key1;
        }

        // 4、获取该routeId下的所有公交实时位置
        //   站点A -> 站点B
        //   找到位于这AB站点中间的公交和将要到达站点A的公交
        //   TODO: 实时数据获取和解析

        // 在路上的tripid序列
        ArrayList<String> realtimeTripIds = new ArrayList<>();

        // 在路上的tripid序列和对应的经纬度位置信息
        HashMap<String, ArrayList<Double>> tripsWithLatAndLon = new HashMap<>();
        // 在路上的tripid到达两个站点的时间
        HashMap<String, Double> stopRFBusTime = new HashMap<>();
        HashMap<String, Double> stopRSBusTime = new HashMap<>();

        // 判断tripid是否已经经过了这两个站点或者其中一个
        // realtimeService.

        // 5、根据设计的算法决定用户应该去往哪个站点
        //    已经获取到一连串direction=?的tripid以及实时位置
        //    首先确定用户步行至两个站点的时间
        //    然后计算出分别最快能到达两个站点的公交id

        // 计算出用户步行至两个站点的时间
        Double stopRFWalkTime = stopsWithDistance.get(stopReachedFirst) / walkSpeed;
        Double stopRSWalkTime = stopsWithDistance.get(stopReachedSecond) / walkSpeed;



    }

}