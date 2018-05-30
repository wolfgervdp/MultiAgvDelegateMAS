//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package labo1;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.model.road.MovingRoadUser;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModelBuilders;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.ui.View;
import com.github.rinde.rinsim.ui.View.Builder;
import com.github.rinde.rinsim.ui.renderers.PlaneRoadModelRenderer;
import com.github.rinde.rinsim.ui.renderers.RoadUserRenderer;
import org.apache.commons.math3.random.RandomGenerator;

public final class Example {
    static final double VEHICLE_SPEED_KMH = 50.0D;
    static final Point MIN_POINT = new Point(0.0D, 0.0D);
    static final Point MAX_POINT = new Point(10.0D, 10.0D);
    static final long TICK_LENGTH = 1000L;
    static final long RANDOM_SEED = 123L;
    static final int NUM_VEHICLES = 200;
    static final int TEST_SPEEDUP = 16;
    static final long TEST_STOP_TIME = 600000L;

    private Example() {
    }

    public static void main(String[] args) {
        run(false);
    }

    public static void run(boolean testing) {
        Builder viewBuilder = View.builder().with(PlaneRoadModelRenderer.builder()).with(RoadUserRenderer.builder());
        if (testing) {
            viewBuilder = viewBuilder.withSpeedUp(TEST_SPEEDUP).withAutoClose().withAutoPlay().withSimulatorEndTime(TEST_STOP_TIME);
        }

        Simulator sim = Simulator.builder().setTickLength(TICK_LENGTH).setRandomSeed(RANDOM_SEED).addModel(
                RoadModelBuilders.plane()
                        .withMinPoint(MIN_POINT)
                        .withMaxPoint(MAX_POINT)
                        .withMaxSpeed(VEHICLE_SPEED_KMH))
                .addModel(viewBuilder).build();

        for(int i = 0; i < 5; ++i) {
            sim.register(new Example.Driver(sim.getRandomGenerator()));
        }

        sim.start();
    }

    static class Driver implements MovingRoadUser, TickListener {
        RoadModel roadModel;
        final RandomGenerator rnd;
        Point destination;

        Driver(RandomGenerator r) {
            this.rnd = r;
        }

        public void initRoadUser(RoadModel model) {
            this.roadModel = model;
            destination = this.roadModel.getRandomPosition(this.rnd);
        }

        public void tick(TimeLapse timeLapse) {
            if (!this.roadModel.containsObject(this)) {
                this.roadModel.addObjectAt(this, this.roadModel.getRandomPosition(this.rnd));
            }
            this.roadModel.getPosition(this);
            if(this.destination == null || this.destination.equals(this.roadModel.getPosition(this))){
                destination = this.roadModel.getRandomPosition(this.rnd); // When destination is reached or destination is null (for some reason), reinitialize somewhere else
            }
            this.roadModel.moveTo(this, new Point(0,0),timeLapse);
            //this.roadModel.moveTo(this, this.roadModel.getRandomPosition(this.rnd), timeLapse);
        }

        public void afterTick(TimeLapse timeLapse) {
        }

        public double getSpeed() {
            return 50.0D;
        }
    }
}
