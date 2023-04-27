package whu.edu.cs.transitnet.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import whu.edu.cs.transitnet.pojo.SplitShapeEntity;
import whu.edu.cs.transitnet.pojo.SplitShapeEntityPK;
import whu.edu.cs.transitnet.vo.ShapePointVo;

import java.util.List;

public interface SplitShapeDao extends JpaRepository<SplitShapeEntity, SplitShapeEntityPK> {
    List<SplitShapeEntity> findAll();

//    List<SplitShapeEntity> findAllByShapeId(String shapeId);
//    List<SplitShapeEntity> findAllByShapeIdOrderByPtId(String shapeId);

    @Query("SELECT new whu.edu.cs.transitnet.vo.ShapePointVo(sse.ptId, sse.lat, sse.lon) FROM SplitShapeEntity sse " +
            "WHERE sse.shapeId = ?1 AND sse.splitId = ?2 " +
            "ORDER BY sse.ptId")
    List<ShapePointVo> findAllShapePointsByShapeIdAndSplitId(String shapeId, Integer splitId);

    @Query("SELECT DISTINCT ssse.shapeId FROM SplitShapeSpeedEntity ssse")
    List<String> findAllShapeId();

    @Query("SELECT MIN(ssse.shapeId) FROM SplitShapeSpeedEntity ssse " +
            "GROUP BY ssse.routeId")
    List<String> findAllShapeIdOne();
}
