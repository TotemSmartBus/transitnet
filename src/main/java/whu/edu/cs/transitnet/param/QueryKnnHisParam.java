package whu.edu.cs.transitnet.param;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class QueryKnnHisParam {
    @Getter
    @Setter
    private List<Point> points;
    private int k;


    public List<Point> getPoints() {
        return points;
    }

    public int getK(){
        return k;
    }

    public QueryKnnHisParam() {
        // 无参数构造函数
    }

    public QueryKnnHisParam(double[] a,String[] b){
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

        public void setTime(String t) {
            this.time = t;
        }

        public Point(double a, double b){
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

