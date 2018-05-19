package project.antsystems;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.model.road.CollisionGraphRoadModelImpl;
import com.github.rinde.rinsim.core.model.road.MovingRoadUser;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;

import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;
import project.InfrastructureAgent;
import project.RealworldAgent;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayDeque;
import java.util.ArrayList;

/*
    AntAgent is a superclass of all ant based agents
     The inner queue indicates the path between parcels. The outer queue connects these paths to construct a
    complete path through different parcels.
 */
public abstract class AntAgent implements TickListener, MovingRoadUser {

    protected RealworldAgent masterAgent;
    protected Simulator sim;
    protected ArrayDeque<ArrayDeque<Point>> path;       //Inner queue is the queue until the first next goalnode, the outer queue is all the paths for the different parcels
    protected InfrastructureAgent lastInfrastructureAgent;
    protected Point position;
    protected Optional<CollisionGraphRoadModelImpl> roadModel;


    //Copy constructor for AntAgent
    public AntAgent(AntAgent agent) {
        this.masterAgent = agent.masterAgent;
        this.sim = agent.sim;
        this.position = agent.position;
        this.path = new ArrayDeque<>(path); //Todo: check deepcopy
        roadModel = Optional.absent();
    }

    //Normal constructor
    public AntAgent(RealworldAgent masterAgent, Point position) {
        this.masterAgent = masterAgent;
        this.position = position;
    }

    //Calculation for heuristic value
    public double calcHeuristicValue(Point p){
        throw new NotImplementedException();
    }

    //Method needed for adding itself to the visual representation (not sure whether we should really add it)
    @Override
    public void initRoadUser(RoadModel roadModel) {
        this.roadModel = Optional.of((CollisionGraphRoadModelImpl)roadModel);
        roadModel.addObjectAt(this, position);
    }

    @Override
    public double getSpeed() {
        return 100;  //AntAgents are way faster and run ahead of normal agents
    }

    @Override
    public void afterTick(TimeLapse timeLapse) {}
}
