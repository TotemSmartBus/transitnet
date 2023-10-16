package whu.edu.cs.transitnet.bean;

import org.springframework.stereotype.Component;
import whu.edu.cs.transitnet.service.index.BusStop;
import whu.edu.cs.transitnet.service.index.BusTrip;
import whu.edu.cs.transitnet.service.index.BusTripEdge;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * // 验证结果；transfer learning对比；验证就是过段时间查看到达时间是不是和预测的一样的
 */
@Component
public class BusRouteMap implements Serializable {
    // 公交站点信息列表
    List<BusStop> busStops;
    // 索引表：站点在列表中的位置 - 站点Id
    HashMap<Integer, BusStop> orderStopIndex;
    // 索引表：站点Id - 站点在列表中的位置
    HashMap<String, Integer> stopIdOrderIndex;

    // trip 起始站和终点站
    List<BusTrip> busTrips;

    // 两个站点连线边信息列表
    List<BusTripEdge> busTripEdges;
    // 两个站点索引边
    HashMap<String, HashMap<String, BusTripEdge>> stopEdgeIndex;

    public BusRouteMap() {
    }

    public List<BusStop> getBusStops() {
        return busStops;
    }

    public void setBusStops(List<BusStop> busStops) {
        this.busStops = busStops;
    }

    public HashMap<Integer, BusStop> getOrderStopIndex() {
        return orderStopIndex;
    }

    public void setOrderStopIndex(HashMap<Integer, BusStop> orderStopIndex) {
        this.orderStopIndex = orderStopIndex;
    }

    public HashMap<String, Integer> getStopIdOrderIndex() {
        return stopIdOrderIndex;
    }

    public void setStopIdOrderIndex(HashMap<String, Integer> stopIdOrderIndex) {
        this.stopIdOrderIndex = stopIdOrderIndex;
    }

    public List<BusTripEdge> getBusTripEdges() {
        return busTripEdges;
    }

    public void setBusTripEdges(List<BusTripEdge> busTripEdges) {
        this.busTripEdges = busTripEdges;
    }

    public HashMap<String, HashMap<String, BusTripEdge>> getStopEdgeIndex() {
        return stopEdgeIndex;
    }

    public void setStopEdgeIndex(HashMap<String, HashMap<String, BusTripEdge>> stopEdgeIndex) {
        this.stopEdgeIndex = stopEdgeIndex;
    }

    public List<BusTrip> getBusTrips() {
        return busTrips;
    }

    public void setBusTrips(List<BusTrip> busTrips) {
        this.busTrips = busTrips;
    }
}

