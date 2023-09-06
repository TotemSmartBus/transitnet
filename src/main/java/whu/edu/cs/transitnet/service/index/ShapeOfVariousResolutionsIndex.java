package whu.edu.cs.transitnet.service.index;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import whu.edu.cs.transitnet.dao.ShapesDao;
import whu.edu.cs.transitnet.dao.TripsDao;
import whu.edu.cs.transitnet.service.EncodeService;
import whu.edu.cs.transitnet.vo.ShapePointVo;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class ShapeOfVariousResolutionsIndex {
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
    private HashMap<ShapeId, ArrayList<GridId>> shapeGridList4;
    private HashMap<ShapeId, ArrayList<GridId>> shapeGridList5;
    private HashMap<ShapeId, ArrayList<GridId>> shapeGridList7;
    private HashMap<ShapeId, ArrayList<GridId>> shapeGridList8;

    private HashMap<GridId, ArrayList<ShapeId>> gridShapeList4;
    private HashMap<GridId, ArrayList<ShapeId>> gridShapeList5;
    private HashMap<GridId, ArrayList<ShapeId>> gridShapeList7;
    private HashMap<GridId, ArrayList<ShapeId>> gridShapeList8;

    // shape_id - trip_id
    private HashMap<ShapeId, ArrayList<TripId>> shapeTripList;


    @Autowired
    ShapesDao shapesDao;

    @Autowired
    TripsDao tripsDao;

    public ShapeOfVariousResolutionsIndex() {
        shapeTripList = new HashMap<>();

        shapeGridList4 = new HashMap<>();
        gridShapeList4 = new HashMap<>();
        shapeGridList5 = new HashMap<>();
        gridShapeList5 = new HashMap<>();
        shapeGridList7 = new HashMap<>();
        gridShapeList7 = new HashMap<>();
        shapeGridList8 = new HashMap<>();
        gridShapeList8 = new HashMap<>();
    }


    // @PostConstruct

    /**
     * resolution = 4/5/7/8 时创建 txt 文件的操作
     */
    public void init() {
        File shapeGridFile4 = new File("./src/main/" + "shape_grid_" + "4.txt");
        File gridShapeFile4 = new File("./src/main/" + "grid_shape_" + "4.txt");
        File shapeGridFile5 = new File("./src/main/" + "shape_grid_" + "5.txt");
        File gridShapeFile5 = new File("./src/main/" + "grid_shape_" + "5.txt");
        File shapeGridFile7 = new File("./src/main/" + "shape_grid_" + "7.txt");
        File gridShapeFile7 = new File("./src/main/" + "grid_shape_" + "7.txt");
        File shapeGridFile8 = new File("./src/main/" + "shape_grid_" + "8.txt");
        File gridShapeFile8 = new File("./src/main/" + "grid_shape_" + "8.txt");

        System.out.println("=============================");
        System.out.println("[SHAPEINDEX] File Not Exists... Start fetching data from database...");

        Long startTime = System.currentTimeMillis();
        List<String> shapeIds = shapesDao.findAllShapeId();
        Long endTime = System.currentTimeMillis();
        System.out.println("[SHAPEINDEX] findAllShapeId time: " + (endTime - startTime) / 1000 / 60 + "min");
        System.out.println("[SHAPEINDEX] Size of Shapes: " + shapeIds.size());

        Long startTime1 = System.currentTimeMillis();
        // 遍历每一个 shapeId

        int num = 0;
        for (String shape : shapeIds) {
            num++;
            System.out.println("[SHAPEINDEX] Number of Scanned Shapes: " + num);

            // 取出每一个 shapeId 对应的点序列
            List<ShapePointVo> shapePointVos = shapesDao.findAllByShapeId(shape);

            ShapeId shapeId = new ShapeId(shape);


            // point - grid 做映射
            for (ShapePointVo shapePointVo : shapePointVos) {
                GridId gridId4 = encodeService.getGridID(shapePointVo.getLat(), shapePointVo.getLng(), 4);
                GridId gridId5 = encodeService.getGridID(shapePointVo.getLat(), shapePointVo.getLng(), 5);
                GridId gridId7 = encodeService.getGridID(shapePointVo.getLat(), shapePointVo.getLng(), 7);
                GridId gridId8 = encodeService.getGridID(shapePointVo.getLat(), shapePointVo.getLng(), 8);

                // resolution = 4
                // 构建 shape - grid 索引
                ArrayList<GridId> gridIds4 = new ArrayList<>();
                if (!shapeGridList4.containsKey(shapeId)) {
                    gridIds4.add(gridId4);
                    shapeGridList4.put(shapeId, gridIds4);
                    } else if (shapeGridList4.get(shapeId).lastIndexOf(gridId4) != (shapeGridList4.get(shapeId).size() - 1)) {
                    gridIds4 = shapeGridList4.get(shapeId);
                    gridIds4.add(gridId4);
                    shapeGridList4.put(shapeId, gridIds4);
                } else {
                    // 什么也不做
                }

                // 构建 grid - shape 索引
                ArrayList<ShapeId> shapeIds4 = new ArrayList<>();
                if (!gridShapeList4.containsKey(gridId4)) {
                    shapeIds4.add(shapeId);
                    gridShapeList4.put(gridId4, shapeIds4);
                } else if (!gridShapeList4.get(gridId4).contains(shapeId)) {
                    shapeIds4 = gridShapeList4.get(gridId4);
                    shapeIds4.add(shapeId);
                    gridShapeList4.put(gridId4, shapeIds4);
                }

                // resolution = 5
                // 构建 shape - grid 索引
                ArrayList<GridId> gridIds5 = new ArrayList<>();
                if (!shapeGridList5.containsKey(shapeId)) {
                    gridIds5.add(gridId5);
                    shapeGridList5.put(shapeId, gridIds5);
                } else if (shapeGridList5.get(shapeId).lastIndexOf(gridId5) != (shapeGridList5.get(shapeId).size() - 1)) {
                    gridIds5 = shapeGridList5.get(shapeId);
                    gridIds5.add(gridId5);
                    shapeGridList5.put(shapeId, gridIds5);
                } else {
                    // 什么也不做
                }

                // 构建 grid - shape 索引
                ArrayList<ShapeId> shapeIds5 = new ArrayList<>();
                if (!gridShapeList5.containsKey(gridId5)) {
                    shapeIds5.add(shapeId);
                    gridShapeList5.put(gridId5, shapeIds5);
                } else if (!gridShapeList5.get(gridId5).contains(shapeId)) {
                    shapeIds5 = gridShapeList5.get(gridId5);
                    shapeIds5.add(shapeId);
                    gridShapeList5.put(gridId5, shapeIds5);
                }

                // resolution = 7
                // 构建 shape - grid 索引
                ArrayList<GridId> gridIds7 = new ArrayList<>();
                if (!shapeGridList7.containsKey(shapeId)) {
                    gridIds7.add(gridId7);
                    shapeGridList7.put(shapeId, gridIds7);
                } else if (shapeGridList7.get(shapeId).lastIndexOf(gridId7) != (shapeGridList7.get(shapeId).size() - 1)) {
                    gridIds7 = shapeGridList7.get(shapeId);
                    gridIds7.add(gridId7);
                    shapeGridList7.put(shapeId, gridIds7);
                } else {
                    // 什么也不做
                }

                // 构建 grid - shape 索引
                ArrayList<ShapeId> shapeIds7 = new ArrayList<>();
                if (!gridShapeList7.containsKey(gridId7)) {
                    shapeIds7.add(shapeId);
                    gridShapeList7.put(gridId7, shapeIds7);
                } else if (!gridShapeList7.get(gridId7).contains(shapeId)) {
                    shapeIds7 = gridShapeList7.get(gridId7);
                    shapeIds7.add(shapeId);
                    gridShapeList7.put(gridId7, shapeIds7);
                }

                // resolution = 8
                // 构建 shape - grid 索引
                ArrayList<GridId> gridIds8 = new ArrayList<>();
                if (!shapeGridList8.containsKey(shapeId)) {
                    gridIds8.add(gridId8);
                    shapeGridList8.put(shapeId, gridIds8);
                } else if (shapeGridList8.get(shapeId).lastIndexOf(gridId8) != (shapeGridList8.get(shapeId).size() - 1)) {
                    gridIds8 = shapeGridList8.get(shapeId);
                    gridIds8.add(gridId8);
                    shapeGridList8.put(shapeId, gridIds8);
                } else {
                    // 什么也不做
                }

                // 构建 grid - shape 索引
                ArrayList<ShapeId> shapeIds8 = new ArrayList<>();
                if (!gridShapeList8.containsKey(gridId8)) {
                    shapeIds8.add(shapeId);
                    gridShapeList8.put(gridId8, shapeIds8);
                } else if (!gridShapeList8.get(gridId8).contains(shapeId)) {
                    shapeIds8 = gridShapeList8.get(gridId8);
                    shapeIds8.add(shapeId);
                    gridShapeList8.put(gridId8, shapeIds8);
                }
            }

        }

        // try catch block
        try {
            FileOutputStream myFileOutStream41
                    = new FileOutputStream(shapeGridFile4);
            FileOutputStream myFileOutStream42
                    = new FileOutputStream(gridShapeFile4);
            FileOutputStream myFileOutStream51
                    = new FileOutputStream(shapeGridFile5);
            FileOutputStream myFileOutStream52
                    = new FileOutputStream(gridShapeFile5);
            FileOutputStream myFileOutStream71
                    = new FileOutputStream(shapeGridFile7);
            FileOutputStream myFileOutStream72
                    = new FileOutputStream(gridShapeFile7);
            FileOutputStream myFileOutStream81
                    = new FileOutputStream(shapeGridFile8);
            FileOutputStream myFileOutStream82
                    = new FileOutputStream(gridShapeFile8);


            ObjectOutputStream myObjectOutStream41
                    = new ObjectOutputStream(myFileOutStream41);
            ObjectOutputStream myObjectOutStream42
                    = new ObjectOutputStream(myFileOutStream42);
            ObjectOutputStream myObjectOutStream51
                    = new ObjectOutputStream(myFileOutStream51);
            ObjectOutputStream myObjectOutStream52
                    = new ObjectOutputStream(myFileOutStream52);
            ObjectOutputStream myObjectOutStream71
                    = new ObjectOutputStream(myFileOutStream71);
            ObjectOutputStream myObjectOutStream72
                    = new ObjectOutputStream(myFileOutStream72);
            ObjectOutputStream myObjectOutStream81
                    = new ObjectOutputStream(myFileOutStream81);
            ObjectOutputStream myObjectOutStream82
                    = new ObjectOutputStream(myFileOutStream82);

            myObjectOutStream41.writeObject(shapeGridList4);
            myObjectOutStream42.writeObject(gridShapeList4);
            myObjectOutStream51.writeObject(shapeGridList5);
            myObjectOutStream52.writeObject(gridShapeList5);
            myObjectOutStream71.writeObject(shapeGridList7);
            myObjectOutStream72.writeObject(gridShapeList7);
            myObjectOutStream81.writeObject(shapeGridList8);
            myObjectOutStream82.writeObject(gridShapeList8);


            // closing FileOutputStream and
            // ObjectOutputStream
            myObjectOutStream41.close();
            myFileOutStream41.close();
            myObjectOutStream42.close();
            myFileOutStream42.close();
            myObjectOutStream51.close();
            myFileOutStream51.close();
            myObjectOutStream52.close();
            myFileOutStream52.close();
            myObjectOutStream71.close();
            myFileOutStream71.close();
            myObjectOutStream72.close();
            myFileOutStream72.close();
            myObjectOutStream81.close();
            myFileOutStream81.close();
            myObjectOutStream82.close();
            myFileOutStream82.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Long endTime1 = System.currentTimeMillis();

        System.out.println("[SHAPEINDEX] index construction and serialization time: " + (endTime1 - startTime1) / 1000 + "s");
    }

}
