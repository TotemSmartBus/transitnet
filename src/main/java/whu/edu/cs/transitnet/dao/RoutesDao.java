package whu.edu.cs.transitnet.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import whu.edu.cs.transitnet.pojo.RoutesEntity;
import whu.edu.cs.transitnet.vo.RoutesVo;

import java.sql.Date;
import java.util.List;

public interface RoutesDao extends JpaRepository<RoutesEntity, String> {
    List<RoutesEntity> findAll();

    @Query(value = "select distinct r.routeId from RoutesEntity as r")
    List<String> findDistinctRouteID();

    @Query(value = "SELECT new whu.edu.cs.transitnet.vo.RoutesVo(re.routeId, re.agencyId, re.routeShortName, "
            + "re.routeLongName, re.routeDesc, re.routeType, re.routeColor, "
            + "re.routeTextColor)"
            + "FROM RoutesEntity re "
            + "WHERE re.routeId = ?1")
    RoutesVo findRoutesVoByRouteId(String routeId);

    @Query("SELECT DISTINCT re FROM RoutesEntity re "
            + "LEFT JOIN TripsEntity te ON te.routeId = re.routeId "
            + "LEFT JOIN CalendarEntity ce ON ce.serviceId= te.serviceId "
            + "WHERE ce.startDate <= ?1 AND ce.endDate >= ?2")
    List<RoutesEntity> findRoutesByTimeSpan(Date startDate, Date endDate);
}
