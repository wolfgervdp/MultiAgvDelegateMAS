package project;

import com.github.rinde.rinsim.core.SimulatorAPI;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.sun.org.apache.xpath.internal.operations.Mult;
import org.eclipse.swt.internal.win32.POINT;
import project.gradientfield.MultiAGVGradientField;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public abstract class MultiAggregateAGV extends MultiAGV {

    private Point agvSpawnPoint;
    private long timeOfLastSpawn = Integer.MAX_VALUE;
    private int agvsLeftToSpawn = 0;
    private MultiParcel deliveryParcel;
    private ArrayList<Point> unregisteredAGVStartLocation=new ArrayList<Point>();
    private ArrayList<Point> garageLocation=new ArrayList<Point>();

    public MultiAggregateAGV(Point startPosition, int capacity, SimulatorAPI sim) {
        super(startPosition, capacity, sim);
    }

    @Override
    protected final void update(TimeLapse timeLapse) {

        if (timeOfLastSpawn  <= timeLapse.getTime() && agvsLeftToSpawn > 0) {
            boolean freeLocation = true;

            Set<MultiAGV> vehicles = (getRoadModel().getObjectsOfType(MultiAGV.class));
            for (MultiAGV agv: vehicles) {
                if (Point.distance(agv.getPosition(),agvSpawnPoint)<4) {
                    freeLocation=false;
                    break;
                }


            }

            if ((freeLocation == true ) ){
                spawnNewAGV(timeLapse);
                timeOfLastSpawn = timeLapse.getTime();
                agvsLeftToSpawn--;
                return;
           }
           return;
        } else if (timeOfLastSpawn != Integer.MAX_VALUE) {
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
        MultiAGV newAGV = createVehicle(agvSpawnPoint, deliveryParcel);
        sim.register(newAGV);
    }

    @Override
    final protected void afterUpdate(TimeLapse timeLapse) {

    }

    protected void startSpawning(int numberOfAgvs, Point location, long currentTime, MultiParcel p){
        semiUnregister();
        agvsLeftToSpawn = numberOfAgvs;
        agvSpawnPoint = location;
        this.timeOfLastSpawn = currentTime;
    }

    protected void deliverParcel(TimeLapse timeLapse, MultiParcel p) {
        getPDPModel().deliver(this, p, timeLapse);
        deliveryParcel=p;
        startSpawning((int) p.getNeededCapacity(), p.getDeliveryLocation(), timeLapse.getTime(),p);
    }

}
