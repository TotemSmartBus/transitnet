package whu.edu.cs.transitnet;

<<<<<<< HEAD
import org.springframework.beans.factory.annotation.Autowired;
import whu.edu.cs.transitnet.service.index.ShapeIndex;

=======
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import whu.edu.cs.transitnet.service.index.ShapeIndex;

import java.util.ArrayList;
import java.util.HashMap;

>>>>>>> ee561d8 (construct shape-grid index and grid-shape index)
public class GridIdTest {
    @Autowired
    ShapeIndex shapeIndex;

//    @Test
//    public void gridIdTest() {
//        shapeIndex = new ShapeIndex();
//
//        ShapeIndex.GridId gridId1 = new ShapeIndex.GridId("11");
//        ShapeIndex.GridId gridId2 = new ShapeIndex.GridId("12");
//        ShapeIndex.GridId gridId3 = new ShapeIndex.GridId("13");
//        ShapeIndex.GridId gridId4 = new ShapeIndex.GridId("14");
//        ShapeIndex.GridId gridId5 = new ShapeIndex.GridId("15");
//        ShapeIndex.GridId gridId6 = new ShapeIndex.GridId("16");
//
//        ArrayList<ShapeIndex.GridId> g1 = new ArrayList<>();
//        g1.add(gridId1);
//        g1.add(gridId2);
//        g1.add(gridId3);
//        g1.add(gridId4);
//        g1.add(gridId5);
//        g1.add(gridId6);
//
//
//        ShapeIndex.GridId gridId7 = new ShapeIndex.GridId("11");
//        ShapeIndex.GridId gridId8 = new ShapeIndex.GridId("11");
//        ShapeIndex.GridId gridId9 = new ShapeIndex.GridId("12");
//        ShapeIndex.GridId gridId10 = new ShapeIndex.GridId("13");
//        ShapeIndex.GridId gridId11 = new ShapeIndex.GridId("14");
//        ShapeIndex.GridId gridId12 = new ShapeIndex.GridId("11");
//
//        ArrayList<ShapeIndex.GridId> g2 = new ArrayList<>();
//        g2.add(gridId7);
//        g2.add(gridId8);
//        g2.add(gridId9);
//        g2.add(gridId10);
//        g2.add(gridId11);
//        g2.add(gridId12);
//
//
//        System.out.println(gridId1);
//        System.out.println(gridId1.equals(gridId7));
//        System.out.println(gridId1.getContent().equals(gridId7.getContent()));
//        Double sim = shapeIndex.getSimilarity(g1, g2, 1000);
//        System.out.println(sim);
//    }
//
//    @Test
//    public void SimTest() {
//        shapeIndex = new ShapeIndex();
//
//        ShapeIndex.GridId gridId1 = new ShapeIndex.GridId("11");
//        ShapeIndex.GridId gridId2 = new ShapeIndex.GridId("12");
//        ShapeIndex.GridId gridId3 = new ShapeIndex.GridId("13");
//        ShapeIndex.GridId gridId4 = new ShapeIndex.GridId("14");
//        ShapeIndex.GridId gridId5 = new ShapeIndex.GridId("15");
//        ShapeIndex.GridId gridId6 = new ShapeIndex.GridId("16");
//
//        ArrayList<ShapeIndex.GridId> g_user = new ArrayList<>();
//        g_user.add(gridId1);
//        g_user.add(gridId2);
//        g_user.add(gridId3);
//        g_user.add(gridId4);
//        g_user.add(gridId5);
//        g_user.add(gridId6);
//
//
//
//
//
//        ShapeIndex.ShapeId shapeId1 = new ShapeIndex.ShapeId("07");
//        ShapeIndex.ShapeId shapeId2 = new ShapeIndex.ShapeId("15");
//        ShapeIndex.ShapeId shapeId3 = new ShapeIndex.ShapeId("03");
//        ShapeIndex.ShapeId shapeId4 = new ShapeIndex.ShapeId("04");
//        ShapeIndex.ShapeId shapeId5 = new ShapeIndex.ShapeId("05");
//
//        HashMap<ShapeIndex.ShapeId, ArrayList<ShapeIndex.GridId>> shapeGridList = new HashMap<>();
//        HashMap<ShapeIndex.GridId, ArrayList<ShapeIndex.ShapeId>> gridShapeList = new HashMap<>();
//
//
//
//
//        ShapeIndex.GridId gridId11 = new ShapeIndex.GridId("11");
//        ShapeIndex.GridId gridId12 = new ShapeIndex.GridId("12");
//        ShapeIndex.GridId gridId13 = new ShapeIndex.GridId("13");
//        ShapeIndex.GridId gridId14 = new ShapeIndex.GridId("14");
//        ShapeIndex.GridId gridId15 = new ShapeIndex.GridId("15");
//        ShapeIndex.GridId gridId16 = new ShapeIndex.GridId("15");
//
//        ArrayList<ShapeIndex.GridId> g_1 = new ArrayList<>();
//        g_1.add(gridId11);
//        g_1.add(gridId12);
//        g_1.add(gridId13);
//        g_1.add(gridId14);
//        g_1.add(gridId15);
//        g_1.add(gridId16);
//
//        shapeGridList.put(shapeId1, g_1);
//
//        ShapeIndex.GridId gridId111 = new ShapeIndex.GridId("11");
//        ShapeIndex.GridId gridId112 = new ShapeIndex.GridId("12");
//        ShapeIndex.GridId gridId113 = new ShapeIndex.GridId("13");
//        ShapeIndex.GridId gridId114 = new ShapeIndex.GridId("15");
//        ShapeIndex.GridId gridId115 = new ShapeIndex.GridId("15");
//        ShapeIndex.GridId gridId116 = new ShapeIndex.GridId("15");
//
//        ArrayList<ShapeIndex.GridId> g_2 = new ArrayList<>();
//        g_2.add(gridId111);
//        g_2.add(gridId112);
//        g_2.add(gridId113);
//        g_2.add(gridId114);
//        g_2.add(gridId115);
//        g_2.add(gridId116);
//
//        shapeGridList.put(shapeId2, g_2);
//
//
//        ShapeIndex.GridId gridId1111 = new ShapeIndex.GridId("11");
//        ShapeIndex.GridId gridId1112 = new ShapeIndex.GridId("12");
//        ShapeIndex.GridId gridId1113 = new ShapeIndex.GridId("15");
//        ShapeIndex.GridId gridId1114 = new ShapeIndex.GridId("15");
//        ShapeIndex.GridId gridId1115 = new ShapeIndex.GridId("15");
//        ShapeIndex.GridId gridId1116 = new ShapeIndex.GridId("15");
//
//        ArrayList<ShapeIndex.GridId> g_3 = new ArrayList<>();
//        g_3.add(gridId1111);
//        g_3.add(gridId1112);
//        g_3.add(gridId1113);
//        g_3.add(gridId1114);
//        g_3.add(gridId1115);
//        g_3.add(gridId1116);
//
//        shapeGridList.put(shapeId3, g_3);
//
//
//        ShapeIndex.GridId gridId11111 = new ShapeIndex.GridId("11");
//        ShapeIndex.GridId gridId11112 = new ShapeIndex.GridId("15");
//        ShapeIndex.GridId gridId11113 = new ShapeIndex.GridId("15");
//        ShapeIndex.GridId gridId11114 = new ShapeIndex.GridId("15");
//        ShapeIndex.GridId gridId11115 = new ShapeIndex.GridId("15");
//        ShapeIndex.GridId gridId11116 = new ShapeIndex.GridId("15");
//
//        ArrayList<ShapeIndex.GridId> g_4 = new ArrayList<>();
//        g_4.add(gridId11111);
//        g_4.add(gridId11112);
//        g_4.add(gridId11113);
//        g_4.add(gridId11114);
//        g_4.add(gridId11115);
//        g_4.add(gridId11116);
//
//        shapeGridList.put(shapeId4, g_4);
//
//        shapeIndex.setGridShapeList(gridShapeList);
//        shapeIndex.setshapeGridList(shapeGridList);
//
////        System.out.println(ShapeIndex.getTopKShapes(g_user, 2));
//
//    }
}
