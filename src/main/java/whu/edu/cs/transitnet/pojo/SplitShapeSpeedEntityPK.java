package whu.edu.cs.transitnet.pojo;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

public class SplitShapeSpeedEntityPK implements Serializable {
    private String shapeId;
    private int splitId;

    @Column(name = "shape_id")
    @Id
    public String getShapeId() {
        return shapeId;
    }

    public void setShapeId(String shapeId) {
        this.shapeId = shapeId;
    }

    @Column(name = "split_id")
    @Id
    public int getSplitId() {
        return splitId;
    }

    public void setSplitId(int splitId) {
        this.splitId = splitId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SplitShapeSpeedEntityPK that = (SplitShapeSpeedEntityPK) o;
        return splitId == that.splitId &&
                Objects.equals(shapeId, that.shapeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shapeId, splitId);
    }
}
