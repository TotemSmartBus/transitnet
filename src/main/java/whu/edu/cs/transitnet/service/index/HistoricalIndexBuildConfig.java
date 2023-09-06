package whu.edu.cs.transitnet.service.index;

import edu.whu.hyk.merge.Generator;
import edu.whu.hytra.core.SocketStorageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

@Component
public class HistoricalIndexBuildConfig {
//    @Autowired
    private SocketStorageManager socketStorageManager;

    @Autowired
    private HytraEngineManager hytraEngineManager;

    @Autowired
    HistoricalTripIndex historicalTripIndex;

    private final Logger log = LoggerFactory.getLogger("Cron");

    public void buildCTList(String date) {
        String seperator = hytraEngineManager.getParams().getSeperator();

        // CubeId 和 TripId 转化为普通的 String
        HashMap<CubeId, ArrayList<TripId>> cubeTripList = historicalTripIndex.getCubeTripList();
        HashMap<String, ArrayList<String>> newCubeTripList = new HashMap<>();
        for (CubeId cubeId : cubeTripList.keySet()) {
            ArrayList<String> tripList = new ArrayList<>();
            for (TripId tripId : cubeTripList.get(cubeId)) {
                tripList.add(tripId.toString());
            }
            String cubeIdDZL = date + seperator + cubeId.toString() + seperator + "0";
            newCubeTripList.put(cubeIdDZL, tripList);
        }

        // 构建 PostingList.CT
        hytraEngineManager.updateCTIndex(newCubeTripList);
    }

    public void buildHistoricalIndexConfig() throws Exception {
        // 1. 给定日期，构建CTList
        String date = "2023-05-20";
        buildCTList(date);

        // 2. 用 generator 从内存索引中构建 LSM 索引和配置
        String configPath = "/tmp";
        long tBeforeConfigGenerate = System.currentTimeMillis();
        try {
            String path = System.getProperty("user.dir");
            configPath = Generator.generateConfig().saveTo(path, date + ".index");
        } catch (IOException e) {
            log.error("[cron]Error while write config to file: " + configPath, e);
        }
        long tAfterConfigGenerate = System.currentTimeMillis();
        log.info("[cron]Generate config for {}s", String.format("%.2f", (tAfterConfigGenerate - tBeforeConfigGenerate) / 1000.0));


        long tBeforeConfigWrite = System.currentTimeMillis();
        try {
            // 传配置文件
            socketStorageManager.config(date, configPath);
        } catch (Exception e) {
            log.error("[cron]Error while read config from file:" + configPath, e);
        }
        long tAfterConfigWrite = System.currentTimeMillis();
        log.info("[cron]Write config for {}s", String.format("%.2f", (tAfterConfigWrite - tBeforeConfigWrite) / 1000.0));

        // 3. 生成要插入的 KV
        long tBeforeIndexGenerate = System.currentTimeMillis();
        // 更改了 indexMap 的定义
        HashMap<String, HashSet<Integer>> indexMap = Generator.generateRawKV();
        long tAfterIndexGenerate = System.currentTimeMillis();
        log.info("[cron]Generate Index for {}s", String.format("%.2f", (tAfterIndexGenerate - tBeforeIndexGenerate) / 1000.0));

        long tBeforeIndexWrite = System.currentTimeMillis();
        log.info(String.format("[cron]Writing %d indexes", indexMap.size()));

        // 4. 查询 LSM 状态
        try {
            String status = socketStorageManager.status();
            log.info("[cron]LSM-Status is " + status);
        } catch (Exception e) {
            log.error("[cron]Error while get status of LSM-Tree", e);
        }


        // 写入数据测试
        String key = "2023-05-20@76672@0";
        HashSet<Integer> integers = indexMap.get(key);
        for(Integer i : integers) {
            socketStorageManager.put(key, String.valueOf(i));
        }
        String key1 = "2023-05-20@76673@0";
        HashSet<Integer> integers1 = indexMap.get(key1);
        for(Integer i : integers1) {
            socketStorageManager.put(key1, String.valueOf(i));
        }
        String key2 = "2023-05-20@76676@0";
        HashSet<Integer> integers2 = indexMap.get(key2);
        for(Integer i : integers2) {
            socketStorageManager.put(key2, String.valueOf(i));
        }
        long tAfterIndexWrite = System.currentTimeMillis();

        log.info("[cron]Write index for {}s", String.format("%.2f", (tAfterIndexWrite - tBeforeIndexWrite) / 1000.0));
//        log.info("[cron]Total time is {}s", String.format("%.2f", (tAfterIndexWrite - tBeforeConfigGenerate) / 1000.0));
//        System.out.printf("[cron]Total time is %.2fs", (tAfterIndexWrite - tBeforeConfigGenerate) / 1000.0);
    }
}
