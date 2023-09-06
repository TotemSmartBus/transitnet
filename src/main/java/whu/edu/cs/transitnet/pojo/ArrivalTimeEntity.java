package whu.edu.cs.transitnet.pojo;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "arrival_time")
public class ArrivalTimeEntity {
    private Timestamp timeSpan;
    private Integer allCount;
    private Integer lateCount;

    @Id
    @Column(name = "time_span")
    public Timestamp getTimeSpan() {
        return timeSpan;
    }

    public void setTimeSpan(Timestamp timeSpan) {
        this.timeSpan = timeSpan;
    }

    @Basic
    @Column(name = "all_count")
    public Integer getAllCount() {
        return allCount;
    }

    public void setAllCount(Integer allCount) {
        this.allCount = allCount;
    }

    @Basic
    @Column(name = "late_count")
    public Integer getLateCount() {
        return lateCount;
    }

    public void setLateCount(Integer lateCount) {
        this.lateCount = lateCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ArrivalTimeEntity that = (ArrivalTimeEntity) o;
        return Objects.equals(timeSpan, that.timeSpan) &&
                Objects.equals(allCount, that.allCount) &&
                Objects.equals(lateCount, that.lateCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeSpan, allCount, lateCount);
    }
}
