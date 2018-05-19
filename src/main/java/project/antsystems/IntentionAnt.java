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
        super(ant.masterAgent, ant.position);
        this.path = new ArrayDeque<>(ant.path);
        currentQueue = path.peekLast();
    }

    @Override
    public void tick(TimeLapse timeLapse) {
        Point p = currentQueue.element();
        moveStep(timeLapse);
    }

    private void moveStep(TimeLapse tl){
        ((CollisionGraphRoadModelImpl)this.roadModel.get()).moveTo(this,path.peekLast().peekLast(), tl);
    }

    public double getHeuristicValue() {
        return heuristicValue;
    }

    public Point peekNextGoalNode(){
        return path.peekFirst().peekFirst();
    }

    public Point peekNextParcelNode(){
        return path.peekFirst().peekLast();
    }

    public Queue<Point> getPath(){
        return path.peekFirst();
    }
}
