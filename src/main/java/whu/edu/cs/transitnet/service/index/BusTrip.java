package whu.edu.cs.transitnet.service.index;

import java.io.Serializable;
import java.sql.Time;
import java.util.Objects;

public class BusTrip implements Serializable {
    private TripId tripId;
    private String startStopId;
    private String endStopId;
    private Time departureTime;
    private Time arrivalTime;

    public BusTrip(TripId tripId, String startStopId, String endStopId, Time departureTime, Time arrivalTime) {
        this.tripId = tripId;
        this.startStopId = startStopId;
        this.endStopId = endStopId;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
    }

    public TripId getTripId() {
        return tripId;
    }

    public void setTripId(TripId tripId) {
        this.tripId = tripId;
    }

    public String getStartStopId() {
        return startStopId;
    }

    public void setStartStopId(String startStopId) {
        this.startStopId = startStopId;
    }

    public String getEndStopId() {
        return endStopId;
    }

    public void setEndStopId(String endStopId) {
        this.endStopId = endStopId;
    }

    public Time getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(Time departureTime) {
        this.departureTime = departureTime;
    }

    public Time getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(Time arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BusTrip busTrip = (BusTrip) o;
        return Objects.equals(tripId, busTrip.tripId) && Objects.equals(startStopId, busTrip.startStopId) && Objects.equals(departureTime, busTrip.departureTime) && Objects.equals(endStopId, busTrip.endStopId) && Objects.equals(arrivalTime, busTrip.arrivalTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tripId, startStopId, departureTime, endStopId, arrivalTime);
    }
}
