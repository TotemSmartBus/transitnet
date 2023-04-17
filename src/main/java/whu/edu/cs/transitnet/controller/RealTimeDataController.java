package whu.edu.cs.transitnet.controller;

import whu.edu.cs.transitnet.realtime.Vehicle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import whu.edu.cs.transitnet.service.RealTimeDataService;
import whu.edu.cs.transitnet.service.index.RealtimeDataIndex;
import whu.edu.cs.transitnet.vo.RealTimeDataVo;
import whu.edu.cs.transitnet.vo.SpeedDateVo;
import whu.edu.cs.transitnet.vo.SpeedQueryVo;

import javax.annotation.Resource;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class RealTimeDataController {
    @Resource
    RealTimeDataService realTimeDataService;

    @Autowired
    RealtimeDataIndex indexService;

    /**
     * Get RealTimeDataVo by curTime, the last five minutes
     *
     * @param curTime
     * @return List<RealTimeDataVo>
     */
    @CrossOrigin
    @GetMapping("/api/realTime")
    @ResponseBody
    public List<RealTimeDataVo> listRealTimeDataLastByRecordedTime(@RequestParam("curTime") String curTime) {
        Timestamp d = Timestamp.valueOf(curTime);
        return realTimeDataService.getRealTimeDataVoLastByRecordedTime(d);
    }

    /**
     * Get vehicle list by recordedTime
     *
     * @param recordedTime
     * @return List<String>
     */
    @CrossOrigin
    @GetMapping("/api/realTime/vehicle")
    @ResponseBody
    public List<String> ListVehicleIdRecordedTimeVo(@RequestParam("recordedTime") String recordedTime) {
        return realTimeDataService.getVehicleIdList(recordedTime);
    }

    /**
     * Get the
     *
     * @param vehicleId
     * @param curTime
     * @return
     */
    @CrossOrigin
    @GetMapping("/api/realTime/speed")
    @ResponseBody
    public List<SpeedDateVo> ListSpeedDateByVehicleLastSevenDay(@RequestParam("vehicleId") String vehicleId, @RequestParam("curTime") String curTime) {
        return realTimeDataService.getSpeedDateListByVehicleIdLastSevenDate(vehicleId, curTime);
    }

    @CrossOrigin
    @GetMapping("/api/realTime/routeOptions")
    @ResponseBody
    public List<String> ListRealTimeRouteOptionsByDate(@RequestParam("date") String date) {
        Date d = Date.valueOf(date);
        return realTimeDataService.getRealTimeRouteOptionsByDate(d);
    }

    @CrossOrigin
    @GetMapping("/api/realTime/tripOptions")
    @ResponseBody
    public List<String> ListRealTimeTripOptionsByDate(@RequestParam("routeId") String routeId, @RequestParam("date") String date) {
        Date d = Date.valueOf(date);
        return realTimeDataService.getRealTimeTripOptionsByDate(routeId, d);
    }

    @CrossOrigin
    @PostMapping("/api/realTime/routeSpeed")
    @ResponseBody
    public List<SpeedDateVo> ListSpeedDateVoByRouteList(@RequestBody SpeedQueryVo speedQueryVo) {
        List<String> routeList = speedQueryVo.getIdList();
        Date date = Date.valueOf(speedQueryVo.getDateStr());
        return realTimeDataService.getSpeedDateListByRouteId(routeList, date);
    }

    @CrossOrigin
    @PostMapping("/api/realTime/tripSpeed")
    @ResponseBody
    public List<SpeedDateVo> ListSpeedDateVoByTripList(@RequestBody SpeedQueryVo speedQueryVo) {
        List<String> tripList = speedQueryVo.getIdList();
        Date date = Date.valueOf(speedQueryVo.getDateStr());
        return realTimeDataService.getSpeedDateListByTripId(tripList, date);
    }

    @GetMapping("/api/realtime/query")
    @ResponseBody
    public List<String> QueryKNNVehicles(@RequestParam("lat") double lat, @RequestParam("lon") double lon, @RequestParam(value = "k", defaultValue = "10") int k) {
        List<Vehicle> vehicles = indexService.search(lat, lon, k);
        return vehicles.stream().map(Vehicle::getId).collect(Collectors.toList());
    }
}
