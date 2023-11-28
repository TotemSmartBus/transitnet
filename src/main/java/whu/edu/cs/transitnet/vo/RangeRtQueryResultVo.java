package whu.edu.cs.transitnet.vo;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class RangeRtQueryResultVo {
    @Getter
    @Setter
    @SerializedName("buses")
    /**
     * 查询到的BUS ID
     **/ private List<RangeRtQueryResultItem> buses;

    @Getter
    @Setter
    @SerializedName("trips")
    private List<tripPoints> trips;

     public RangeRtQueryResultVo(List<RangeRtQueryResultItem> temp,List<tripPoints> ts){
         buses=temp;
         trips=ts;
     }
}
