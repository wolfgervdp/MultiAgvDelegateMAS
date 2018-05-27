package project.antsystems;

import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.PDPObject;
import com.github.rinde.rinsim.core.model.road.CollisionGraphRoadModelImpl;
import com.github.rinde.rinsim.core.model.road.GraphRoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.util.TimeWindow;
import project.InfrastructureAgent;
import project.helperclasses.DeepCopy;

import java.util.ArrayDeque;

import java.util.Queue;

public class IntentionAnt extends AntAgent {

    private static final long INTENTION_FREQ = 8000;
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
        Point point0;
        Point point1;

        int increasingPheromone = 5;

        ArrayDeque<Point> allPoints = makeFlat(queue);
        while(!allPoints.isEmpty()){

            long timeOfArrivalEnd = currentTime + Math.round(roadModel.getDistanceOfPath(allPoints).intValue(roadModel.getDistanceUnit())/masterAgent.getSpeed());
            point1 = allPoints.removeLast();
            if(allPoints.isEmpty()){
                break;
            }
            long timeOfArrivalBegin = currentTime + Math.round(roadModel.getDistanceOfPath(allPoints).intValue(roadModel.getDistanceUnit())/masterAgent.getSpeed());
            point0 = allPoints.getLast();
            InfrastructureAgent a = (InfrastructureAgent) (roadModel.getGraph().getConnection(point0,point1).data().get());
            TimeWindow tw = TimeWindow.create(timeOfArrivalBegin, timeOfArrivalEnd);

            a.updateReservationPheromone(tw,increasingPheromone);
            increasingPheromone *= 1.5;
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

    @Override
    public  final void initRoadUser(RoadModel model) {
        roadModel = (GraphRoadModel)model ;

        model.addObjectAt(this, super.currentPosition);

    }
}
