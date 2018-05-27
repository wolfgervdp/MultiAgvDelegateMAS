package project.antsystems;

import com.github.rinde.rinsim.core.SimulatorAPI;

import com.github.rinde.rinsim.core.model.road.GraphRoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;

import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.util.TimeWindow;
import org.jetbrains.annotations.Nullable;
import project.InfrastructureAgent;
import project.MultiAGV;
import project.helperclasses.DeepCopy;
import project.visualisers.ExplorationAntVisualiser;

import java.util.ArrayDeque;
import java.util.Queue;

import static com.google.common.base.Preconditions.checkState;

/*
    AntAgent is a superclass of all ant based agents
     The inner queue indicates the path between parcels. The outer queue connects these paths to construct a
    complete path through different parcels.
 */
public abstract class AntAgent  implements TickListener {

    //by mate
    private Point startPosition;
    private RoadModel roadModel_test;
    private boolean isRegistered;
    //

    static final double URGENCY_COEFFICIENT = 0.5;
    static final double RESERVATION_COEFFICIENT = 0.5;

    protected MultiAGV masterAgent;
    protected SimulatorAPI sim;
    protected ArrayDeque<ArrayDeque<Point>> path;       //Inner queue is the queue until the first next goalnode, the outer queue is all the paths for the different parcels
    protected InfrastructureAgent lastInfrastructureAgent;
    protected GraphRoadModel roadModel;
    protected Point currentPosition;
    private double heuristicValue = 0;


    Queue<ExplorationAntVisualiser> visualiserQueue = new ArrayDeque<>();
    int visualiserHistorySize = 50;

    //Copy constructor for AntAgent
    public AntAgent(AntAgent agent) {
        this.masterAgent = agent.masterAgent;
        this.sim = agent.sim;
        this.currentPosition = agent.currentPosition;
        this.path = (ArrayDeque<ArrayDeque<Point>>) DeepCopy.copy(agent.path);
        this.roadModel = agent.roadModel;
        this.heuristicValue = agent.heuristicValue;

    }


    protected void initVisualisationQueue(Point p) {
        for (int i = 0; i < visualiserHistorySize; i++) {
            ExplorationAntVisualiser v = new ExplorationAntVisualiser(p, roadModel.getPosition(masterAgent));
            sim.register(v);
            visualiserQueue.add(v);
        }
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
        startPosition = this.currentPosition;

        this.sim = sim;
        path = new ArrayDeque<>();
        pushQueue();
        path.peekFirst().addLast(currentPosition);
    }

    @Nullable
    protected InfrastructureAgent getInfrastructureAgentAt(Point position){
        for(InfrastructureAgent infrastructureAgent: this.roadModel.getObjectsOfType(InfrastructureAgent.class)){
            if(infrastructureAgent.getPosition().equals(position)){
                return infrastructureAgent;
            }
        }
        return null;
    }

    //Calculation for heuristic value from current location to Point p
    protected double queryLocalHeuristicValue(Point p){

        //InfrastructureAgent data = (InfrastructureAgent) roadModel.getGraph().getConnection(currentPosition, p).data().get();
        return getInfrastructureAgentAt(p).getLocalHeuristicValue();
    }

    protected double queryGlobalHeuristicValue(Point p, TimeWindow tw) {
        //InfrastructureAgent data = (InfrastructureAgent) roadModel.getGraph().getConnection(currentPosition, p).data().get();
        return 1000/getInfrastructureAgentAt(p).getReservationValue(tw, masterAgent.getId());
    }

    protected void destroySelf(){
        for(ExplorationAntVisualiser v : visualiserQueue){
            sim.unregister(v);
        }
        sim.unregister(this);
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

    protected void visualiseAt(Point actualPoint, Point previousPoint){
        ExplorationAntVisualiser toAdd = new ExplorationAntVisualiser(actualPoint,previousPoint);
        ExplorationAntVisualiser toRemove = visualiserQueue.remove();
        sim.unregister(toRemove);
        sim.register(toAdd);
        visualiserQueue.add(toAdd);
    }

    @Override
    public void afterTick(TimeLapse timeLapse) {}
}
