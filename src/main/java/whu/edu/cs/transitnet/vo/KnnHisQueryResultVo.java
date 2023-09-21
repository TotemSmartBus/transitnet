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

    public KnnHisQueryResultVo(List<SimilarityQueryResultItem> temp) {
        buses=temp;
    }
}
