package project;

import com.github.rinde.rinsim.core.SimulatorAPI;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.pdp.VehicleDTO;
import com.github.rinde.rinsim.core.model.road.GraphRoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import javafx.geometry.Pos;
import project.antsystems.ExplorationAnt;
import project.visualisers.GoalVisualiser;
import project.antsystems.IntentionAnt;

import java.util.NoSuchElementException;
import java.util.Set;

public class MultiAGV extends Vehicle {

    static final float RECONSIDERATION_TRESHOLD = 1.3f;
    static final int EXPLORATION_FREQ = 200000; //In ms
    static final int NUMBER_OF_EXPL_ANTS = 1;
    static final int WAIT_FOR_EXPL_ANTS = 1;


    private int id;
    private static int idCounter = 0;

    private int strenght = 10;
    private IntentionAnt currentIntention;
    private SimulatorAPI sim;
    private boolean isWaiting = false;
    private boolean isWaitingForExplorationAnts = false;
    private int numOfExplAntsReportedBack = 0;

    private static final double SPEED = 0.1d;
    private long timeAtLastExploration = 0;

    private int numberOfAntCounter = 0;
    private boolean hasSentOutAnts = false;

    private int explorationCounter;

    private Point prevPos;

    public MultiAGV(Point startPosition,  int capacity, SimulatorAPI sim) {
        super(VehicleDTO.builder()
                .capacity(capacity)
                .startPosition(startPosition)
                .speed(SPEED)
                .build());
        this.sim = sim;
        id = ++idCounter;
    }

    
    public int getStrenght() {
        return strenght;
    }

    @Override
    public String toString() {

        if(currentIntention != null)
        return "MultiAGV " + id + ", h=" + currentIntention.getTotalHeuristicValue();
        else return "MultiAGV " + id;
    }

    @Override
    protected void tickImpl(TimeLapse timeLapse) {
        //System.out.println(id + "->" + getPDPModel().getVehicleState(this));

        //System.out.println("Number of ants sent out from multiagv: " + numberOfAntCounter);

        //Send exploration ants every EXPLORATION_FREQ ms
        if(timeAtLastExploration + EXPLORATION_FREQ <= timeLapse.getTime()){
            //sendExplorationAnts();
            timeAtLastExploration = timeLapse.getTime();
        }

        //If we don't know what to do (=no intention), certainly send out exploration ants, and don't do anything else
        if(currentIntention == null){
            sendExplorationAnts();
            return;
        }

        RoadModel rm = getRoadModel();
        //If we got to the parcel, pick up and set next goal
        if (atParcel() && getPDPModel().getVehicleState(this) == PDPModel.VehicleState.IDLE) {
            Set<MultiParcel> parcels = rm.getObjectsAt(this, MultiParcel.class);
            if(parcels.iterator().hasNext()){
                System.out.println("There was a parcel");
                pickUp(parcels.iterator().next(), timeLapse);    //Pick random parcel on that location
            }
            currentIntention.popPath();

        //If we got to the parcel, pick up and set next goal
        }else if(atNextGoal()) {
            sendExplorationAnts();
            System.out.println("at next goal");
            prevPos = currentIntention.peekNextGoalLocation();
            currentIntention.popGoalLocation();
            Point p = currentIntention.peekNextGoalLocation();
            sim.register(new GoalVisualiser(p, sim, timeLapse.getStartTime(), 100000));
        }

        //Move to the direction of next goal
        if(!isWaiting && !isWaitingForExplorationAnts){
            Point p = currentIntention.peekNextGoalLocation();
            //If no next goal left, pop path for finding next parcel
            while(p == null){
                try {
                    currentIntention.popPath();
                    p = currentIntention.peekNextGoalLocation();

                }catch(NoSuchElementException e){
                    // If there is nothing left in this intention, then do nothing,
                    // set currentIntention to null to send exploration ants more quickly and return
                    currentIntention = null;
                    return;
                }
            }

//            if(prevPos != null && !prevPos.equals(currentIntention.peekNextGoalLocation())) {
//                System.out.println("prevpos : " + prevPos + " - currentgoal " + currentIntention.peekNextGoalLocation());
//                //System.out.println(prevPos.equals(currentIntention.peekNextGoalLocation()));
//                System.out.println("prevpos not null");
//                InfrastructureAgent data = (InfrastructureAgent) ((GraphRoadModel) getRoadModel()).getGraph().getConnection(prevPos, currentIntention.peekNextGoalLocation()).data().get();
//                System.out.println(data.reservations);
//            }

            rm.moveTo(this, p, timeLapse);
            //System.out.println(currentIntention);
        }
    }

