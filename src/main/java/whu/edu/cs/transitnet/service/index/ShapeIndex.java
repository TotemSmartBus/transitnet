package whu.edu.cs.transitnet.service.index;


import com.github.davidmoten.guavamini.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import whu.edu.cs.transitnet.dao.ShapesDao;
import whu.edu.cs.transitnet.dao.TripsDao;
import whu.edu.cs.transitnet.pojo.TripsEntity;
import whu.edu.cs.transitnet.service.EncodeService;
import whu.edu.cs.transitnet.vo.ShapePointVo;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ShapeIndex {

    @Value("${transitnet.gridindex.enable}")
    private boolean indexEnable;

    @Autowired
    RealtimeDataIndex realtimeDataIndex;

    @Autowired
    HytraEngineManager hytraEngineManager;

    @Autowired
    EncodeService encodeService;

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
        shapeGridList = new HashMap<>();
        gridShapeList = new HashMap<>();
        shapeTripList = new HashMap<>();
    }


    /**
     * resolution = 6 时创建 txt 文件的操作
     */
    @PostConstruct
    public void init() {

        // 勿删
//        if(!indexEnable) {
//            System.out.println("[SHAPEINDEX] Index is not enabled, skipped.");
//            return;
//        }
        String constructionDate="20230918";

        int resolution = hytraEngineManager.getParams().getResolution();
        Resource shapeGridResource = new ClassPathResource("indexFiles/shape_grid_6_20230918.txt");
        Resource gridShapeResource = new ClassPathResource("indexFiles/grid_shape_6_20230918.txt");
        Resource shapeTripResource = new ClassPathResource("indexFiles/shape_trip_20230918.txt");

        if (shapeGridResource.exists() && gridShapeResource.exists() && shapeTripResource.exists()) {
            // 读取文件
            System.out.println("======================");
            System.out.println("[SHAPEINDEX] FILE EXISTS... now version 2023/11/21 14:45");
            System.out.println("======================");
            System.out.println("[SHAPEINDEX] Start Deserializing HashMap..");

            Long starttime = System.currentTimeMillis();


            try {
                InputStream shapeGridStream = shapeGridResource.getInputStream();
                InputStream gridShapeStream = gridShapeResource.getInputStream();
                InputStream shapeTripStream = shapeTripResource.getInputStream();


                ObjectInputStream objectInput1
                        = new ObjectInputStream(shapeGridStream);
                ObjectInputStream objectInput2
                        = new ObjectInputStream(gridShapeStream);
                ObjectInputStream objectInput3
                        = new ObjectInputStream(shapeTripStream);

                shapeGridList = (HashMap)objectInput1.readObject();
                gridShapeList = (HashMap)objectInput2.readObject();
                shapeTripList = (HashMap)objectInput3.readObject();

                objectInput1.close();
                shapeGridStream.close();
                objectInput2.close();
                gridShapeStream.close();
                objectInput3.close();
                shapeTripStream.close();
            }

            catch (IOException obj1) {
                obj1.printStackTrace();
                return;
            }

            catch (ClassNotFoundException obj2) {
                System.out.println("[SHAPEINDEX] Class not found");
                obj2.printStackTrace();
                return;
            }

            Long endtime = System.currentTimeMillis();

            System.out.println("======================");
            System.out.println("[SHAPEINDEX] Deserializing HashMap DONE!");
            System.out.println("[SHAPEINDEX] Deserializing time: " + (endtime - starttime) / 1000 + "s");

        } else {
            System.out.println("=============================");
            System.out.println("[SHAPEINDEX] File Not Exists... Start fetching data from database...");

            Long startTime = System.currentTimeMillis();
            List<String> shapeIds = shapesDao.findAllShapeId();
            Long endTime = System.currentTimeMillis();
            System.out.println("=============================");
            System.out.println("[SHAPEINDEX] findAllShapeId time: " + (endTime - startTime) / 1000 / 60 + "min");

            Long startTime1 = System.currentTimeMillis();
            // 遍历每一个 shapeId
            for (String shape : shapeIds) {
                // 取出每一个 shapeId 对应的点序列
                List<ShapePointVo> shapePointVos = shapesDao.findAllByShapeId(shape);

                ShapeId shapeId = new ShapeId(shape);

                // point - grid 做映射
                for (ShapePointVo shapePointVo : shapePointVos) {
                    GridId gridId = encodeService.getGridID(shapePointVo.getLat(), shapePointVo.getLng());

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

            // try catch block
//            try {
//                FileOutputStream myFileOutStream1
//                        = new FileOutputStream(shapeGridResource);
//                FileOutputStream myFileOutStream2
//                        = new FileOutputStream(gridShapeFile);
//                FileOutputStream myFileOutStream3
//                        = new FileOutputStream(shapeTripFile);
//
//                ObjectOutputStream myObjectOutStream1
//                        = new ObjectOutputStream(myFileOutStream1);
//                ObjectOutputStream myObjectOutStream2
//                        = new ObjectOutputStream(myFileOutStream2);
//                ObjectOutputStream myObjectOutStream3
//                        = new ObjectOutputStream(myFileOutStream3);
//
//                myObjectOutStream1.writeObject(shapeGridList);
//                myObjectOutStream2.writeObject(gridShapeList);
//                myObjectOutStream3.writeObject(shapeTripList);
//
//                // closing FileOutputStream and
//                // ObjectOutputStream
//                myObjectOutStream1.close();
//                myFileOutStream1.close();
//                myObjectOutStream2.close();
//                myFileOutStream2.close();
//                myObjectOutStream3.close();
//                myFileOutStream3.close();
//            }
//            catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }

            Long endTime1 = System.currentTimeMillis();
            System.out.println("=============================");
            System.out.println("[SHAPEINDEX] index construction and serialization time: " + (endTime1 - startTime1) / 1000 + "s");
        }

    }


    public ArrayList<TripId> getTripIdsByShapeId(ShapeId id) {
        ArrayList<TripId> tripIds = shapeTripList.get(id);
        return tripIds;
    }

    public ArrayList<ShapeId> getTopKShapes(ShapeId userShapeId, ArrayList<GridId> userPassedGrids, int k) {
        HashSet<ShapeId> shapeCandidates = new HashSet<>();
        // 1. 过滤所有有交集的shape
        for (GridId grid : userPassedGrids) {
            if (gridShapeList.keySet().contains(grid)) {
                shapeCandidates.addAll(gridShapeList.get(grid));
            }
        }
        // 2. 返回相似度最大的前k的shapeId
//        int theta = 5;

        HashMap<ShapeId, Double> shapeSimMap = new HashMap<>();

        List<ShapeId> topShapes = shapeGridList.entrySet().stream().filter(entry -> shapeCandidates.contains(entry.getKey())).map(Map.Entry::getKey).collect(Collectors.toList());
        Collections.sort(topShapes, new Comparator<ShapeId>() {
            @Override
            public int compare(ShapeId a, ShapeId b) { // 从大到小
                Double t = getGridSimilarity(shapeGridList.get(a), userPassedGrids) - getGridSimilarity(shapeGridList.get(b), userPassedGrids);
                int flag = -1;
                if (t < 0) {
                    flag = 1;
                }
                if (t == 0) {
                    flag = 0;
                }
                return flag;
            }
        });

        for (ShapeId shapeId : topShapes) {
            Double sim = getGridSimilarity(shapeGridList.get(shapeId), userPassedGrids);
            shapeSimMap.put(shapeId, sim);
        }

        System.out.println("=============================");

        //System.out.println("[SHAPEINDEX] " + shapeGridList.get(userShapeId));
        //System.out.println("[SHAPEINDEX] " + shapeSimMap);
        //System.out.println("[SHAPEINDEX] " + topShapes);
        if (topShapes.size() >= k) {
            return Lists.newArrayList(topShapes.subList(0, k));
        } else {
            return Lists.newArrayList(topShapes);
        }

    }

    public double getGridSimilarity(ArrayList<GridId> grids1, ArrayList<GridId> grids2) {
        if (grids1 == null || grids2 == null || grids1.size() == 0 || grids2.size() == 0) {
            return 0;
        }

        int[][] dp = new int[grids1.size()][grids2.size()];
        int maxSimilarity = 0;

        if (grids1.get(0).equals(grids2.get(0))) {
            dp[0][0] = 1;
        }

        for (int i = 1; i < grids1.size(); i++) {
            if (grids1.get(i).equals(grids2.get(0))) {
                dp[i][0] = 1;
            } else {
                dp[i][0] = dp[i - 1][0];
            }
        }

        for (int j = 1; j < grids2.size(); j++) {
            if (grids2.get(j).equals(grids1.get(0))) {
                dp[0][j] = 1;
            } else {
                dp[0][j] = dp[0][j - 1];
            }
        }

        for (int i = 1; i < grids1.size(); i++) {
            for (int j = 1; j < grids2.size(); j++) {
//                if (Math.abs(i - j) <= theta) {
                if (Math.abs(i - j) <= Integer.MAX_VALUE) {
                    if (grids1.get(i).equals(grids2.get(j))) {
                        dp[i][j] = 1 + dp[i - 1][j - 1];
                    } else {
                        dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                    }
                }

//                if (maxSimilarity < dp[i][j]) {
//                    maxSimilarity = dp[i][j];
//                }
            }
        }

        maxSimilarity = dp[grids1.size() - 1][grids2.size() - 1];

        return maxSimilarity;
    }

    public ArrayList<TripId> getTripsOfTopKShapes(ShapeId userShapeId, ArrayList<GridId> userPassedGrids, int k) {
        ArrayList<ShapeId> topKShapes = getTopKShapes(userShapeId, userPassedGrids, k);
        ArrayList<TripId> tripIds = new ArrayList<>();

        for (ShapeId shapeId : topKShapes) {
            tripIds.addAll(shapeTripList.get(shapeId));
        }
        //test use
//        HashSet<TripId> all=new HashSet<>();
//        for(ArrayList<TripId> idls:shapeTripList.values()){
//            for(int i=0;i<idls.size();i++){
//                all.add(idls.get(i));
//            }
//        }

        List<TripId> tripIds1 = tripIds.stream().distinct().collect(Collectors.toList());
        return Lists.newArrayList(tripIds1);
    }


}