package whu.edu.cs.transitnet.vo;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 查询结果 VO
 **/
public class SimilarityQueryResultVo {

    @Getter
    @Setter
    @SerializedName("routes")
    /**
     * 查询到的路线
     **/ private List<SimilarityQueryResultItem> routes;

    @Getter
    @Setter
    @SerializedName("buses")
    /**
     * 查询到的公交
     **/ private List<SimilarityQueryResultItem> buses;
}
