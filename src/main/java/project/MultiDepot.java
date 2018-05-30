package project;

import com.github.rinde.rinsim.core.SimulatorAPI;
import com.github.rinde.rinsim.core.model.pdp.Depot;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.road.GraphRoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import project.antsystems.Explorable;
import project.antsystems.SignAnt;

// currently has no function
class MultiDepot extends Depot implements TickListener, Explorable {

    private static final long SETSIGN_FREQ = 40000;

    private SimulatorAPI sim;
    private long timeAtLastExploration;

    MultiDepot(Point position, SimulatorAPI sim) {
        super(position);
        this.sim = sim;
    }

    public double getHeuristicAddition(){
        return 0;
    }

    @Override
    public void initRoadPDP(RoadModel pRoadModel, PDPModel pPdpModel) {}

    @Override
    public String toString() {
        return "Depot";
    }

    @Override
    public void tick(TimeLapse timeLapse) {
        if (timeAtLastExploration + SETSIGN_FREQ <= timeLapse.getTime()) {
            sendAnts();
            timeAtLastExploration = timeLapse.getTime();
        }
    }

    private void sendAnts(){
        sim.register(new SignAnt(getRoadModel().getPosition(this), (GraphRoadModel) getRoadModel(), sim, waitingAGVs));
    }

    @Override
    public void afterTick(TimeLapse timeLapse) {}
}
