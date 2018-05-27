package project.antsystems;

import com.github.rinde.rinsim.core.SimulatorAPI;
import com.github.rinde.rinsim.core.model.road.CollisionGraphRoadModelImpl;
import com.github.rinde.rinsim.core.model.road.GraphRoadModel;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.util.TimeWindow;
import org.jetbrains.annotations.Nullable;
import project.InfrastructureAgent;
import project.MultiAGV;
import project.MultiParcel;

import java.util.*;

import static com.google.common.base.Preconditions.checkState;

/*
    Explores the map for a possible path going through Parcels. Adds new points at the back of the queue.
 */
public class ExplorationAnt extends AntAgent {

    static final int MAX_NR_ANT_SPLIT = 1;   //Number of ants this ant will create >extra<. High values for this parameter can result in really big performance drop
    static final int PATH_PARCEL_NUMBER = 1;    //Number of parcels to include in path
    static final int MAX_PATH_LENGTH = 35;

    static int counter = 0;
    int antId = 0;

    public ExplorationAnt(MultiAGV masterAgent, Point position, GraphRoadModel roadModel, SimulatorAPI sim) {
        super(masterAgent, position, roadModel, sim);
        antId = counter;
        counter++;
        initVisualisationQueue(position);
    }

    public ExplorationAnt(ExplorationAnt explorationAnt) {
        super(explorationAnt);
        antId = counter;
        counter++;
        initVisualisationQueue(explorationAnt.currentPosition);
    }



    @Override
    public String toString() {

        return "ExplorationAnt{" +
                "path=" + path +
                '}';
    }

    @Override
    public void tick(TimeLapse timeLapse) {

        //System.out.println("Antid: " + antId);
        //System.out.println("Ticking in ExplorationAnt. Current path: " + this);

        MultiParcel parcel = getParcelAtCurrentLocation();
        if (parcel != null) {
            //System.out.println("At parcel location!!!--------------");
            pushQueue();
            addUrgencyHeuristic(parcel.getUrgencyHeuristic(timeLapse.getTime()));
        }

        //If goal found, report back to masterAgent
        if (hasFinishedPath()) {
            //System.out.println("Ant finished path, reporting back");
            masterAgent.reportBack(this);
            destroySelf();
            return;
        }
        if (isMaxPathLength()) {
            //System.out.println("Unregistered ant with id=" + antId);
            destroySelf();
            return;
        }

        //System.out.println("Getting the outgoing connections starting from " + currentPosition);
        Collection<Point> points = ((CollisionGraphRoadModelImpl) this.roadModel)
                .getGraph()
                .getOutgoingConnections(currentPosition);

        points.remove(currentPosition);
        List<Point> chosenPoints = chosePoints(points);

        //System.out.println("Chosen points (" + chosenPoints.size() + "): ");
        /*for(Point p : points)
            System.out.println("\t" + p);*/
/*
        //Create new ants for k-1 new paths (the kth path is for the ant itself, see next block)
        for (int i = 0; i < chosenPoints.size()-1; i++){
            ExplorationAnt ant = new ExplorationAnt(this);
            System.out.println("Creating new exploration ant");
            ant.pushPoint(chosenPoints.get(i));
            ant.moveStep();
            sim.register(ant);
            System.out.println("Creating new ExplorationAnt at position " + ant.currentPosition);
        }
*/
        //Last chosen point always goes to the current ant
        if (!chosenPoints.isEmpty()) {
            //System.out.println("\t" + chosenPoints.get(chosenPoints.size()-1));
            pushPoint(chosenPoints.get(chosenPoints.size() - 1), timeLapse.getTime());
            moveStep();
        }
    }

    private boolean isMaxPathLength() {
        int totalPathSize = 0;
        for (Queue q : path) {
            totalPathSize += q.size();
        }
        //System.out.println(totalPathSize);
        return path == null || totalPathSize > MAX_PATH_LENGTH;
    }

    private void pushPoint(Point p, long currentTime) {

        //If there is no last element yet, then we're not yet using a connection, so no reservation needed. Otherwise, do add a reservation
        if (!path.isEmpty() && !path.getLast().isEmpty()) {
            //InfrastructureAgent agent = (InfrastructureAgent) roadModel.getGraph().getConnection(path.getLast().getLast(),p).data().get();
            InfrastructureAgent agent = getInfrastructureAgentAt(p);
            long timeOfArrival = currentTime + Math.round(roadModel.getDistanceOfPath(makeFlat(path)).getValue() - agent.getLength() / masterAgent.getSpeed());
            long timeOfCompletion = timeOfArrival + Math.round(agent.getLength() * 4 / masterAgent.getSpeed());

            addReservationHeuristic(queryGlobalHeuristicValue(p, TimeWindow.create(timeOfArrival, timeOfCompletion)));
        }
        //Add it to the path
        path.peekLast().addLast(p);
    }

    //Todo: Maybe get rid of the fixed path number size
    private boolean hasFinishedPath() {
        return path != null && path.size() > PATH_PARCEL_NUMBER;
    }

    @Nullable
    private MultiParcel getParcelAtCurrentLocation() {
        for (MultiParcel parcel : this.roadModel.getObjectsOfType(MultiParcel.class)) {
            if (parcel.getPickupLocation().equals(currentPosition)) {
                //System.out.println("Found parcel!")
                return parcel;
            }
        }
        return null;
    }

    private void moveStep() {
        //System.out.println("Stepping to " + path.peekLast().peekLast());
        visualiseAt(currentPosition);
        currentPosition = path.peekLast().peekLast();
    }

    //Uses a kind of proportionate selection method to randomly select points
    //Assumes all points in the collection are reachable from the current ant location
    private List<Point> chosePoints(Collection<Point> points) {
        Random r = new Random();
        r.setSeed(antId + System.currentTimeMillis());
        r.setSeed(r.nextInt()); //Yes this is strange but the pseudorandom generator is as pseudo as a generator can be,
        // since the values created were all the same, even with a seed which had a difference of a whole int

        double maxCumulObjV = 0;
        ArrayList<Double> objValues = new ArrayList<>();
        for (Point p : points) {
            //Calculate heuristic value

            maxCumulObjV += queryLocalHeuristicValue(p);
            //System.out.println(maxCumulObjV);
            objValues.add(maxCumulObjV);
        }

        ArrayList<Point> chosenPoints = new ArrayList<>();
        for (int j = 0; j < Math.min(MAX_NR_ANT_SPLIT + 1, points.size()); j++) {

            double randVal = r.nextDouble() * maxCumulObjV;
            //System.out.println(randVal);
            for (int i = 0; i < points.size(); i++) {
                //
                if (objValues.get(i) > randVal) {
                    chosenPoints.add(points.toArray(new Point[points.size()])[i]);
                    break;
                }
            }
        }
        //System.out.println("selected " + chosenPoints.size() + " points");
        return chosenPoints;
    }

}
