package whu.edu.cs.transitnet.service.index;


<<<<<<< HEAD
import com.github.davidmoten.guavamini.Lists;
import org.locationtech.jts.triangulate.tri.Tri;
=======
import edu.whu.hyk.model.Point;
import edu.whu.hytra.EngineFactory;
>>>>>>> ee561d8 (construct shape-grid index and grid-shape index)
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import whu.edu.cs.transitnet.dao.ShapesDao;
<<<<<<< HEAD
import whu.edu.cs.transitnet.dao.TripsDao;
import whu.edu.cs.transitnet.pojo.TripsEntity;
import whu.edu.cs.transitnet.service.EncodeService;
=======
>>>>>>> ee561d8 (construct shape-grid index and grid-shape index)
import whu.edu.cs.transitnet.vo.ShapePointVo;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ShapeIndex {

    @Autowired
    RealtimeDataIndex realtimeDataIndex;

    @Autowired
    HytraEngineManager hytraEngineManager;
<<<<<<< HEAD

    @Autowired
    EncodeService encodeService;

    // shape - grid 的映射关系
    // arraylist 有序
    private HashMap<ShapeId, ArrayList<GridId>> shapeGridList;
//    private HashMap<ShapeId, ArrayList<GridId>> shapeGridList4;
//    private HashMap<ShapeId, ArrayList<GridId>> shapeGridList5;
//    private HashMap<ShapeId, ArrayList<GridId>> shapeGridList7;
//    private HashMap<ShapeId, ArrayList<GridId>> shapeGridList8;

    private HashMap<GridId, ArrayList<ShapeId>> gridShapeList;
//    private HashMap<GridId, ArrayList<ShapeId>> gridShapeList4;
//    private HashMap<GridId, ArrayList<ShapeId>> gridShapeList5;
//    private HashMap<GridId, ArrayList<ShapeId>> gridShapeList7;
//    private HashMap<GridId, ArrayList<ShapeId>> gridShapeList8;

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

//        shapeGridList4 = new HashMap<>();
//        gridShapeList4 = new HashMap<>();
//        shapeGridList5 = new HashMap<>();
//        gridShapeList5 = new HashMap<>();
//        shapeGridList7 = new HashMap<>();
//        gridShapeList7 = new HashMap<>();
//        shapeGridList8 = new HashMap<>();
//        gridShapeList8 = new HashMap<>();
    }

    // resolution = 4/5/7/8 时创建 txt 文件的操作
