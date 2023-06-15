package whu.edu.cs.transitnet.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import whu.edu.cs.transitnet.pojo.RealTimeDataEntity;
import whu.edu.cs.transitnet.pojo.RealTimePointEntity;

import java.util.List;


public interface RealTimeDataDao extends JpaRepository<RealTimeDataEntity, String> {

    @Query(value = "SELECT  route_id, direction, trip_id, agency_id, origin_stop, lat, lon, bearing, vehicle_id, aimed_arrival_time, distance_from_origin, presentable_distance, distance_from_next_stop, next_stop, MAX(recorded_time) as recorded_time from real_time_data_temp " +
            "WHERE recorded_time > ?1 AND recorded_time <= ?2 GROUP BY vehicle_id", nativeQuery = true)
    List<RealTimeDataEntity> findAllLastByRecordedTimeSpan(String startTime, String endTime);

    @Query(value = "SELECT DISTINCT vehicle_id " +
            "FROM real_time_data_temp " +
            "WHERE recorded_time = ?1", nativeQuery = true)
    List<String> findAllVehicleIdByRecordedTime(String recordedTime);

    @Query(value = "SELECT new whu.edu.cs.transitnet.pojo.RealTimePointEntity(rte.tripId, rte.vehicleId, rte.lat, rte.lon, rte.recordedTime) " +
            "FROM RealTimeDataEntity rte " +
            "WHERE rte.vehicleId = ?1 AND rte.recordedTime >= ?2 AND rte.recordedTime < ?3 ORDER BY rte.recordedTime")
    List<RealTimePointEntity> findAllPointsByVehicleIdByTimeSpan(String vehicleId, String startTime, String endTime);

    @Query(value = "SELECT new whu.edu.cs.transitnet.pojo.RealTimePointEntity(rte.tripId, rte.vehicleId, rte.lat, rte.lon, rte.recordedTime) " +
            "FROM RealTimeDataEntity  rte " +
            "WHERE rte.recordedTime >= ?1 AND rte.recordedTime < ?2 ORDER BY rte.recordedTime")
    List<RealTimePointEntity> findAllVehiclePointsByTimeSpan(String startTime, String endTime);

    @Query(value = "SELECT DISTINCT route_id FROM real_time_data_temp " +
            "WHERE recorded_time >= ?1 and recorded_time < ?2", nativeQuery = true)
    List<String> findAllRoutesByDate(String startTime, String endTime);

    @Query(value = "SELECT DISTINCT trip_id FROM real_time_data_temp " +
            "WHERE route_id=?1 AND recorded_time >= ?2 AND recorded_time < ?3", nativeQuery = true)
    List<String> findAllTripsByDate(String routeId, String startTime, String endTime);

    @Query(value = "SELECT DISTINCT trip_id FROM real_time_data_temp " +
            "WHERE recorded_time >= ?1 AND recorded_time < ?2", nativeQuery = true)
    List<String> findAllTripsOnlyByDate(String startTime, String endTime);

    @Query(value = "SELECT * " +
            "FROM real_time_data_temp " +
            "WHERE route_id = ?1 AND recorded_time >= ?2 AND recorded_time < ?3 ORDER BY recorded_time", nativeQuery = true)
    List<RealTimeDataEntity> findAllPointsByRouteIdByTimeSpan(String routeId, String startTime, String endTime);

    @Query(value = "SELECT * " +
            "FROM real_time_data_temp " +
            "WHERE trip_id = ?1 AND recorded_time >= ?2 AND recorded_time < ?3 ORDER BY recorded_time", nativeQuery = true)
    List<RealTimeDataEntity> findAllPointsByTripIdByTimeSpan(String tripId, String startTime, String endTime);

    @Query(value = "SELECT new whu.edu.cs.transitnet.pojo.RealTimePointEntity(rte.tripId, rte.vehicleId, rte.lat, rte.lon, rte.recordedTime) " +
            "FROM RealTimeDataEntity rte " +
            "WHERE rte.tripId = ?1 AND rte.recordedTime >= ?2 AND rte.recordedTime < ?3 ORDER BY rte.recordedTime")
    List<RealTimePointEntity> findAllSimplePointsByTripIdByTimeSpan(String tripId, String startTime, String endTime);

    <S extends RealTimeDataEntity> List<S> saveAll(Iterable<S> list);
}
