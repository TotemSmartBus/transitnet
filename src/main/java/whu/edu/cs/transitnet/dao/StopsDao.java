package whu.edu.cs.transitnet.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import whu.edu.cs.transitnet.pojo.StopsEntity;
import whu.edu.cs.transitnet.vo.StopsVo;

import java.util.List;

public interface StopsDao extends JpaRepository<StopsEntity, String> {

    @Query(value = "SELECT new whu.edu.cs.transitnet.vo.StopsVo(ste.stopId, ste.tripId, se.stopName,"
            + "ste.arrivalTime, ste.departureTime, ste.stopSequence,"
            + "se.stopLat, se.stopLon) FROM StopsEntity se left join StopTimesEntity ste on se.stopId = ste.stopId "
            + "WHERE ste.tripId = ?1 AND ste.arrivalTime < '24:00:00' ORDER BY ste.stopSequence")
    List<StopsVo> findAllByTripId(String tripId);

    List<StopsEntity> findAll();


}
