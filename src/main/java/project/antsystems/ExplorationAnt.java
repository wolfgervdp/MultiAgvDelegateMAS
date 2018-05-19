package project.antsystems;

import com.github.rinde.rinsim.core.model.road.CollisionGraphRoadModelImpl;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import project.InfrastructureAgent;
import project.MultiAGV;
import project.MultiParcel;
import project.RealworldAgent;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.*;

/*
    Explores the map for a possible path going through Parcels. Adds new points at the back of the queue.
 */
public class ExplorationAnt extends AntAgent{

    static final int PATH_PARCEL_NUMBER = 5;
    double heuristicValue;

    public ExplorationAnt(RealworldAgent masterAgent, Point position) {
        super(masterAgent, position);
    }

    public ExplorationAnt(ExplorationAnt explorationAnt){
        super(explorationAnt.masterAgent, explorationAnt.position);
        this.path = new ArrayDeque<>(explorationAnt.path);
    }

    public double getHeuristicValue() {
        return heuristicValue;
    }



    @Override
    public void tick(TimeLapse timeLapse) {
        //If goal found, report back to masterAgent
        if(hasFinishedPath()){
            masterAgent.reportBack(this);
            return;
        }

        Collection<Point> points = ((CollisionGraphRoadModelImpl)this.roadModel.get())
                                            .getGraph()
                                            .getOutgoingConnections(((CollisionGraphRoadModelImpl)this.roadModel.get())
                                            .getPosition(this));

        List<Point> chosenPoints = chosePoints(points);

        //Create new ants for k-1 new paths (the kth path is for the ant itself, see next block)
        for (int i = 0; i < chosenPoints.size()-1; i++){
            ExplorationAnt ant = new ExplorationAnt(this);
            ant.pushPoint(chosenPoints.get(i));
            ant.moveStep(timeLapse);
            if(atParcelLocation()){
                pushQueue();
            }
        }
        //Last chosen point always goes to the current ant
        if(!chosenPoints.isEmpty()){
            pushPoint(chosenPoints.get(chosenPoints.size()));
            moveStep(timeLapse);
        }
    }

    private void pushPoint(Point p){
        heuristicValue += calcHeuristicValue(p);
        path.peekLast().push(p);
    }

    private void pushQueue(){
        ArrayDeque<Point> arrayDeque = new ArrayDeque<>();
        path.push(arrayDeque);
    }

    //Todo: Maybe get rid of the fixed path number size
    private boolean hasFinishedPath(){
        return path.size() >= PATH_PARCEL_NUMBER;
    }

    private boolean atParcelLocation(){
        return ! ((CollisionGraphRoadModelImpl)this.roadModel.get()).getObjectsAt(this, MultiParcel.class).isEmpty();
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
}
