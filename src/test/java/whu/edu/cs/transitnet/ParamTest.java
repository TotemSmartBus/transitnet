package whu.edu.cs.transitnet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import whu.edu.cs.transitnet.service.index.HytraEngineManager;

@RunWith(SpringRunner.class)
@SpringBootTest
@MapperScan("whu.edu.cs.transitnet.*")
public class ParamTest {

    @Autowired
    HytraEngineManager hytraEngineManager;

    @Test
    public void paramTest() {
        int resolution = hytraEngineManager.getParams().getResolution();
        double[] spatialDomain = hytraEngineManager.getParams().getSpatialDomain();
        double deltaX = (spatialDomain[2] - spatialDomain[0]) / Math.pow(2.0D, (double) resolution);
        double deltaY = (spatialDomain[3] - spatialDomain[1]) / Math.pow(2.0D, (double) resolution);
        double deltaT = 86400.0D / Math.pow(2.0D, (double)resolution);

        System.out.println("[ParamTest] resolution: " + resolution);
        System.out.println("[ParamTest] spatialDomain: " + spatialDomain);
        System.out.println("[ParamTest] deltaX: " + deltaX);
        System.out.println("[ParamTest] deltaY: " + deltaY);
        System.out.println("[ParamTest] deltaT: " + deltaT);
    }

}
