package whu.edu.cs.transitnet.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import whu.edu.cs.transitnet.param.QueryPathParam;
import whu.edu.cs.transitnet.vo.QueryResultVo;

import java.util.ArrayList;
import java.util.Arrays;

@Slf4j
@Controller
public class QueryController {


    @CrossOrigin(origins = "*")
    @PostMapping("/api/query/line")
    @ResponseBody
    public QueryResultVo queryLine(@RequestBody QueryPathParam params) {
        QueryResultVo result = new QueryResultVo();
        result.setRoutes(Arrays.asList("B1", "B2", "BX29"));
        return result;
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/api/query/point")
    @ResponseBody
    public QueryResultVo queryPoint(@RequestBody QueryPathParam params) {
        QueryResultVo result = new QueryResultVo();
        result.setRoutes(new ArrayList<>());
        result.setBuses(new ArrayList<>());
        return result;
    }

}
