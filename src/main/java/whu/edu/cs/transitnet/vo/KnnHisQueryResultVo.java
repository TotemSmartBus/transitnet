package whu.edu.cs.transitnet.vo;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 查询结果 VO
 **/
public class KnnHisQueryResultVo {

    @Getter
    @Setter
    @SerializedName("buses")
    /**
     * 查询到的公交
     **/ private List<SimilarityQueryResultItem> buses;

    @Getter
    @Setter
    @SerializedName("trips")
    private List<tripPoints> trips;
    public KnnHisQueryResultVo(List<SimilarityQueryResultItem> temp,List<tripPoints> ts) {
        buses=temp;
        trips=ts;
    }
}
