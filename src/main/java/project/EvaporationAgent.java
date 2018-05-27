package project;

import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Connection;
import com.github.rinde.rinsim.geom.Graph;

import java.util.List;

public class EvaporationAgent implements TickListener {

    private static final long EVAPORATION_FREQ = 100000;

    List<InfrastructureAgent> infrastructureAgentList;
    private long timeAtLastExploration;

    public EvaporationAgent(List<InfrastructureAgent> infrastructureAgentList) {
        this.infrastructureAgentList = infrastructureAgentList;
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
        for(InfrastructureAgent agent : infrastructureAgentList){
            agent.evaporate();
        }
    }

    @Override
    public void afterTick(TimeLapse timeLapse) {}

}
