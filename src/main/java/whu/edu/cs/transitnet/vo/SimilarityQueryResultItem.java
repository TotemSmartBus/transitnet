package whu.edu.cs.transitnet.vo;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

public class SimilarityQueryResultItem {
    public SimilarityQueryResultItem(String id, double similarity) {
        this.id = id;
        this.similarity = similarity;
    }

    @Getter
    @Setter
    @SerializedName("id")
    private String id;

    @Getter
    @Setter
    @SerializedName("similarity")
    private double similarity;
}
