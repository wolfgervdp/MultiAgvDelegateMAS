package project.antsystems;

import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.util.TimeWindow;
import project.InfrastructureAgent;
import project.helperclasses.DeepCopy;
import project.visualisers.IntentionAntVisualiser;

import java.util.ArrayDeque;

import java.util.Queue;

public class IntentionAnt extends AntAgent {

    private static final long INTENTION_FREQ = 1000;
    Queue<Point> currentQueue;
    private long timeAtLastExploration;

    //Create intention ant from exploration ant, with same path
    public IntentionAnt(ExplorationAnt ant) {
       // super(ant.masterAgent, ant.currentPosition, ant.roadModel, ant.sim);
        super(ant);
        this.path = new ArrayDeque<>(ant.path);
        currentQueue = path.peekLast();
    }

    @Override
    public void tick(TimeLapse timeLapse) {

        if(timeAtLastExploration + INTENTION_FREQ <= timeLapse.getTime()){
            reservePath(timeLapse.getTime());
            timeAtLastExploration = timeLapse.getTime();
        }
    }

    private void reservePath(long currentTime){
        //Todo: update intention values
        ArrayDeque<ArrayDeque<Point>> queue = ( ArrayDeque<ArrayDeque<Point>>) DeepCopy.copy(path);
        int increasingPheromone = 5;
        long timeOfArrivalBegin = 0;
        ArrayDeque<Point> allPoints = makeFlat(queue);
        while(!allPoints.isEmpty()){
            Point p = allPoints.removeFirst();
            IntentionAntVisualiser iav = new IntentionAntVisualiser(p,sim, currentTime, INTENTION_FREQ);
            sim.register(iav);
            InfrastructureAgent a = getInfrastructureAgentAt(p);
            long deltaT = Math.round(a.getLength()/masterAgent.getSpeed());
            TimeWindow tw = TimeWindow.create(timeOfArrivalBegin, timeOfArrivalBegin + deltaT);
            a.updateReservationPheromone(tw,increasingPheromone, masterAgent.getId());
            timeOfArrivalBegin += deltaT;
            //increasingPheromone *= 1.5;
        }
    }

    @Override
    public String toString() {
        return "IntentionAnt{" +
                "path=" + path +
                '}';
    }

    public Point peekNextGoalLocation(){
        //System.out.println("path from intention ant: " + this);
        ArrayDeque<Point> queue = path.peekFirst();
        if(queue != null)
            return queue.peekFirst();
        else return null;
    }

    public Point peekNextParcelLocation(){
        ArrayDeque<Point> queue = path.peekFirst();
        if(path.peekFirst() != null){
            return queue.peekLast();
        }
        return null;
    }

    public Queue<Point> getPath(){
        return path.peekFirst();
    }

    public void popGoalLocation() {
        path.getFirst().pop();
    }

    public void popPath(){
        //path.getFirst();
        path.pop();
    }
}
