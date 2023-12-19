package whu.edu.cs.transitnet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import whu.edu.cs.transitnet.dao.RealTimeDataDao;
import whu.edu.cs.transitnet.dao.TripsDao;
import whu.edu.cs.transitnet.pojo.RealTimePointEntity;
import whu.edu.cs.transitnet.pojo.TripsEntity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@Service
public class HistoricalMatchForDirectTrip {

    @Autowired
    TripsDao tripsDao;

    @Autowired
    RealTimeDataDao realTimeDataDao;

    List<TripsEntity> tripsEntityList = new ArrayList<>();
    Map<String, List<RealTimePointEntity>> slicedTripTrajectoryList = new HashMap<>();

    public List<TripsEntity> getTripsEntityList() {
        return tripsEntityList;
    }

    public Map<String, List<RealTimePointEntity>> getSlicedTripTrajectoryList() {
        return slicedTripTrajectoryList;
    }

    /**
     * Get all trip_ids on the specified bus route; specifically, by route_id and direction_id.
     * @param routeId
     * @param directionId
     * @return
     */
    public List<TripsEntity> getAllTripsByRouteIdAndDirectionId(String routeId, String directionId) {
        return tripsDao.findAllByRouteIdAndDirectionId(routeId, directionId);
    }

    /**
     * Get all historical points for one trip and slice them by date.
     * @return
     */
    public void getAllHistoricalTrajectoriesOnTheSpecifiedRoute(String routeId, String directionId) throws IOException {
        tripsEntityList = getAllTripsByRouteIdAndDirectionId(routeId, directionId);

        // scan all the trips and get historical points for each trip
        for (TripsEntity tripsEntity : tripsEntityList) {
            String tripId = tripsEntity.getTripId();
            List<RealTimePointEntity> realTimePointEntityList = realTimeDataDao.findAllSimplePointsByTripId(tripId);
            // slice the trajectories by date
            String loopDate = "";
            String key = "";
            List<RealTimePointEntity> realTimePointEntityList1 = new ArrayList<>();
            for (RealTimePointEntity realTimePointEntity : realTimePointEntityList) {
                String recorded_time = realTimePointEntity.getRecordedTime();
                String date = recorded_time.substring(0, 10);
                String newKey = tripId + "@" + date;
                if (!date.equals(loopDate)) {
                    loopDate = date;
                    key = newKey;
                    realTimePointEntityList1 = new ArrayList<>();
                    realTimePointEntityList1.add(realTimePointEntity);
                    slicedTripTrajectoryList.put(key, realTimePointEntityList1);
                    continue;
                }
                realTimePointEntityList1.add(realTimePointEntity);
                slicedTripTrajectoryList.put(key, realTimePointEntityList1);
            }
        }

        File file = new File("./src/main/" + " routingPointSize.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));

        // print the map sorted by key
        // get the key set
        Set keySet = slicedTripTrajectoryList.keySet();
        // turn the key set into an array
        Object[] arr= keySet.toArray();
        // sort the array using Arrays.sort()
        Arrays.sort(arr);
        int number = 0;
        int size = 0;
        for (Object key : arr){
            number++;
            int newSize = slicedTripTrajectoryList.get(key).size();
            size += newSize;
            writer.write(String.valueOf(newSize));
            writer.newLine();

            if(newSize >= 20 && newSize <= 30) {
                System.out.println(newSize + ": " + key);
            }
            // System.out.println(key + ": " + slicedTripTrajectoryList.get(key));
        }
        writer.close();
        // print the size of the historical trajectories
        System.out.println("Size of trajectories: " + slicedTripTrajectoryList.size());
        // print the average size of trajectory points
        System.out.println("Average size of trajectory points: " + size/number);
    }
}
