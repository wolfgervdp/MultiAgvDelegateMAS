package project;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.ParcelDTO;
import com.github.rinde.rinsim.core.model.road.RoadModel;

import java.util.ArrayList;

public class MultiParcel extends Parcel {
    private int weight = 2; //The amount of AGV which is needed.
    private ArrayList<MultiAGV> carryers = new ArrayList<>();
    public int availableAGVs = 0;

    public void waitingAGVs() {
        this.availableAGVs++;

    }

    public void addRoadUser() {

            if(availableAGVs==getNeededCapacity())

    {
        getRoadModel().register(new MultiAGV(this.getPickupLocation(), availableAGVs));
    }

}

    public void clearAvailableAGVs() {
        this.availableAGVs = 0;
    }


    public double requirredCapacity() {
        return this.getNeededCapacity() - this.availableAGVs;
    }


    @Override
    public String toString() {
        return "MP (w=" + weight + ")";
    }

    public MultiParcel(ParcelDTO parcelDto) {
        super(parcelDto);
        System.out.println(parcelDto.getPickupDuration());

    }

    public MultiParcel(ParcelDTO parcelDto, int weight) {
        super(parcelDto);
        this.weight = weight;
        System.out.println(parcelDto.getPickupDuration());
    }

    private boolean pickUp() {
        int totalStrengh = 0;
        for (MultiAGV multiAGV : carryers) {
            totalStrengh += multiAGV.getStrenght();
        }
        System.out.println("totalStrength " + totalStrengh);
        return totalStrengh > weight;
    }

    public boolean tryPickUp(MultiAGV multiAGV) {
        carryers.add(multiAGV);
        if (pickUp()) {
            for (MultiAGV carryer : carryers) {
                carryer.startCarrying();
            }
            return true;
        }
        weight = weight - 1;
        availableAGVs++;
        getRoadModel().removeObject(multiAGV);

        return false;

    }

    public double getUrgencyHeuristic(long currentTime) {
        return (carryers.size() + 1) * (1 - getOrderAnnounceTime());
    }

    @Override
    public void initRoadPDP(RoadModel pRoadModel, PDPModel pPdpModel) {
    }
}

