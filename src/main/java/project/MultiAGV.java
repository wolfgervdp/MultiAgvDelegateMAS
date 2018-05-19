package project;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.pdp.VehicleDTO;
import com.github.rinde.rinsim.core.model.road.CollisionGraphRoadModelImpl;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.google.common.base.Optional;
import org.apache.commons.math3.random.RandomGenerator;
import project.antsystems.AntAgent;
import project.antsystems.ExplorationAnt;
import project.antsystems.IntentionAnt;

import java.util.ArrayList;
import java.util.List;

public class MultiAGV extends Vehicle {

    static final float RECONSIDERATION_TRESHOLD = 1.3f;

    private int strenght;
    private MultiParcel carriedParcel;
    private List<AntAgent> antAgentList = new ArrayList<>();
    private IntentionAnt currentIntention;
    private Simulator sim;
    private boolean isWaiting = false;

    public MultiAGV(VehicleDTO dto, Simulator sim) {
        super(dto);
        this.sim = sim;
    }

    public int getStrenght() {
        return strenght;
    }
//
//    public void tick(TimeLapse timeLapse) {
//
//        if ((currentIntention != null) && ((CollisionGraphRoadModelImpl)this.roadModel.get()).getPosition(this).equals(currentIntention.peekNextGoalNode())) {
//            //If we got to the destination, take action to start moving to next target
//            this.setNextDestination();
//        }
//    }

    @Override
    protected void tickImpl(TimeLapse timeLapse) {
      if ((currentIntention != null) && (getRoadModel()).getPosition(this).equals(currentIntention.peekNextGoalNode())) {
            //If we got to the destination, take action to start moving to next target
            this.setNextDestination();
        }
    }

    public void reportBack(ExplorationAnt ant){
        if(ant.getHeuristicValue() >= currentIntention.getHeuristicValue()*RECONSIDERATION_TRESHOLD){
            currentIntention = new IntentionAnt(ant);
        }
    }

    public void pickUp(MultiParcel parcel){
        if(carriedParcel != null){
            carriedParcel = parcel;
        }else{
            //What to do when we try to pick up parcel when we already have one?
            //(should probably never happen, so maybe exception?)
        }

       if(parcel.tryPickUp(this)){
           //Succeeded! Start carrying parcel to its destination
       }else{
            //Wait, since not enough agv's are helping
       }
        //Todo
    }

    public void sendExplorationAnts(){

    }

    public void setNextDestination(){
        //path = currentIntention.getPath();
    }

}
