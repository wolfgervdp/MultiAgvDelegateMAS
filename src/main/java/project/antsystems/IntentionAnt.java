package project.antsystems;

import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;

import java.util.ArrayDeque;

import java.util.Queue;

public class IntentionAnt extends AntAgent {

    //Create intention ant from exploration ant, with same path
    public IntentionAnt(ExplorationAnt ant) {
        super(ant.masterAgent, ant.position);
        this.path = new ArrayDeque<>(ant.path);
    }

    @Override
    public void tick(TimeLapse timeLapse) {

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
