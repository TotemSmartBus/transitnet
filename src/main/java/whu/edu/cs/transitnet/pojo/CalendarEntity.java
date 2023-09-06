package whu.edu.cs.transitnet.pojo;

import javax.persistence.*;
import java.util.Arrays;
import java.sql.Date;
import java.util.Objects;

@Entity
@Table(name = "calendar")
public class CalendarEntity {
    private String serviceId;
    private byte[] monday;
    private byte[] tuesday;
    private byte[] wednesday;
    private byte[] thursday;
    private byte[] friday;
    private byte[] saturday;
    private byte[] sunday;
    private Date startDate;
    private Date endDate;

    @Id
    @Column(name = "service_id")
    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    @Basic
    @Column(name = "monday")
    public byte[] getMonday() {
        return monday;
    }

    public void setMonday(byte[] monday) {
        this.monday = monday;
    }

    @Basic
    @Column(name = "tuesday")
    public byte[] getTuesday() {
        return tuesday;
    }

    public void setTuesday(byte[] tuesday) {
        this.tuesday = tuesday;
    }

    @Basic
    @Column(name = "wednesday")
    public byte[] getWednesday() {
        return wednesday;
    }

    public void setWednesday(byte[] wednesday) {
        this.wednesday = wednesday;
    }

    @Basic
    @Column(name = "thursday")
    public byte[] getThursday() {
        return thursday;
    }

    public void setThursday(byte[] thursday) {
        this.thursday = thursday;
    }

    @Basic
    @Column(name = "friday")
    public byte[] getFriday() {
        return friday;
    }

    public void setFriday(byte[] friday) {
        this.friday = friday;
    }

    @Basic
    @Column(name = "saturday")
    public byte[] getSaturday() {
        return saturday;
    }

    public void setSaturday(byte[] saturday) {
        this.saturday = saturday;
    }

    @Basic
    @Column(name = "sunday")
    public byte[] getSunday() {
        return sunday;
    }

    public void setSunday(byte[] sunday) {
        this.sunday = sunday;
    }

    @Basic
    @Column(name = "start_date")
    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    @Basic
    @Column(name = "end_date")
    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CalendarEntity that = (CalendarEntity) o;
        return Objects.equals(serviceId, that.serviceId) &&
                Arrays.equals(monday, that.monday) &&
                Arrays.equals(tuesday, that.tuesday) &&
                Arrays.equals(wednesday, that.wednesday) &&
                Arrays.equals(thursday, that.thursday) &&
                Arrays.equals(friday, that.friday) &&
                Arrays.equals(saturday, that.saturday) &&
                Arrays.equals(sunday, that.sunday) &&
                Objects.equals(startDate, that.startDate) &&
                Objects.equals(endDate, that.endDate);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(serviceId, startDate, endDate);
        result = 31 * result + Arrays.hashCode(monday);
        result = 31 * result + Arrays.hashCode(tuesday);
        result = 31 * result + Arrays.hashCode(wednesday);
        result = 31 * result + Arrays.hashCode(thursday);
        result = 31 * result + Arrays.hashCode(friday);
        result = 31 * result + Arrays.hashCode(saturday);
        result = 31 * result + Arrays.hashCode(sunday);
        return result;
    }
}