    public int getId() {
        return id;
    }

    public void reportBack(ExplorationAnt ant){
        IntentionAnt tempIntentionAnt = new IntentionAnt(ant);
        //numberOfAntCounter--;
        if((currentIntention == null
                || ant.getTotalHeuristicValue() > currentIntention.getTotalHeuristicValue() + Math.abs(currentIntention.getTotalHeuristicValue())*(RECONSIDERATION_TRESHOLD-1)
                //|| explorationCounter %10 == 0
            ) && getRoadModel().getShortestPathTo(this,tempIntentionAnt.peekNextGoalLocation()).size() <= 1){

            if(currentIntention != null) {
                sim.unregister(currentIntention);
            }
            IntentionAnt oldIntention = currentIntention;
            currentIntention = tempIntentionAnt;
            Point p = getRoadModel().getPosition(this);

            System.out.println("position : " + p);
            System.out.println("before trim" + currentIntention);
            currentIntention.trimPath(p);
            System.out.println("after trim" + currentIntention);
            sim.register(currentIntention);
            //System.out.println("----------------------------Found better path!!" + (ant.getTotalHeuristicValue()) + ant);
        }else{
            System.out.println("currentCapToExceed" + currentIntention.getTotalHeuristicValue() + Math.abs(currentIntention.getTotalHeuristicValue())*(RECONSIDERATION_TRESHOLD-1));
        }
        numOfExplAntsReportedBack++;
        if(numOfExplAntsReportedBack >= WAIT_FOR_EXPL_ANTS){
            isWaitingForExplorationAnts = false;
        }
    }

    private boolean atNextGoal(){
        return currentIntention == null ? false: getRoadModel().getPosition(this).equals(currentIntention.peekNextGoalLocation());
    }

    private boolean atParcel() {
        //System.out.println("peekNextParcelLocation: " + currentIntention.peekNextParcelLocation());
        boolean b = currentIntention == null ? false : getRoadModel().getPosition(this).equals(currentIntention.peekNextParcelLocation());
        if(b) System.out.println("At parcel!!");
        return b;
    }

    public void pickUp(MultiParcel parcelToPickup, TimeLapse time){
        RoadModel rm = getRoadModel();
        PDPModel pm = getPDPModel();

        if (parcelToPickup != null ){
            if (rm.equalPosition(parcelToPickup, this)
                    && pm.getTimeWindowPolicy().canPickup(parcelToPickup.getPickupTimeWindow(),
                    time.getTime(), parcelToPickup.getPickupDuration())) {

                if(parcelToPickup.tryPickUp(this)){
                    //Succeeded! Start carrying parcel to its destination
                    //System.out.println(pm.getVehicleState(this));
                    pm.pickup(this, parcelToPickup, time);
                }else{
                    //Wait, since not enough agv's are helping
                    isWaiting = true;
                    System.out.println("Waiting for other agvs....");
                }
            }
        }
        //Todo
    }

    public void sendExplorationAnts(){
        //System.out.println("Sending exploration ants. Starting at position " + getRoadModel().getPosition(this));
        numberOfAntCounter+=NUMBER_OF_EXPL_ANTS;
       // isWaitingForExplorationAnts = true;
        explorationCounter++;

        for(int i = 0; i < NUMBER_OF_EXPL_ANTS; i++){
            ExplorationAnt ant = new ExplorationAnt(this, getRoadModel().getPosition(this), (GraphRoadModel) getRoadModel(), sim);
            sim.register(ant);
        }
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
