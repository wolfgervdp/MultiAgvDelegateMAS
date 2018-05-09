package project;

import org.apache.commons.math3.random.RandomGenerator;

import java.util.ArrayList;

public class MultiAGV extends AGVAgent {
    private int strenght;
    private MultiParcel carriedParcel;


    public MultiAGV(RandomGenerator r) {
        super(r);
    }

    public int getStrenght() {
        return strenght;
    }

    public void pickUp(MultiParcel parcel){
        if(carriedParcel != null){
            carriedParcel = parcel;
        }else {
            //What to do when we try to pick up parcel when we already have one?
            //(should probably never happen, so maybe exception?)
        }

       if(parcel.tryPickUp(this)){
           //Succeeded! Start carrying parcel to its destination
       }else{
            //Wait, since not enough agv's are helping
       }
        //Todo
    }

}
