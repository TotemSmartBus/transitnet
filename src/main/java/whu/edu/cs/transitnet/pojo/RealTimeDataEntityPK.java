package whu.edu.cs.transitnet.pojo;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

public class RealTimeDataEntityPK implements Serializable {
    private String vehicleId;
    private String recordedTime;

    @Column(name = "vehicle_id")
    @Id
    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    @Column(name = "recorded_time")
    @Id
    public String getRecordedTime() {
        return recordedTime;
    }

    public void setRecordedTime(String recordedTime) {
        this.recordedTime = recordedTime;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RealTimeDataEntityPK that = (RealTimeDataEntityPK) o;
        return Objects.equals(vehicleId, that.vehicleId) &&
                Objects.equals(recordedTime, that.recordedTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vehicleId, recordedTime);
    }
}
