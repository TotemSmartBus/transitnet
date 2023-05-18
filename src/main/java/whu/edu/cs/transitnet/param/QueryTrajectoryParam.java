package whu.edu.cs.transitnet.param;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class QueryTrajectoryParam {
    @Getter
    @Setter
    private List<QueryTrajectoryPoint> points;

    @Getter
    @Setter
    private boolean withTime;

    @Getter
    @Setter
    private int k;
}
