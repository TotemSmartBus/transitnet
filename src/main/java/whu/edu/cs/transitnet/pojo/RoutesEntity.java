package whu.edu.cs.transitnet.pojo;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "routes")
public class RoutesEntity {
    private String routeId;
    private String agencyId;
    private String routeShortName;
    private String routeLongName;
    private String routeDesc;
    private String routeType;
    private String routeColor;
    private String routeTextColor;

    @Id
    @Column(name = "route_id")
    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
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
    @Column(name = "route_short_name")
    public String getRouteShortName() {
        return routeShortName;
    }

    public void setRouteShortName(String routeShortName) {
        this.routeShortName = routeShortName;
    }

    @Basic
    @Column(name = "route_long_name")
    public String getRouteLongName() {
        return routeLongName;
    }

    public void setRouteLongName(String routeLongName) {
        this.routeLongName = routeLongName;
    }

    @Basic
    @Column(name = "route_desc")
    public String getRouteDesc() {
        return routeDesc;
    }

    public void setRouteDesc(String routeDesc) {
        this.routeDesc = routeDesc;
    }

    @Basic
    @Column(name = "route_type")
    public String getRouteType() {
        return routeType;
    }

    public void setRouteType(String routeType) {
        this.routeType = routeType;
    }

    @Basic
    @Column(name = "route_color")
    public String getRouteColor() {
        return routeColor;
    }

    public void setRouteColor(String routeColor) {
        this.routeColor = routeColor;
    }

    @Basic
    @Column(name = "route_text_color")
    public String getRouteTextColor() {
        return routeTextColor;
    }

    public void setRouteTextColor(String routeTextColor) {
        this.routeTextColor = routeTextColor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RoutesEntity that = (RoutesEntity) o;
        return Objects.equals(routeId, that.routeId) &&
                Objects.equals(agencyId, that.agencyId) &&
                Objects.equals(routeShortName, that.routeShortName) &&
                Objects.equals(routeLongName, that.routeLongName) &&
                Objects.equals(routeDesc, that.routeDesc) &&
                Objects.equals(routeType, that.routeType) &&
                Objects.equals(routeColor, that.routeColor) &&
                Objects.equals(routeTextColor, that.routeTextColor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(routeId, agencyId, routeShortName, routeLongName, routeDesc, routeType, routeColor, routeTextColor);
    }
}
