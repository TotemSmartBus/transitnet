package whu.edu.cs.transitnet.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import whu.edu.cs.transitnet.pojo.ShapesEntity;
import whu.edu.cs.transitnet.pojo.ShapesEntityPK;
import whu.edu.cs.transitnet.vo.ShapePointVo;

import java.util.List;

public interface ShapesDao extends JpaRepository<ShapesEntity, ShapesEntityPK> {

    @Query(value = "SELECT new whu.edu.cs.transitnet.vo.ShapePointVo("
            + "se.shapePtLat, se.shapePtLon)"
            + "FROM ShapesEntity se left join TripsEntity te on se.shapeId = te.shapeId "
            + "WHERE te.routeId = ?1 AND te.tripId = ?2 "
            + "ORDER BY se.shapePtSequence")
    List<ShapePointVo> findAllByRouteIdAndTripId(String routeId, String tripId);

    @Query(value = "SELECT new whu.edu.cs.transitnet.vo.ShapePointVo("
            + "se.shapePtLat, se.shapePtLon)"
            + "FROM ShapesEntity se "
            + "WHERE se.shapeId = ?1 "
            + "ORDER BY se.shapePtSequence")
    List<ShapePointVo> findAllByShapeId(String shapeId);

    @Query(value = "SELECT DISTINCT shape_id FROM shapes", nativeQuery = true)
    List<String> findAllShapeId();

    ShapesEntity findByShapeIdAndShapePtSequence(String shapeId, String shapePtSequence);
}
