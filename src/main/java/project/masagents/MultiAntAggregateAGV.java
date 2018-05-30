package project.masagents;

import com.github.rinde.rinsim.core.SimulatorAPI;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.road.GraphRoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import project.MultiAggregateAGV;
import project.MultiParcel;
import project.antsystems.*;
import project.MultiDepot;

import java.util.NoSuchElementException;
import java.util.Set;

public class MultiAntAggregateAGV  extends MultiAggregateAGV implements AntAGV {

    static final float RECONSIDERATION_TRESHOLD = 1.3f;
    static final int EXPLORATION_FREQ = 20000; //In ms
    static final int NUMBER_OF_EXPL_ANTS = 3;
    static final int WAIT_FOR_EXPL_ANTS = 1;

    private IntentionAnt currentIntention;
    private boolean isWaitingForExplorationAnts = false;
    private int numOfExplAntsReportedBack = 0;
    private long timeAtLastExploration = 0;
    private Point deliveryLocation;

    public MultiAntAggregateAGV(Point startPosition, int capacity, SimulatorAPI sim) {
        super(startPosition, capacity, sim);
    }

    @Override
    protected void updateImpl(TimeLapse timeLapse) {

        //If we don't know what to do (=no intention), send out exploration ants with a certain frequency, and don't do anything else
        if(currentIntention == null){
            if(timeAtLastExploration + EXPLORATION_FREQ <= timeLapse.getTime()){
                sendExplorationAnts();
                timeAtLastExploration = timeLapse.getTime();
            }
            return;
        }

        RoadModel rm = getRoadModel();

        //If we got to the parcel or depot, pick up/deliver and set next goal
        if (atParcelOrDepot() && getPDPModel().getVehicleState(this) == PDPModel.VehicleState.IDLE) {
            Set<MultiDepot> depots = rm.getObjectsAt(this, MultiDepot.class);   //Get Depots at current location
            if(depots.iterator().hasNext()){
                System.out.println("There was a depot, ");
                deliverParcel(timeLapse, getPDPModel().getContents(this).iterator().next());    //Drop off the parcel on that location
            }
            currentIntention.popPath();
            //If at next waypoint, resend exploration ants, and pop the location we got to
        }else if(atNextGoal()) {

            countTime = timeLapse.getTime();
            sendExplorationAnts();
            System.out.println("at next goal");
            currentIntention.popGoalLocation();
            //Point p = currentIntention.peekNextGoalLocation();
            //sim.register(new GoalVisualiser(p, sim, timeLapse.getStartTime(), 100000));
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
                    // set currentIntention to null to start sending exploration ants and return
                    currentIntention = null;
                    return;
                }
            }
            rm.moveTo(this, p, timeLapse);
        }
    }

    @Override
    protected void unregister() {
        getRoadModel().unregister(this);
        sim.unregister(this);
        getPDPModel().unregister(this);
    }

    @Override
    protected void register() {
        sim.register(this);
    }

    @Override
    protected void semiUnregister() {
        getRoadModel().unregister(this);
        getPDPModel().unregister(this);
    }

    @Override
    protected MultiAggregateAGV createVehicle(Point location, MultiParcel parcel) {
        return null;
    }

    private boolean atNextGoal(){
        return currentIntention == null ? false: getRoadModel().getPosition(this).equals(currentIntention.peekNextGoalLocation());
    }

    private boolean atParcelOrDepot() {
        //System.out.println("peekNextParcelLocation: " + currentIntention.peekNextParcelLocation());
        boolean b = currentIntention == null ? false : getRoadModel().getPosition(this).equals(currentIntention.peekNextParcelLocation());
        if(b) System.out.println("At parcel!!");
        return b;
    }


    public void sendExplorationAnts(){
        //System.out.println("Sending exploration ants. Starting at position " + getRoadModel().getPosition(this));

        for(int i = 0; i < NUMBER_OF_EXPL_ANTS; i++){
            GenericExplorationAnt ant = new GenericExplorationAnt(this, getRoadModel().getPosition(this), (GraphRoadModel) getRoadModel(), sim, MultiDepot.class);
            ant.setCondition(explorable -> {
                getRoadModel().getPosition(explorable).equals(deliveryLocation);
                return true;
            });
            sim.register(ant);
        }
    }


    public void reportBack(GenericExplorationAnt ant){
        //Todo: Check whether the returned intention ant is a
        Point p = getRoadModel().getPosition(this);
        IntentionAnt tempIntentionAnt = new IntentionAnt(ant);
        tempIntentionAnt.trimPath(p);
        //numberOfAntCounter--;
        if((currentIntention == null
                || ant.getTotalHeuristicValue() > currentIntention.getTotalHeuristicValue() + Math.abs(currentIntention.getTotalHeuristicValue())*(RECONSIDERATION_TRESHOLD-1))
                && getRoadModel().getShortestPathTo(this,tempIntentionAnt.peekNextGoalLocation()).size() <= 2){

            if(currentIntention != null) {
                sim.unregister(currentIntention);
            }
            currentIntention = tempIntentionAnt;
            sim.register(currentIntention);
            //System.out.println("----------------------------Found better path!!" + (ant.getTotalHeuristicValue()) + ant);
        }
        numOfExplAntsReportedBack++;
        if(numOfExplAntsReportedBack >= WAIT_FOR_EXPL_ANTS){
            isWaitingForExplorationAnts = false;
        }
    }

    public void setDeliveryLocation(Point deliveryLocation) {
        this.deliveryLocation = deliveryLocation;
    }
}
