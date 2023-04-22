package whu.edu.cs.transitnet.controller;

import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import whu.edu.cs.transitnet.service.MapVService;
import whu.edu.cs.transitnet.vo.RoutesVo;

import javax.annotation.Resource;
import java.sql.Date;
import java.util.List;

@Controller
public class MapVController {
    @Resource
    MapVService mapVService;

    /**
     * Get the trajectory of route By routeId and tripId, including route info
     * @param routeId
     * @param tripId
     * @return routesVo
     */
    @CrossOrigin
    @GetMapping("/api/mapv")
    @ResponseBody
    public RoutesVo listTrajByRouteIdTripId(@RequestParam(value = "routeId") String routeId, @RequestParam(value = "tripId") String tripId) {
        return mapVService.getRouteVoByRouteIdAndTripId(routeId, tripId);
    }

    /**
     * Get trajectories of all routes (first in DataBase)
     * for visualization test
     * @return List<RoutesVo>
     */
    @CrossOrigin
    @GetMapping("/api/mapv/transitnetwork")
    @ResponseBody
    public List<RoutesVo> listTrajs() {
        return mapVService.getRoutesVoOriginList();
    }

    /**
     * Get trajectories by timespan
     * Accurate to the day
     * @param startDate
     * @param endDate
     * @return List<RoutesVo>
     */
    @CrossOrigin
    @GetMapping("/api/mapv/timespan")
    @ResponseBody
    public List<RoutesVo> listTrajsByTimeSpan(@Param("startDate") String startDate, @Param("endDate") String endDate) {
        Date sD = Date.valueOf(startDate);
        Date eD = Date.valueOf(endDate);
        return mapVService.getRoutesVoByTimeSpan(sD, eD);
    }
}
