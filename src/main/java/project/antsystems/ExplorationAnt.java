package project.antsystems;

import com.github.rinde.rinsim.core.model.road.CollisionGraphRoadModelImpl;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import project.InfrastructureAgent;
import project.MultiAGV;
import project.RealworldAgent;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;

public class ExplorationAnt extends AntAgent{


    public ExplorationAnt(RealworldAgent masterAgent, Point position) {
        super(masterAgent, position);
    }

    public ExplorationAnt(ExplorationAnt explorationAnt){
        super(explorationAnt.masterAgent, explorationAnt.position);
        this.path = new ArrayDeque<>(explorationAnt.path);
    }


    //Todo: Implement when the agent has reached a goal
    protected boolean hasFoundGoal(){
        throw new NotImplementedException();
    }

    public void pushPoint(Point p){

        //Todo
        throw new NotImplementedException();
    }

    @Override
    public void tick(TimeLapse timeLapse) {

        //If goal found, report back to masterAgent
        if(hasFoundGoal()){
            masterAgent.reportBack(this);
            return;
        }

        Collection<Point> points = ((CollisionGraphRoadModelImpl)this.roadModel.get()).getGraph().getOutgoingConnections(((CollisionGraphRoadModelImpl)this.roadModel.get()).getPosition(this));
        List<Point> chosenPoints = chosePoints(points);

        for (int i = 0; i < chosenPoints.size()-1; i++){
            ExplorationAnt ant = new ExplorationAnt(this);
            ant.pushPoint(chosenPoints.get(i));
            ant.moveStep(timeLapse);
        }

        if(!chosenPoints.isEmpty()){
            pushPoint(chosenPoints.get(chosenPoints.size()));
            moveStep(timeLapse);
        }
    }

    private void moveStep(TimeLapse tl){
        ((CollisionGraphRoadModelImpl)this.roadModel.get()).moveTo(this,path.peekLast().peekLast(), tl);
    }

    //Uses a kind of proportionate selection method to randomly select points
    private List<Point> chosePoints(Collection<Point> points){

        Random r = new Random();
        double maxCumulObjV = 0;
        ArrayList<Double> objValues = new ArrayList<>();
        for(Point p : points){
            //Calculate heuristic value
            maxCumulObjV += calcHeuristicValue(p);
            objValues.add(maxCumulObjV);
        }

        int select_size = 5;
        ArrayList<Point> chosenPoints = new ArrayList<>();
        for(int j = 0; j < select_size; j++){
            double randVal = r.nextDouble()*maxCumulObjV;
            for(int i = 1; i < points.size(); i++){
                if(objValues.get(i) > randVal){
                    chosenPoints.add(points.toArray(new Point[points.size()])[i-1]);
                }
            }
        }
        return chosenPoints;
    }

    private double calcHeuristicValue(Point p) {
        //Todo
        throw new NotImplementedException();
    }
}
