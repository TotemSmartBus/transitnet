package whu.edu.cs.transitnet.service;

import edu.whu.hyk.exp.RealtimeRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.github.davidmoten.rtree.Entry;
import com.github.davidmoten.rtree.RTree;
import com.github.davidmoten.rtree.geometry.Geometries;
import edu.whu.hyk.encoding.Decoder;
import edu.whu.hyk.encoding.Encoder;
import edu.whu.hyk.model.Point;
import edu.whu.hyk.util.GeoUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import whu.edu.cs.transitnet.realtime.RealtimeService;
import whu.edu.cs.transitnet.realtime.Vehicle;
import whu.edu.cs.transitnet.service.index.*;

@Service
public class RealtimeRangeService {

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

    private double[] query_range = new double[4];
    public ConcurrentHashMap<TripId, ArrayList<Vehicle>> vehiclesByTripId  = new ConcurrentHashMap<>();
    private int resolution=6;

    public RealtimeRangeService() {
    }

    public void setup(double[] ps) {
        query_range = ps;
        vehiclesByTripId=realtimeService.getVehiclesByTripId();
    }


    public HashSet<TripId> hytra() {
        HashSet<TripId> can = new HashSet();
        int[] ij_s = Decoder.decodeZ2(Encoder.encodeGrid(query_range[0], query_range[1]));
        int[] ij_e = Decoder.decodeZ2(Encoder.encodeGrid(query_range[2], query_range[3]));
        HashSet<Integer> def_window = new HashSet();

        int j;
        for(int i = ij_s[0] + 1; i < ij_e[0]; ++i) {
            for(j = ij_s[1] + 1; j < ij_e[1]; ++j) {
                def_window.add(Encoder.combine2(i, j, 2 * resolution));
            }
        }

        HashSet<Integer> convert_G=new HashSet<>();
        for(GridId g: realtimeService.GT_List.keySet()){
            convert_G.add(Integer.parseInt(g.toString()));
        }
        def_window.retainAll(convert_G);

        Iterator var12 = def_window.iterator();

        while(var12.hasNext()) {
            Integer gid = (Integer)var12.next();
            can.addAll((Collection)realtimeService.GT_List.get(new GridId(gid.toString())));
        }

        HashSet<Integer> indef_window = new HashSet();

        for(j = ij_s[0]; j <= ij_e[0]; ++j) {
            indef_window.add(Encoder.combine2(j, ij_s[1], resolution * 2));
            indef_window.add(Encoder.combine2(j, ij_e[1], resolution * 2));
        }

        for(j = ij_s[1] + 1; j <= ij_e[1] - 1; ++j) {
            indef_window.add(Encoder.combine2(ij_s[0], j, resolution * 2));
            indef_window.add(Encoder.combine2(ij_e[0], j, resolution * 2));
        }

        indef_window.retainAll(convert_G);
        Iterator var15 = indef_window.iterator();

        while(var15.hasNext()) {
            Integer gid = (Integer)var15.next();
            ((HashSet)realtimeService.GT_List.get(new GridId(gid.toString()))).forEach((tid) -> {
                int size = vehiclesByTripId.get(tid).size();
                Vehicle temp = vehiclesByTripId.get(tid).get(size - 1);
                Point p = new Point(temp.getLat(),temp.getLon());
                if (contains(query_range, p.getLat(), p.getLon())) {
                    can.add((TripId) tid);
                }

            });
        }

        System.out.println(can.size());
        return can;
    }

    public static boolean contains(double[] Qr, double lat, double lon) {
        return Qr[0] <= lat && lat <= Qr[2] && Qr[1] <= lon && lon <= Qr[3];
    }

}
