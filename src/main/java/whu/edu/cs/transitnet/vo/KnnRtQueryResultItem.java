package whu.edu.cs.transitnet.vo;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

public class KnnRtQueryResultItem {
    public KnnRtQueryResultItem(int r, String id, double sim) {
        this.r=r;
        this.id = id;
        this.sim = sim;
    }
    @Getter
    @Setter
    @SerializedName("r")
    private int r;

    @Getter
    @Setter
    @SerializedName("id")
    private String id;

    @Getter
    @Setter
    @SerializedName("sim")
    private double sim;
}
