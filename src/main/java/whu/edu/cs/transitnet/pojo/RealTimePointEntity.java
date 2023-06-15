package whu.edu.cs.transitnet.pojo;

import java.io.Serializable;

public class RealTimePointEntity implements Serializable {

    private String tripId;
    private String vehicleId;
    private Double lat;
    private Double lon;
    private String recordedTime;

    public RealTimePointEntity(String tripId, String vehicleId, Double lat, Double lon, String recordedTime) {
        this.tripId = tripId;
        this.vehicleId = vehicleId;
        this.lat = lat;
        this.lon = lon;
        this.recordedTime = recordedTime;
    }

    public RealTimePointEntity() {
        this.tripId = null;
        this.vehicleId = null;
        this.lat = null;
        this.lon = null;
        this.recordedTime = null;
    }


    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public String getRecordedTime() {
        return recordedTime;
    }

    public void setRecordedTime(String recordedTime) {
        this.recordedTime = recordedTime;
    }
}
