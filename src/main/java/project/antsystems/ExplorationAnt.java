package project.antsystems;

import com.github.rinde.rinsim.core.model.road.CollisionGraphRoadModelImpl;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import project.InfrastructureAgent;
import project.MultiAGV;
import project.RealworldAgent;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayDeque;

public class ExplorationAnt extends AntAgent{

    public ExplorationAnt(RealworldAgent masterAgent, Point position) {
        super(masterAgent, position);
    }

    public ExplorationAnt(ExplorationAnt explorationAnt){
        super(explorationAnt.masterAgent, explorationAnt.position);
        this.path = new ArrayDeque<>(explorationAnt.path);
    }


    //Todo: Implement when the agent has reached a goal
    protected boolean hasFoundGoal(){
        throw new NotImplementedException();
    }


    @Override
    public void tick(TimeLapse timeLapse) {


        //((CollisionGraphRoadModelImpl)this.roadModel.get()).followPath(this, this., timeLapse);

        //If goal found, report back to masterAgent
        if(hasFoundGoal()){
            masterAgent.reportBack(this);
            return;
        }

        //Todo: this is just a dummy implementation, should be changed do something more meaningful

        //Create new agents for a few promising paths
        ExplorationAnt ant = new ExplorationAnt(this);

        //Register these agents
        sim.register(ant);

        //Move self somewhere
    }
}
