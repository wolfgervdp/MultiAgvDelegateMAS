package project.antsystems;

import com.github.rinde.rinsim.core.SimulatorAPI;

import com.github.rinde.rinsim.core.model.road.GraphRoadModel;
import com.github.rinde.rinsim.core.model.time.TimeLapse;

import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.util.TimeWindow;
import project.helperclasses.DeepCopy;
import project.masagents.AntAGV;
import project.visualisers.ExplorationAntVisualiser;

import java.util.ArrayDeque;
import java.util.Queue;

import static com.google.common.base.Preconditions.checkState;

/*
    PathAntAgent is a superclass of all ant based agents
     The inner queue indicates the path between parcels. The outer queue connects these paths to construct a
    complete path through different parcels.
 */
public abstract class PathAntAgent  extends AntAgent {

    static final double URGENCY_COEFFICIENT = 0.5;
    static final double RESERVATION_COEFFICIENT = 0.5;

    protected AntAGV masterAgent;
    protected ArrayDeque<ArrayDeque<Point>> path;       //Inner queue is the queue until the first next goalnode, the outer queue is all the paths for the different parcels
    private double heuristicValue = 0;


    Queue<ExplorationAntVisualiser> visualiserQueue = new ArrayDeque<>();
    int visualiserHistorySize = 50;

    //Copy constructor for PathAntAgent
    public PathAntAgent(PathAntAgent agent) {
        super(agent);

        this.masterAgent = agent.masterAgent;
        this.path = (ArrayDeque<ArrayDeque<Point>>) DeepCopy.copy(agent.path);
        this.heuristicValue = agent.heuristicValue;
    }

    //Normal constructor
    public PathAntAgent(AntAGV masterAgent, Point position, GraphRoadModel roadModel, SimulatorAPI sim) {
        super(position,roadModel,sim);

        this.masterAgent = masterAgent;
        path = new ArrayDeque<>();
        pushQueue();
        path.peekFirst().addLast(currentPosition);
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
        heuristicValue -= value*RESERVATION_COEFFICIENT;
    }

    //Calculation for heuristic value from current location to Point p
    protected double queryLocalHeuristicValue(Point p){

        //InfrastructureAgent data = (InfrastructureAgent) roadModel.getGraph().getConnection(currentPosition, p).data().get();
        return getInfrastructureAgentAt(p).getLocalHeuristicValue();
    }

    protected double queryGlobalHeuristicValue(Point p, TimeWindow tw) {
        //InfrastructureAgent data = (InfrastructureAgent) roadModel.getGraph().getConnection(currentPosition, p).data().get();
        return 10000/getInfrastructureAgentAt(p).getReservationValue(tw, masterAgent.getId());
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
    protected void destroySelf(){
        for(ExplorationAntVisualiser v : visualiserQueue){
            sim.unregister(v);
        }
        super.destroySelf();
    }

    @Override
    public void afterTick(TimeLapse timeLapse) {}
}
