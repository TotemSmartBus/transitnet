package whu.edu.cs.transitnet.param;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class QueryRangeHisParam {
    @Getter
    @Setter
    private List<Point> points;
    private String timerange1;
    private String timerange2;


    public List<Point> getPoints() {
        return points;
    }

    public String getTimerange1(){
        return timerange1;
    }
    public String getTimerange2(){
        return timerange2;
    }

    public QueryRangeHisParam() {
        // 无参数构造函数
    }

    public QueryRangeHisParam(double[] a,String[] b){
        points=new ArrayList<>();
        for(int i=0;i<a.length;i=i+2){
            Point p=new Point(a[i],a[i+1],b[i/2]);
            points.add(p);
        }
    }

    public static class Point {
        private double lat;
        private double lng;
        private String time; // 假设 time 是整数类型

        // 构造函数、getter 和 setter 方法

        public double getLat() {
            return lat;
        }
        public double getLng() {
            return lng;
        }
        public String getTime() { return time;}

        public Point(double a,double b){
            lat=a;
            lng=b;
        }

        public Point(double a,double b,String t){
            lat=a;
            lng=b;
            time=t;
        }

        public Point(){
        }

    }
}
