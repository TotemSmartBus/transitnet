package whu.edu.cs.transitnet.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import whu.edu.cs.transitnet.param.QueryPathParam;
import whu.edu.cs.transitnet.param.QueryTrajectoryParam;
import whu.edu.cs.transitnet.vo.SimilarityQueryResultItem;
import whu.edu.cs.transitnet.vo.SimilarityQueryResultVo;

import java.util.ArrayList;
import java.util.Arrays;

@Slf4j
@Controller
public class QueryController {

    @CrossOrigin(origins = "*")
    @PostMapping("/api/query/point")
    @ResponseBody
    public SimilarityQueryResultVo queryPoint(@RequestBody QueryPathParam params) {
        SimilarityQueryResultVo result = new SimilarityQueryResultVo();
        SimilarityQueryResultItem item1 = new SimilarityQueryResultItem("B1", 0.9);
        SimilarityQueryResultItem item2 = new SimilarityQueryResultItem("B2", 0.5);
        SimilarityQueryResultItem item3 = new SimilarityQueryResultItem("BX29", 0.2);
        result.setRoutes(Arrays.asList(item1, item2, item3));
        result.setBuses(new ArrayList<>());
        return result;
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/api/query/trajectory")
    @ResponseBody
    public SimilarityQueryResultVo queryTrajectory(@RequestBody QueryTrajectoryParam params) {
        SimilarityQueryResultVo result = new SimilarityQueryResultVo();
        SimilarityQueryResultItem item1 = new SimilarityQueryResultItem("B1", 0.9);
        SimilarityQueryResultItem item2 = new SimilarityQueryResultItem("B2", 0.5);
        SimilarityQueryResultItem item3 = new SimilarityQueryResultItem("BX29", 0.2);
        result.setRoutes(Arrays.asList(item1, item2, item3));
        result.setBuses(new ArrayList<>());
        return result;
    }

}
