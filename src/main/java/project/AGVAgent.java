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

abstract class AGVAgent implements TickListener, MovingRoadUser, Reportable {
    protected final RandomGenerator rng;
    protected Optional<CollisionGraphRoadModelImpl> roadModel;
    protected Optional<Point> destination;
    protected Queue<Point> path;

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

    public abstract void tick(TimeLapse timeLapse) ;

    public void afterTick(TimeLapse timeLapse) {}
}
