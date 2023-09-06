package whu.edu.cs.transitnet.pojo;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "trips")
public class TripsEntity {
    private String routeId;
    private String serviceId;
    private String tripId;
    private String tripHeadsign;
    private String directionId;
    private String shapeId;

    @Basic
    @Column(name = "route_id")
    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    @Basic
    @Column(name = "service_id")
    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    @Id
    @Column(name = "trip_id")
    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    @Basic
    @Column(name = "trip_headsign")
    public String getTripHeadsign() {
        return tripHeadsign;
    }

    public void setTripHeadsign(String tripHeadsign) {
        this.tripHeadsign = tripHeadsign;
    }

    @Basic
    @Column(name = "direction_id")
    public String getDirectionId() {
        return directionId;
    }

    public void setDirectionId(String directionId) {
        this.directionId = directionId;
    }

    @Basic
    @Column(name = "shape_id")
    public String getShapeId() {
        return shapeId;
    }

    public void setShapeId(String shapeId) {
        this.shapeId = shapeId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TripsEntity that = (TripsEntity) o;
        return Objects.equals(routeId, that.routeId) &&
                Objects.equals(serviceId, that.serviceId) &&
                Objects.equals(tripId, that.tripId) &&
                Objects.equals(tripHeadsign, that.tripHeadsign) &&
                Objects.equals(directionId, that.directionId) &&
                Objects.equals(shapeId, that.shapeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(routeId, serviceId, tripId, tripHeadsign, directionId, shapeId);
    }
}
