package whu.edu.cs.transitnet.pojo;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

public class ShapesEntityPK implements Serializable {
    private String shapeId;
    private Integer shapePtSequence;

    public ShapesEntityPK(String shapeId, Integer shapePtSequence) {
        this.shapeId = shapeId;
        this.shapePtSequence = shapePtSequence;
    }

    public ShapesEntityPK() {

    }
    @Column(name = "shape_id")
    @Id
    public String getShapeId() {
        return shapeId;
    }

    public void setShapeId(String shapeId) {
        this.shapeId = shapeId;
    }

    @Column(name = "shape_pt_sequence")
    @Id
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
        ShapesEntityPK that = (ShapesEntityPK) o;
        return Objects.equals(shapeId, that.shapeId) &&
                Objects.equals(shapePtSequence, that.shapePtSequence);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shapeId, shapePtSequence);
    }
}
