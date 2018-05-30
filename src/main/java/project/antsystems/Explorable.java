package project.antsystems;

import com.github.rinde.rinsim.core.model.road.RoadUser;

public interface Explorable extends RoadUser {
    double getHeuristicAddition();
}
