package project.visualisers;

import com.github.rinde.rinsim.core.SimulatorAPI;
import com.github.rinde.rinsim.geom.Point;

public class GoalVisualiser extends TimeOutVisualiser{

    public GoalVisualiser(Point position, SimulatorAPI simulator, long startTime, long timeOut) {
        super(position, simulator, startTime, timeOut);
    }
}
