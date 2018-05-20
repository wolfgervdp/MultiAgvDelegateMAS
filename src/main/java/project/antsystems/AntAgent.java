package project.antsystems;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.model.road.CollisionGraphRoadModelImpl;
import com.github.rinde.rinsim.core.model.road.GraphRoadModel;
import com.github.rinde.rinsim.core.model.road.MovingRoadUser;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;

import com.github.rinde.rinsim.geom.ConnectionData;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;
import project.HeuristicConData;
import project.InfrastructureAgent;
import project.RealworldAgent;
import project.helperclasses.DeepCopy;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayDeque;
import java.util.ArrayList;

/*
    AntAgent is a superclass of all ant based agents
     The inner queue indicates the path between parcels. The outer queue connects these paths to construct a
    complete path through different parcels.
 */
public abstract class AntAgent implements TickListener {

    protected RealworldAgent masterAgent;
    protected Simulator sim;
    protected ArrayDeque<ArrayDeque<Point>> path;       //Inner queue is the queue until the first next goalnode, the outer queue is all the paths for the different parcels
    protected InfrastructureAgent lastInfrastructureAgent;
    protected GraphRoadModel roadModel;
    protected Point currentPosition;

    //Copy constructor for AntAgent
    public AntAgent(AntAgent agent) {
        this.masterAgent = agent.masterAgent;
        this.sim = agent.sim;
        this.currentPosition = agent.currentPosition;
        //this.path = new ArrayDeque<>(agent.path); //Todo: check deepcopy
        this.path = (ArrayDeque<ArrayDeque<Point>>) DeepCopy.copy(agent.path);
        this.roadModel = agent.roadModel;

    }

    //Normal constructor
    public AntAgent(RealworldAgent masterAgent, Point position, GraphRoadModel roadModel, Simulator sim) {
        this.masterAgent = masterAgent;
        this.currentPosition = position;
        this.roadModel = roadModel;
        this.sim = sim;
        path = new ArrayDeque<ArrayDeque<Point>>();
        pushQueue();
        path.peekFirst().addLast(currentPosition);
    }

    //Calculation for heuristic value from current location to Point p
    protected double calcHeuristicValue(Point p){
        InfrastructureAgent data = (InfrastructureAgent) roadModel.getGraph().getConnection(currentPosition, p).data().get();
        return data.getHeuristicValue();
    }

    protected void pushQueue(){
        ArrayDeque<Point> arrayDeque = new ArrayDeque<>();
        path.push(arrayDeque);
    }

    @Override
    public void afterTick(TimeLapse timeLapse) {}
}
