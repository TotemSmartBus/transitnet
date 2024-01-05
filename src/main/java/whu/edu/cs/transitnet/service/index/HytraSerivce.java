package whu.edu.cs.transitnet.service.index;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import edu.whu.hyk.model.Point;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import whu.edu.cs.transitnet.lsm.SocketStorageManager;
import whu.edu.cs.transitnet.pojo.RealTimePointEntity;
import whu.edu.cs.transitnet.realtime.Vehicle;
import whu.edu.cs.transitnet.service.EncodeService;
import whu.edu.cs.transitnet.service.GeneratorService;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Configuration
public class HytraSerivce {
    @Autowired
    SocketStorageManager socketStorageManager;
    @Autowired
    GeneratorService generatorService;
    @Autowired
    EncodeService encodeService;

    private String sep="@";

    private ScheduledExecutorService executor;

    private RefreshTask refreshTask = new RefreshTask();

    private String date="yyyy-MM-dd";

    //这个数据结构用于在采集实时数据时，转化为cube-trip并存入其中，每24h将该数据结构存入LSM并重置
    //需要用到两个MAP是因为当map过大时clone的开销过高，所以不能每天午夜保存快照，同时让实时数据继续采集
    //为了保证hytraservice在处理数据时不会接触到realtimeservice正在写入的新数据，使用两个映射
    // 一个用于realtimeservice当前正在写入的数据，另一个用于hytraservice即将读取的数据。
    //每天午夜切换这两个映射的角色，让realtimeservice开始写入新的映射，而将hytraservice需要处理的数据移动到另一个映射中。
    private ConcurrentHashMap<CubeId, HashSet<TripId>> currentMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<CubeId, HashSet<TripId>> processingMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<TripId, List<Point_t>> currentList=new ConcurrentHashMap<>();
    private ConcurrentHashMap<TripId, List<Point_t>> processingList=new ConcurrentHashMap<>();
    private Lock lock = new ReentrantLock();


    @PostConstruct
    public void setScheduleTask(){
        executor = Executors.newSingleThreadScheduledExecutor();
        scheduleNextRun();
    }

