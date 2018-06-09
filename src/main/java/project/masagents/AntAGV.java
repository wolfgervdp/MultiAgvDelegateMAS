package project.masagents;

import com.github.rinde.rinsim.core.model.road.MovingRoadUser;
import project.antsystems.GenericExplorationAnt;

public interface AntAGV extends MovingRoadUser {
    void reportBack(GenericExplorationAnt ant);
    int getId();
}
