package whu.edu.cs.transitnet.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import whu.edu.cs.transitnet.realtime.RealtimeService;
import whu.edu.cs.transitnet.realtime.Vehicle;

import java.util.List;
import java.util.Queue;

@Controller
public class RealTimeController {

    @Autowired
    RealtimeService realtimeService;

    @CrossOrigin
    @RequestMapping("/api/realtime/latest")
    @ResponseBody
    List<Vehicle> getLatestRealtimeData() {
        return realtimeService.getAllVehicles();
    }

    @CrossOrigin
    @RequestMapping("/api/timestemp")
    @ResponseBody
    Long getCurrentServerTimestamp() {
        return realtimeService.getCurrentTimestamp();
    }
}
