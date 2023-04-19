package whu.edu.cs.transitnet.service.index;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import whu.edu.cs.transitnet.dao.StopTimesDao;
import whu.edu.cs.transitnet.dao.TripsDao;
import whu.edu.cs.transitnet.pojo.StopTimesEntity;
import whu.edu.cs.transitnet.pojo.TripsEntity;
import whu.edu.cs.transitnet.vo.TripTimesVo;

import java.sql.Time;
import java.util.*;

@Component
public class ScheduleIndex {
    @Autowired
    StopTimesDao stopTimesDao;

    @Autowired
    TripsDao tripsDao;

    // trip_id - start_time - end_time
    private HashMap<TripId, ArrayList<Time>> tripStartEndList;


    public ScheduleIndex() {
        // 取出所有 trip_id
        List<TripsEntity> tripsEntities = tripsDao.findAll();

        for(TripsEntity tripsEntity : tripsEntities) {
            String trip = tripsEntity.getTripId();
            TripId tripId = new TripId(trip);

            // 取出该 trip_id 下的到站时间序列
            List<TripTimesVo> tripTimesVos = stopTimesDao.findAllByTripId(trip);
            ArrayList<Time> startEndTime = new ArrayList<>();
            startEndTime.add(tripTimesVos.get(0).getArrivalTime());
            startEndTime.add(tripTimesVos.get(tripTimesVos.size() - 1).getArrivalTime());

            tripStartEndList.put(tripId, startEndTime);
        }
//        List<StopTimesEntity> stopTimesEntities = stopTimesDao.FindAllByTridId("35671183-BPPB3-BP_B3-Weekday-02");
//        System.out.println(stopTimesEntities);
    }

    public class TripId implements CharSequence {
        private final String content;


        public TripId(String content) {
            this.content = content;
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
            TripId tripId = (TripId) o;
            return Objects.equals(content, tripId.content);
        }

        @Override
        public int hashCode() {
            return Objects.hash(content);
        }
    }
}
