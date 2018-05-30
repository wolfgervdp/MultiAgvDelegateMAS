package project.visualisers;

import com.github.rinde.rinsim.core.SimulatorAPI;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;

public class TimeOutVisualiser  extends Visualiser implements TickListener {

    //    private static final long LIFETIME = 150000;
    SimulatorAPI simulator;
    private long timeOut;
    private long timeAtLastExploration = 0;

    public TimeOutVisualiser(Point position, SimulatorAPI simulator, long startTime, long timeOut) {
        super(position);
        this.simulator = simulator;
        this.timeOut = timeOut;
        timeAtLastExploration = startTime;
    }


    @Override
    public void tick(TimeLapse timeLapse) {
        if(timeAtLastExploration + timeOut <= timeLapse.getTime()){
            simulator.unregister(this);
            timeAtLastExploration = timeLapse.getTime();
        }
    }

    @Override
    public void afterTick(TimeLapse timeLapse) { }
}
