package whu.edu.cs.transitnet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import whu.edu.cs.transitnet.dao.StopTimesDao;
import whu.edu.cs.transitnet.dao.StopsDao;
import whu.edu.cs.transitnet.pojo.RealTimePointEntity;
import whu.edu.cs.transitnet.pojo.StopTimesEntity;
import whu.edu.cs.transitnet.pojo.StopsLocationEntity;
import whu.edu.cs.transitnet.utils.GeoUtil;
import whu.edu.cs.transitnet.utils.TimeUtil;

import java.io.IOException;
import java.sql.Time;
import java.util.*;

import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toMap;

@Service
public class ArrivalTimePrediction {
    @Autowired
    HistoricalMatchForDirectTrip historicalMatchForDirectTrip;

    @Autowired
    StopTimesDao stopTimesDao;

    @Autowired
    StopsDao stopsDao;

    public void predictArrivalTime() throws IOException, ClassNotFoundException {
        String traId = "37566472-BPPD3-BP_D3-Weekday-04-SDon@2023-12-19";
        int k = 1;

        List<String> traCandidates = historicalMatchForDirectTrip.getTopK(traId, k);

        // k = 1, so only one trajectory is fetched and used to predict the arrival time.
        // TODO: when k > 1
        String traCandidate = traCandidates.get(0);
        String[] tripIdAndDate = traCandidate.split("@");
        String canTripId = tripIdAndDate[0];

        // The stop list of the trip
        List<StopTimesEntity> stopTimesEntityList = stopTimesDao.findPassedStopsByTripId(canTripId);
        // The historical point list of the trip
        List<RealTimePointEntity> realTimePointEntityList = historicalMatchForDirectTrip.getSlicedTripTrajectoryList().get(traCandidate);

        // Index for each point and its RealTimePointEntity
        // Equivalent to the KEY / a Posting List
        // tripId@recordedTime -> RealTimePointEntity
        // Used to calculate the arrival time for each stop
        HashMap<String, RealTimePointEntity> pointAndItsEntity = new HashMap<>();
        for (RealTimePointEntity realTimePointEntity : realTimePointEntityList) {
            String tripId = realTimePointEntity.getTripId();
            String recordedTime = realTimePointEntity.getRecordedTime();
            String key = tripId + "@" + recordedTime;

            pointAndItsEntity.put(key, realTimePointEntity);
        }

        // Calculate the arrival time for each stop according to the historical trajectory
        // Traverse all the stops
        for (StopTimesEntity stopTimesEntity : stopTimesEntityList) {
            Time arrivalTime = stopTimesEntity.getArrivalTime();
            String stopId = stopTimesEntity.getStopId();

            // Get its lat and lon
            StopsLocationEntity stopsLocation = stopsDao.findLatAndLonByStopId(stopId);
            Double stopLat = stopsLocation.getStopLat();
            Double stopLon = stopsLocation.getStopLon();

            HashMap<String, Double> pointAndDistance = new HashMap<>();
            for (String key : pointAndItsEntity.keySet()) {
                RealTimePointEntity realTimePoint = pointAndItsEntity.get(key);
                Double pointLat = realTimePoint.getLat();
                Double pointLon = realTimePoint.getLon();

                Double distance = GeoUtil.calculateHaversineDistance(stopLat, stopLon, pointLat, pointLon);
                pointAndDistance.put(key, distance);
            }

            // Calculating distance is reasonable because the distance is generally consistent with the timestamp
            Map<String, Double> sortedDistance = pointAndDistance
                    .entrySet()
                    .stream()
                    .sorted(comparingByValue())
                    .limit(2)
                    .collect(
                            toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                                    LinkedHashMap::new));

            // Determine the relative positions of the stop and the two points
            List<String> twoKeys = new ArrayList<>();
            List<Double> twoDistances = new ArrayList<>();
            List<Double> twoLats = new ArrayList<>();
            List<Double> twoLons = new ArrayList<>();
            for (String key : sortedDistance.keySet()) {
                twoKeys.add(key);
                twoDistances.add(sortedDistance.get(key));
                twoLats.add(pointAndItsEntity.get(key).getLat());
                twoLons.add(pointAndItsEntity.get(key).getLon());
            }
            Double stopWithTheNearestPoint = twoDistances.get(0);
            Double stopWithTheSecondNearestPoint = twoDistances.get(1);
            Double twoPointsDistance = GeoUtil.calculateHaversineDistance(twoLats.get(0), twoLons.get(0), twoLats.get(1), twoLons.get(1));

            // flag == 1 => The stop is between the two points.
            // flag == 0 => The two points are on the same side of the stop.
            int flag = twoPointsDistance - Math.max(stopWithTheNearestPoint, stopWithTheSecondNearestPoint) > 0? 1 : 0;

            String timeOfTheNearestPoint = pointAndItsEntity.get(twoKeys.get(0)).getRecordedTime();
            String[] date_time1 = timeOfTheNearestPoint.split(" ");
            String[] hour_min_sec1 = date_time1[1].split(":");
            int seconds1 = Integer.parseInt(hour_min_sec1[0]) * 3600 + Integer.parseInt(hour_min_sec1[1]) * 60 + Integer.parseInt(hour_min_sec1[2]);

            String timeOfTheSecondNearstPoint = pointAndItsEntity.get(twoKeys.get(1)).getRecordedTime();
            String[] date_time2 = timeOfTheSecondNearstPoint.split(" ");
            String[] hour_min_sec2 = date_time2[1].split(":");
            int seconds2 = Integer.parseInt(hour_min_sec2[0]) * 3600 + Integer.parseInt(hour_min_sec2[1]) * 60 + Integer.parseInt(hour_min_sec2[2]);

            int diffSeconds = Math.abs(seconds1 - seconds2);
            int seconds = 0;
            if (flag == 1) {
                if (seconds1 < seconds2) { // The nearest point hasn't reached the stop
                    seconds = (int) (seconds1 + diffSeconds * (stopWithTheNearestPoint / (stopWithTheNearestPoint + stopWithTheSecondNearestPoint)));
                } else { // The nearest point has passed the stop
                    seconds = (int) (seconds2 + diffSeconds * (stopWithTheSecondNearestPoint / (stopWithTheNearestPoint + stopWithTheSecondNearestPoint)));
                }
            } else {
                if (seconds1 < seconds2) { // The two points have passed the stop
                    seconds = (int) (seconds1 - diffSeconds / twoPointsDistance * stopWithTheNearestPoint);
                } else { // The two points haven't reached the stop
                    seconds = (int) (seconds1 + diffSeconds / twoPointsDistance * stopWithTheNearestPoint);
                }
            }

            TimeUtil timeUtil = new TimeUtil();
            List<Integer> hourMinSecond = timeUtil.secondsToMinHourSecond(seconds);
            int hour = hourMinSecond.get(0);
            int min = hourMinSecond.get(1);
            int sec = hourMinSecond.get(2);

            String shour = String.valueOf(hour);
            String smin = String.valueOf(min);
            String ssec = String.valueOf(sec);

            if (hour < 10) shour = "0" + hour;
            if (min < 10) smin = "0" + min;
            if (sec < 10) ssec = "0" + sec;

            System.out.println("[SCHEDULED - PREDICTED]  " + shour + ":" + smin + ":" + ssec + " - " + arrivalTime);
        }
    }
}
