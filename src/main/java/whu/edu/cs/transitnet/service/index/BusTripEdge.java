package whu.edu.cs.transitnet.service.index;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BusTripEdge implements Serializable {
    private String firstStopId;
    private String secondStopId;
    private List<TripId> tripIds;
    // distance
    // time

    public BusTripEdge(String firstStopId, String secondStopId) {
        this.firstStopId = firstStopId;
        this.secondStopId = secondStopId;
        this.tripIds = new ArrayList<>();
    }

    // Getter methods for start and end

    public String getFirstStopId() {
        return firstStopId;
    }

    public void setFirstStopId(String firstStopId) {
        this.firstStopId = firstStopId;
    }

    public String getSecondStopId() {
        return secondStopId;
    }

    public void setSecondStopId(String secondStopId) {
        this.secondStopId = secondStopId;
    }

    public List<TripId> getTripIds() {
        return tripIds;
    }

    public void setTripIds(List<TripId> tripIds) {
        this.tripIds = tripIds;
    }

    public void addTripIds(TripId tripId) {
        if (!this.tripIds.contains(tripId)) {
            this.tripIds.add(tripId);
        }
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BusTripEdge that = (BusTripEdge) o;
        return Objects.equals(firstStopId, that.firstStopId) && Objects.equals(secondStopId, that.secondStopId) && Objects.equals(tripIds, that.tripIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstStopId, secondStopId, tripIds);
    }
}

