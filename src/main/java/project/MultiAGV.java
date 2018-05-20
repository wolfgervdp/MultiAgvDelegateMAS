package project;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.pdp.VehicleDTO;
import com.github.rinde.rinsim.core.model.road.GraphRoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;
import project.antsystems.AntAgent;
import project.antsystems.ExplorationAnt;
import project.antsystems.IntentionAnt;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MultiAGV extends Vehicle implements RealworldAgent {

    static final float RECONSIDERATION_TRESHOLD = 1.3f;
    static final int EXPLORATION_FREQ = 1000; //In ms

    private int strenght;
    private List<AntAgent> antAgentList = new ArrayList<>();
    private IntentionAnt currentIntention;
    private Simulator sim;
    private boolean isWaiting = false;
    private static final double SPEED = 1000d;
    private Optional<Parcel> currentParcel;
    private long timeAtLastExploration = 0;
    
    public MultiAGV(Point startPosition,  int capacity, Simulator sim) {
        super(VehicleDTO.builder()
                .capacity(capacity)
                .startPosition(startPosition)
                .speed(SPEED)
                .build());
        currentParcel = Optional.absent();
        this.sim = sim;
    }

    public int getStrenght() {
        return strenght;
    }

    @Override
    protected void tickImpl(TimeLapse timeLapse) {


        //Send exploration ants every EXPLORATION_FREQ ms
        if(timeAtLastExploration + EXPLORATION_FREQ <= timeLapse.getTime()){
           // sendExplorationAnts();
            timeAtLastExploration = timeLapse.getTime();
        }

        //If no intention set, there's nothing to do yets
        if(currentIntention == null){
            sendExplorationAnts();
            return;
        }

        RoadModel rm = getRoadModel();
        //If we got to the parcel, pick up and set next goal
        if (atParcel()) {
            Set<MultiParcel> parcels = rm.getObjectsAt(this, MultiParcel.class);
            pickUp(parcels.iterator().next());  //Pick random parcel on that location
            currentIntention.popPath();

        //If we got to the parcel, pick up and set next goal
        }else if(atNextGoal()) {
            currentIntention.popGoalLocation();
        }

        //Move to the direction of next goal
        if(!isWaiting){
            Point p = currentIntention.peekNextGoalLocation();
            //rm.moveTo(this, p, timeLapse); Todo : solve nullptr
        }
    }

    public void reportBack(ExplorationAnt ant){
        if(currentIntention == null || ant.getHeuristicValue() >= currentIntention.getHeuristicValue()*RECONSIDERATION_TRESHOLD){
            currentIntention = new IntentionAnt(ant);
        }
    }

    private boolean atNextGoal(){
        return currentIntention == null ? false : getRoadModel().getPosition(this).equals(currentIntention.peekNextGoalLocation());
    }
    private boolean atParcel() {
        return currentIntention == null ? false : getRoadModel().getPosition(this).equals(currentIntention.peekNextParcelLocation());
    }

    public void pickUp(MultiParcel parcel){

       if(parcel.tryPickUp(this)){

           //Succeeded! Start carrying parcel to its destination
       }else{
           //Wait, since not enough agv's are helping
           isWaiting = true;
       }
        //Todo
    }

    public void sendExplorationAnts(){
        System.out.println("Sending exploration ants. Starting at position " + getRoadModel().getPosition(this));
        ExplorationAnt ant = new ExplorationAnt(this, getRoadModel().getPosition(this), (GraphRoadModel) getRoadModel(), sim);
        sim.register(ant);
    }

    public void startCarrying() {
        //Todo
    }
}
