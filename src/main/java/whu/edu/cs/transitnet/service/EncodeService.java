package whu.edu.cs.transitnet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import whu.edu.cs.transitnet.service.index.CubeId;
import whu.edu.cs.transitnet.service.index.GridId;
import whu.edu.cs.transitnet.service.index.HytraEngineManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@Service
public class EncodeService {
    @Autowired
    HytraEngineManager hytraEngineManager;

    /**
     * point - grid 做映射
     * @param lat
     * @param lon
     * @return
     */
    public GridId getGridID(double lat, double lon) {

        int resolution = hytraEngineManager.getParams().getResolution();
        double[] spatialDomain = hytraEngineManager.getParams().getSpatialDomain();
        double deltaX = (spatialDomain[2] - spatialDomain[0]) / Math.pow(2.0D, (double) resolution);
        double deltaY = (spatialDomain[3] - spatialDomain[1]) / Math.pow(2.0D, (double) resolution);

        int i = (int) ((lat - spatialDomain[0]) / deltaX);
        int j = (int) ((lon - spatialDomain[1]) / deltaY);
        int gridId = combine2(i, j, resolution);

        return new GridId(String.valueOf(gridId));
    }

    /**
     * point - grid 做映射；需要传入参数 resolution
     * @param lat
     * @param lon
     * @param resolution
     * @return
     */
    public GridId getGridID(double lat, double lon, int resolution) {

        double[] spatialDomain = hytraEngineManager.getParams().getSpatialDomain();
        double deltaX = (spatialDomain[2] - spatialDomain[0]) / Math.pow(2.0D, (double) resolution);
        double deltaY = (spatialDomain[3] - spatialDomain[1]) / Math.pow(2.0D, (double) resolution);

        int i = (int) ((lat - spatialDomain[0]) / deltaX);
        int j = (int) ((lon - spatialDomain[1]) / deltaY);
        int gridId = combine2(i, j, resolution);

        return new GridId(String.valueOf(gridId));
    }

    public int combine2(int aid, int bid, int lengtho) {
        int length = lengtho;
        int[] a = new int[lengtho];

        int[] b;
        for (b = new int[lengtho]; length-- >= 1; bid /= 2) {
            a[length] = aid % 2;
            aid /= 2;
            b[length] = bid % 2;
        }

        int[] com = new int[2 * lengtho];

        for (int i = 0; i < lengtho; ++i) {
            com[2 * i] = a[i];
            com[2 * i + 1] = b[i];
        }

        return bitToint(com, 2 * lengtho);
    }

    public int bitToint(int[] a, int length) {
        int sum = 0;

        for (int i = 0; i < length; ++i) {
            sum = (int) ((double) sum + (double) a[i] * Math.pow(2.0D, (double) (length - i - 1)));
        }

        return sum;
    }

    public int bitToint(String bits) {
        int sum = 0;
        int length = bits.length();

        for (int i = 0; i < length; ++i) {
            sum = (int) ((double) sum + (double) Integer.parseInt(String.valueOf(bits.charAt(i))) * Math.pow(2.0D, (double) (length - i - 1)));
        }

        return sum;
    }

    /**
     * encode cube
     * @param lat
     * @param lon
     * @param time
     * @return
     */
    public CubeId encodeCube(double lat, double lon, Long time) {
        int resolution = hytraEngineManager.getParams().getResolution();
        double[] spatialDomain = hytraEngineManager.getParams().getSpatialDomain();
        double deltaX = (spatialDomain[2] - spatialDomain[0]) / Math.pow(2.0D, (double) resolution);
        double deltaY = (spatialDomain[3] - spatialDomain[1]) / Math.pow(2.0D, (double) resolution);
        double deltaT = 86400.0D / Math.pow(2.0D, (double)resolution);

        Date d = new Date();
        d.setTime(time);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        String date_hour_min_sec  = sdf.format(d);

        // 取的是当天日期
        String[] date_time = date_hour_min_sec.split(" ");
        // 取的是时分秒
        String[] hour_min_sec = date_time[1].split(":");
        // 转化成秒
        double t = (double)(Integer.parseInt(hour_min_sec[0]) * 3600 + Integer.parseInt(hour_min_sec[1]) * 60 + Integer.parseInt(hour_min_sec[2]));
        int i = (int)((lat - spatialDomain[0]) / deltaX);
        int j = (int)((lon - spatialDomain[1]) / deltaY);
        int k = (int)(t / deltaT);
        int zorder = combine3(i, j, k, resolution);
        return new CubeId(String.valueOf(zorder));
    }

    public CubeId encodeCube(double lat, double lon, Long time, int resolution) {
        double[] spatialDomain = hytraEngineManager.getParams().getSpatialDomain();
        double deltaX = (spatialDomain[2] - spatialDomain[0]) / Math.pow(2.0D, (double) resolution);
        double deltaY = (spatialDomain[3] - spatialDomain[1]) / Math.pow(2.0D, (double) resolution);
        double deltaT = 86400.0D / Math.pow(2.0D, (double)resolution);

        Date d = new Date();
        d.setTime(time);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));
        String date_hour_min_sec  = sdf.format(d);

        // 取的是当天日期
        String[] date_time = date_hour_min_sec.split(" ");
        // 取的是时分秒
        String[] hour_min_sec = date_time[1].split(":");
        // 转化成秒
        double t = (double)(Integer.parseInt(hour_min_sec[0]) * 3600 + Integer.parseInt(hour_min_sec[1]) * 60 + Integer.parseInt(hour_min_sec[2]));
        int i = (int)((lat - spatialDomain[0]) / deltaX);
        int j = (int)((lon - spatialDomain[1]) / deltaY);
        int k = (int)(t / deltaT);
        int zorder = combine3(i, j, k, resolution);
        return new CubeId(String.valueOf(zorder));
    }

    public int combine3(int aid, int bid, int cid, int lengtho) {
        int length = lengtho;
        int[] a = new int[lengtho];
        int[] b = new int[lengtho];

        int[] c;
        for(c = new int[lengtho]; length-- >= 1; cid /= 2) {
            a[length] = aid % 2;
            aid /= 2;
            b[length] = bid % 2;
            bid /= 2;
            c[length] = cid % 2;
        }

        int[] com = new int[3 * lengtho];

        for(int i = 0; i < lengtho; ++i) {
            com[3 * i] = a[i];
            com[3 * i + 1] = b[i];
            com[3 * i + 2] = c[i];
        }

        return bitToint(com, 3 * lengtho);
    }

}
