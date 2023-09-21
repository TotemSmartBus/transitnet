package whu.edu.cs.transitnet.controller;


import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import edu.whu.hyk.exp.RealtimeRange;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import whu.edu.cs.transitnet.param.*;
import whu.edu.cs.transitnet.realtime.Vehicle;
import whu.edu.cs.transitnet.service.HistoricalKNNExpService;
import whu.edu.cs.transitnet.service.HistoricalRangeService;
import whu.edu.cs.transitnet.service.RealtimeKNNExpService;
import whu.edu.cs.transitnet.service.RealtimeRangeService;
import whu.edu.cs.transitnet.service.index.TripId;
import whu.edu.cs.transitnet.vo.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;


@Slf4j
@Controller
public class QueryController {

    @Resource
    RealtimeKNNExpService RealtimeKNNExpService;
    @Resource
    HistoricalKNNExpService HistoricalKNNExpService;
    @Resource
    HistoricalRangeService HistoricalRangeService;
    @Resource
    RealtimeRangeService RealtimeRangeService;


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

    @CrossOrigin(origins = "*")
    @PostMapping("/api/query/traj_range_realtime")
    @ResponseBody
    public RangeRtQueryResultVo queryTraj_Range_Rt(@RequestBody QueryRangeRtParam params) {
        HashMap<String, Object> rect=new HashMap<String, Object>();
        double[] temp={params.getPoints().get(3).getLat(),
                params.getPoints().get(3).getLng(),
                params.getPoints().get(1).getLat(),
                params.getPoints().get(1).getLng()};
        RealtimeRangeService.setup(temp);
        HashSet<TripId> res=new HashSet<>();
        res=RealtimeRangeService.hytra();

        List<RangeRtQueryResultItem> list=new ArrayList<>();
        for(TripId tid:res){
            int size=RealtimeRangeService.vehiclesByTripId.get(tid).size();
            Vehicle v = RealtimeRangeService.vehiclesByTripId.get(tid).get(size - 1);
            double[] pos={v.getLat(),v.getLon()};
            RangeRtQueryResultItem item=new RangeRtQueryResultItem(tid.toString(),pos);
            list.add(item);
        }
        RangeRtQueryResultVo result=new RangeRtQueryResultVo(list);
        return result;
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/api/query/traj_knn_realtime")
    @ResponseBody
    public KnnRtQueryResultVo queryTraj_Knn_Rt(@RequestBody QueryKnnRtParam params) throws IOException, InterruptedException {
        List<QueryKnnRtParam.Point> points = params.getPoints();
        int k=params.getK_backdate()[0];
        int backdate=params.getK_backdate()[1];
        RealtimeKNNExpService.setup(points,k,backdate);
        RealtimeKNNExpService.getTopKTrips();

        List<whu.edu.cs.transitnet.service.RealtimeKNNExpService.resItem> temp_l=RealtimeKNNExpService.get_res();
        List<SimilarityQueryResultItem> temp=new ArrayList<>();
        for(int i=0;i<temp_l.size();i++){
            SimilarityQueryResultItem item=new SimilarityQueryResultItem(temp_l.get(i).getBusId(),temp_l.get(i).getSim());
            temp.add(item);
        }
        KnnRtQueryResultVo res_conv=new KnnRtQueryResultVo(temp);
        return res_conv;
    }


    @CrossOrigin(origins = "*")
    @PostMapping("/api/query/traj_knn_history")
    @ResponseBody
    public KnnHisQueryResultVo queryTraj_Knn_His(@RequestBody QueryKnnHisParam params) throws IOException, InterruptedException, ParseException {
        HistoricalKNNExpService.setup(params.getPoints(),params.getK());
        HistoricalKNNExpService.getTopKTrips();
        List<RealtimeKNNExpService.resItem> res = HistoricalKNNExpService.get_res();
        System.out.println("==============================================================\n");
        System.out.println("Rank |                   TripID                   | Similarity\n");
        for(int i=0;i<res.size();i++){
            System.out.println(res.get(i).getRank()+"        "+res.get(i).getBusId()+"              "+res.get(i).getSim());
        }
        System.out.println("==============================================================\n");

        List<SimilarityQueryResultItem> temp=new ArrayList<>();
        for(int i=0;i<res.size();i++){
            SimilarityQueryResultItem item=new SimilarityQueryResultItem(res.get(i).getBusId(),res.get(i).getSim());
            temp.add(item);
        }
        KnnHisQueryResultVo convert_res=new KnnHisQueryResultVo(temp);
        return convert_res;
    }

    @CrossOrigin(origins = "*")
    @PostMapping("/api/query/traj_range_history")
    @ResponseBody
    public RangeHisQueryResultVo queryTraj_Range_His(@RequestBody QueryRangeHisParam params) throws IOException, InterruptedException, ParseException {
        double[] temp={params.getPoints().get(3).getLat(),
                params.getPoints().get(3).getLng(),
                params.getPoints().get(1).getLat(),
                params.getPoints().get(1).getLng()};
        String day=params.getTimerange();
        HistoricalRangeService.setup(temp, day);
        HashSet<TripId> res = HistoricalRangeService.historaical_range_search();
        RangeHisQueryResultVo res_re=new RangeHisQueryResultVo(res);
        return res_re;
    }


}
