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
        planes = generatorService.generatePlanes();
        return spatial_hytra(planes);
    }

    public HashSet<TripId> spatial_hytra(HashMap<Integer, HashSet<String>> planes){
        //decode spatial range
        int[] ij_s = Decoder.decodeZ2(Encoder.encodeGrid(spatial_range[0],spatial_range[1]));
        int[] ij_e = Decoder.decodeZ2(Encoder.encodeGrid(spatial_range[2],spatial_range[3]));

        //encode time range
        int t_s = 3600 * 9, t_e = 3600 * 13;
        double delta_t = 86400 / Math.pow(2, resolution);
        int k_s = (int)(t_s/delta_t), k_e = (int) (t_e/delta_t);

        //union
        HashSet<String> planes_i = new HashSet<>(), planes_j = new HashSet<>(), planes_k = new HashSet<>();
        for(int i = ij_s[0]; i <= ij_e[0]; i++) {
            if(planes.containsKey(i))
                planes_i.addAll(planes.get(i));
        }
        for(int j = ij_s[1] + (int) Math.pow(2,resolution); j <= ij_e[1] + (int) Math.pow(2,resolution); j++) {
            if(planes.containsKey(j))
                planes_j.addAll(planes.get(j));
        }
        for(int k = k_s + (int) Math.pow(2,resolution+1); k <= k_e + (int) Math.pow(2,resolution+1); k++) {
            if(planes.containsKey(k))
                planes_k.addAll(planes.get(k));
        }

        //intersection
        planes_i.retainAll(planes_j);
        planes_i.retainAll(planes_k);

        HashSet<TripId> res = new HashSet<>();
        planes_i.forEach(cid -> {
            String []items=cid.split("@");
            CubeId cid_zorder=new CubeId(items[1]);
            if(generatorService.merge_CT_List.containsKey(cid_zorder)){
                res.addAll(generatorService.merge_CT_List.get(cid_zorder));
            }
        });
        return res;
    }
}
