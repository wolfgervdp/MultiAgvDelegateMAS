package project;

import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.geom.ConnectionData;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.util.TimeWindow;
import com.google.common.base.Optional;
import project.antsystems.ExplorationAnt;

import java.util.*;

public class InfrastructureAgent implements ConnectionData, RoadUser {

    static final double EVAPORATION_RATE = 0.707106;
    static final double signCoefficient = 0.5f;
    static final double reservationCoefficient = 0.5f;

    TreeMap<Long,Double> reservations = new TreeMap<>();

    double reservationPheromone = 1;
    double signPheromone = 0;

    private double length = 2.0;

    @Override
    public Optional<Double> getLength() {
        return Optional.of(length);
    }
    public void updateSignPheromone(double value) {
        signPheromone += value;
    }

    private double getOrCreate(long timestamp){
        if(!reservations.containsKey(timestamp)){
            reservations.put(timestamp,0d);
        }
        return reservations.get(timestamp);
    }


    public void updateReservationPheromone(TimeWindow tw, double value) {

        Iterator<Long> reservationTimes = reservations.navigableKeySet().iterator();
        long currReservationTime = 0;
        List<Long> reservationTimesToUpdate = new ArrayList<>();

        //Add to tw.begin() timestamp, or create it if it doesn't already exist



        //Find first intermediate timestamp
        while(reservationTimes.hasNext() && currReservationTime <= tw.begin()){
            currReservationTime = reservationTimes.next();
        }

        //Loop through all intermediate timestamps and add pheromone to every timestamp
        while(reservationTimes.hasNext() && currReservationTime < tw.end()){
            reservations.put(currReservationTime, reservations.get(currReservationTime) + value);
            currReservationTime = reservationTimes.next();
        }


        reservations.put(tw.begin(), getOrCreate(tw.begin()) + value);  //Beginning value
        getOrCreate(tw.end());                  //End value
    }

    public double getHeuristicValue(TimeWindow tw){

        Iterator<Long> reservationTimes = reservations.navigableKeySet().iterator();
        long currReservationTime = 0;
        long previousReservationTime = 0;
        double totalReservationValue = 0;

        //Loop until we find the reservation times which are intersecting with the timewindow
        while(reservationTimes.hasNext() && currReservationTime < tw.begin()-5000){
            previousReservationTime = currReservationTime;
            currReservationTime = reservationTimes.next();
        }

        //Don't forget to add the reservation of the first one

        totalReservationValue += reservations.get(previousReservationTime) != null ? reservations.get(previousReservationTime) : 0;

        //Loop through all intersecting timestamps and add to total reservationValue
        while(reservationTimes.hasNext() && currReservationTime < tw.end()+5000) {
            totalReservationValue += reservations.get(currReservationTime);
            currReservationTime = reservationTimes.next();
        }
        return totalReservationValue;
    }

    public double getLocalHeuristicValue(){
        return signCoefficient*(signPheromone) + reservationCoefficient/reservationPheromone;
    }

    public void evaporate() {
        Iterator<Map.Entry<Long,Double>> reservationTimes = reservations.entrySet().iterator();

        while(reservationTimes.hasNext()){
            Map.Entry<Long,Double> entry = reservationTimes.next();
            reservations.put(entry.getKey(),entry.getValue()*EVAPORATION_RATE);
        }
    }

    @Override
    public void initRoadUser(RoadModel model) {

    }
}
