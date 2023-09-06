package whu.edu.cs.transitnet.pojo;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "shapes")
@IdClass(ShapesEntityPK.class)
public class ShapesEntity {
    private String shapeId;
    private Double shapePtLat;
    private Double shapePtLon;
    private Integer shapePtSequence;

    @Id
    @Column(name = "shape_id")
    public String getShapeId() {
        return shapeId;
    }

    public void setShapeId(String shapeId) {
        this.shapeId = shapeId;
    }

    @Basic
    @Column(name = "shape_pt_lat")
    public Double getShapePtLat() {
        return shapePtLat;
    }

    public void setShapePtLat(Double shapePtLat) {
        this.shapePtLat = shapePtLat;
    }

    @Basic
    @Column(name = "shape_pt_lon")
    public Double getShapePtLon() {
        return shapePtLon;
    }

    public void setShapePtLon(Double shapePtLon) {
        this.shapePtLon = shapePtLon;
    }

    @Id
    @Column(name = "shape_pt_sequence")
    public Integer getShapePtSequence() {
        return shapePtSequence;
    }

    public void setShapePtSequence(Integer shapePtSequence) {
        this.shapePtSequence = shapePtSequence;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ShapesEntity that = (ShapesEntity) o;
        return Objects.equals(shapeId, that.shapeId) &&
                Objects.equals(shapePtLat, that.shapePtLat) &&
                Objects.equals(shapePtLon, that.shapePtLon) &&
                Objects.equals(shapePtSequence, that.shapePtSequence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shapeId, shapePtLat, shapePtLon, shapePtSequence);
    }
}
