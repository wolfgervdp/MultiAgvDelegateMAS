package project.antsystems;

import com.github.rinde.rinsim.geom.Graph;
import com.github.rinde.rinsim.geom.LengthData;
import com.github.rinde.rinsim.geom.ListenableGraph;
import project.HeuristicConData;

public abstract class AntAgent {

    protected Graph<HeuristicConData> graph;

    public abstract void step();


}
