package project.gradientfield;


import com.github.rinde.rinsim.core.SimulatorAPI;
import com.github.rinde.rinsim.core.model.pdp.ParcelDTO;
import com.github.rinde.rinsim.core.model.pdp.PDPModel.ParcelState;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.geom.Point;
import project.MultiParcel;

import java.util.ArrayList;

public class MultiParcelGradientField extends MultiParcel implements FieldEmitter {

    static final float AVAILABLE_STRENGTH = 20.0f;
    private final Point pos;
    private ArrayList<Point> unregisteredAGVStartLocation = new ArrayList<Point>();


    MultiParcelGradientField(ParcelDTO pDto) {
        super(pDto);
        pos = pDto.getPickupLocation();

    }

    public ArrayList<Point> getUnregisteredAGVStartLocation() {
        return unregisteredAGVStartLocation;
    }

    public void setUnregisteredAGVStartLocation(Point garageLocation) {
        this.unregisteredAGVStartLocation.add(garageLocation);
    }
    public void removeUnregisteredAGVStartLocation(int index) {
        this.unregisteredAGVStartLocation.remove(index);
    }

    @Override
    public void setModel(GradientModel model) { }

    @Override
    public Point getPosition() {
        return pos;
    }

    @Override
    public float getStrength() {
        if (!isInitialized()) {
            return 0f;
        }
        return getPDPModel().getParcelState(this) == ParcelState.AVAILABLE
                ? AVAILABLE_STRENGTH
                : 0.0f;
    }

    @Override
    public float getStrength(Point vehiclesPosition, Parcel parcel) {
        // TODO Auto-generated method stub
        return 0;
    }


}
