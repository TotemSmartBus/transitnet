package whu.edu.cs.transitnet.param;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class QueryRangeRtParam {

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

    }

    @Getter
    @Setter
    private List<Point> points;
    public List<Point> getPoints() {
        return points;
    }
}
