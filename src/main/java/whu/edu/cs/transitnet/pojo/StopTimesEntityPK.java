package whu.edu.cs.transitnet.pojo;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

public class StopTimesEntityPK implements Serializable {
    private String tripId;
    private String stopId;

    @Column(name = "trip_id")
    @Id
    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    @Column(name = "stop_id")
    @Id
    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StopTimesEntityPK that = (StopTimesEntityPK) o;
        return Objects.equals(tripId, that.tripId) &&
                Objects.equals(stopId, that.stopId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tripId, stopId);
    }
}
