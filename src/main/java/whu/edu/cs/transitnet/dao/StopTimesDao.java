package whu.edu.cs.transitnet.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import whu.edu.cs.transitnet.pojo.StopTimesEntity;
import whu.edu.cs.transitnet.vo.TripTimesVo;

import java.util.List;

public interface StopTimesDao extends JpaRepository<StopTimesEntity, String> {

//    @Query(value = "SELECT * " +
//    "FROM stop_times " +
//    "WHERE trip_id = ?1", nativeQuery = true)
//    List<StopTimesEntity> FindAllByTridId(String tripId);

    @Query(value = "SELECT new whu.edu.cs.transitnet.vo.TripTimesVo("
            + "ste.arrivalTime, ste.departureTime)"
            + "FROM StopTimesEntity ste "
            + "WHERE ste.tripId = ?1 AND ste.arrivalTime < '24:00:00' "
            + "ORDER BY ste.stopSequence")
    List<TripTimesVo> findAllByTripId(String tripId);


}
