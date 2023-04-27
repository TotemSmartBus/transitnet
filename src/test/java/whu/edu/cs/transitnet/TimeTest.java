package whu.edu.cs.transitnet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import whu.edu.cs.transitnet.dao.StopsDao;
import whu.edu.cs.transitnet.pojo.StopsEntity;
import whu.edu.cs.transitnet.service.StopsService;
import whu.edu.cs.transitnet.vo.StopsVo;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("whu.edu.cs.transitnet.*")
public class TimeTest {

    @Autowired
    StopsDao stopsDao;

    @Test
    public void timeFormatTest() {

        String tripId = "YU_D1-Sunday-141000_MISC_777";
        List<StopsVo> stopsVos = stopsDao.findAllByTripId(tripId);

        System.out.println(stopsVos.size());
//
//        List<StopsEntity> stopsEntities = stopsDao.findAll();
//        System.out.println(stopsEntities.size());
    }
}
