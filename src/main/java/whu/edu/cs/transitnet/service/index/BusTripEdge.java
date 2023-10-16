package whu.edu.cs.transitnet.service.index;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BusTripEdge implements Serializable {
    private String firstStopId;
    private String secondStopId;
    private Double distance;
    private List<TripId> tripIds;


    public BusTripEdge(String firstStopId, String secondStopId, Double distance) {
        this.firstStopId = firstStopId;
        this.secondStopId = secondStopId;
        this.distance = distance;
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

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
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
        return Objects.equals(firstStopId, that.firstStopId) && Objects.equals(secondStopId, that.secondStopId) && Objects.equals(distance, that.distance) && Objects.equals(tripIds, that.tripIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstStopId, secondStopId, distance, tripIds);
    }
}

