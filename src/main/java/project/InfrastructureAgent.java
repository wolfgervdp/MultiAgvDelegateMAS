package project;

import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.util.TimeWindow;

import java.util.*;

public class InfrastructureAgent implements RoadUser, TickListener {

    static final double EVAPORATION_RATE = 0.707106;
    static final double signCoefficient = 0.5f;
    static final double reservationCoefficient = 0.5f;

    //ArrayList<TreeMap<Long,Double>> reservations = new ArrayList<>();

    HashMap<Integer, OrderBook> orders = new HashMap<>();

    double signPheromone = 0;

    Point position;

    private double length = 4000;
    private long currentTime = 0;

    public InfrastructureAgent(Point position) {
        this.position = position;
    }

    public double getLength() {
        return length;
    }

    public Point getPosition(){
        return position;
    }

    public void updateSignPheromone(double value) {
        signPheromone += value;
    }

    @Override
    public String toString() {
        return "" + /* (int) position.x + "," + (int) position.y + " " + */ (long) this.getReservationValue(TimeWindow.create(currentTime,currentTime+5), -1)/* + (int) signPheromone */;
    }

    public void updateReservationPheromone(TimeWindow tw, double value, int owner) {
        if(!orders.containsKey(owner)){
            orders.put(owner, new OrderBook());
        }
        orders.get(owner).updateReservationPheromone(tw,value);
    }

    public double getReservationValue(TimeWindow tw, int askerId){
        double totalReservationValue = 0;
        for(Map.Entry<Integer,OrderBook> orderBook : orders.entrySet()){
            if(orderBook.getKey() != askerId){
                totalReservationValue += orderBook.getValue().getReservationValue(tw);
            }
        }
        return totalReservationValue+1;
    }

    public double getLocalHeuristicValue(){
        return signCoefficient*(signPheromone);
    }

    public void evaporate() {
        signPheromone *= EVAPORATION_RATE;
        for(Map.Entry<Integer,OrderBook> orderBook : orders.entrySet()){
            orderBook.getValue().evaporate();
        }
    }

    @Override
    public void initRoadUser(RoadModel model) {
        model.addObjectAt(this, position);
    }

    @Override
    public void tick(TimeLapse timeLapse) {
        currentTime = timeLapse.getTime();
    }

    @Override
    public void afterTick(TimeLapse timeLapse) {}
}
