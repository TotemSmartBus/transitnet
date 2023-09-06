package whu.edu.cs.transitnet.service.index;

import edu.whu.hytra.EngineFactory;
import edu.whu.hytra.EngineParam;
import edu.whu.hytra.entity.Vehicle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 底层的 Hytra 引擎的一些方法，所有依赖的 Hytra 包里的方法都需要在这里做一次传递，使用 Spring 的组件方法的方式暴露出去，而不是其他地方直接用 Hytra 里的方法。
 */
@Component
public class HytraEngineManager {
    private EngineFactory engineFactory;

    @Value("${hytra.param.city}")
    private String city;

    @Value("${hytra.param.spatialDomain}")
    private double[] spatialDomain;

    @Value("${hytra.param.resolution}")
    private int resolution;

    @Value("${hytra.param.seperator}")
    private String seperator;

    @Value("${hytra.param.epsilon}")
    private int eposilon;

    @Value("${hytra.param.dataSize}")
    private int dataSize;

    private EngineParam params;

    @PostConstruct
    public void init() {
        EngineParam params = new EngineParam(
                city,
                spatialDomain,
                resolution,
                seperator,
                eposilon,
                dataSize
        );
        this.params = params;
        engineFactory = new EngineFactory(params);
    }

    public List<Integer> searchRealtime(double lat, double lon, int k) {
        return engineFactory.searchRealtime(lat, lon, k);
    }

    public void updateIndex(List<Vehicle> data) {
        engineFactory.updateIndex(data);
    }

    public void updateCTIndex(HashMap<String, ArrayList<String>> data) {
        engineFactory.updateCTIndex(data);
    }

    public EngineParam getParams() {
        return params;
    }
}
