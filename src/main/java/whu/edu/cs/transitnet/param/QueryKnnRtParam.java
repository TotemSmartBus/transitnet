package whu.edu.cs.transitnet.param;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class QueryKnnRtParam {

    public static class Point {
        private double lat;
        private double lng;
        private int time; // 假设 time 是整数类型

        // 构造函数、getter 和 setter 方法

        public double getLat() {
            return lat;
        }
        public double getLng() {
            return lng;
        }

        public Point(double a,double b){
            lat=a;
            lng=b;
        }

    }

    @Getter
    @Setter
    private List<Point> points;
    private int[]k_backdate;
    public int[] getK_backdate(){return k_backdate;}
    public List<Point> getPoints() {
        return points;
    }
    public QueryKnnRtParam(double[] a){
        points=new ArrayList<>();
        for(int i=0;i<a.length;i=i+2){
            Point p=new Point(a[i],a[i+1]);
            points.add(p);
        }
    }

    public QueryKnnRtParam(){

    }
}

