package whu.edu.cs.transitnet.service;

import edu.whu.hyk.encoding.Decoder;
import edu.whu.hyk.encoding.Encoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import whu.edu.cs.transitnet.service.index.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


@Service
public class HistoricalRangeService {
    @Autowired
    HytraEngineManager hytraEngineManager;

    @Autowired
    HistoricalTripIndex historicalTripIndex;

    @Autowired
    EncodeService encodeService;

    @Autowired
    DecodeService decodeService;

    @Autowired
    ShapeIndex shapeIndex;

    @Autowired
    ScheduleIndex scheduleIndex;

    @Autowired
    GeneratorService generatorService;


    private double[] spatial_range = new double[4];
    private String date="";
    private int resolution=6;
    private HashMap<Integer, HashSet<String>> planes=new HashMap<>();
    private int start_time;
    private int end_time;

    public void setup(double[] ps, String d,String st,String et){
        spatial_range=ps;
        date=d;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date dateOrigin=dateFormat.parse("2023-05-20 00:00:00");
            long seconds0 = dateOrigin.getTime()/1000;
            Date date1 = dateFormat.parse(st);
            long seconds1 = date1.getTime()/1000;
            start_time= (int) (seconds1-seconds0);
            Date date2 = dateFormat.parse(et);
            long seconds2 = date2.getTime()/1000;
            end_time= (int) (seconds2-seconds0);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public HashSet<TripId> historaical_range_search() throws ParseException {
        generatorService.setup(date);
        //师姐说先不考虑merge，那么去掉合并与更新索引这两步,下面两行的函数注释掉了主要内容，只做了最简单的工作
        generatorService.generateMap();
        generatorService.updateMergeCTandTC();
        //去掉了generateplanes，对于不合并的情况换了一种搜索方法
        return spatial_hytra();
    }
    /* 根据时空范围进行轨迹搜索的函数
    *  resolution 地图划分小cubes时分割的分辨率
    *  ij_s 2D地图x，y两轴上的最小值，限定范围
    *  ij_e 2D地图x，y两轴上的最大值，限定范围
    *  k_s,k_e 第三维，时间坐标轴上的最小最大值
    *  对于这三维确定出的一个由一系列小立方体堆叠成的大立方体，遍历每个小立方体
    *  若CubeTripList包含该Cube，则查List，将包含该Cube的TripId全部加入结果集中
    *  遍历完毕时，结果集则为所有经过查询范围的轨迹的ID
    */

    public HashSet<TripId> spatial_hytra(){
        int resolution = 6;
        int[] ij_s = Decoder.decodeZ2(Encoder.encodeGrid(spatial_range[0],spatial_range[1]));
        int[] ij_e = Decoder.decodeZ2(Encoder.encodeGrid(spatial_range[2],spatial_range[3]));

        int t_s = 3600 * 0, t_e = 3600 * 24;
        double delta_t = 86400 / Math.pow(2, resolution);
        int k_s = (int)(start_time/delta_t), k_e = (int) (end_time/delta_t);

        HashSet<TripId> res = new HashSet<>();
        //原先是使用planes进行搜索，但是不合并的情况下不需要用planes，直接三成循环对i，j，k三个维度限定的方块们进行遍历
        for (int i = ij_s[0]; i <= ij_e[0]; i++) {
            for (int j = ij_s[1]; j <= ij_e[1]; j++) {
                for (int k = k_s; k <= k_e; k++) {
                    int zOrder = encodeService.combine3(i,j,k,6);
                    if(generatorService.merge_CT_List.containsKey(new CubeId(Integer.toString(zOrder))))
                        res.addAll(generatorService.merge_CT_List.get(new CubeId(Integer.toString(zOrder))));
                }
            }
        }
        return res;

    }
}
