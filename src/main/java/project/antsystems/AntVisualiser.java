package project.antsystems;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.geom.Point;

public class AntVisualiser implements RoadUser{

    Point position;

    public AntVisualiser(Point position) {
        this.position = position;
    }

    @Override
    public void initRoadUser(RoadModel model) {
        model.addObjectAt(this,position);
    }

    @Override
    public String toString() {
       return "";
    }
}
