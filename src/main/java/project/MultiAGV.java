package project;

import com.github.rinde.rinsim.core.SimulatorAPI;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.pdp.VehicleDTO;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import project.masagents.MultiAntAGV;
import project.masagents.MultiAntAggregateAGV;

import java.util.Map;

public abstract class MultiAGV extends Vehicle {

    protected int id;
    private static int idCounter = 0;

    private int strenght = 10;

    protected SimulatorAPI sim;
    protected boolean isWaiting = false;
    private static final double SPEED = 0.1d;

    private boolean shouldUnregister = false;

    public MultiAGV(Point startPosition, int capacity, SimulatorAPI sim) {
        super(VehicleDTO.builder()
                .capacity(capacity)
                .startPosition(startPosition)
                .speed(SPEED)
                .build());
        this.sim = sim;

        id = ++idCounter;
    }

    public MultiAGV(Point startPosition,  int capacity) {
        super(VehicleDTO.builder()
                .capacity(capacity)
                .startPosition(startPosition)
                .speed(SPEED)
                .build());
        id = ++idCounter;
    }

    @Override
    protected final void tickImpl(TimeLapse time) {
        if (shouldUnregister) {
            unregister();
            return;
        }
        update(time);
    }

    public Point getPosition() {
        return getRoadModel().getPosition(this);
    }

    protected abstract void update(TimeLapse timeLapse);
    public int getStrenght() {
        return strenght;
    }

    @Override
    public String toString() {
        return "MultiAGV " + id+" class: "+ this.getClass();
    }

    public int getId() {
        return id;
    }

    public void pickUp(MultiParcel parcelToPickup, TimeLapse time){
        PDPModel pm = getPDPModel();

        final double newSize = getPDPModel().getContentsSize(this)
                + parcelToPickup.requiredCapacity();
        if (newSize <= getCapacity()) {
            //We are the last one, let's create an aggregate vehicle!
          //  if (this.getCapacity() != parcelToPickup.getNeededCapacity()) {
            shouldUnregister = true;
            unregister();

            MultiAGV newBigVehicle = createVehicle(parcelToPickup.getPickupLocation(), parcelToPickup);//(new MultiAGVGradientField(closest.getPickupLocation(), (int) closest.getNeededCapacity(), this.sim, true));
        ///sim.register(newBigVehicle);
            newBigVehicle.register();
            pm.pickup(newBigVehicle, parcelToPickup, time);
            return;
           /* } else {
                pm.pickup(this, parcelToPickup, time);
            }*/
        } else {

            parcelToPickup.incrementWaitingAgvs();
            shouldUnregister = true;
            return;
        }

    }

    protected boolean moveTo(Point p, TimeLapse timeLapse){
       // if(!hasMovingRoadUser(p)){
//            try{
                getRoadModel().moveTo(this, p, timeLapse);
                return true;
//            }catch(VerifyException vexc){
//                //  currentIntention = null;
//                return false;
//            }
       // }
    }

    private boolean hasMovingRoadUser(Point p){
        for (Map.Entry<RoadUser, Point> entry : this.getRoadModel().getObjectsAndPositions().entrySet()) {
            if ((entry.getKey() instanceof MultiAntAGV || entry.getKey() instanceof MultiAntAggregateAGV) && entry.getValue().equals(p)) {
                return true;
            }
        }
        return false;
    }

//    public void pickUp(){
//        getPDPModel().pickup(this,);
//    }

    /*
        This method should unregister the AGVs from the whole simulator
     */
    protected abstract void unregister();
    protected abstract void register();
    /*
        This method should unregister the AGV from all the models, but still keep it registerd in the simulator for ticks
     */

    protected abstract void semiUnregister();
    protected abstract MultiAGV createVehicle(Point location, MultiParcel parcel);
    protected abstract  void afterUpdate(TimeLapse timeLapse);

    @Override
    public void afterTick(TimeLapse time) {
        afterUpdate(time);

    }

    public void startCarrying() {
        isWaiting = false;
        //Todo: unify the AGV's

    }

    @Override
    public int hashCode() {
        return id;
    }
}
