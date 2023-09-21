package whu.edu.cs.transitnet.service;

import org.apache.ibatis.javassist.compiler.ast.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import whu.edu.cs.transitnet.dao.TripsDao;
import whu.edu.cs.transitnet.param.QueryKnnRtParam;
import whu.edu.cs.transitnet.pojo.TripsEntity;
import whu.edu.cs.transitnet.realtime.RealtimeService;
import whu.edu.cs.transitnet.realtime.Vehicle;
import whu.edu.cs.transitnet.service.index.*;

import java.io.*;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class RealtimeKNNExpService {

    @Autowired
    RealtimeService realtimeService;

    @Autowired
    HytraEngineManager hytraEngineManager;

    @Autowired
    EncodeService encodeService;

    @Autowired
    DecodeService decodeService;

    @Autowired
    ShapeIndex shapeIndex;

    @Autowired
    ScheduleIndex scheduleIndex;
    // 这个k指的是取前k个shape
    int kShape = 15;

    private List<QueryKnnRtParam.Point> points;
    private int top_k;
    private int backdate;
    private int length;
    private List<TripId> topkTripsLOC= new ArrayList<>();;
    private List<resItem>output_query_res;

    public static class resItem {
        private int rank;
        private String busId;
        private double sim;

        public resItem( int rank,String first, double second) {
            this.rank=rank;
            this.busId = first;
            this.sim = second;
        }
        public int getRank(){
            return rank;
        }

        public void setRank(int r){
            this.rank=r;
        }

        public String getBusId() {
            return busId;
        }

        public void setBusId(String first) {
            this.busId = first;
        }

        public double getSim() {
            return sim;
        }

        public void setSim(double second) {
            this.sim = second;
        }
    }



    public void setup(List<QueryKnnRtParam.Point> points_in, int top_k_in, int backdate_in){
        points=points_in;
        top_k=top_k_in;
        backdate=backdate_in;
        vehiclesByTripId=realtimeService.getVehiclesByTripId();
    }


    // trip - cube  map做操作 删掉list
    private HashMap<TripId, ArrayList<GridId>> tripGridList = new HashMap<>();
    // 存 50 个值
    ConcurrentHashMap<TripId, ArrayList<Vehicle>> vehiclesByTripId  = new ConcurrentHashMap<>();


    @Autowired
    TripsDao tripsDao;
    /**
     * 不用 schedule 做筛选；user: [start_time, end_time]
     * @param userTripId
     * @param userGridList
     * @return
     */
    public ArrayList<TripId> filterTripList(TripId userTripId, ArrayList<GridId> userGridList) {
        ArrayList<TripId> filteredTripList = new ArrayList<>();
        // top-k shapes -> trips of top-k shapes
        ArrayList<TripId> tripIds = shapeIndex.getTripsOfTopKShapes(null, userGridList, kShape);
        return tripIds;
    }

    /**
     * 使用 shape - trip 一层索引
     * @param userTripId
     * @param userGridList
     * @throws InterruptedException
     */
    public void getTripIdCubeList(TripId userTripId, ArrayList<GridId> userGridList) throws InterruptedException {
        tripGridList = new HashMap<>();

        // 首先根据形状过滤，得到top-k个形状最相似的车次的tripid
        ArrayList<TripId> filteredTripList = filterTripList(userTripId, userGridList);
        if (filteredTripList.isEmpty()) {
            return;
        }

        System.out.println("[REALTIMEKNNEXPSERVICE] " + "size of filtered trips: " + filteredTripList.size());

        List<TripId> vehiclesByTripIdkeyList = new ArrayList<>(vehiclesByTripId.keySet());
        List<TripId> filteredTripList_temp=new ArrayList<>(filteredTripList);
        //test use
//        vehiclesByTripIdkeyList.add(new TripId("test_for_right"));
//        filteredTripList_temp.add(new TripId("test_for_right"));
        vehiclesByTripIdkeyList.retainAll(filteredTripList_temp);
//
//        for(int i=0;i<vehiclesByTripIdkeyList.size();i++){
//            TripId v = vehiclesByTripIdkeyList.get(i);
//            for(int j=0;j<filteredTripList_temp.size();j++){
//                TripId f=filteredTripList_temp.get(j);
//                if(v.toString()==f.toString()){
//                    System.out.println("has common!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//                }
//            }
//        }
        System.out.println(vehiclesByTripIdkeyList.size());

        if(vehiclesByTripIdkeyList.size()==0){
            return;
        }

        //对于过滤出的每个车次
        for (TripId tripId : filteredTripList) {
            //在实时车次车辆信息hash表中取出这趟车次的位置信息
            if (vehiclesByTripId.containsKey(tripId) && vehiclesByTripId.get(tripId) != null) {
                ArrayList<Vehicle> vehicles = new ArrayList<>();
                vehicles = vehiclesByTripId.get(tripId);
                ArrayList<GridId> gridIds = new ArrayList<>();

                // 只取最实时的 length 个采样点值
                int size_l=0;
                if(length>vehicles.size()){
                    size_l=vehicles.size();
                }else{
                    size_l=length;
                }

                for (int i = vehicles.size()-size_l; i <vehicles.size(); i++) {
                    Vehicle v= vehicles.get(i);
                    GridId gridId = encodeService.getGridID(v.getLat(), v.getLon());
                    if(gridIds.isEmpty() || gridIds.lastIndexOf(gridId) != (gridIds.size() - 1)) {
                        gridIds.add(gridId);
                    }
                }
                tripGridList.put(tripId, gridIds);
            }
        }
    }

    // trip_id - similarity
    HashMap<TripId, Double> tripSimListLOCS = new HashMap<>();
    HashMap<TripId, Integer> tripSimListLOC = new HashMap<>();




    /**
     * 获取 Top-k trip （用户自己指定k，不是前面定义的变量k）
     * @param
     * @throws IOException
     * @throws InterruptedException
     */
    public void getTopKTrips() throws IOException, InterruptedException {

        // 轨迹长度
        length = backdate/30;
        //int sleepTime = backdate*1000;

        // 50 * 30 * 1000 ms
        //Thread.sleep(sleepTime);

        //将points转化为grids
        ArrayList<GridId> userGridList = new ArrayList<>();
        for(int i=0;i<points.size();i++){
            double lat=points.get(i).getLat();
            double lng=points.get(i).getLng();
            GridId Grid=encodeService.getGridID(lat,lng);
            userGridList.add(Grid);
        }

        // 进行knn查询，先做筛选，实时knn----------先筛选
        int choice = 1;
        switch (choice) {
            case 1:
                // 一层索引
                getTripIdCubeList(null, userGridList);
                break;
            default:
                break;
        }

        System.out.println("[REALTIMEKNNEXPSERVICE] size of tripGridList: " + tripGridList.size());
        Set<TripId> keySet = tripGridList.keySet();

        for (TripId tripId1 : keySet) {
            List<GridId> intersection0 = new ArrayList<>(userGridList);
            intersection0.retainAll(tripGridList.get(tripId1));
            List<GridId> intersection1 = intersection0.stream().distinct().collect(Collectors.toList());
            tripSimListLOC.put(tripId1, intersection1.size());
            }


        // topk LOC
        List<TripId> topTripsLOC = tripSimListLOC.entrySet().stream().sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).map(Map.Entry::getKey).collect(Collectors.toList());
        if(topTripsLOC.size() >= top_k) {
            topkTripsLOC = topTripsLOC.subList(0, top_k);
        } else {
            topkTripsLOC = topTripsLOC;
        }
    }

    public List<resItem> get_res(){
        if(topkTripsLOC.isEmpty()){
            return null;
        }
        output_query_res=new ArrayList<>();
        int min=Math.min(top_k,topkTripsLOC.size());
        for(int i=0;i<min;i++){
            TripId temp_tid=topkTripsLOC.get(i);
            ArrayList<Vehicle> temp_vl=vehiclesByTripId.get(temp_tid);
            String busid=temp_vl.get(temp_vl.size()-1).getId();
            double s=tripSimListLOC.get(temp_tid);
            resItem temp=new resItem(i+1,busid,s);
            output_query_res.add(temp);
        }
        return  output_query_res;
    }
}
