package project;

import com.github.rinde.rinsim.core.SimulatorAPI;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.ParcelDTO;
import com.github.rinde.rinsim.core.model.road.GraphRoadModel;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import project.antsystems.SignAnt;

public class MultiAntParcel extends MultiParcel implements TickListener {
    private static final long SETSIGN_FREQ = 40000;
    private SimulatorAPI sim;
    private long timeAtLastExploration;

    public MultiAntParcel(ParcelDTO parcelDto, SimulatorAPI sim) {
        super(parcelDto);
        this.sim = sim;
    }

    public double getUrgencyHeuristic(long currentTime) {
        return (carryers.size() + 1) * (1 - getOrderAnnounceTime());
    }

    @Override
    public void tick(TimeLapse timeLapse) {
        if (getPDPModel().getParcelState(this) == PDPModel.ParcelState.AVAILABLE || getPDPModel().getParcelState(this) == PDPModel.ParcelState.ANNOUNCED) {
            if (timeAtLastExploration + SETSIGN_FREQ <= timeLapse.getTime()) {
                sendAnts();
                timeAtLastExploration = timeLapse.getTime();
            }
        }
    }

    private void sendAnts() {
        sim.register(new SignAnt(getRoadModel().getPosition(this), (GraphRoadModel) getRoadModel(), sim));
    }

    @Override
    public void afterTick(TimeLapse timeLapse) { }
}
