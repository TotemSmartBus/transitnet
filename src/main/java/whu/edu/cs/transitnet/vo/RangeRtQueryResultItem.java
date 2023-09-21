package whu.edu.cs.transitnet.vo;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

public class RangeRtQueryResultItem {
    public RangeRtQueryResultItem(String id, double[] position) {
        this.id = id;
        this.position = position;
    }

    @Getter
    @Setter
    @SerializedName("id")
    private String id;

    @Getter
    @Setter
    @SerializedName("position")
    private double[] position;
}