    public void scheduleNextRun(){
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/New_York"));
        ZonedDateTime nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay(ZoneId.of("America/New_York"));
        long delayUntilMidnight = Duration.between(now, nextMidnight).getSeconds();

        LocalDate currentDateInNewYork = LocalDate.now(ZoneId.of("America/New_York"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String recordDate = currentDateInNewYork.format(formatter);

        date=recordDate;

        executor.schedule(() -> {
            try {
                refreshTask.run();
            } finally {
                scheduleNextRun(); // 重新安排下一次执行
            }
        }, delayUntilMidnight, TimeUnit.SECONDS);
        //delayUntilMidnight
    }

    public void buildDateLsmConfigAndInsertKV() throws Exception {
        socketStorageManager.open();
        //用 generator 从内存索引中构建 LSM 索引和配置
        String configPath = "";

        try {
            //生成配置文件
            String path = System.getProperty("user.dir");
            generatorService.setup(date,processingMap);
            configPath = generatorService.generateConfig().saveTo(path, date + ".index");
        } catch (IOException e) {
            log.error("[cron]Error while write config to file: " + configPath, e);
        }

        try {
            // 传配置文件
            socketStorageManager.config(date, configPath);
        } catch (Exception e) {
            log.error("[cron]Error while read config from file:" + configPath, e);
        }

        HashMap<Integer, HashSet<TripId>> indexMap = generatorService.generateKV();
        //检查服务状态
        checkLsmStatus();
        
        //写入数据

        long tBeforeIndexWrite = System.currentTimeMillis();

        for (Map.Entry<Integer,HashSet<TripId>> cube_trips:indexMap.entrySet()) {
            String zorder=cube_trips.getKey().toString();
            HashSet<TripId> trips=cube_trips.getValue();
            int level=0;
            String DZL_cid=date+sep+zorder+sep+level;
            for (TripId tid:trips) {
                socketStorageManager.put(DZL_cid,tid.toString());
            }
        }

        long tAfterIndexWrite = System.currentTimeMillis();
        log.info("[cron]Write index for {}s", String.format("%.2f", (tAfterIndexWrite - tBeforeIndexWrite) / 1000.0));

        //检查服务状态
        checkLsmStatus();
        socketStorageManager.close();
    }

    private void storeTodayTPandTCList() throws ParseException, IOException {
        String url = "jdbc:mysql://localhost:3306/Day_TPList";
        String user = "root";
        String password = "021212";

        //TPList
        try {
            //建立数据库连接
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, user, password);
            Statement stmt = conn.createStatement();

            //建立今天的表
            String tableName="TP_"+date.replace("-","_");
            String c_table="CREATE TABLE "+tableName+" (\n" +
                    "    trip_id VARCHAR(255) PRIMARY KEY,\n" +
                    "    points JSON\n" +
                    ");\n";
            stmt.execute(c_table);

            //遍历每个trip，生成对应的记录，插入数据库
            for (Map.Entry<TripId, List<Point_t>> entry:processingList.entrySet()) {
                TripId tid=entry.getKey();
                List<Point_t> plist=entry.getValue();

                Gson gson = new Gson();
                String json=gson.toJson(plist);

                String i_record="INSERT INTO "+tableName+" (trip_id, points) VALUES \n";
                i_record+="(";
                i_record+="\'"+tid.toString()+"\'"+",";
                i_record+="\'"+json+"\'";
                i_record+=");";

                stmt.execute(i_record);
            }
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //TCList
        HashMap<TripId, ArrayList<CubeId>> todayTCList=new HashMap<>();

        for (Map.Entry<TripId, List<Point_t>> entry : processingList.entrySet()) {
            TripId tid = entry.getKey();
            List<Point_t> points = entry.getValue();
            ArrayList<CubeId> cubeIds = new ArrayList<>();

            for(int i=0;i< points.size();i++){
                double lat = points.get(i).getLat();
                double lon = points.get(i).getLng();
                String recordedTime = points.get(i).getRecorded_time();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));
                Date parse = sdf.parse(recordedTime);
                Long time = parse.getTime();

                CubeId cubeId = encodeService.encodeCube(lat, lon, time);
                if(cubeIds.isEmpty() || cubeIds.lastIndexOf(cubeId) != (cubeIds.size() - 1)) {
                    cubeIds.add(cubeId);
                }
            }

            todayTCList.put(tid, cubeIds);
        }

        // 设置文件名和路径
        File directory = new File("src/main/resources/indexFiles/");
        if (!directory.exists()) {
            directory.mkdir();
        }
        File file = new File(directory, date + " TCList.txt");

        try (FileOutputStream fileOut = new FileOutputStream(file);
             ObjectOutputStream objectOut = new ObjectOutputStream(fileOut)) {
            // 序列化对象并写入文件
            objectOut.writeObject(todayTCList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HashMap<TripId, ArrayList<CubeId>> getTCListByDate(String date){
        Resource tripCubeResource = new ClassPathResource("indexFiles/"+date+" TCList.txt");
        if(tripCubeResource.exists()){
            HashMap<TripId, ArrayList<CubeId>> res= new HashMap<>();
            try {
                InputStream tripCubeStream = tripCubeResource.getInputStream();
                ObjectInputStream objectInput = new ObjectInputStream(tripCubeStream);

                res=((HashMap)objectInput.readObject());
                objectInput.close();
                tripCubeStream.close();
            }
            catch (IOException obj1) {
                obj1.printStackTrace();
            }
            catch (ClassNotFoundException obj2) {
                System.out.println("[HISTORICALTRIPINDEX] Class not found");
                obj2.printStackTrace();
            }
            return res;
        }
        return null;
    }


    public void checkLsmStatus(){
        try {
            String status = socketStorageManager.status();
            log.info("[cron]LSM-Status is " + status);
        } catch (Exception e) {
            log.error("[cron]Error while get status of LSM-Tree", e);
        }
    }

    private void refresh() throws Exception {
        switchMaps();
        buildDateLsmConfigAndInsertKV();
        storeTodayTPandTCList();
        processingMap.clear();
    }


    private void switchMaps(){
        //switch时不允许两个hashmap改变
        lock.lock();
        try {
            ConcurrentHashMap<CubeId, HashSet<TripId>> temp = processingMap;
            processingMap = currentMap;
            currentMap = temp;
            currentMap.clear();

            ConcurrentHashMap<TripId, List<Point_t>> temp1 = processingList;
            processingList = currentList;
            currentList = temp1;
            currentList.clear();
        } finally {
            lock.unlock();
        }
        // 清空新的currentMap，为realtimeservice接下来的写入做准备
    }

    public void updateCMap(Vehicle v){
        TripId tid=new TripId(v.getTripID());
        double lat=v.getLat();
        double lng=v.getLon();
        Long time=v.getRecordedTime();
        CubeId cid=encodeService.encodeCube(lat,lng,time);

        if(!currentMap.containsKey(cid)){
            HashSet<TripId> tid_set=new HashSet<>();
            tid_set.add(tid);
            currentMap.put(cid,tid_set);
        }else{
            currentMap.get(cid).add(tid);
        }
    }

    public void updatePList(Vehicle v){
        TripId tid=new TripId(v.getTripID());
        double lat=v.getLat();
        double lng=v.getLon();
        Long time=v.getRecordedTime();

        Instant instant = Instant.ofEpochSecond(time);
        ZoneId newYorkZoneId = ZoneId.of("America/New_York");
        ZonedDateTime newYorkDateTime = instant.atZone(newYorkZoneId);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = newYorkDateTime.format(formatter);

        Point_t p=new Point_t(lat,lng,formattedDateTime);

        if(!currentList.containsKey(tid)){
            List<Point_t> points=new ArrayList<>();
            points.add(p);
            currentList.put(tid,points);
        }else{
            currentList.get(tid).add(p);
        }
    }

    public SocketStorageManager getConn(){
        return socketStorageManager;
    }

    public HashMap<TripId, ArrayList<RealTimePointEntity>>findAllPointsForTripids(HashSet<TripId> tripIds,String date){
        String url = "jdbc:mysql://localhost:3306/Day_TPList";
        String user = "root";
        String password = "021212";

        HashMap<TripId, ArrayList<RealTimePointEntity>> res=new HashMap<>();

        try {
            //建立数据库连接
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, user, password);
            Statement stmt = conn.createStatement();

            //遍历每个trip，生成对应的TripId, ArrayList<RealTimePointEntity>条目，放入结果的hashmap
            String tableName="TP_"+date.replace("-","_");
            for (TripId tid:tripIds) {
                String query = "SELECT points FROM " + tableName + " WHERE trip_id = '" + tid.toString() + "'";
                ResultSet rs = stmt.executeQuery(query);
                if (rs.next()) {
                    String pointsJson = rs.getString("points");
                    // 解析JSON并添加到结果中
                    ArrayList<RealTimePointEntity> points = parsePointsJson(pointsJson);
                    res.put(tid, points);
                }
                rs.close();
            }

            //关闭数据库连接
            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public ArrayList<RealTimePointEntity> parsePointsJson(String json){
        Gson gson = new Gson();
        JsonArray jsonArray = JsonParser.parseString(json).getAsJsonArray();
        ArrayList<RealTimePointEntity> points = new ArrayList<>();

        for (JsonElement element : jsonArray) {
            Double lat = gson.fromJson(element.getAsJsonObject().get("lat"), Double.class);
            Double lon = gson.fromJson(element.getAsJsonObject().get("lng"), Double.class);
            String recordedTime = gson.fromJson(element.getAsJsonObject().get("recorded_time"), String.class);

            points.add(new RealTimePointEntity("default", "default", lat, lon, recordedTime));
        }

        return points;
    }

    private class RefreshTask implements Runnable {
        @Override
        public void run() {
            try {
                refresh();
            } catch (Exception ex) {
                log.error("error while trying to store oneDayCubeTripList to Lsm", ex);
            }
        }
    }

    private class Point_t{
        private double lat;
        private double lng;
        private String recorded_time;

        public Point_t(double lat, double lng, String formattedDateTime) {
            this.lat=lat;
            this.lng=lng;
            this.recorded_time=formattedDateTime;
        }

        public double getLat() {
            return lat;
        }

        public double getLng() {
            return lng;
        }

        public String getRecorded_time() {
            return recorded_time;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public void setLng(double lng) {
            this.lng = lng;
        }

        public void setRecorded_time(String recorded_time) {
            this.recorded_time = recorded_time;
        }
    }
}
