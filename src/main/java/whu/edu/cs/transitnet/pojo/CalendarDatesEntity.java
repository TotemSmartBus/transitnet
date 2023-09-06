package whu.edu.cs.transitnet.pojo;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "calendar_dates")
@IdClass(CalendarDatesEntityPK.class)
public class CalendarDatesEntity {
    private String serviceId;
    private String date;
    private Integer exceptionType;

    @Id
    @Column(name = "service_id")
    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    @Id
    @Column(name = "date")
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    @Basic
    @Column(name = "exception_type")
    public Integer getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(Integer exceptionType) {
        this.exceptionType = exceptionType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CalendarDatesEntity that = (CalendarDatesEntity) o;
        return Objects.equals(serviceId, that.serviceId) &&
                Objects.equals(date, that.date) &&
                Objects.equals(exceptionType, that.exceptionType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serviceId, date, exceptionType);
    }
}
