package whu.edu.cs.transitnet.pojo;

import javax.persistence.*;
import java.sql.Time;
import java.util.Objects;

@Entity
@Table(name = "stop_times")
@IdClass(StopTimesEntityPK.class)
public class StopTimesEntity {
    private String tripId;
    private Time arrivalTime;
    private Time departureTime;
    private String stopId;
    private int stopSequence;
    private String pickupType;
    private String dropOffType;

    @Id
    @Column(name = "trip_id")
    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    @Id
    @Column(name = "stop_id")
    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    @Basic
    @Column(name = "arrival_time")
    public Time getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(Time arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    @Basic
    @Column(name = "departure_time")
    public Time getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(Time departureTime) {
        this.departureTime = departureTime;
    }

    @Basic
    @Column(name = "stop_sequence")
    public int getStopSequence() {
        return stopSequence;
    }

    public void setStopSequence(int stopSequence) {
        this.stopSequence = stopSequence;
    }

    @Basic
    @Column(name = "pickup_type")
    public String getPickupType() {
        return pickupType;
    }

    public void setPickupType(String pickupType) {
        this.pickupType = pickupType;
    }

    @Basic
    @Column(name = "drop_off_type")
    public String getDropOffType() {
        return dropOffType;
    }

    public void setDropOffType(String dropOffType) {
        this.dropOffType = dropOffType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StopTimesEntity that = (StopTimesEntity) o;
        return stopSequence == that.stopSequence &&
                Objects.equals(tripId, that.tripId) &&
                Objects.equals(arrivalTime, that.arrivalTime) &&
                Objects.equals(departureTime, that.departureTime) &&
                Objects.equals(stopId, that.stopId) &&
                Objects.equals(pickupType, that.pickupType) &&
                Objects.equals(dropOffType, that.dropOffType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tripId, arrivalTime, departureTime, stopId, stopSequence, pickupType, dropOffType);
    }
}
