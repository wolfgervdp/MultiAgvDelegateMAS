package project.antsystems;

import com.github.rinde.rinsim.core.model.road.CollisionGraphRoadModelImpl;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;

import java.util.ArrayDeque;

import java.util.Queue;

public class IntentionAnt extends AntAgent {

    private double heuristicValue;
    Queue<Point> currentQueue;

    //Create intention ant from exploration ant, with same path
    public IntentionAnt(ExplorationAnt ant) {
        super(ant.masterAgent, ant.currentPosition, ant.roadModel, ant.sim);
        this.path = new ArrayDeque<>(ant.path);
        currentQueue = path.peekLast();
    }

    @Override
    public void tick(TimeLapse timeLapse) {
        //Todo: update inention values
    }


    public double getHeuristicValue() {
        return heuristicValue;
    }

    public Point peekNextGoalLocation(){
        return path.peekFirst().peekFirst();
    }

    public Point peekNextParcelLocation(){
        return path.peekFirst().peekLast();
    }

    public Queue<Point> getPath(){
        return path.peekFirst();
    }

    public void popGoalLocation() {
        path.getFirst().getFirst();
    }
    public void popPath(){
        path.getFirst();
    }
}
