package whu.edu.cs.transitnet.dao;


import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("whu.edu.cs.transitnet.*")
public class RouteDaoTest {

    @Autowired
    RoutesDao routesDao;

    @Test
    public void getDistinctRouteTest() {
        int countInDB = 293;
        List<String> routes = routesDao.findDistinctRouteID();
        Assert.assertEquals(routes.size(), countInDB);
    }
}
