package project;

import com.github.rinde.rinsim.core.SimulatorAPI;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import project.gradientfield.MultiAGVGradientField;

public abstract class MultiAggregateAGV extends MultiAGV{

    private Point agvSpawnPoint;
    private long timeOfLastSpawn = Integer.MAX_VALUE;
    private int agvsLeftToSpawn = 0;

    public MultiAggregateAGV(Point startPosition, int capacity, SimulatorAPI sim) {
        super(startPosition, capacity, sim);
    }

    @Override
    protected final void update(TimeLapse timeLapse) {
        if (timeOfLastSpawn + 800000 <= timeLapse.getTime() && agvsLeftToSpawn > 0) {
            spawnNewAGV(timeLapse);
            timeOfLastSpawn = timeLapse.getTime();
            agvsLeftToSpawn--;
            return;
        } else if(timeOfLastSpawn != Integer.MAX_VALUE) {
            if (agvsLeftToSpawn == 0) {
                sim.unregister(this);
            }
            return;
        }
        updateImpl(timeLapse);
    }

    protected abstract void updateImpl(TimeLapse timeLapse);

    private void spawnNewAGV(TimeLapse time) {
        RoadModel rm = getRoadModel();
        MultiAGVGradientField newAGVRoadUser = (new MultiAGVGradientField(agvSpawnPoint, 1, this.sim));
        //newAGVRoadUser.setStartPosition();
        sim.register(newAGVRoadUser);
        rm.moveTo(newAGVRoadUser, new Point(32, 32), time);
    }

    protected void startSpawning(int numberOfAgvs, Point location, long currentTime){

        semiUnregister();
        agvsLeftToSpawn = numberOfAgvs;
        agvSpawnPoint = location;
        this.timeOfLastSpawn = currentTime;
    }

    protected void deliverParcel(TimeLapse timeLapse, Parcel p) {
        getPDPModel().deliver(this, p, timeLapse);
        semiUnregister();
        semiUnregister();
        startSpawning( (int) p.getNeededCapacity(), p.getDeliveryLocation() ,timeLapse.getTime());
    }


}
