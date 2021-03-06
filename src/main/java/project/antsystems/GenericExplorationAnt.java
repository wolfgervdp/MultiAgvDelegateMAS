package project.antsystems;

import com.github.rinde.rinsim.core.SimulatorAPI;
import com.github.rinde.rinsim.core.model.road.CollisionGraphRoadModelImpl;
import com.github.rinde.rinsim.core.model.road.GraphRoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.util.TimeWindow;
import com.google.common.base.Predicate;
import org.jetbrains.annotations.Nullable;
import project.MultiParcel;
import project.masagents.AntAGV;
import project.masagents.InfrastructureAgent;

import javax.measure.unit.SI;
import java.util.*;

public class GenericExplorationAnt extends PathAntAgent{

    static final int MAX_NR_ANT_SPLIT = 1;   //Number of ants this ant will create >extra<. High values for this parameter can result in really big performance drop
    static final int PATH_PARCEL_NUMBER = 1;    //Number of parcels to include in path
    static final int MAX_PATH_LENGTH = 40;
    static final int MAX_NUMBER_TICKS = 50;
    static final int ANT_SLOWDOWN = 1;

    int tickCounter = 0;

    Class<? extends Explorable> classToLookFor;

    HashSet<Point> visitedParcels = new HashSet<>();

    static int counter = 0;
    int antId = 0;

    MultiParcel lastParcel;
    Predicate<Explorable> explorablePredicate;

    public GenericExplorationAnt(AntAGV masterAgent, Point position, GraphRoadModel roadModel, SimulatorAPI sim, Class<? extends Explorable> classToLookFor) {
        super(masterAgent, position, roadModel, sim);
        antId = counter;
        counter++;
        System.out.println(position);
        this.classToLookFor = classToLookFor;

        initVisualisationQueue(position);
    }


    public Point getCurrentPosition() {
        return currentPosition;
    }

    public GenericExplorationAnt(ExplorationAnt explorationAnt) {
        super(explorationAnt);
        antId = counter;
        counter++;
        initVisualisationQueue(explorationAnt.currentPosition);
    }

    public void setCondition( Predicate<Explorable> predicate){
        explorablePredicate = predicate;
    }

    @Override
    public String toString() {

        return "ExplorationAnt{ l=" + path.size() +
                ", path=" + path +
                '}';
    }


    @Override
    public void tick(TimeLapse timeLapse) {

        //System.out.println("Antid: " + antId);
        //System.out.println("Ticking in ExplorationAnt. Current path: " + this);

        Explorable object = getExplorableAtLocation();
        //explorablePredicate.apply(object)
        if (object != null
                && (explorablePredicate == null || explorablePredicate.apply(object))
            /*&& currentPosition.equals(lastParcel.getDeliveryLocation())*/) {
            pushQueue();
            addUrgencyHeuristic(object.getHeuristicAddition());
        }

        //If goal found, report back to masterAgent
        if (hasFinishedPath()) {
            //System.out.println("Ant finished path, reporting back");
            masterAgent.reportBack(this);
            destroySelf();
            return;
        }
        if (isMaxPathLength() || tickCounter >= MAX_NUMBER_TICKS) {
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

        //Last chosen point always goes to the current ant
        if (!chosenPoints.isEmpty() && !visitedParcels.contains(chosenPoints.get(chosenPoints.size() - 1))) {
            //System.out.println("\t" + chosenPoints.get(chosenPoints.size()-1));
            pushPoint(chosenPoints.get(chosenPoints.size() - 1), timeLapse.getTime());
            moveStep();
        }
        tickCounter++;

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
            int distance = roadModel.getDistanceOfPath(makeFlat(path)).intValue(SI.MILLIMETER);
            long timeOfArrival = currentTime + Math.round(distance / masterAgent.getSpeed());
            long timeOfCompletion = timeOfArrival + Math.round(agent.getLength() / masterAgent.getSpeed());
            double reservationValue = getInfrastructureAgentAt(p).getReservationValue(TimeWindow.create(timeOfArrival, timeOfCompletion), masterAgent.getId());
            if(reservationValue > 10){
                redFlag = true;
            }
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
    private Explorable getExplorableAtLocation() {
        for (Map.Entry<RoadUser, Point> entry : this.roadModel.getObjectsAndPositions().entrySet()) {
            if (entry.getKey().getClass().isAssignableFrom(classToLookFor) && entry.getValue().equals(currentPosition)) {
                return (Explorable) entry.getKey();
            }
        }
        return null;
    }

    private void moveStep() {
        //System.out.println("Stepping to " + path.peekLast().peekLast());
        visualiseAt(path.peekLast().peekLast(),currentPosition);
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
