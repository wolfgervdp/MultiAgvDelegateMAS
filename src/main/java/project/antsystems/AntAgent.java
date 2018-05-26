package project.antsystems;

import com.github.rinde.rinsim.core.SimulatorAPI;
import com.github.rinde.rinsim.core.model.road.CollisionGraphRoadModelImpl;
import com.github.rinde.rinsim.core.model.road.GraphRoadModel;
import com.github.rinde.rinsim.core.model.road.MovingRoadUser;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;

import com.github.rinde.rinsim.geom.ConnectionData;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.util.TimeWindow;
import com.google.common.base.Optional;
import project.InfrastructureAgent;
import project.MultiAGV;
import project.helperclasses.DeepCopy;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayDeque;
import java.util.ArrayList;

/*
    AntAgent is a superclass of all ant based agents
     The inner queue indicates the path between parcels. The outer queue connects these paths to construct a
    complete path through different parcels.
 */
public abstract class AntAgent implements TickListener, RoadUser {

    static final double URGENCY_COEFFICIENT = 0.5;
    static final double RESERVATION_COEFFICIENT = 0.5;

    protected MultiAGV masterAgent;
    protected SimulatorAPI sim;
    protected ArrayDeque<ArrayDeque<Point>> path;       //Inner queue is the queue until the first next goalnode, the outer queue is all the paths for the different parcels
    protected InfrastructureAgent lastInfrastructureAgent;
    protected GraphRoadModel roadModel;
    protected Point currentPosition;
    private double heuristicValue = 0;

    //Copy constructor for AntAgent
    public AntAgent(AntAgent agent) {
        this.masterAgent = agent.masterAgent;
        this.sim = agent.sim;
        this.currentPosition = agent.currentPosition;
        this.path = (ArrayDeque<ArrayDeque<Point>>) DeepCopy.copy(agent.path);
        this.roadModel = agent.roadModel;
        this.heuristicValue = agent.heuristicValue;
    }

    public double getTotalHeuristicValue(){
        return heuristicValue;
    }

    protected void addUrgencyHeuristic(double value){
        heuristicValue += value*URGENCY_COEFFICIENT;
    }
    protected void addReservationHeuristic(double value){
        heuristicValue += value*RESERVATION_COEFFICIENT;
    }

    //Normal constructor
    public AntAgent(MultiAGV masterAgent, Point position, GraphRoadModel roadModel, SimulatorAPI sim) {
        this.masterAgent = masterAgent;
        this.currentPosition = position;
        this.roadModel = roadModel;
        this.sim = sim;
        path = new ArrayDeque<ArrayDeque<Point>>();
        pushQueue();
        path.peekFirst().addLast(currentPosition);
    }
    

	@Override
	public void initRoadUser(RoadModel model) {
		// TODO Auto-generated method stub
		
	}

    //Calculation for heuristic value from current location to Point p
    protected double queryHeuristicValue(Point p){
        InfrastructureAgent data = (InfrastructureAgent) roadModel.getGraph().getConnection(currentPosition, p).data().get();
        return data.getLocalHeuristicValue();
    }

    protected double queryGlobalHeuristicValue(Point p, TimeWindow tw) {
        InfrastructureAgent data = (InfrastructureAgent) roadModel.getGraph().getConnection(currentPosition, p).data().get();
        return data.getHeuristicValue(tw);
    }

    protected void pushQueue(){
        ArrayDeque<Point> arrayDeque = new ArrayDeque<>();
        //path.push(arrayDeque);
        path.addLast(arrayDeque);
        //System.out.println("Pushed queue: " + path);
    }

    protected ArrayDeque<Point> makeFlat(ArrayDeque<ArrayDeque<Point>> queue){
        ArrayDeque<Point>  retQ = new ArrayDeque<>();
        for (ArrayDeque<Point> q: queue){
            for(Point p : q)
                retQ.addLast(p);
        }
        return retQ;
    }

    @Override
    public void afterTick(TimeLapse timeLapse) {}
}
