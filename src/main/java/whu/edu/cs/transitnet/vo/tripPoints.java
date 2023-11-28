package whu.edu.cs.transitnet.vo;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import whu.edu.cs.transitnet.param.QueryKnnRtParam;
import whu.edu.cs.transitnet.pojo.RealTimePointEntity;
import whu.edu.cs.transitnet.realtime.Vehicle;

import java.util.ArrayList;
import java.util.List;

public class tripPoints {
    @Getter
    @Setter
    @SerializedName("tid")
    public String tripid="";
    @Getter
    @Setter
    @SerializedName("ps")
    public List<QueryKnnRtParam.Point> points=new ArrayList<>();

    public tripPoints(String t, ArrayList<Vehicle> vs){
        tripid=t;
        for (Vehicle v:vs) {
            double lat=v.getLat();
            double lon=v.getLon();
            points.add(new QueryKnnRtParam.Point(lat,lon));
        }
    }
    public tripPoints(String t, ArrayList<RealTimePointEntity> vs,int i){
        tripid=t;
        for (RealTimePointEntity v:vs) {
            double lat=v.getLat();
            double lon=v.getLon();
            points.add(new QueryKnnRtParam.Point(lat,lon));
        }
    }
}