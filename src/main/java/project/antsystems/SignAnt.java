package project.antsystems;

import com.github.rinde.rinsim.core.SimulatorAPI;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.road.CollisionGraphRoadModelImpl;
import com.github.rinde.rinsim.core.model.road.GraphRoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Graph;
import com.github.rinde.rinsim.geom.LengthData;
import com.github.rinde.rinsim.geom.Point;
import project.GraphHelper;
import project.MultiAGV;

import java.util.Collection;
import java.util.Random;

public class SignAnt extends AntAgent {

    static final double SIGN_PHEROMONE_DROPOFF = 0.85;
    static final double SIGN_PHEROMONE_DOSE = 5.0;
    static final int NUM_PHEROMONES_TO_LEAVE = 15;

    int numberOfPheromonesLeft = 0;
    int numberOfWaitingAGVs = 0;
    long timeSinceParcelSpawn = 0;

    Graph<LengthData> inverseGraph;

    public SignAnt(Point startPosition, GraphRoadModel roadModel, SimulatorAPI sim) {
        super( startPosition, roadModel, sim);
        inverseGraph = (Graph<LengthData>) GraphHelper.getReverseGraph(roadModel.getGraph());
    }

    @Override
    public void tick(TimeLapse timeLapse) {
        Collection<Point> points = inverseGraph.getOutgoingConnections(currentPosition);//((CollisionGraphRoadModelImpl)this.roadModel).getGraph().getOutgoingConnections(currentPosition);
        currentPosition = pickNextPoint(points);


        getInfrastructureAgentAt(currentPosition).updateSignPheromone(SIGN_PHEROMONE_DOSE*Math.pow(SIGN_PHEROMONE_DROPOFF,numberOfPheromonesLeft));


        numberOfPheromonesLeft++;
        if(numberOfPheromonesLeft>= NUM_PHEROMONES_TO_LEAVE){
            destroySelf();
        }
    }

    //Current implementation is using a random node.This should probably be changed
    private Point pickNextPoint(Collection<Point> points){

        Random r = new Random();
        r.setSeed(System.currentTimeMillis() + numberOfPheromonesLeft);
        int randomNumber  = r.nextInt(points.size());

        Point[] pointArray = new Point[points.size()];
        points.toArray(pointArray);

        return pointArray[randomNumber];
    }
}
