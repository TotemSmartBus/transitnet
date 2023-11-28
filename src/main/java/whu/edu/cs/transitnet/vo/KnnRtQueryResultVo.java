package whu.edu.cs.transitnet.vo;

import com.google.gson.annotations.SerializedName;
import edu.whu.hyk.model.Point;
import lombok.Getter;
import lombok.Setter;
import whu.edu.cs.transitnet.param.QueryKnnRtParam;
import whu.edu.cs.transitnet.realtime.Vehicle;
import whu.edu.cs.transitnet.service.RealtimeKNNExpService;

import java.util.ArrayList;
import java.util.List;

public class KnnRtQueryResultVo {
    @Getter
    @Setter
    @SerializedName("knn_rt_res")
    /**
     * 查询到的res
     **/
    private List<SimilarityQueryResultItem> buses;
    @Getter
    @Setter
    @SerializedName("trips")
    private List<tripPoints> trips;
    public KnnRtQueryResultVo(List<SimilarityQueryResultItem> temp ,List<tripPoints> ts) {
        buses=temp;
        trips=ts;
    }

}
