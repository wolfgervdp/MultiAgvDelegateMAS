package project.visualisers;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.geom.Point;

public class ExplorationAntVisualiser extends ConnectedVisualiser{


    public ExplorationAntVisualiser(Point position, Point prevPos) {
        super(position, prevPos);
    }
}