//    @PostConstruct
//    public void init() {
//
////        int resolution = hytraEngineManager.getParams().getResolution();
//        File shapeGridFile4 = new File("./src/main/" + "shape_grid_" + "4.txt");
//        File gridShapeFile4 = new File("./src/main/" + "grid_shape_" + "4.txt");
//        File shapeGridFile5 = new File("./src/main/" + "shape_grid_" + "5.txt");
//        File gridShapeFile5 = new File("./src/main/" + "grid_shape_" + "5.txt");
//        File shapeGridFile7 = new File("./src/main/" + "shape_grid_" + "7.txt");
//        File gridShapeFile7 = new File("./src/main/" + "grid_shape_" + "7.txt");
//        File shapeGridFile8 = new File("./src/main/" + "shape_grid_" + "8.txt");
//        File gridShapeFile8 = new File("./src/main/" + "grid_shape_" + "8.txt");
//
//        System.out.println("=============================");
//        System.out.println("[SHAPEINDEX] File Not Exists... Start fetching data from database...");
//
//        Long startTime = System.currentTimeMillis();
//        List<String> shapeIds = shapesDao.findAllShapeId();
//        Long endTime = System.currentTimeMillis();
//        System.out.println("[SHAPEINDEX] findAllShapeId time: " + (endTime - startTime) / 1000 / 60 + "min");
//        System.out.println("[SHAPEINDEX] Size of Shapes: " + shapeIds.size());
//
//        Long startTime1 = System.currentTimeMillis();
//        // 遍历每一个 shapeId
//
//        int num = 0;
//        for (String shape : shapeIds) {
//            num++;
//            System.out.println("[SHAPEINDEX] Number of Scanned Shapes: " + num);
//
//            // 取出每一个 shapeId 对应的点序列
//            List<ShapePointVo> shapePointVos = shapesDao.findAllByShapeId(shape);
//
//            ShapeId shapeId = new ShapeId(shape);
//
//
//            // point - grid 做映射
//            for (ShapePointVo shapePointVo : shapePointVos) {
//                GridId gridId4 = encodeService.getGridID(shapePointVo.getLat(), shapePointVo.getLng(), 4);
//                GridId gridId5 = encodeService.getGridID(shapePointVo.getLat(), shapePointVo.getLng(), 5);
//                GridId gridId7 = encodeService.getGridID(shapePointVo.getLat(), shapePointVo.getLng(), 7);
//                GridId gridId8 = encodeService.getGridID(shapePointVo.getLat(), shapePointVo.getLng(), 8);
//
//                // resolution = 4
//                // 构建 shape - grid 索引
//                ArrayList<GridId> gridIds4 = new ArrayList<>();
//                if (!shapeGridList4.containsKey(shapeId)) {
//                    gridIds4.add(gridId4);
//                    shapeGridList4.put(shapeId, gridIds4);
//                    } else if (shapeGridList4.get(shapeId).lastIndexOf(gridId4) != (shapeGridList4.get(shapeId).size() - 1)) {
//                    gridIds4 = shapeGridList4.get(shapeId);
//                    gridIds4.add(gridId4);
//                    shapeGridList4.put(shapeId, gridIds4);
//                } else {
//                    // 什么也不做
//                }
//
//                // 构建 grid - shape 索引
//                ArrayList<ShapeId> shapeIds4 = new ArrayList<>();
//                if (!gridShapeList4.containsKey(gridId4)) {
//                    shapeIds4.add(shapeId);
//                    gridShapeList4.put(gridId4, shapeIds4);
//                } else if (!gridShapeList4.get(gridId4).contains(shapeId)) {
//                    shapeIds4 = gridShapeList4.get(gridId4);
//                    shapeIds4.add(shapeId);
//                    gridShapeList4.put(gridId4, shapeIds4);
//                }
//
//                // resolution = 5
//                // 构建 shape - grid 索引
//                ArrayList<GridId> gridIds5 = new ArrayList<>();
//                if (!shapeGridList5.containsKey(shapeId)) {
//                    gridIds5.add(gridId5);
//                    shapeGridList5.put(shapeId, gridIds5);
//                } else if (shapeGridList5.get(shapeId).lastIndexOf(gridId5) != (shapeGridList5.get(shapeId).size() - 1)) {
//                    gridIds5 = shapeGridList5.get(shapeId);
//                    gridIds5.add(gridId5);
//                    shapeGridList5.put(shapeId, gridIds5);
//                } else {
//                    // 什么也不做
//                }
//
//                // 构建 grid - shape 索引
//                ArrayList<ShapeId> shapeIds5 = new ArrayList<>();
//                if (!gridShapeList5.containsKey(gridId5)) {
//                    shapeIds5.add(shapeId);
//                    gridShapeList5.put(gridId5, shapeIds5);
//                } else if (!gridShapeList5.get(gridId5).contains(shapeId)) {
//                    shapeIds5 = gridShapeList5.get(gridId5);
//                    shapeIds5.add(shapeId);
//                    gridShapeList5.put(gridId5, shapeIds5);
//                }
//
//                // resolution = 7
//                // 构建 shape - grid 索引
//                ArrayList<GridId> gridIds7 = new ArrayList<>();
//                if (!shapeGridList7.containsKey(shapeId)) {
//                    gridIds7.add(gridId7);
//                    shapeGridList7.put(shapeId, gridIds7);
//                } else if (shapeGridList7.get(shapeId).lastIndexOf(gridId7) != (shapeGridList7.get(shapeId).size() - 1)) {
//                    gridIds7 = shapeGridList7.get(shapeId);
//                    gridIds7.add(gridId7);
//                    shapeGridList7.put(shapeId, gridIds7);
//                } else {
//                    // 什么也不做
//                }
//
//                // 构建 grid - shape 索引
//                ArrayList<ShapeId> shapeIds7 = new ArrayList<>();
//                if (!gridShapeList7.containsKey(gridId7)) {
//                    shapeIds7.add(shapeId);
//                    gridShapeList7.put(gridId7, shapeIds7);
//                } else if (!gridShapeList7.get(gridId7).contains(shapeId)) {
//                    shapeIds7 = gridShapeList7.get(gridId7);
//                    shapeIds7.add(shapeId);
//                    gridShapeList7.put(gridId7, shapeIds7);
//                }
//
//                // resolution = 8
//                // 构建 shape - grid 索引
//                ArrayList<GridId> gridIds8 = new ArrayList<>();
//                if (!shapeGridList8.containsKey(shapeId)) {
//                    gridIds8.add(gridId8);
//                    shapeGridList8.put(shapeId, gridIds8);
//                } else if (shapeGridList8.get(shapeId).lastIndexOf(gridId8) != (shapeGridList8.get(shapeId).size() - 1)) {
//                    gridIds8 = shapeGridList8.get(shapeId);
//                    gridIds8.add(gridId8);
//                    shapeGridList8.put(shapeId, gridIds8);
//                } else {
//                    // 什么也不做
//                }
//
//                // 构建 grid - shape 索引
//                ArrayList<ShapeId> shapeIds8 = new ArrayList<>();
//                if (!gridShapeList8.containsKey(gridId8)) {
//                    shapeIds8.add(shapeId);
//                    gridShapeList8.put(gridId8, shapeIds8);
//                } else if (!gridShapeList8.get(gridId8).contains(shapeId)) {
//                    shapeIds8 = gridShapeList8.get(gridId8);
//                    shapeIds8.add(shapeId);
//                    gridShapeList8.put(gridId8, shapeIds8);
//                }
//            }
//
//        }
//
//        // try catch block
//        try {
//            FileOutputStream myFileOutStream41
//                    = new FileOutputStream(shapeGridFile4);
//            FileOutputStream myFileOutStream42
//                    = new FileOutputStream(gridShapeFile4);
//            FileOutputStream myFileOutStream51
//                    = new FileOutputStream(shapeGridFile5);
//            FileOutputStream myFileOutStream52
//                    = new FileOutputStream(gridShapeFile5);
//            FileOutputStream myFileOutStream71
//                    = new FileOutputStream(shapeGridFile7);
//            FileOutputStream myFileOutStream72
//                    = new FileOutputStream(gridShapeFile7);
//            FileOutputStream myFileOutStream81
//                    = new FileOutputStream(shapeGridFile8);
//            FileOutputStream myFileOutStream82
//                    = new FileOutputStream(gridShapeFile8);
//
//
//            ObjectOutputStream myObjectOutStream41
//                    = new ObjectOutputStream(myFileOutStream41);
//            ObjectOutputStream myObjectOutStream42
//                    = new ObjectOutputStream(myFileOutStream42);
//            ObjectOutputStream myObjectOutStream51
//                    = new ObjectOutputStream(myFileOutStream51);
//            ObjectOutputStream myObjectOutStream52
//                    = new ObjectOutputStream(myFileOutStream52);
//            ObjectOutputStream myObjectOutStream71
//                    = new ObjectOutputStream(myFileOutStream71);
//            ObjectOutputStream myObjectOutStream72
//                    = new ObjectOutputStream(myFileOutStream72);
//            ObjectOutputStream myObjectOutStream81
//                    = new ObjectOutputStream(myFileOutStream81);
//            ObjectOutputStream myObjectOutStream82
//                    = new ObjectOutputStream(myFileOutStream82);
//
//            myObjectOutStream41.writeObject(shapeGridList4);
//            myObjectOutStream42.writeObject(gridShapeList4);
//            myObjectOutStream51.writeObject(shapeGridList5);
//            myObjectOutStream52.writeObject(gridShapeList5);
//            myObjectOutStream71.writeObject(shapeGridList7);
//            myObjectOutStream72.writeObject(gridShapeList7);
//            myObjectOutStream81.writeObject(shapeGridList8);
//            myObjectOutStream82.writeObject(gridShapeList8);
//
//
//            // closing FileOutputStream and
//            // ObjectOutputStream
//            myObjectOutStream41.close();
//            myFileOutStream41.close();
//            myObjectOutStream42.close();
//            myFileOutStream42.close();
//            myObjectOutStream51.close();
//            myFileOutStream51.close();
//            myObjectOutStream52.close();
//            myFileOutStream52.close();
//            myObjectOutStream71.close();
//            myFileOutStream71.close();
//            myObjectOutStream72.close();
//            myFileOutStream72.close();
//            myObjectOutStream81.close();
//            myFileOutStream81.close();
//            myObjectOutStream82.close();
//            myFileOutStream82.close();
//        }
//        catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        Long endTime1 = System.currentTimeMillis();
//
//        System.out.println("[SHAPEINDEX] index construction and serialization time: " + (endTime1 - startTime1) / 1000 + "s");
//    }


    // resolution = 6 时创建 txt 文件的操作
