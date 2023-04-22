package whu.edu.cs.transitnet.service.index;

import edu.whu.hyk.Engine;
import edu.whu.hyk.merge.Generator;
import edu.whu.hytra.core.StorageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

@Service
@EnableScheduling
public class HytraHistoricalIndex {

    @Value("${transitnet.index.enable}")
    private boolean indexEnable;

    @Autowired
    private StorageManager storageManager;

    @Autowired
    SimpleDateFormat formatter;

    private final Logger log = LoggerFactory.getLogger("Cron");

    @Scheduled(cron = "${scheduled.historicalIndex}")
    public void buildHistoricalIndex() {
        log.info("[cron]Running cron");
        if(!indexEnable) {
            log.info("[cron]Index is not enabled, skipped.");
            return;
        }
        // 1. 获取日期 key
        Date date = getDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        // 2. 用 generator 从内存索引中构建 LSM 索引和配置
        String dateKey = dateFormat.format(date);
        String datetimeKey = formatter.format(date);
        String configPath = "/tmp";
        long tBeforeConfigGenerate = System.currentTimeMillis();
        try {
            String path = System.getProperty("user.dir");
            configPath = Generator.generateConfig().saveTo(path, datetimeKey + ".index");
        } catch (IOException e) {
            log.error("[cron]Error while write config to file:" + configPath, e);
        }
        long tAfterConfigGenerate = System.currentTimeMillis();
        log.info("[cron]Generate config for {}s", String.format("%.2f", (tAfterConfigGenerate - tBeforeConfigGenerate) / 1000.0));
        long tBeforeIndexGenerate = System.currentTimeMillis();
        HashMap<String, Integer> indexMap = Generator.generateKV();
        long tAfterIndexGenerate = System.currentTimeMillis();
        log.info("[cron]Generate Index for {}s", String.format("%.2f", (tAfterIndexGenerate - tBeforeIndexGenerate) / 1000.0));
        // 3. 写入索引
        long tBeforeConfigWrite = System.currentTimeMillis();
        try {
            storageManager.config(dateKey, configPath);
        } catch (Exception e) {
            log.error("[cron]Error while read config from file:" + configPath, e);
        }
        long tAfterConfigWrite = System.currentTimeMillis();
        log.info("[cron]Write config for {}s", String.format("%.2f", (tAfterConfigWrite - tBeforeConfigWrite) / 1000.0));
        // 4. 写入数据
        long tBeforeIndexWrite = System.currentTimeMillis();
        log.info(String.format("[cron]Writing %d indexes", indexMap.size()));
        // matrix
        // 5. 查询 LSM 状态
        try {
            String status = storageManager.status();
            log.info("[cron]LSM-Status is " + status);
        } catch (Exception e) {
            log.error("[cron]Error while get status of LSM-Tree", e);
        }
        indexMap.forEach((key, value) -> {
            try {
                storageManager.put(key, String.valueOf(value));
            } catch (Exception e) {
                log.error(String.format("[cron]Error while write index for [%s, %d]", key, value), e);
            }
        });
        long tAfterIndexWrite = System.currentTimeMillis();
        log.info("[cron]Write index for {}s", String.format("%.2f", (tAfterIndexWrite - tBeforeIndexWrite) / 1000.0));
        log.info("[cron]Total time is {}s", String.format("%.2f", (tAfterIndexWrite - tBeforeConfigGenerate) / 1000.0));
        System.out.printf("[cron]Total time is %.2fs", (tAfterIndexWrite - tBeforeConfigGenerate) / 1000.0);
    }

    private Date getDate() {
        // 找到最新的 datetime - 60s,获取前一天的日期
        Date date = new Date(System.currentTimeMillis() - 60 * 1000);

        String datetime = formatter.format(date);
        if (Engine.trajDataBase.size() > 0) {
            datetime = Engine.trajDataBase.entrySet().stream().findFirst().get().getValue().get(0).getDatetime();
            try {
                date = formatter.parse(datetime);
            } catch (ParseException e) {
                log.error("[cron]Data format is wrong: " + datetime, e);
            }
        }
        return date;
    }
}
