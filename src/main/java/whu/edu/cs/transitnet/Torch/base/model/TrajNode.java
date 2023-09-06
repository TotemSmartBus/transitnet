package whu.edu.cs.transitnet.Torch.base.model;

import java.util.ArrayList;
import java.util.List;

/**
 * A trajNode models a node on a trajectory.
 * Besides GPS coordinate, each node could also carries other information such as timestep, current speed etc.
 * However, algorithm from raw trajectory nodes to graph vertices for other kind of information requires further research and experiment.
 * This function is expected to be developed in future.
 */
public class TrajNode extends Coordinate{

    public int id;
    private long time = -1;
    List<String> bundle = new ArrayList<>(3);

    public TrajNode(double lat, double lng) {
        super(lat,lng);
    }

    public TrajNode(double lat, double lng, long time) {
        this(lat, lng);
        time = time;
    }

    public void addExtraInfo(String extra){
        bundle.add(extra);
    }

    public void setTime(long time){
        time = time;
    }

    public long getTime() {
        if (time == -1) {
            throw new IllegalStateException("try to fetch time in node not containing time information");
        }
        return time;
    }

    @Override
    public int getId() {
        return id;
    }
}
