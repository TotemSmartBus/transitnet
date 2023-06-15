package whu.edu.cs.transitnet.pojo;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "real_time_data_temp")
@IdClass(RealTimeDataEntityPK.class)
public class RealTimeDataEntity extends RealTimePointEntity {
    private String routeId;
    private String direction;
    private String tripId;
    private String agencyId;
    private String originStop;
    private Double lat;
    private Double lon;
    private Double bearing;
    private String vehicleId;
    private String aimedArrivalTime;
    private Double distanceFromOrigin;
    private Double presentableDistance;
    private String distanceFromNextStop;
    private String nextStop;
    private String recordedTime;

    public RealTimeDataEntity(String tripId, String vehicleId, Double lat, Double lon, String recordedTime) {
        super(tripId, vehicleId, lat, lon, recordedTime);
    }

    public RealTimeDataEntity() {
        super();
        this.direction = null;
        this.agencyId = null;
        this.originStop = null;
        this.tripId = null;
        this.direction = null;
        this.bearing = null;
        this.aimedArrivalTime = null;
        this.distanceFromOrigin = null;
        this.presentableDistance = null;
        this.distanceFromNextStop = null;
        this.nextStop = null;
    }

    @Basic
    @Column(name = "route_id")
    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    @Basic
    @Column(name = "direction")
    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    @Basic
    @Column(name = "trip_id")
    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    @Basic
    @Column(name = "agency_id")
    public String getAgencyId() {
        return agencyId;
    }

    public void setAgencyId(String agencyId) {
        this.agencyId = agencyId;
    }

    @Basic
    @Column(name = "origin_stop")
    public String getOriginStop() {
        return originStop;
    }

    public void setOriginStop(String originStop) {
        this.originStop = originStop;
    }

    @Basic
    @Column(name = "lat")
    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    @Basic
    @Column(name = "lon")
    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    @Basic
    @Column(name = "bearing")
    public Double getBearing() {
        return bearing;
    }

    public void setBearing(Double bearing) {
        this.bearing = bearing;
    }

    @Id
    @Column(name = "vehicle_id")
    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    @Basic
    @Column(name = "aimed_arrival_time")
    public String getAimedArrivalTime() {
        return aimedArrivalTime;
    }

    public void setAimedArrivalTime(String aimedArrivalTime) {
        this.aimedArrivalTime = aimedArrivalTime;
    }

    @Basic
    @Column(name = "distance_from_origin")
    public Double getDistanceFromOrigin() {
        return distanceFromOrigin;
    }

    public void setDistanceFromOrigin(Double distanceFromOrigin) {
        this.distanceFromOrigin = distanceFromOrigin;
    }

    @Basic
    @Column(name = "presentable_distance")
    public Double getPresentableDistance() {
        return presentableDistance;
    }

    public void setPresentableDistance(Double presentableDistance) {
        this.presentableDistance = presentableDistance;
    }

    @Basic
    @Column(name = "distance_from_next_stop")
    public String getDistanceFromNextStop() {
        return distanceFromNextStop;
    }

    public void setDistanceFromNextStop(String distanceFromNextStop) {
        this.distanceFromNextStop = distanceFromNextStop;
    }

    @Basic
    @Column(name = "next_stop")
    public String getNextStop() {
        return nextStop;
    }

    public void setNextStop(String nextStop) {
        this.nextStop = nextStop;
    }

    @Id
    @Column(name = "recorded_time")
    public String getRecordedTime() {
        return recordedTime;
    }

    public void setRecordedTime(String recordedTime) {
        this.recordedTime = recordedTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RealTimeDataEntity that = (RealTimeDataEntity) o;
        return Objects.equals(routeId, that.routeId) &&
                Objects.equals(direction, that.direction) &&
                Objects.equals(tripId, that.tripId) &&
                Objects.equals(agencyId, that.agencyId) &&
                Objects.equals(originStop, that.originStop) &&
                Objects.equals(lat, that.lat) &&
                Objects.equals(lon, that.lon) &&
                Objects.equals(bearing, that.bearing) &&
                Objects.equals(vehicleId, that.vehicleId) &&
                Objects.equals(aimedArrivalTime, that.aimedArrivalTime) &&
                Objects.equals(distanceFromOrigin, that.distanceFromOrigin) &&
                Objects.equals(presentableDistance, that.presentableDistance) &&
                Objects.equals(distanceFromNextStop, that.distanceFromNextStop) &&
                Objects.equals(nextStop, that.nextStop) &&
                Objects.equals(recordedTime, that.recordedTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(routeId, direction, tripId, agencyId, originStop, lat, lon, bearing, vehicleId, aimedArrivalTime, distanceFromOrigin, presentableDistance, distanceFromNextStop, nextStop, recordedTime);
    }
}
