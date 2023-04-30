package whu.edu.cs.transitnet.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import whu.edu.cs.transitnet.pojo.TripsEntity;
import whu.edu.cs.transitnet.vo.StringPair;

import java.sql.Date;
import java.util.List;

public interface TripsDao extends JpaRepository<TripsEntity, String> {
    @Query(value = "SELECT * FROM trips", nativeQuery = true)
    List<TripsEntity> findAll();

    @Query(value = "SELECT * FROM trips WHERE trip_id = ?1", nativeQuery = true)
    List<TripsEntity> findAllByTripId(String tripId);

    @Query(value = "SELECT * FROM trips WHERE route_id =?1", nativeQuery = true)
    List<TripsEntity> findAllByRouteId(String routeId);

    @Query(value = "SELECT * FROM trips WHERE shape_id = ?1", nativeQuery = true)
    List<TripsEntity> findAllByShapeId(String shapeId);

    @Query(value = "SELECT * FROM trips GROUP BY route_id", nativeQuery = true)
    List<TripsEntity> findOriginTrips();

    @Query(value = "SELECT * FROM trips WHERE service_id IN (SELECT service_id "
            + "FROM calendar WHERE start_date <= ?1 and end_date >= ?2)  "
            + " GROUP BY route_id",
            nativeQuery = true)
    List<TripsEntity> findAllTripsByTimeSpan(Date startDate, Date endDate);

    @Query(value = "SELECT * FROM trips WHERE service_id IN (SELECT service_id "
            + "FROM calendar WHERE start_date <= ?2 and end_date >= ?3) "
            + " AND route_id = ?1", nativeQuery = true)
    List<TripsEntity> findAllTripsByRouteIdAndTimeSpan(String routeId, Date startDate, Date endDate);


    @Query(value = "SELECT DISTINCT new whu.edu.cs.transitnet.vo.StringPair("
            + "te.routeId, te.shapeId)"
            + "FROM TripsEntity te")
    List<StringPair> findAllRouteIdAndShapeIdPair();

}
