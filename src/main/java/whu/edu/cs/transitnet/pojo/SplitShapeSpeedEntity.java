package whu.edu.cs.transitnet.pojo;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "split_shape_speed")
@IdClass(SplitShapeSpeedEntityPK.class)
public class SplitShapeSpeedEntity {
    private String routeId;
    private String shapeId;
    private int splitId;
    private double speed;

    @Basic
    @Column(name = "route_id")
    public String getRouteId() {
        return routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    @Id
    @Column(name = "shape_id")
    public String getShapeId() {
        return shapeId;
    }

    public void setShapeId(String shapeId) {
        this.shapeId = shapeId;
    }

    @Id
    @Column(name = "split_id")
    public int getSplitId() {
        return splitId;
    }

    public void setSplitId(int splitId) {
        this.splitId = splitId;
    }

    @Basic
    @Column(name = "speed")
    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        this.speed = speed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SplitShapeSpeedEntity that = (SplitShapeSpeedEntity) o;
        return splitId == that.splitId &&
                Double.compare(that.speed, speed) == 0 &&
                Objects.equals(routeId, that.routeId) &&
                Objects.equals(shapeId, that.shapeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(routeId, shapeId, splitId, speed);
    }
}
