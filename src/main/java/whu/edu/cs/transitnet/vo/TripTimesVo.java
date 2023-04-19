package whu.edu.cs.transitnet.vo;

import java.sql.Time;
import java.util.Date;

public class TripTimesVo {
    private Integer id;
    private Time arrivalTime;
    private Time departureTime;

    public TripTimesVo(Date arrivalTime, Date departureTime) {
        this.arrivalTime = Time.valueOf(arrivalTime.toString());
        this.departureTime = Time.valueOf(departureTime.toString());
    }

    public TripTimesVo(Time arrivalTime, Time departureTime) {
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Time getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(Time arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public Time getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(Time departureTime) {
        this.departureTime = departureTime;
    }
}
