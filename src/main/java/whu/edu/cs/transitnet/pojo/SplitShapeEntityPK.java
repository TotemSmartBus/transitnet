package whu.edu.cs.transitnet.pojo;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

public class SplitShapeEntityPK implements Serializable {
    private String shapeId;
    private int splitId;
    private int ptId;

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

    @Column(name = "pt_id")
    @Id
    public int getPtId() {
        return ptId;
    }

    public void setPtId(int ptId) {
        this.ptId = ptId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SplitShapeEntityPK that = (SplitShapeEntityPK) o;
        return splitId == that.splitId &&
                ptId == that.ptId &&
                Objects.equals(shapeId, that.shapeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shapeId, splitId, ptId);
    }
}
