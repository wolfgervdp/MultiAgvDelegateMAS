package project.masagents;

import com.github.rinde.rinsim.core.SimulatorAPI;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.road.GraphRoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.util.TimeWindow;
import project.MultiAGV;
import project.MultiDepot;
import project.MultiParcel;
import project.antsystems.GenericExplorationAnt;
import project.antsystems.IntentionAnt;

import java.util.NoSuchElementException;
import java.util.Set;

public class MultiAntAGV extends MultiAGV implements AntAGV {

    static final float RECONSIDERATION_TRESHOLD = 1.3f;
    static final int EXPLORATION_FREQ = 20000; //In ms
    static final int NUMBER_OF_EXPL_ANTS = 3;
    static final int WAIT_FOR_EXPL_ANTS = 1;

    private IntentionAnt currentIntention;
    private boolean isWaitingForExplorationAnts = false;
    private int numOfExplAntsReportedBack = 0;
    private long timeAtLastExploration = 0;

    private Point randomLocation = null;
    private Point lastLocation;

    public MultiAntAGV(Point startPosition, int capacity, SimulatorAPI sim) {
        super(startPosition, capacity, sim);
    }

    @Override
    public String toString() {
        if(currentIntention != null)
            return "MultiAGV " + id + ", h=" + currentIntention.getTotalHeuristicValue();
        else return "MultiAGV " + id;
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
    protected MultiAGV createVehicle(Point location, MultiParcel parcel) {
        MultiAntAggregateAGV agv =  new MultiAntAggregateAGV(location, (int) parcel.getNeededCapacity(), sim);
        agv.setDeliveryLocation(parcel.getDeliveryLocation());
        return agv;
    }

    @Override
    protected void afterUpdate(TimeLapse timeLapse) {}

    public void setRandomLocation(Point randomLocation){
        this.randomLocation = randomLocation;
    }

    private InfrastructureAgent getInfrastructureAgentAt(Point position){
        for(InfrastructureAgent infrastructureAgent: getRoadModel().getObjectsOfType(InfrastructureAgent.class)){
            if(infrastructureAgent.getPosition().equals(position)){
                return infrastructureAgent;
            }
        }
        return null;
    }

    @Override
    protected void update(TimeLapse timeLapse) {

        //If we don't know what to do (=no intention), send out exploration ants with a certain frequency, and don't do anything else
        if(currentIntention == null && randomLocation == null){
            if(timeAtLastExploration + EXPLORATION_FREQ <= timeLapse.getTime()){
                sendExplorationAnts();
                timeAtLastExploration = timeLapse.getTime();
            }
            RoadModel rm = getRoadModel();
            getInfrastructureAgentAt(getPosition()).updateReservationPheromone(TimeWindow.create(timeLapse.getTime(), timeLapse.getTime()+40000), 5, id);
            rm.moveTo(this,getPosition(), timeLapse);

            return;
        }
//        if(randomLocation != null) {
//            if(getRoadModel().getPosition(this).equals(randomLocation)) {
//                randomLocation = null;
//            }else{
//                getRoadModel().moveTo(this,  getRoadModel().getRandomPosition(sim.getRandomGenerator()), timeLapse);
//            }
//            return;
//        }

        RoadModel rm = getRoadModel();
        Set<MultiAGV> objects = rm.getObjectsOfType(MultiAGV.class);

        //If we got to the parcel or depot, pick up/deliver and set next goal
        if (atParcelOrDepot() && getPDPModel().getVehicleState(this) == PDPModel.VehicleState.IDLE) {
            Set<MultiAntParcel> parcels = rm.getObjectsAt(this, MultiAntParcel.class);    //Get Parcels at current locations
            Set<MultiDepot> depots = rm.getObjectsAt(this, MultiDepot.class);   //Get Depots at current location
            if(parcels.iterator().hasNext()){
                System.out.println("There was a parcel");
                MultiAntParcel parcel = parcels.iterator().next();
                parcel.increaseWaitingAGVs();
                pickUp(parcel, timeLapse);    //Pick random parcel on that location

            }
            if(depots.iterator().hasNext()){
                System.out.println("There was a depot, ");
                //deliverParcel(timeLapse);    //Drop off the parcel on that location
            }
            currentIntention.popPath();
            //If at next waypoint, resend exploration ants, and pop the location we got to
        }else if(atNextGoal()) {
            sendExplorationAnts();
            System.out.println("at next goal");
            popPath();
            //Point p = currentIntention.peekNextGoalLocation();
            //sim.register(new GoalVisualiser(p, sim, timeLapse.getStartTime(), 100000));
        }

        //Move to the direction of next goal
        if(!isWaiting && !isWaitingForExplorationAnts){
            Point p = currentIntention.peekNextGoalLocation();
            //If no next goal left, pop path for finding next parcel
            while(p == null){
                try {
                    popPath();
                    p = currentIntention.peekNextGoalLocation();

                }catch(NoSuchElementException e){
                    // If there is nothing left in this intention, then do nothing,
                    // set currentIntention to null to start sending exploration ants and return
                    currentIntention = null;
                    return;
                }
            }

          if(!moveTo(p,timeLapse)){
                if(lastLocation != null) moveTo(lastLocation, timeLapse);
          }
        }
    }

    private void popPath(){
        lastLocation = currentIntention.peekNextGoalLocation();
        currentIntention.popGoalLocation();
    }

    private boolean atNextGoal(){
        return currentIntention != null && getRoadModel().getPosition(this).equals(currentIntention.peekNextGoalLocation());
    }

    private boolean atParcelOrDepot() {
        //System.out.println("peekNextParcelLocation: " + currentIntention.peekNextParcelLocation());
        boolean b = currentIntention != null && getRoadModel().getPosition(this).equals(currentIntention.peekNextParcelLocation());
        if(b) System.out.println("At parcel!!");
        return b;
    }


    public void sendExplorationAnts(){
        //System.out.println("Sending exploration ants. Starting at position " + getRoadModel().getPosition(this));
        // isWaitingForExplorationAnts = true;
        for(int i = 0; i < NUMBER_OF_EXPL_ANTS; i++){
            GenericExplorationAnt ant = new GenericExplorationAnt(this, getRoadModel().getPosition(this), (GraphRoadModel) getRoadModel(), sim, MultiAntParcel.class);
            sim.register(ant);
        }

    }


    public void reportBack(GenericExplorationAnt ant){
        Point p = getRoadModel().getPosition(this);
        IntentionAnt tempIntentionAnt = new IntentionAnt(ant);
        tempIntentionAnt.trimPath(p);
        //numberOfAntCounter--;
        if((currentIntention == null
                || ant.getTotalHeuristicValue() > currentIntention.getTotalHeuristicValue() + Math.abs(currentIntention.getTotalHeuristicValue())*(RECONSIDERATION_TRESHOLD-1))
                && getRoadModel().getShortestPathTo(this,tempIntentionAnt.peekNextGoalLocation()).size() <= 2 && !tempIntentionAnt.getRedFlag()){

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


}
