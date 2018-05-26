package project.antsystems;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.SimulatorAPI;
import com.github.rinde.rinsim.core.model.road.CollisionGraphRoadModelImpl;
import com.github.rinde.rinsim.core.model.road.GraphRoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Predicate;
import com.google.common.base.Verify;
import project.MultiParcel;
import project.RealworldAgent;
import project.helperclasses.DeepCopy;

import javax.annotation.Nullable;
import java.nio.file.attribute.PosixFileAttributes;
import java.util.*;

/*
    Explores the map for a possible path going through Parcels. Adds new points at the back of the queue.
 */
public class ExplorationAnt extends AntAgent{

    static final int MAX_NR_ANT_SPLIT = 1;   //High values for this parameter can result in really big performance drop
    static final int PATH_PARCEL_NUMBER = 1;    //Number of parcels to include in path

    double heuristicValue = 0;

    public ExplorationAnt(RealworldAgent masterAgent, Point position, GraphRoadModel roadModel, SimulatorAPI sim) {
        super(masterAgent, position, roadModel, sim);
    }

    public ExplorationAnt(ExplorationAnt explorationAnt){
        super(explorationAnt);
        this.heuristicValue = explorationAnt.heuristicValue;
    }

    public double getHeuristicValue() {
        return heuristicValue;
    }


    @Override
    public String toString() {
        return "ExplorationAnt{" +
                "path=" + path +
                '}';
    }

    @Override
    public void tick(TimeLapse timeLapse) {
        System.out.println("Ticking in ExplorationAnt. Current path: " + this);

        //If goal found, report back to masterAgent
        if(hasFinishedPath()){
            System.out.println("Ant finished path, reporting back");
            masterAgent.reportBack(this);
            sim.unregister(this);
            return;
        }
        if(atParcelLocation()){
            System.out.println("At parcel location!!!--------------");
            pushQueue();
        }
        //System.out.println("Getting the outgoing connections starting from " + currentPosition);
        Collection<Point> points = ((CollisionGraphRoadModelImpl)this.roadModel)
                                            .getGraph()
                                            .getOutgoingConnections(currentPosition);
        points.remove(currentPosition);
        List<Point> chosenPoints = chosePoints(points);

        //System.out.println("Chosen points (" + points.size() + "): ");
        /*for(Point p : points)
            System.out.println("\t" + p);*/

        //Create new ants for k-1 new paths (the kth path is for the ant itself, see next block)
        for (int i = 0; i < chosenPoints.size()-1; i++){
            ExplorationAnt ant = new ExplorationAnt(this);
            ant.pushPoint(chosenPoints.get(i));
            ant.moveStep();
            sim.register(ant);
            System.out.println("Creating new ExplorationAnt at position " + ant.currentPosition);

        }
        //Last chosen point always goes to the current ant
        if(!chosenPoints.isEmpty()){
            System.out.println("\t" + chosenPoints.get(chosenPoints.size()-1));
            pushPoint(chosenPoints.get(chosenPoints.size()-1));
            moveStep();
        }
    }

    private void pushPoint(Point p){
        System.out.println("Pushing point " + p);
        heuristicValue += calcHeuristicValue(p);
        path.peekLast().addLast(p);
    }

    //Todo: Maybe get rid of the fixed path number size
    private boolean hasFinishedPath(){
        return path != null? path.size() > PATH_PARCEL_NUMBER : false;
    }

    private boolean atParcelLocation(){
        //return ! ((CollisionGraphRoadModelImpl)this.roadModel.get()).getObjectsAt(this, MultiParcel.class).isEmpty();
        for(MultiParcel parcel: this.roadModel.getObjectsOfType(MultiParcel.class)){
            //System.out.println("Parcel location: " + parcel.getPickupLocation());
            //System.out.println("current location: " + currentPosition);
            Point p = (Point) DeepCopy.copy(currentPosition);

            if(parcel.getPickupLocation().equals(currentPosition)){
                System.out.println("Found parcel!");
                return true;
            }
        }
        return false;
//
//        for(Map.Entry<RoadUser, Point> entry: this.roadModel.getObjectsAndPositions().entrySet()){
//            if(entry.getValue().equals(currentPosition) && entry.getKey() instanceof MultiParcel){
//                return true;
//            }
//        }return false;
    }

    private void moveStep(){
        //System.out.println("Stepping to " + path.peekLast().peekLast());
        currentPosition = path.peekLast().peekLast();
        System.out.println();
    }

    //Uses a kind of proportionate selection method to randomly select points
    //Assumes all points in the collection are reachable from the current ant location
    private List<Point> chosePoints(Collection<Point> points){

        //System.out.println("points size " + points.size());
        Random r = new Random();
        double maxCumulObjV = 0;
        ArrayList<Double> objValues = new ArrayList<>();
        for(Point p : points){
            //Calculate heuristic value
            maxCumulObjV += calcHeuristicValue(p);
            //System.out.println(maxCumulObjV);
            objValues.add(maxCumulObjV);
        }

        ArrayList<Point> chosenPoints = new ArrayList<>();
        for(int j = 0; j < Math.min(MAX_NR_ANT_SPLIT,points.size()); j++){

            double randVal = r.nextDouble()*maxCumulObjV;
            //System.out.println(randVal);
            for(int i = 0; i < points.size(); i++){
                //System.out.println(objValues.get(i));
                if(objValues.get(i) > randVal){
                    chosenPoints.add(points.toArray(new Point[points.size()])[i]);
                }
            }
        }
        //System.out.println("selected " + chosenPoints.size() + " points");
        return chosenPoints;
    }

}
