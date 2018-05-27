package project.visualisers;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.SimulatorAPI;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;

public class IntentionAntVisualiser extends TimeOutVisualiser{

    public IntentionAntVisualiser(Point position, SimulatorAPI simulator, long startTime, long timeOut) {
        super(position, simulator, startTime, timeOut);
    }
}
