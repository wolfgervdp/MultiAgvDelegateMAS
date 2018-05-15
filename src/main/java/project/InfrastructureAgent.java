package project;

import com.github.rinde.rinsim.geom.ConnectionData;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Optional;
import project.antsystems.ExplorationAnt;

public class InfrastructureAgent implements ConnectionData, RealworldAgent {

    float reservationPheromone;
    private double length;

    @Override
    public Optional<Double> getLength() {
        return Optional.of(length);
    }

    @Override
    public void reportBack(ExplorationAnt ant) {

    }
}
