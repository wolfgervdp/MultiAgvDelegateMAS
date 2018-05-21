package project;

import com.github.rinde.rinsim.core.model.pdp.Depot;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.ParcelDTO;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.geom.Point;

import java.util.ArrayList;

public class MultiParcel extends Parcel {
	private int weight; //The amount of AGV which is needed.
	private ArrayList<MultiAGV> carryers = new ArrayList<>();

	public MultiParcel(ParcelDTO parcelDto) {
		super(parcelDto);
	}

	private boolean pickUp(){
		int totalStrengh = 0;
		for(MultiAGV multiAGV : carryers){
			totalStrengh += multiAGV.getStrenght();
		}
		return totalStrengh > weight;
	}

	public boolean tryPickUp(MultiAGV multiAGV) {
		carryers.add(multiAGV);
		if(pickUp()){
			for(MultiAGV carryer : carryers){
				multiAGV.startCarrying();
			}
		}
		return false;
	}

	@Override
	public void initRoadPDP(RoadModel pRoadModel, PDPModel pPdpModel) {}
}

// currently has no function
class DepotBase extends Depot {
	DepotBase(Point position, double capacity) {
		super(position);
		setCapacity(capacity);
	}

	@Override
	public void initRoadPDP(RoadModel pRoadModel, PDPModel pPdpModel) {}
}

