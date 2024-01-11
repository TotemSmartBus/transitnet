package whu.edu.cs.transitnet.pojo;

public class StopsLocationEntity {
    private String stopId;
    private Double stopLat;
    private Double stopLon;

    public StopsLocationEntity (String stopId, Double stopLat, Double stopLon) {
        this.stopId = stopId;
        this.stopLat = stopLat;
        this.stopLon = stopLon;
    }

    public StopsLocationEntity () {
        this.stopId = null;
        this.stopLat = null;
        this.stopLon = null;
    }

    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public Double getStopLat() {
        return stopLat;
    }

    public void setStopLat(Double stopLat) {
        this.stopLat = stopLat;
    }

    public Double getStopLon() {
        return stopLon;
    }

    public void setStopLon(Double stopLon) {
        this.stopLon = stopLon;
    }
}
