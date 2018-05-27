package project.visualisers;

import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.geom.Point;

public abstract class Visualiser implements RoadUser {

    Point position;

    public Visualiser(Point position) {
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
