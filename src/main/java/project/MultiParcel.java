package project;

import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.ParcelDTO;

import java.util.ArrayList;

public class MultiParcel extends Parcel {
    private int weight;
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
        return pickUp();
    }
}
