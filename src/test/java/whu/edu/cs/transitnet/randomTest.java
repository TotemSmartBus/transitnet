package whu.edu.cs.transitnet;

import edu.whu.hyk.model.PostingList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import whu.edu.cs.transitnet.service.index.HistoricalIndexBuildConfig;

import java.util.HashSet;

@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("whu.edu.cs.transitnet.*")
public class randomTest {

    @Autowired
    HistoricalIndexBuildConfig historicalIndexBuildConfig;

    @Test
    public void hashcodeTest() {
        String date = "2023-05-20";
        historicalIndexBuildConfig.buildCTList(date);
        System.out.println(PostingList.CT.size());

        HashSet<Integer> offsetNum = new HashSet<>();
        PostingList.CT.forEach((cid, idList) -> {
            String[] dzl = cid.split("@");
            String day = dzl[0];
            int zorder = Integer.parseInt(dzl[1]);
            int level = Integer.parseInt(dzl[2]);

            // 求出 cubeid 在 cubeVol 的 value 数组中的位置
            int offset = getOffset(zorder, level);
            offsetNum.add(offset);
        });

        System.out.println(offsetNum.size());

    }

    public int getOffset(int zorder, int level) {
        int base = 0;
        int resolution = 6;

        for(int i = 0; i < level; ++i) {
            base += (int)Math.pow(8.0, (double)(resolution - i));
        }

        return base + zorder;
    }


}