//    @PostConstruct
    public void init() {

        int resolution = hytraEngineManager.getParams().getResolution();
        File shapeGridFile = new File("./src/main/" + "shape_grid_" + resolution + ".txt");
        File gridShapeFile = new File("./src/main/" + "grid_shape_" + resolution + ".txt");
        File shapeTripFile = new File("./src/main/" + "shape_trip"+ ".txt");

        if (shapeGridFile.exists() && gridShapeFile.exists() && shapeTripFile.exists()) {
            // 读取文件
            System.out.println("======================");
            System.out.println("[SHAPEINDEX] FILE EXISTS...");
            System.out.println("======================");
            System.out.println("[SHAPEINDEX] Start Deserializing HashMap..");

            Long starttime = System.currentTimeMillis();


            try {
                FileInputStream fileInput1 = new FileInputStream(
                        shapeGridFile);
                FileInputStream fileInput2 = new FileInputStream(
                        gridShapeFile);
                FileInputStream fileInput3 = new FileInputStream(
                        shapeTripFile);


                ObjectInputStream objectInput1
                        = new ObjectInputStream(fileInput1);
                ObjectInputStream objectInput2
                        = new ObjectInputStream(fileInput2);
                ObjectInputStream objectInput3
                        = new ObjectInputStream(fileInput3);

                shapeGridList = (HashMap)objectInput1.readObject();
                gridShapeList = (HashMap)objectInput2.readObject();
                shapeTripList = (HashMap)objectInput3.readObject();

                objectInput1.close();
                fileInput1.close();
                objectInput2.close();
                fileInput2.close();
                objectInput3.close();
                fileInput3.close();
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


            // Displaying content in "newHashMap.txt" using
            // Iterator
//            Set set = shapeGridList.entrySet();
//            Iterator iterator = set.iterator();
//
//            while (iterator.hasNext()) {
//                Map.Entry entry = (Map.Entry)iterator.next();
//
//                System.out.print("key : " + entry.getKey()
//                        + " & Value : ");
//                System.out.println(entry.getValue());
//            }

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
            try {
                FileOutputStream myFileOutStream1
                        = new FileOutputStream(shapeGridFile);
                FileOutputStream myFileOutStream2
                        = new FileOutputStream(gridShapeFile);
                FileOutputStream myFileOutStream3
                        = new FileOutputStream(shapeTripFile);

                ObjectOutputStream myObjectOutStream1
                        = new ObjectOutputStream(myFileOutStream1);
                ObjectOutputStream myObjectOutStream2
                        = new ObjectOutputStream(myFileOutStream2);
                ObjectOutputStream myObjectOutStream3
                        = new ObjectOutputStream(myFileOutStream3);

                myObjectOutStream1.writeObject(shapeGridList);
                myObjectOutStream2.writeObject(gridShapeList);
                myObjectOutStream3.writeObject(shapeTripList);

                // closing FileOutputStream and
                // ObjectOutputStream
                myObjectOutStream1.close();
                myFileOutStream1.close();
                myObjectOutStream2.close();
                myFileOutStream2.close();
                myObjectOutStream3.close();
                myFileOutStream3.close();
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

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
=======


    // shape - grid 的映射关系
    // arraylist 有序
    private HashMap<ShapeId, ArrayList<GridId>> shapeGridList;

    private HashMap<GridId, ArrayList<ShapeId>> gridShapeList;


    @Autowired
    ShapesDao shapesDao;
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
        }
    }
        // TODO 构建 2 个索引

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
>>>>>>> ee561d8 (construct shape-grid index and grid-shape index)
        // 1. 过滤所有有交集的shape
        for (GridId grid : userPassedGrids) {
            if (gridShapeList.keySet().contains(grid)) {
                shapeCandidates.addAll(gridShapeList.get(grid));
            }
        }
        // 2. 返回相似度最大的前k的shapeId
<<<<<<< HEAD
//        int theta = 5;

        HashMap<ShapeId, Double> shapeSimMap = new HashMap<>();

//        List<ShapeId> topShapes = shapeGridList.entrySet().stream().filter(entry -> shapeCandidates.contains(entry.getKey())).sorted((a, b) -> getGridSimilarity(a.getValue(), userPassedGrids) >= getGridSimilarity(b.getValue(), userPassedGrids) ? -1 : 1).limit(k).map(Map.Entry::getKey).collect(Collectors.toList());
        List<ShapeId> topShapes = shapeGridList.entrySet().stream().filter(entry -> shapeCandidates.contains(entry.getKey())).map(Map.Entry::getKey).collect(Collectors.toList());
        Collections.sort(topShapes, new Comparator<ShapeId>() {
            @Override
            public int compare(ShapeId a, ShapeId b) { // 从大到小
                Double t = getGridSimilarity(shapeGridList.get(a), userPassedGrids) - getGridSimilarity(shapeGridList.get(b), userPassedGrids);
                int flag = -1;
                if (t < 0) flag = 1;
                if (t == 0) flag = 0;
                return flag;
            }
        });

        for (ShapeId shapeId : topShapes) {
            Double sim = getGridSimilarity(shapeGridList.get(shapeId), userPassedGrids);
            shapeSimMap.put(shapeId, sim);
        }

        System.out.println("=============================");

        System.out.println("[SHAPEINDEX] " + shapeGridList.get(userShapeId));
        System.out.println("[SHAPEINDEX] " + shapeSimMap);
        System.out.println("[SHAPEINDEX] " + topShapes);
        if (topShapes.size() >= k) {
            return Lists.newArrayList(topShapes.subList(0, k));
        } else {
            return Lists.newArrayList(topShapes);
        }

    }

//    public double getGridSimilarity(ArrayList<GridId> grids1, ArrayList<GridId> grids2, int theta) {
    public double getGridSimilarity(ArrayList<GridId> grids1, ArrayList<GridId> grids2) {
=======
        int theta = 5;
        List<ShapeId> topShapes = shapeGridList.entrySet().stream().filter(entry -> shapeCandidates.contains(entry.getKey()))
                .sorted((a, b) -> getSimilarity(a.getValue(), userPassedGrids, theta) >= getSimilarity(b.getValue(), userPassedGrids, theta) ? -1 : 1)
                .limit(k).map(Map.Entry::getKey).collect(Collectors.toList());

    }

    public double getSimilarity(ArrayList<GridId> grids1, ArrayList<GridId> grids2, int theta) {
>>>>>>> ee561d8 (construct shape-grid index and grid-shape index)
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
                dp[i][0] = dp[i - 1][0];
            }
        }

        for (int j = 1; j < grids2.size(); j++) {
            if (grids2.get(j).toString().equals(grids1.get(0).toString())) {
                dp[0][j] = 1;
            } else {
                dp[0][j] = dp[0][j - 1];
            }
        }

        for (int i = 1; i < grids1.size(); i++) {
            for (int j = 1; j < grids2.size(); j++) {
<<<<<<< HEAD
//                if (Math.abs(i - j) <= theta) {
                if (Math.abs(i - j) <= Integer.MAX_VALUE) {
                    if (grids1.get(i).equals(grids2.get(j))) {
                        dp[i][j] = 1 + dp[i - 1][j - 1];
=======
                if (Math.abs(i - j) <= theta) {
                    if (grids1.get(i).toString().equals(grids2.get(j).toString())) {
                        dp[i][j] = 1 + dp[i-1][j-1];
>>>>>>> ee561d8 (construct shape-grid index and grid-shape index)
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
//        System.out.println("两条轨迹的相似度为：" + maxSimilarity);
        return maxSimilarity;
    }

    public ArrayList<TripId> getTripsOfTopKShapes(ShapeId userShapeId, ArrayList<GridId> userPassedGrids, int k) {
        ArrayList<ShapeId> topKShapes = getTopKShapes(userShapeId, userPassedGrids, k);
        ArrayList<TripId> tripIds = new ArrayList<>();

<<<<<<< HEAD
        for (ShapeId shapeId : topKShapes) {
            tripIds.addAll(shapeTripList.get(shapeId));
        }

        List<TripId> tripIds1 = tripIds.stream().distinct().collect(Collectors.toList());
        return Lists.newArrayList(tripIds1);
    }


=======
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
>>>>>>> ee561d8 (construct shape-grid index and grid-shape index)
}