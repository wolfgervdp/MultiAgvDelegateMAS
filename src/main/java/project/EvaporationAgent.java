package project;

import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Connection;
import com.github.rinde.rinsim.geom.Graph;

public class EvaporationAgent implements TickListener {

    private static final long EVAPORATION_FREQ = 5500;

    Graph<InfrastructureAgent> graph;
    private long timeAtLastExploration;

    public EvaporationAgent(Graph<InfrastructureAgent> graph) {
        this.graph = graph;
    }


    @Override
    public void tick(TimeLapse timeLapse) {
        if(timeAtLastExploration + EVAPORATION_FREQ <= timeLapse.getTime()){
            doEvaporation();
            System.out.println("Done evaporation");
            timeAtLastExploration = timeLapse.getTime();
        }
    }

    private void doEvaporation(){
        for(Connection<InfrastructureAgent> con : graph.getConnections()){
            con.data().get().evaporate();
        }
    }

    @Override
    public void afterTick(TimeLapse timeLapse) {}

}
