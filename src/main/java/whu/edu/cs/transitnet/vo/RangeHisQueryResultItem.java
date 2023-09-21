package whu.edu.cs.transitnet.vo;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

public class RangeHisQueryResultItem {
    public RangeHisQueryResultItem(String id){
        this.id=id;
    }

    @Getter
    @Setter
    @SerializedName("id")
    private String id;


}
