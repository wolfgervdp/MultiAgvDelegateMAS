package project.antsystems;

import com.github.rinde.rinsim.core.SimulatorAPI;
import com.github.rinde.rinsim.core.model.road.CollisionGraphRoadModelImpl;
import com.github.rinde.rinsim.core.model.road.GraphRoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import project.MultiAGV;

import java.util.Collection;
import java.util.Random;

public class SignAnt extends AntAgent {

    int numberOfWaitingAGVs = 0;
    long timeSinceParcelSpawn = 0;

    public SignAnt(MultiAGV masterAgent, Point position, GraphRoadModel roadModel, SimulatorAPI sim) {
        super(masterAgent, position, roadModel, sim);
    }

    @Override
    public void tick(TimeLapse timeLapse) {
        Collection<Point> points = ((CollisionGraphRoadModelImpl)this.roadModel).getGraph().getOutgoingConnections(currentPosition);
        currentPosition = pickNextPoint(points);
    }

    //Current implementation is using a random node.This should probably be changed
    private Point pickNextPoint(Collection<Point> points){
        Random r = new Random();
        int randomNumber  = r.nextInt(points.size());
        for(int i = 0 ; i < randomNumber - 1; i++)
            points.iterator().next();
        return points.iterator().next();
    }
}
