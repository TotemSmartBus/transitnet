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
import whu.edu.cs.transitnet.pojo.RealTimePointEntity;
import whu.edu.cs.transitnet.realtime.Vehicle;
import whu.edu.cs.transitnet.service.HistoricalKNNExpService;
import whu.edu.cs.transitnet.service.HistoricalRangeService;
import whu.edu.cs.transitnet.service.RealtimeKNNExpService;
import whu.edu.cs.transitnet.service.RealtimeRangeService;
import whu.edu.cs.transitnet.service.index.HistoricalTripIndex;
import whu.edu.cs.transitnet.service.index.TripId;
import whu.edu.cs.transitnet.vo.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
    @Resource
    HistoricalTripIndex HistoricalTripIndex;


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
        List<tripPoints> ts=new ArrayList<>();
        for(TripId tid:res){
            ArrayList<Vehicle> vs = RealtimeRangeService.vehiclesByTripId.get(tid);
            int size=vs.size();
            Vehicle v=vs.get(size-1);
            double[] pos={v.getLat(),v.getLon()};
            RangeRtQueryResultItem item=new RangeRtQueryResultItem(tid.toString(),pos);
            list.add(item);

            tripPoints tps=new tripPoints(tid.toString(),vs);
            ts.add(tps);
        }
        RangeRtQueryResultVo result=new RangeRtQueryResultVo(list,ts);
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
        List<tripPoints> ts=new ArrayList<>();
        for (SimilarityQueryResultItem it:temp) {
            String tid=it.getId();
            ArrayList<Vehicle> vs = RealtimeKNNExpService.vehiclesByTripId.get(new TripId(tid));
            tripPoints tps=new tripPoints(tid,vs);
            ts.add(tps);
        }
        KnnRtQueryResultVo res_conv=new KnnRtQueryResultVo(temp,ts);
        return res_conv;
    }


    @CrossOrigin(origins = "*")
    @PostMapping("/api/query/traj_knn_history")
    @ResponseBody
    public KnnHisQueryResultVo queryTraj_Knn_His(@RequestBody QueryKnnHisParam params) throws IOException, InterruptedException, ParseException {
        //所有时间设置为2023-05-20 xx:xx:xx
        List<QueryKnnHisParam.Point> ListP = new ArrayList<>();
        ListP=params.getPoints();
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


        //存储时间字符串
        ArrayList<String> times=new ArrayList<>();
        // 获取当前时间
        LocalTime currentTime = LocalTime.now();
        // 定义时间格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        // 循环
        for (int i = 0; i <ListP.size(); i++) {
            // 增加30秒
            LocalTime currentTime1 = currentTime.plusSeconds(30 * i);
            // 格式化并输出
            String formattedTime = currentTime1.format(formatter);
            times.add(i,formattedTime);
        }

        int w=0;
        for (QueryKnnHisParam.Point point : ListP) {
            try {
                // 解析原始的日期字符串
                Date date;
                // 设置日期为"2023-05-20"
                date = outputFormat.parse("2023-05-20 " + times.get(w));
                w++;
                // 将修改后的日期设置回点对象
                point.setTime(outputFormat.format(date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            System.out.println("changed P data: "+point.getLat()+" "+point.getLng()+" "+point.getTime());
        }
        HistoricalKNNExpService.setup(ListP,params.getK());
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
        List<tripPoints> ts=new ArrayList<>();
        HashMap<TripId, ArrayList<RealTimePointEntity>> Tplist=HistoricalTripIndex.getTripPointList();
        for (SimilarityQueryResultItem it:temp) {
            String tid=it.getId();
            ArrayList<RealTimePointEntity> vs = Tplist.get(new TripId(tid));
            if(vs!=null){
                tripPoints tps=new tripPoints(tid,vs,0);
                ts.add(tps);
            }
        }
        KnnHisQueryResultVo convert_res=new KnnHisQueryResultVo(temp,ts);
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
        String s_time=params.getTimerange1();
        String e_time=params.getTimerange2();
        //HistoricalRangeService.setup(temp, day);
        HistoricalRangeService.setup(temp, "2023-05-20",s_time,e_time);
        HashSet<TripId> res = HistoricalRangeService.historaical_range_search();

        List<tripPoints> ts=new ArrayList<>();
        HashMap<TripId, ArrayList<RealTimePointEntity>> Tplist=HistoricalTripIndex.getTripPointList();
        for (TripId tid:res) {
            ArrayList<RealTimePointEntity> vs = Tplist.get(tid);
            if(vs!=null){
                tripPoints tps=new tripPoints(tid.toString(),vs,0);
                ts.add(tps);
            }
        }
        RangeHisQueryResultVo res_re=new RangeHisQueryResultVo(res,ts);
        return res_re;
    }


}
