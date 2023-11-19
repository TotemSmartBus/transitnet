package whu.edu.cs.transitnet.service;

import edu.whu.hyk.encoding.Decoder;
import edu.whu.hyk.encoding.Encoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import whu.edu.cs.transitnet.service.index.*;

import java.text.ParseException;
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

    public void setup(double[] ps, String d){
        spatial_range=ps;
        date=d;
    }

    public HashSet<TripId> historaical_range_search() throws ParseException {
        generatorService.setup(date);
        //师姐说先不考虑merge，那么去掉合并与更新索引这两步,下面两行的函数注释掉了主要内容，只做了最简单的工作
        generatorService.generateMap();
        generatorService.updateMergeCTandTC();
        return spatial_hytra();
    }

    public HashSet<TripId> spatial_hytra(){
        int resolution = 6;
        int[] ij_s = Decoder.decodeZ2(Encoder.encodeGrid(spatial_range[0],spatial_range[1]));
        int[] ij_e = Decoder.decodeZ2(Encoder.encodeGrid(spatial_range[2],spatial_range[3]));

        int t_s = 3600 * 0, t_e = 3600 * 24;
        double delta_t = 86400 / Math.pow(2, resolution);
        int k_s = (int)(t_s/delta_t), k_e = (int) (t_e/delta_t);

        HashSet<TripId> res = new HashSet<>();

        for (int i = ij_s[0]; i <= ij_e[0]; i++) {
            for (int j = ij_s[1]; j <= ij_e[1]; j++) {
                for (int k = k_s; k <= k_e; k++) {
                    int zOrder = Encoder.combine3(i,j,k,6);
                    if(generatorService.merge_CT_List.containsKey(new CubeId(Integer.toString(zOrder))))
                        res.addAll(generatorService.merge_CT_List.get(new CubeId(Integer.toString(zOrder))));
                }
            }
        }
        return res;

    }
}
