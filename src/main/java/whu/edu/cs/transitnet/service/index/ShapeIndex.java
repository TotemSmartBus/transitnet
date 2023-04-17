package whu.edu.cs.transitnet.service.index;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import whu.edu.cs.transitnet.dao.ShapesDao;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class ShapeIndex {

    @Autowired
    RealtimeDataIndex realtimeDataIndex;


    @Autowired
    ShapesDao shapesDao;
    public ShapeIndex() {
        List<String> shapeIds = shapesDao.findAllShapeId();
        // TODO 去构建 2 个索引
        int resolution = realtimeDataIndex.engineFactory.params.getResolution();

    }

    // shape - grid 的映射关系
    // arraylist 有序
    private HashMap<ShapeId, ArrayList<GridId>> shapeGridIndex;

    private HashMap<GridId, ArrayList<ShapeId>> gridShapeList;

    public void getTopKShapes(ArrayList<GridId> userPassedGrids, int k) {
        HashSet<ShapeId> shapeCandidates = new HashSet<>();
        // 1. 过滤所有有交集的shape
        for (GridId grid : userPassedGrids) {
            shapeCandidates.addAll(gridShapeList.get(grid));
        }
        // 2.
        int theta = 5;
        List<ShapeId> topShapes = shapeGridIndex.entrySet().stream().filter(entry -> shapeCandidates.contains(entry.getKey()))
                .sorted((a, b) -> getSimilarity(a.getValue(), userPassedGrids, theta) > getSimilarity(b.getValue(), userPassedGrids, theta) ? 1 : -1)
                .limit(k).map(Map.Entry::getKey).collect(Collectors.toList());
    }

    private double getSimilarity(ArrayList<GridId> grids1, ArrayList<GridId> grids2, int theta) {
        if (grids1 == null || grids2 == null || grids1.size() == 0 || grids2.size() == 0) {
            return 0;
        }

        int[][] dp = new int[grids1.size()][grids2.size()]; // dp数组
        int maxSimilarity = 0; // 相似度

        if (grids1.get(0).equals(grids2.get(0))) dp[0][0] = 1;

        for (int i = 1; i < grids1.size(); i++) {
            if (grids1.get(i).equals(grids2.get(0))) {
                dp[i][0] = 1;
            } else {
                dp[i][0] = dp[i-1][0];
            }
        }

        for (int j = 1; j < grids2.size(); j++) {
            if (grids2.get(j).equals(grids1.get(0))) {
                dp[0][j] = 1;
            } else {
                dp[0][j] = dp[0][j-1];
            }
        }

        for (int i = 1; i < grids1.size(); i++) {
            for (int j = 1; j < grids2.size(); j++) {
                if (Math.abs(i - j) <= theta) {
                    if (grids1.get(i).equals(grids2.get(j))) {
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


    private GridId getGridID(double lon, double lat) {
        // TODO
        int resolution = ???
        int
        return gridID;
    }

    class GridId implements CharSequence {
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
    }

    class ShapeId implements CharSequence {
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
    }
}