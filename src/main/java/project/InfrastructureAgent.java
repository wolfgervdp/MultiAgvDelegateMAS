package project;

import com.github.rinde.rinsim.geom.ConnectionData;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;
import project.antsystems.ExplorationAnt;

public class InfrastructureAgent implements ConnectionData {

    static final double signCoefficient = 0.5f;
    static final double reservationCoefficient = 0.5f;

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
    public void updateReservationPheromone(double value) {
        reservationPheromone += value;
    }
    public double getHeuristicValue(){
        return signCoefficient*(signPheromone)+reservationCoefficient/reservationPheromone;
    }
}
