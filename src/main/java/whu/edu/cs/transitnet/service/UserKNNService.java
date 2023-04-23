package whu.edu.cs.transitnet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import whu.edu.cs.transitnet.service.index.HytraEngineManager;
import whu.edu.cs.transitnet.service.index.ShapeIndex;

import java.sql.Time;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserKNNService {

    @Autowired
    HytraEngineManager hytraEngineManager;

    @Autowired
    ShapeIndex shapeIndex;
    // 给定用户轨迹 <lat, lon, timestamp>
    // ArrayList<Point> user_tra;
    int k = 10;


    // TODO hashmap初始化

    // user - grid
    ArrayList<ShapeIndex.GridId> userGridList = new ArrayList<>();
    // user - cube
    ArrayList<ShapeIndex.CubeId> userCubeList = new ArrayList<>();
//    // user - [start_time, end_time]
//    Time userStartTime;
//    Time userEndTime;

    // shape - grid
    private HashMap<ShapeIndex.ShapeId, ArrayList<ShapeIndex.GridId>> shapeGridList = new HashMap<>();
    // grid - shape 倒排索引
    private HashMap<ShapeIndex.GridId, ArrayList<ShapeIndex.ShapeId>> gridShapeList = new HashMap<>();
    // shape_id - trip_id
    private HashMap<ShapeIndex.ShapeId, ArrayList<ShapeIndex.TripId>> shapeTripList = new HashMap<>();
    // trip_id - [start_time, end_time] 写个类
    private HashMap<ShapeIndex.TripId, ArrayList<Time>> tripStartEndList = new HashMap<>();
    // trip - cube  map做操作 删掉list
    private HashMap<ShapeIndex.TripId, ArrayList<ShapeIndex.CubeId>> tripCubeList = new HashMap<>();

    // 先获取Top-k的shape
//    ArrayList<ShapeIndex.ShapeId> topkShapeList = shapeIndex.getTopKShapes(userGridList, k);
    // 再获取下面的所有trip
    ArrayList<ShapeIndex.TripId> tripListByShape = new ArrayList<>();

    // 获取所有trip
    public void getTripList(ArrayList<ShapeIndex.ShapeId> topkShapeList) {
        for (ShapeIndex.ShapeId shapeId : topkShapeList) {
            ArrayList<ShapeIndex.TripId> tripIds = shapeTripList.get(shapeId);
            for (ShapeIndex.TripId tripId : tripIds) {
                tripListByShape.add(tripId);
            }
        }
    }

    // 再根据schedule找到和用户轨迹时间有重叠的trip
    ArrayList<ShapeIndex.TripId> filteredTripList = new ArrayList<>();

    // schedule做一次筛选
    // user: [start_time, end_time]
    public void filterTripList(Time start, Time end) {
        for (ShapeIndex.TripId tripId : tripListByShape) {
            ArrayList<Time> times = tripStartEndList.get(tripId);
            if (times.get(1).before(start) || times.get(0).before(end)) {

            } else {
                filteredTripList.add(tripId);
            }
        }
    }

    int theta = 5;

    // trip_id - similarity
    HashMap<ShapeIndex.TripId, Double> tripSimList = new HashMap<>();

    // 获取 Top-k trip
    public void getTopKTrips(ArrayList<ShapeIndex.CubeId> userCubeList, HashMap<ShapeIndex.TripId, ArrayList<ShapeIndex.CubeId>> tripCubeList, int k) {
        Set<ShapeIndex.TripId> keySet = tripCubeList.keySet();
        for (ShapeIndex.TripId tripId : keySet) {
            double sim = DynamicTimeWarping(userCubeList, tripCubeList.get(tripId));
            tripSimList.put(tripId, sim);
        }

        // 给 filteredTripList 排序就是最后结果
        List<ShapeIndex.TripId> topTrips = tripSimList.entrySet().stream().sorted((a, b) -> a.getValue() >= b.getValue() ? -1 : 1).limit(k).map(Map.Entry::getKey).collect(Collectors.toList());
    }

//    // cube 做相似度计算
//    public double getCubeSimilarity(ArrayList<ShapeIndex.CubeId> cubes1, ArrayList<ShapeIndex.CubeId> cubes2, int theta) {
//
//    }

    // DTW
    public double DynamicTimeWarping(ArrayList<ShapeIndex.CubeId> T1, ArrayList<ShapeIndex.CubeId> T2) {
        int resolution = hytraEngineManager.getParams().getResolution();


        if (T1.size() == 0 && T2.size() == 0) return 0;
        if (T1.size() == 0 || T2.size() == 0) return Integer.MAX_VALUE;

        double[][] dpInts = new double[T1.size() + 1][T2.size() + 1];

        for (int i = 1; i <= T1.size(); ++i) {
            dpInts[i][0] = Integer.MAX_VALUE;
        }

        for (int j = 1; j <= T2.size(); ++j) {
            dpInts[0][j] = Integer.MAX_VALUE;
        }

        for (int i = 1; i <= T1.size(); ++i) {
            for (int j = 1; j <= T2.size(); ++j) {
                int xyz1[] = decodeZ3(Integer.parseInt(T1.get(i - 1).toString()), resolution);
                int xyz2[] = decodeZ3(Integer.parseInt(T2.get(i - 1).toString()), resolution);
//                dpInts[i][j] = distFunc.apply(T1.get(i - 1), T2.get(j - 1)) + min(dpInts[i - 1][j - 1], dpInts[i - 1][j], dpInts[i][j - 1]);
                dpInts[i][j] = getDistances(xyz1[0], xyz1[2], xyz1[4], xyz2[0], xyz2[2], xyz2[4]) + min(dpInts[i - 1][j - 1], dpInts[i - 1][j], dpInts[i][j - 1]);
            }
        }

        return dpInts[T1.size()][T2.size()];
    }

    // EDR
    public double EditDistanceonRealSequence(ArrayList<ShapeIndex.CubeId> T1, ArrayList<ShapeIndex.CubeId> T2) {
        int resolution = hytraEngineManager.getParams().getResolution();

        if (T1 == null || T1.size() == 0) {
            if (T2 != null) return T2.size();
            else return 0;
        }

        if (T2 == null || T2.size() == 0) {
            return T1.size();
        }

        int[][] dpInts = new int[T1.size() + 1][T2.size() + 1];

        for (int i = 1; i <= T1.size(); ++i) {
            dpInts[i][0] = i;
        }

        for (int j = 1; j <= T2.size(); ++j) {
            dpInts[0][j] = j;
        }

        for (int i = 1; i <= T1.size(); ++i) {
            for (int j = 1; j <= T2.size(); ++j) {
                int xyz1[] = decodeZ3(Integer.parseInt(T1.get(i - 1).toString()), resolution);
                int xyz2[] = decodeZ3(Integer.parseInt(T2.get(i - 1).toString()), resolution);
                int subCost = 1;
                // TODO 确定阈值
                if (getDistances(xyz1[0], xyz1[2], xyz1[4], xyz2[0], xyz2[2], xyz2[4]) <= 50)
                    subCost = 0;
                dpInts[i][j] = min(dpInts[i - 1][j - 1] + subCost, dpInts[i - 1][j] + 1, dpInts[i][j - 1] + 1);
            }
        }

        return dpInts[T1.size()][T2.size()] * 1.0;
    }

    // ERP
    // g: cube0
    public double EditDistanceWithRealPenalty(List<ShapeIndex.CubeId> T1, List<ShapeIndex.CubeId> T2, ShapeIndex.CubeId g) {
        int resolution = hytraEngineManager.getParams().getResolution();

        int xyz[] = decodeZ3(Integer.parseInt(g.toString()), resolution);

        if (T1 == null || T1.size() == 0) {
            double res = 0.0;
            if (T2 != null) {
                for (ShapeIndex.CubeId t : T2) {
                    int xyz2[] = decodeZ3(Integer.parseInt(t.toString()), resolution);
                    res += getDistances(xyz2[0], xyz2[2], xyz2[4], xyz[0], xyz[2], xyz[4]);
                }
            }
            return res;
        }

        if (T2 == null || T2.size() == 0) {
            double res = 0.0;
            for (ShapeIndex.CubeId t : T1) {
                int xyz1[] = decodeZ3(Integer.parseInt(t.toString()), resolution);
                res += getDistances(xyz1[0], xyz1[2], xyz1[4], xyz[0], xyz[2], xyz[4]);
            }
            return res;
        }

        double[][] dpInts = new double[T1.size() + 1][T2.size() + 1];

        for (int i = 1; i <= T1.size(); ++i) {
            int xyz1[] = decodeZ3(Integer.parseInt(T1.get(i - 1).toString()), resolution);
            dpInts[i][0] = getDistances(xyz1[0], xyz1[2], xyz1[4], xyz[0], xyz[2], xyz[4]) + dpInts[i - 1][0];
        }

        for (int j = 1; j <= T2.size(); ++j) {
            int xyz2[] = decodeZ3(Integer.parseInt(T2.get(j - 1).toString()), resolution);
            dpInts[0][j] = getDistances(xyz2[0], xyz2[2], xyz2[4], xyz[0], xyz[2], xyz[4]) + dpInts[0][j - 1];
        }

        for (int i = 1; i <= T1.size(); ++i) {
            for (int j = 1; j <= T2.size(); ++j) {
                int xyz1[] = decodeZ3(Integer.parseInt(T1.get(i - 1).toString()), resolution);
                int xyz2[] = decodeZ3(Integer.parseInt(T2.get(j - 1).toString()), resolution);

                dpInts[i][j] = min(dpInts[i - 1][j - 1] + getDistances(xyz1[0], xyz1[2], xyz1[4], xyz2[0], xyz2[2], xyz2[4]),
                        dpInts[i - 1][j] + getDistances(xyz1[0], xyz1[2], xyz1[4], xyz[0], xyz[2], xyz[4]),
                        dpInts[i][j - 1] + getDistances(xyz2[0], xyz2[2], xyz2[4], xyz[0], xyz[2], xyz[4]));
            }
        }

        return dpInts[T1.size()][T2.size()] * 1.0 / Math.max(T1.size(), T2.size());
    }

    // distance between two cubes
    public double getDistances(int x1, int y1, int z1, int x2, int y2, int z2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) + Math.pow(z2 - z1, 2));
    }

    public int min(int a, int b, int c) {
        if (a > b) a = b;
        if (a > c) a = c;
        return a;
    }

    public double min(double a, double b, double c) {
        if (a > b) a = b;
        if (a > c) a = c;
        return a;
    }

    //
    public int[] decodeZ3(int zorder, int level) {
        int resolution = hytraEngineManager.getParams().getResolution();
        int digits = 3 * resolution;

        String bits;
        for(bits = Integer.toBinaryString(zorder); digits > bits.length(); bits = "0" + bits) {
        }

        String bitsI = "";
        String bitsJ = "";
        String bitsK = "";

        int i;
        for(i = 0; i < bits.length(); ++i) {
            if (i % 3 == 0) {
                bitsK = bitsK + bits.charAt(i);
            }

            if (i % 3 == 1) {
                bitsJ = bitsJ + bits.charAt(i);
            }

            if (i % 3 == 2) {
                bitsI = bitsI + bits.charAt(i);
            }
        }

        i = shapeIndex.bitToint(bitsI);
        int J = shapeIndex.bitToint(bitsJ);
        int K = shapeIndex.bitToint(bitsK);
        int i1 = i * (int)Math.pow(8.0D, (double)level);
        int i2 = i1 + (int)Math.pow(8.0D, (double)level) - 1;
        int j1 = J * (int)Math.pow(8.0D, (double)level);
        int j2 = j1 + (int)Math.pow(8.0D, (double)level) - 1;
        int k1 = K * (int)Math.pow(8.0D, (double)level);
        int k2 = k1 + (int)Math.pow(8.0D, (double)level) - 1;
        return new int[]{i1, i2, j1, j2, k1, k2};
    }
}
