//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package labo2;

import com.github.rinde.rinsim.core.model.road.DynamicGraphRoadModelImpl;
import com.github.rinde.rinsim.core.model.road.RoadModelBuilders.AbstractDynamicGraphRMB;

import com.github.rinde.rinsim.geom.ListenableGraph;
import com.github.rinde.rinsim.geom.Point;

import org.apache.commons.math3.random.AbstractRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.Random;


public class NewRoadModel extends DynamicGraphRoadModelImpl {

    protected NewRoadModel(ListenableGraph<?> g, AbstractDynamicGraphRMB<?, ?> b) {
        super(g, b);
    }

    public void addRandomConnection(){
        Point[] test = new Point[0];
        RandomGenerator g = new AbstractRandomGenerator() {
            @Override
            public void setSeed(long l) {

            }
            @Override
            public double nextDouble() {
                Random r = new Random();
                return r.nextDouble();
            }
        };

        super.graph.addConnection(super.graph.getRandomNode(g), super.graph.getRandomNode(g));
    }

    public void removeRandomConnection(){
        Point[] test = new Point[0];
        RandomGenerator g = new AbstractRandomGenerator() {
            @Override
            public void setSeed(long l) {

            }
            @Override
            public double nextDouble() {
                Random r = new Random();
                return r.nextDouble();
            }
        };
        super.graph.removeConnection(super.graph.getRandomNode(g), super.graph.getRandomNode(g));
    }

}
