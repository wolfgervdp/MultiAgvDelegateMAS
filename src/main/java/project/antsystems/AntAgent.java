package project.antsystems;

import com.github.rinde.rinsim.core.SimulatorAPI;
import com.github.rinde.rinsim.core.model.road.GraphRoadModel;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import org.jetbrains.annotations.Nullable;
import project.masagents.InfrastructureAgent;

public abstract class AntAgent implements TickListener{

    protected SimulatorAPI sim;
    protected GraphRoadModel roadModel;
    protected Point currentPosition;

    //Copy constructor for PathAntAgent
    public AntAgent(AntAgent agent) {
        this.sim = agent.sim;
        this.currentPosition = agent.currentPosition;
        this.roadModel = agent.roadModel;
    }

    //Normal constructor
    public AntAgent(Point position, GraphRoadModel roadModel, SimulatorAPI sim) {

        this.currentPosition = round(position);
        this.roadModel = roadModel;
        this.sim = sim;
    }

    protected final Point round(Point p){
        return new Point(Math.round(p.x / 4)*4, Math.round(p.y / 4)*4);
    }

    @Nullable
    protected InfrastructureAgent getInfrastructureAgentAt(Point position){
        for(InfrastructureAgent infrastructureAgent: this.roadModel.getObjectsOfType(InfrastructureAgent.class)){
            if(infrastructureAgent.getPosition().equals(position)){
                return infrastructureAgent;
            }
        }
        return null;
    }

    protected void destroySelf(){
        sim.unregister(this);
    }

    @Override
    public void afterTick(TimeLapse timeLapse) {}


}
