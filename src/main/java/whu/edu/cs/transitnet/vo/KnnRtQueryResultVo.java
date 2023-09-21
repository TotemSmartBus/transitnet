package whu.edu.cs.transitnet.vo;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import whu.edu.cs.transitnet.service.RealtimeKNNExpService;

import java.util.ArrayList;
import java.util.List;

public class KnnRtQueryResultVo {
    @Getter
    @Setter
    @SerializedName("knn_rt_res")
    /**
     * 查询到的res
     **/ private List<SimilarityQueryResultItem> buses;

    public KnnRtQueryResultVo(List<SimilarityQueryResultItem> temp) {
        buses=temp;
    }

}
