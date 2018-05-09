//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package project;

import com.github.rinde.rinsim.core.model.road.CollisionGraphRoadModelImpl;
import com.github.rinde.rinsim.core.model.road.MovingRoadUser;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.LinkedList;
import java.util.Queue;

class AGVAgent implements TickListener, MovingRoadUser {
    private final RandomGenerator rng;
    private Optional<CollisionGraphRoadModelImpl> roadModel;
    private Optional<Point> destination;
    private Queue<Point> path;

    AGVAgent(RandomGenerator r) {
        this.rng = r;
        this.roadModel = Optional.absent();
        this.destination = Optional.absent();
        this.path = new LinkedList();
    }

    public void initRoadUser(RoadModel model) {
        this.roadModel = Optional.of((CollisionGraphRoadModelImpl)model);

        Point p;
        do {
            p = model.getRandomPosition(this.rng);
        } while(((CollisionGraphRoadModelImpl)this.roadModel.get()).isOccupied(p));

        ((CollisionGraphRoadModelImpl)this.roadModel.get()).addObjectAt(this, p);
    }

    public double getSpeed() {
        return 1.0D;
    }

    void nextDestination() {
        this.destination = Optional.of(((CollisionGraphRoadModelImpl)this.roadModel.get()).getRandomPosition(this.rng));
        this.path = new LinkedList(((CollisionGraphRoadModelImpl)this.roadModel.get()).getShortestPathTo(this, (Point)this.destination.get()));
    }

    public void tick(TimeLapse timeLapse) {
        if (!this.destination.isPresent()) {
            this.nextDestination();
        }

        ((CollisionGraphRoadModelImpl)this.roadModel.get()).followPath(this, this.path, timeLapse);
        if (((CollisionGraphRoadModelImpl)this.roadModel.get()).getPosition(this).equals(this.destination.get())) {
            this.nextDestination();
        }

    }

    public void afterTick(TimeLapse timeLapse) {
    }
}
