package whu.edu.cs.transitnet.service.index;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import whu.edu.cs.transitnet.dao.ShapesDao;
import whu.edu.cs.transitnet.dao.TripsDao;
import whu.edu.cs.transitnet.pojo.TripsEntity;
import whu.edu.cs.transitnet.vo.ShapePointVo;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ShapeIndex {

    @Autowired
    RealtimeDataIndex realtimeDataIndex;

    @Autowired
    HytraEngineManager hytraEngineManager;


    // shape - grid 的映射关系
    // arraylist 有序
    private HashMap<ShapeId, ArrayList<GridId>> shapeGridList;

    private HashMap<GridId, ArrayList<ShapeId>> gridShapeList;

    // shape_id - trip_id
    private HashMap<ShapeId, ArrayList<TripId>> shapeTripList;


    @Autowired
    ShapesDao shapesDao;

    @Autowired
    TripsDao tripsDao;

    public ShapeIndex() {
        List<String> shapeIds = shapesDao.findAllShapeId();

        // 遍历每一个 shapeId
        for(String shape : shapeIds) {
            // 取出每一个 shapeId 对应的点序列
            List<ShapePointVo> shapePointVos = shapesDao.findAllByShapeId(shape);

            ShapeId shapeId = new ShapeId(shape);

            // point - grid 做映射
            for(ShapePointVo shapePointVo : shapePointVos) {
                GridId gridId = getGridID(shapePointVo.getLat(), shapePointVo.getLng());

                // 构建 shape - grid 索引
                ArrayList<GridId> gridIds = new ArrayList<>();
                if (!shapeGridList.containsKey(shapeId)) {
                    gridIds.add(gridId);
                    shapeGridList.put(shapeId, gridIds);
                } else if (shapeGridList.get(shapeId).lastIndexOf(gridId) != (shapeGridList.get(shapeId).size() - 1)) {
                    gridIds = shapeGridList.get(shapeId);
                    gridIds.add(gridId);
                    shapeGridList.put(shapeId, gridIds);
                } else {
                        // 什么也不做
                }

                // 构建 grid - shape 索引
                ArrayList<ShapeId> shapeIds1 = new ArrayList<>();
                if (!gridShapeList.containsKey(gridId)) {
                    shapeIds1.add(shapeId);
                    gridShapeList.put(gridId, shapeIds1);
                } else if (!gridShapeList.get(gridId).contains(shapeId)) {
                    shapeIds1 = gridShapeList.get(gridId);
                    shapeIds1.add(shapeId);
                    gridShapeList.put(gridId, shapeIds1);
                }
            }

            // shape_id - trip_id
            List<TripsEntity> tripsEntities = tripsDao.findAllByShapeId(shape);
            ArrayList<TripId> tripIds = new ArrayList<>();
            for (TripsEntity tripsEntity : tripsEntities) {
                tripIds.add(new TripId(tripsEntity.getTripId()));
            }
            shapeTripList.put(shapeId, tripIds);
        }
    }

    // point - grid 做映射
    private GridId getGridID(double lat, double lon) {
        // TODO
//        int resolution = ???
//        int
//        return gridID;
        int resolution = hytraEngineManager.getParams().getResolution();
        double[] spatialDomain = hytraEngineManager.getParams().getSpatialDomain();
        double deltaX = (spatialDomain[2] - spatialDomain[0]) / Math.pow(2.0D, (double)resolution);
        double deltaY = (spatialDomain[3] - spatialDomain[1]) / Math.pow(2.0D, (double)resolution);

        int i = (int)((lat - spatialDomain[0]) / deltaX);
        int j = (int)((lon - spatialDomain[1]) / deltaY);
        int gridId = combine2(i, j, resolution);

        return new GridId(String.valueOf(gridId));
    }

    public int combine2(int aid, int bid, int lengtho) {
        int length = lengtho;
        int[] a = new int[lengtho];

        int[] b;
        for(b = new int[lengtho]; length-- >= 1; bid /= 2) {
            a[length] = aid % 2;
            aid /= 2;
            b[length] = bid % 2;
        }

        int[] com = new int[2 * lengtho];

        for(int i = 0; i < lengtho; ++i) {
            com[2 * i] = a[i];
            com[2 * i + 1] = b[i];
        }

        return bitToint(com, 2 * lengtho);
    }

    public int bitToint(int[] a, int length) {
        int sum = 0;

        for(int i = 0; i < length; ++i) {
            sum = (int)((double)sum + (double)a[i] * Math.pow(2.0D, (double)(length - i - 1)));
        }

        return sum;
    }


    public void getTopKShapes(ArrayList<GridId> userPassedGrids, int k) {
         HashSet<ShapeId> shapeCandidates = new HashSet<>();
        // 1. 过滤所有有交集的shape
        for (GridId grid : userPassedGrids) {
            shapeCandidates.addAll(gridShapeList.get(grid));
        }
        // 2. 返回相似度最大的前k的shapeId
        int theta = 5;
        List<ShapeId> topShapes = shapeGridList.entrySet().stream().filter(entry -> shapeCandidates.contains(entry.getKey()))
                .sorted((a, b) -> getSimilarity(a.getValue(), userPassedGrids, theta) >= getSimilarity(b.getValue(), userPassedGrids, theta) ? -1 : 1)
                .limit(k).map(Map.Entry::getKey).collect(Collectors.toList());

    }

    public double getSimilarity(ArrayList<GridId> grids1, ArrayList<GridId> grids2, int theta) {
        if (grids1 == null || grids2 == null || grids1.size() == 0 || grids2.size() == 0) {
            return 0;
        }

        int[][] dp = new int[grids1.size()][grids2.size()]; // dp数组
        int maxSimilarity = 0; // 相似度

        if (grids1.get(0).toString().equals(grids2.get(0).toString())) dp[0][0] = 1;

        for (int i = 1; i < grids1.size(); i++) {
            if (grids1.get(i).toString().equals(grids2.get(0).toString())) {
                dp[i][0] = 1;
            } else {
                dp[i][0] = dp[i-1][0];
            }
        }

        for (int j = 1; j < grids2.size(); j++) {
            if (grids2.get(j).toString().equals(grids1.get(0).toString())) {
                dp[0][j] = 1;
            } else {
                dp[0][j] = dp[0][j-1];
            }
        }

        for (int i = 1; i < grids1.size(); i++) {
            for (int j = 1; j < grids2.size(); j++) {
                if (Math.abs(i - j) <= theta) {
                    if (grids1.get(i).toString().equals(grids2.get(j).toString())) {
                        dp[i][j] = 1 + dp[i-1][j-1];
                    } else {
                        dp[i][j] = Math.max(dp[i-1][j], dp[i][j-1]);
                    }
                }

//                if (maxSimilarity < dp[i][j]) {
//                    maxSimilarity = dp[i][j];
//                }
            }
        }

        maxSimilarity = dp[grids1.size() - 1][grids2.size() - 1];
//        System.out.println("两条轨迹的相似度为：" + maxSimilarity);
        return maxSimilarity;
    }


    public static class GridId implements CharSequence {
        public String getContent() {
            return content;
        }

        private final String content;

        public GridId(String str) {
            content = str;
        }

        @Override
        public int length() {
            return content.length();
        }

        @Override
        public char charAt(int index) {
            return content.charAt(index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return content.subSequence(start, end);
        }

        @Override
        public String toString() {
            return content;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GridId gridId = (GridId) o;
            return Objects.equals(content, gridId.content);
        }

        @Override
        public int hashCode() {
            return Objects.hash(content);
        }
    }

    public class ShapeId implements CharSequence {
        private final String content;

        public ShapeId(String str) {
            content = str;
        }

        @Override
        public int length() {
            return content.length();
        }

        @Override
        public char charAt(int index) {
            return content.charAt(index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return content.subSequence(start, end);
        }

        @Override
        public String toString() {
            return content;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ShapeId shapeId = (ShapeId) o;
            return Objects.equals(content, shapeId.content);
        }

        @Override
        public int hashCode() {
            return Objects.hash(content);
        }
    }

    public class TripId implements CharSequence {
        private final String content;


        public TripId(String content) {
            this.content = content;
        }


        @Override
        public int length() {
            return content.length();
        }

        @Override
        public char charAt(int index) {
            return content.charAt(index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return content.subSequence(start, end);
        }

        @Override
        public String toString() {
            return content;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TripId tripId = (TripId) o;
            return Objects.equals(content, tripId.content);
        }

        @Override
        public int hashCode() {
            return Objects.hash(content);
        }
    }
}