package whu.edu.cs.transitnet.Torch.base.visualization.trajectory;

import whu.edu.cs.transitnet.vo.ShapePointVo;

import java.util.List;
public class Geometry {

    String type = "LineString";

    /**
     * lng, lat
     */
    double[][] coordinates;

    public Geometry(List<ShapePointVo> path){
        int pathLen = path == null ? 0 : path.size();
        coordinates = new double[pathLen][2];
        for (int i = 0; i < pathLen; i++){
            coordinates[i][0] = path.get(i).getLng();
            coordinates[i][1] = path.get(i).getLat();
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double[][] getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(double[][] coordinates) {
        this.coordinates = coordinates;
    }
}
