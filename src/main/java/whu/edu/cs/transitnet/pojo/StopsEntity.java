package whu.edu.cs.transitnet.pojo;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "stops")
public class StopsEntity {
    private String stopId;
    private String stopName;
    private String stopDesc;
    private Double stopLat;
    private Double stopLon;
    private String zoneId;
    private String stopUrl;
    private String locationType;
    private String parentStation;

    @Id
    @Column(name = "stop_id")
    public String getStopId() {
        return stopId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    @Basic
    @Column(name = "stop_name")
    public String getStopName() {
        return stopName;
    }

    public void setStopName(String stopName) {
        this.stopName = stopName;
    }

    @Basic
    @Column(name = "stop_desc")
    public String getStopDesc() {
        return stopDesc;
    }

    public void setStopDesc(String stopDesc) {
        this.stopDesc = stopDesc;
    }

    @Basic
    @Column(name = "stop_lat")
    public Double getStopLat() {
        return stopLat;
    }

    public void setStopLat(Double stopLat) {
        this.stopLat = stopLat;
    }

    @Basic
    @Column(name = "stop_lon")
    public Double getStopLon() {
        return stopLon;
    }

    public void setStopLon(Double stopLon) {
        this.stopLon = stopLon;
    }

    @Basic
    @Column(name = "zone_id")
    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    @Basic
    @Column(name = "stop_url")
    public String getStopUrl() {
        return stopUrl;
    }

    public void setStopUrl(String stopUrl) {
        this.stopUrl = stopUrl;
    }

    @Basic
    @Column(name = "location_type")
    public String getLocationType() {
        return locationType;
    }

    public void setLocationType(String locationType) {
        this.locationType = locationType;
    }

    @Basic
    @Column(name = "parent_station")
    public String getParentStation() {
        return parentStation;
    }

    public void setParentStation(String parentStation) {
        this.parentStation = parentStation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StopsEntity that = (StopsEntity) o;
        return Objects.equals(stopId, that.stopId) &&
                Objects.equals(stopName, that.stopName) &&
                Objects.equals(stopDesc, that.stopDesc) &&
                Objects.equals(stopLat, that.stopLat) &&
                Objects.equals(stopLon, that.stopLon) &&
                Objects.equals(zoneId, that.zoneId) &&
                Objects.equals(stopUrl, that.stopUrl) &&
                Objects.equals(locationType, that.locationType) &&
                Objects.equals(parentStation, that.parentStation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stopId, stopName, stopDesc, stopLat, stopLon, zoneId, stopUrl, locationType, parentStation);
    }
}
