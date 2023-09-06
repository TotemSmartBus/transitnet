package whu.edu.cs.transitnet.utils;

import org.springframework.stereotype.Component;
import whu.edu.cs.transitnet.vo.ShapePointVo;
import whu.edu.cs.transitnet.vo.StopsVo;

import java.util.ArrayList;
import java.util.List;

@Component
public class TimeUtil {

    private static double EARTH_RADIUS = 6378.137;
    public static TimeUtil timeUtil;

    public List<Double> CalculateVehicleStopArriveTimes(List<StopsVo> stopsVos, List<ShapePointVo> shapePointVos) {
        System.out.println("Calculating Route Time...");

        //closest List of shape points index for every stop
        List<Integer> nearestIdxs = new ArrayList<Integer>();
        StopsVo lastStop = null;
        Integer cnt = 0;
        Double speed = 0.0;
        //List for the speed of the bus between each stop
        List<Double> speeds = new ArrayList<Double>();
        //Find the shape point closest to stop
        for (StopsVo stop: stopsVos) {
            double minDistance = Double.MAX_VALUE;
            Integer idx = 0;
            for(Integer i = 0; i < shapePointVos.size(); i ++) {
                ShapePointVo shape = shapePointVos.get(i);
                double distance = getDistance(stop.getLng(), stop.getLat(), shape.getLng(), shape.getLat());
                if(distance < minDistance) {
                    idx = i;
                    minDistance = distance;
                }
            }
            nearestIdxs.add(idx);
            if(cnt > 0) {
                speed = getDistance(lastStop.getLng(), lastStop.getLat(), stop.getLng(), stop.getLat());
                speeds.add(speed);
            }
            cnt ++;
            lastStop = stop;
            System.out.println(idx);
        }
        Integer stopIdx = 0;
        ShapePointVo stopNearest = shapePointVos.get(nearestIdxs.get(stopIdx));
        ShapePointVo lastShape = null;
        List<Double> times = new ArrayList<Double>();
        //calculate the time span between stops by distance and speedList
        for(Integer i = 0; i < shapePointVos.size(); i ++) {
            ShapePointVo shapePointVo = shapePointVos.get(i);
            if(i > 0) {
                double s = getDistance(shapePointVo.getLng(), shapePointVo.getLat(), lastShape.getLng(), lastShape.getLat());
                double t = s/speeds.get(stopIdx);
                times.add(t*500);
            }
            lastShape = shapePointVo;
            if(i != 0 && stopNearest.getLat() == shapePointVo.getLat() && stopNearest.getLng() == shapePointVo.getLng()) {
                System.out.println(i);
                stopIdx ++;
                stopNearest = shapePointVos.get(nearestIdxs.get(stopIdx));
            }
        }
        System.out.println("Finish the Calculation");
        return times;
    }

    public double getDistance(double lng1, double lat1, double lng2, double lat2) {
        double lng_1 = Math.toRadians(lng1);
        double lat_1 = Math.toRadians(lat1);
        double lng_2 = Math.toRadians(lng2);
        double lat_2 = Math.toRadians(lat2);
        double a = lat_1 - lat_2;
        double b = lng_1 - lng_2;
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2), 2) +
                Math.cos(lat_1) * Math.cos(lat_2) * Math.pow(Math.sin(b/2), 2)));
        s = s * EARTH_RADIUS;
        return s;
    }
}
