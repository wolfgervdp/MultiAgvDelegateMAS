package project;

import com.github.rinde.rinsim.core.SimulatorAPI;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.ParcelDTO;
import com.github.rinde.rinsim.core.model.road.GraphRoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import project.antsystems.AntAgent;
import project.antsystems.SignAnt;

import java.util.ArrayList;

public class MultiParcel extends Parcel implements TickListener {
	private static final long SETSIGN_FREQ = 40000;
	private int weight = 1; //The amount of AGV which is needed.
	private ArrayList<MultiAGV> carryers = new ArrayList<>();
	private SimulatorAPI sim;
	private long timeAtLastExploration;

	@Override
	public String toString() {
		return "MP (w=" + weight + ")";
	}

	public MultiParcel(ParcelDTO parcelDto, SimulatorAPI sim) {
		super(parcelDto);
		this.sim = sim;
		System.out.println(parcelDto.getPickupDuration());
	}

	private boolean pickUp(){
		int totalStrengh = 0;
		for(MultiAGV multiAGV : carryers){
			totalStrengh += multiAGV.getStrenght();
		}
		System.out.println("totalStrength " + totalStrengh);
		return totalStrengh > weight;
	}

	public boolean tryPickUp(MultiAGV multiAGV) {
		carryers.add(multiAGV);
		if(pickUp()){
			for(MultiAGV carryer : carryers){
				carryer.startCarrying();
			}
			return true;
		}
		return false;

	}

	public double getUrgencyHeuristic(long currentTime){
		return (carryers.size()+1)*(1-getOrderAnnounceTime());
	}

	@Override
	public void initRoadPDP(RoadModel pRoadModel, PDPModel pPdpModel) {}

	@Override
	public void tick(TimeLapse timeLapse) {
		if(getPDPModel().getParcelState(this) == PDPModel.ParcelState.AVAILABLE || getPDPModel().getParcelState(this) == PDPModel.ParcelState.ANNOUNCED) {
			if (timeAtLastExploration + SETSIGN_FREQ <= timeLapse.getTime()) {
				sendAnts();
				timeAtLastExploration = timeLapse.getTime();
			}
		}
	}

	private void sendAnts(){
		sim.register(new SignAnt(getRoadModel().getPosition(this), (GraphRoadModel) getRoadModel(), sim));
	}

	@Override
	public void afterTick(TimeLapse timeLapse) { }
}

