package whu.edu.cs.transitnet.service.index;

import java.io.Serializable;
import java.util.Objects;

public class BusStop implements Serializable {
    private String stopId;
    private double stopLon;
    private double stopLat;

    public BusStop(String stopId, double stopLon, double stopLat) {
        this.stopId = stopId;
        this.stopLon = stopLon;
        this.stopLat = stopLat;
    }

    // Getter and setter methods
    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public double getStopLon() {
        return stopLon;
    }

    public void setStopLon(double stopLon) {
        this.stopLon = stopLon;
    }

    public double getStopLat() {
        return stopLat;
    }

    public void setStopLat(double stopLat) {
        this.stopLat = stopLat;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BusStop busStop = (BusStop) o;
        return Double.compare(busStop.stopLon, stopLon) == 0 && Double.compare(busStop.stopLat, stopLat) == 0 && Objects.equals(stopId, busStop.stopId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stopId, stopLon, stopLat);
    }
}
