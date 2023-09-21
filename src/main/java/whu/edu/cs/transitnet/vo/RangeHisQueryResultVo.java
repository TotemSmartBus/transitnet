package whu.edu.cs.transitnet.vo;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import whu.edu.cs.transitnet.service.index.TripId;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class RangeHisQueryResultVo {
    @Getter
    @Setter
    @SerializedName("buses")
    /**
     * 查询到的公交
     **/ private List<RangeHisQueryResultItem> buses;

    public RangeHisQueryResultVo(HashSet<TripId> temp) {
        buses=new ArrayList<>();
        for(TripId item:temp){
            buses.add(new RangeHisQueryResultItem(item.toString()));
        }
    }
}
