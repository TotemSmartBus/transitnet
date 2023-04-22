package whu.edu.cs.transitnet.param;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class QueryPathParam {

    @SerializedName("points")
    private ArrayList<QueryPathPoint> points;

    @SerializedName("k")
    private int k;
}
