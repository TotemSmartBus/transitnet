package whu.edu.cs.transitnet.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import whu.edu.cs.transitnet.dao.RealTimeDataDao;
import whu.edu.cs.transitnet.pojo.IndexEntity;
import whu.edu.cs.transitnet.pojo.RealTimeDataEntity;
import whu.edu.cs.transitnet.utils.TimeUtil;
import whu.edu.cs.transitnet.vo.RealTimeDataVo;
import whu.edu.cs.transitnet.pojo.RealTimePointEntity;
import whu.edu.cs.transitnet.vo.SpeedDateVo;
import whu.edu.cs.transitnet.vo.StringPair;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RealTimeDataService {
    @Autowired
    RealTimeDataDao realTimeDataDao;

    @Autowired
    private SimpleDateFormat formatter;

    long TIMESTEP = 1000 * 60 * 15;
    long TIMEDATE = 1000 * 60 * 60 * 24;
    long LASTTIMESPAN = 1000 * 60 * 5;
    Integer firstIndex = 0;
    Integer tempIndex = 0;

    public List<RealTimeDataVo> getRealTimeDataVoLastByRecordedTime(Timestamp curTime) {
        Timestamp end = curTime;
        Timestamp start = new Timestamp(curTime.getTime() - LASTTIMESPAN);
        String startTime = formatter.format(start);
        String endTime = formatter.format(end);
        List<RealTimeDataEntity> realTimeDataEntities = realTimeDataDao.findAllLastByRecordedTimeSpan(startTime, endTime);
        List<RealTimePointEntity> realTimePointEntities = realTimeDataDao.findAllVehiclePointsByTimeSpan(startTime, endTime);
        List<RealTimeDataVo> realTimeDataVos = new ArrayList<>();
        for (RealTimeDataEntity re : realTimeDataEntities) {
            String curVehicleId = re.getVehicleId();
            List<RealTimePointEntity> tempList = new ArrayList<>();
            for (RealTimePointEntity rtp : realTimePointEntities) {
                if (rtp.getVehicleId().equals(curVehicleId)) {
                    tempList.add(rtp);
                }
            }
            Double speed = calSpeed(tempList);
            realTimeDataVos.add(new RealTimeDataVo(re, speed));
        }
        return realTimeDataVos;
    }

    public List<String> getVehicleIdList(String recordedTime) {
        return realTimeDataDao.findAllVehicleIdByRecordedTime(recordedTime);
    }

    /**
     * Get speedDateVo in the lastest 7 days
     *
     * @param vehicleId
     * @param curTimeStr
     * @return
     */
    public List<SpeedDateVo> getSpeedDateListByVehicleIdLastSevenDate(String vehicleId, String curTimeStr) {
        List<SpeedDateVo> speedDateVos = new ArrayList<>();
        Timestamp curTime = Timestamp.valueOf(curTimeStr);
        String curTimeDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(curTime);
        curTimeDate = curTimeDate.substring(0, 10);
        Date lastDate = Date.valueOf(curTimeDate);
        Timestamp startTime = new Timestamp(lastDate.getTime() - 6 * TIMEDATE);
        Timestamp endTime = curTime;
        String timeStr1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(startTime);
        String timeStr2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(endTime);
        List<RealTimePointEntity> vehiclePoints = realTimeDataDao.findAllPointsByVehicleIdByTimeSpan(vehicleId, timeStr1, timeStr2);
        Timestamp loopEndTime = new Timestamp(lastDate.getTime() + TIMEDATE);
        Integer allPointsCount = vehiclePoints.size();
        firstIndex = 0;
        tempIndex = 0;
        IndexEntity indexEntity = new IndexEntity(0, 0);
        while (!startTime.equals(loopEndTime)) {
            Timestamp dateStart = startTime;
            Timestamp dateEnd = new Timestamp(dateStart.getTime() + TIMEDATE);
            SpeedDateVo speedDateVo = getSpeedDateVoByTimeSpan(dateStart, dateEnd,
                    vehiclePoints, indexEntity);
            speedDateVos.add(speedDateVo);
            startTime = dateEnd;
        }
        return speedDateVos;
    }

    /**
     * Get speedList of vehiclePoints
     *
     * @param dateStart
     * @param dateEnd
     * @param vehiclePoints
     * @param indexEntity
     * @return
     */
    public List<Double> getSpeedListByPointList(Timestamp dateStart, Timestamp dateEnd, List<? extends RealTimePointEntity> vehiclePoints, IndexEntity indexEntity) {
        Timestamp lastTime = dateStart;
        List<Double> speedList = new ArrayList<>();
        Integer allPointsCount = vehiclePoints.size();
        Integer tempIndex = indexEntity.getTempIndex();
        Integer firstIndex = indexEntity.getFirstIndex();
        while (!lastTime.equals(dateEnd) & tempIndex < allPointsCount) {
            Timestamp timeSpanEnd = new Timestamp(lastTime.getTime() + TIMESTEP);
            tempIndex = firstIndex;
            Timestamp tempTime = Timestamp.valueOf(vehiclePoints.get(tempIndex).getRecordedTime());
            //every timespan
            while (tempTime.getTime() < timeSpanEnd.getTime() & tempIndex < allPointsCount) {
                tempTime = Timestamp.valueOf(vehiclePoints.get(tempIndex).getRecordedTime());
                tempIndex += 1;
            }
            if (!firstIndex.equals(tempIndex)) {
                List<? extends RealTimePointEntity> PointswithinTimeSpan = vehiclePoints.subList(firstIndex, tempIndex - 1);
                speedList.add(calSpeed(PointswithinTimeSpan));
            } else {
                speedList.add(0.0);
            }
            if (firstIndex.equals(tempIndex)) {
                firstIndex = tempIndex;
            } else {
                firstIndex = tempIndex - 1;
            }
            lastTime = timeSpanEnd;
        }
//        speedList.size() 24*4
        while (speedList.size() < 24 * 4) {
            speedList.add(0.0);
        }
        indexEntity.setFirstIndex(firstIndex);
        indexEntity.setTempIndex(tempIndex);
        return speedList;
    }

    public SpeedDateVo getSpeedDateVoByTimeSpan(Timestamp dateStart, Timestamp dateEnd,
                                                List<? extends RealTimePointEntity> vehiclePoints, IndexEntity indexEntity) {
        List<Double> speedList = getSpeedListByPointList(dateStart, dateEnd, vehiclePoints, indexEntity);
        SpeedDateVo result = new SpeedDateVo(dateStart.toString().substring(5, 10), speedList);
        return result;
    }

    private Double calSpeed(List<? extends RealTimePointEntity> realTimePointEntities) {
        TimeUtil timeUtil = new TimeUtil();
        if (realTimePointEntities.size() == 0 || realTimePointEntities.size() == 1) {
            return 0.0;
        }
        // km
        Double dist = 0.0;
        String startTime = realTimePointEntities.get(0).getRecordedTime();
        String endTime = realTimePointEntities.get(realTimePointEntities.size() - 1).getRecordedTime();
        for (Integer i = 0; i < realTimePointEntities.size() - 1; i++) {
            RealTimePointEntity rtv1 = realTimePointEntities.get(0);
            RealTimePointEntity rtv2 = realTimePointEntities.get(1);
            dist += timeUtil.getDistance(rtv1.getLon(), rtv1.getLat(), rtv2.getLon(), rtv2.getLat());
        }
        Timestamp time1 = Timestamp.valueOf(startTime);
        Timestamp time2 = Timestamp.valueOf(endTime);
        // s
        long span = (time2.getTime() - time1.getTime()) / 1000;
        if (span == 0) {
            return 0.0;
        } else {
            return dist * 3600 / span;
        }
    }

    public List<String> getRealTimeRouteOptionsByDate(Date date) {
        Timestamp startTime = new Timestamp(date.getTime());
        Timestamp endTime = new Timestamp(startTime.getTime() + TIMEDATE);
        String startTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(startTime);
        String endTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(endTime);
        return realTimeDataDao.findAllRoutesByDate(startTimeString, endTimeString);
    }

    public List<String> getRealTimeTripOptionsByDate(String routeId, Date date) {
        Timestamp endTime = new Timestamp(System.currentTimeMillis());
        Timestamp startTime = new Timestamp(endTime.getTime() - TIMEDATE);
        String startTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(startTime);
        String endTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(endTime);
        return realTimeDataDao.findAllTripsByDate(routeId, startTimeString, endTimeString);
    }

    public List<SpeedDateVo> getSpeedDateListByRouteId(List<String> routeIdList, Date date) {
        List<SpeedDateVo> routeSpeedList = new ArrayList<>();
        for (String routeId : routeIdList) {
            routeSpeedList.add(getSpeedDateByRouteId(routeId, date));
        }
        return routeSpeedList;
    }

    public List<SpeedDateVo> getSpeedDateListByTripId(List<String> tripIdList, Date date) {
        List<SpeedDateVo> tripSpeedList = new ArrayList<>();
        for (String tripId : tripIdList) {
            tripSpeedList.add(getSpeedDateByTripId(tripId, date));
        }
        return tripSpeedList;
    }

    public SpeedDateVo getSpeedDateByRouteId(String routeId, Date date) {
        List<SpeedDateVo> speedDateVos = new ArrayList<>();
        Timestamp startTime = new Timestamp(date.getTime());
        Timestamp endTime = new Timestamp(startTime.getTime() + TIMEDATE);
        String startTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(startTime);
        String endTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(endTime);
        List<RealTimeDataEntity> routesPoints = realTimeDataDao.findAllPointsByRouteIdByTimeSpan(routeId, startTimeString, endTimeString);
        List<StringPair> vehicleIdAndTripIdList = getVehicleIdAndTripIdListInPointsList(routesPoints);
        for (StringPair p : vehicleIdAndTripIdList) {
            IndexEntity indexEntity = new IndexEntity(0, 0);
            List<RealTimeDataEntity> tempList = routesPoints.stream().filter(item -> item.getVehicleId().equals(p.getKey()) && item.getTripId().equals(p.getValue()))
                    .collect(Collectors.toList());
            speedDateVos.add(getSpeedDateVoByTimeSpan(startTime, endTime, tempList, indexEntity));
        }
        SpeedDateVo result = new SpeedDateVo(routeId, date.toString(), calMeanSpeed(speedDateVos));
        return result;
    }

    public SpeedDateVo getSpeedDateByTripId(String tripId, Date date) {
        List<SpeedDateVo> speedDateVos = new ArrayList<>();
        Timestamp startTime = new Timestamp(date.getTime());
        Timestamp endTime = new Timestamp(startTime.getTime() + TIMEDATE);
        String startTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(startTime);
        String endTimeString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(endTime);
        List<RealTimeDataEntity> tripsPoints = realTimeDataDao.findAllPointsByTripIdByTimeSpan(tripId, startTimeString, endTimeString);
        List<String> vehicleIdList = getVehicleIdListInPointsList(tripsPoints);
        for (String vId : vehicleIdList) {
            IndexEntity indexEntity = new IndexEntity(0, 0);
            List<RealTimeDataEntity> tempList = tripsPoints.stream().filter(item -> item.getVehicleId().equals(vId))
                    .collect(Collectors.toList());
            speedDateVos.add(getSpeedDateVoByTimeSpan(startTime, endTime, tempList, indexEntity));
        }
        SpeedDateVo result = new SpeedDateVo(tripId, date.toString(), calMeanSpeed(speedDateVos));
        return result;
    }

    public List<StringPair> getVehicleIdAndTripIdListInPointsList(List<RealTimeDataEntity> routesPoints) {
        List<StringPair> result = new ArrayList<>();
        for (RealTimeDataEntity rtpt : routesPoints) {
            String vId = rtpt.getVehicleId();
            String tId = rtpt.getTripId();
            StringPair pair = new StringPair(vId, tId);
            if (result.contains(pair) == false) {
                result.add(pair);
            }
        }
        return result;
    }

    public List<String> getVehicleIdListInPointsList(List<RealTimeDataEntity> routesPoints) {
        List<String> result = new ArrayList<>();
        for (RealTimeDataEntity rtpt : routesPoints) {
            String vId = rtpt.getVehicleId();
            if (result.contains(vId) == false) {
                result.add(vId);
            }
        }
        return result;
    }

    public List<Double> calMeanSpeed(List<SpeedDateVo> speedDateVos) {
        double[] speedList = new double[96];
        int[] countList = new int[96];
        List<Double> resultSpeed = new ArrayList<>();
        for (SpeedDateVo sd : speedDateVos) {
            List<Double> tempSpeedList = sd.getSpeedList();
            for (int i = 0; i < 96; i++) {
                if (!tempSpeedList.get(i).equals(0.0)) {
                    speedList[i] += tempSpeedList.get(i);
                    countList[i] += 1;
                }
            }
        }
        for (int i = 0; i < 96; i++) {
            if (countList[i] == 0) {
                resultSpeed.add(0.0);
            } else {
                resultSpeed.add(speedList[i] / countList[i]);
            }
        }
        return resultSpeed;
    }
}
