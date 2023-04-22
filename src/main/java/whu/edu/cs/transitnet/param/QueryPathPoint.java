package whu.edu.cs.transitnet.param;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class QueryPathPoint {

    @Getter
    @Setter
    @SerializedName("lat")
    private double lat;

    @Getter
    @Setter
    @SerializedName("lng")
    private double lon;

}
