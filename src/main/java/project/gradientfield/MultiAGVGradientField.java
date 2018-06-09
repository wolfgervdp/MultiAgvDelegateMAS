package project.gradientfield;

import com.github.rinde.rinsim.core.SimulatorAPI;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.PDPModel.ParcelState;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.VehicleDTO;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModels;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Predicate;
import org.apache.commons.math3.random.RandomGenerator;
import project.MultiAGV;
import project.MultiAggregateAGV;
import project.MultiParcel;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Verify.verifyNotNull;


public class MultiAGVGradientField extends MultiAGV implements FieldContainer {


    private RandomGenerator rng;
    private float strenght = -40;
    static final int DISTANCE_THRESHOLD_KM = 40;
    @Nullable
    private GradientModel gradientModel;
    private List<Point> storedPoint = null;
    private Point movePoint = null;

    private ArrayList<Point> unregisteredAGVStartLocation = new ArrayList<Point>();
    private ArrayList<Point> garageLocation = new ArrayList<Point>();

    public MultiAGVGradientField(Point startPosition, int capacity, SimulatorAPI sim) {
        super(startPosition, capacity, sim);
        this.rng = sim.getRandomGenerator();
        //garageLocation.add(startPosition);
    }

    MultiAGVGradientField(VehicleDTO pDto, SimulatorAPI sim) {
        super(pDto.getStartPosition(), pDto.getCapacity(), sim);
        this.sim = sim;
        garageLocation.add(pDto.getStartPosition());

    }

    public ArrayList<Point> getUnregisteredAGVStartLocation() {
        return unregisteredAGVStartLocation;
    }

    public void setUnregisteredAGVStartLocation(Point garageLocation) {
        this.unregisteredAGVStartLocation.add(garageLocation);
    }

    protected void movingWithAvoidCollisionWithGradientField(Parcel delivery, TimeLapse time, RoadModel rm) {

        Point p = verifyNotNull(gradientModel).getTargetFor(this);
        if (p != null) {
            //storedPoint = p;
        }

        Point getPosition = this.getPosition();
        Point point = new Point(Math.round(this.getPosition().x / 4) * 4, Math.round(this.getPosition().y / 4) * 4);
        List<Point> points = rm.getShortestPathTo(point, delivery.getDeliveryLocation());
        Point nextPoint = points.get(0);
        Point thisPoint = points.get(0);
        float fieldValue = 0;
        if (points.size() < 2) {
            nextPoint = points.get(0);
        } else {
            nextPoint = points.get(1);
            Point difference = Point.diff(getPosition, thisPoint);

            fieldValue = verifyNotNull(gradientModel).getField(Point.add(nextPoint, difference), this);
            //System.out.println("deliver FieldValue: "+fieldValue);
            float ownFieldValue = verifyNotNull(gradientModel).getField(thisPoint, this);
            //System.out.println("field value without ownfield "+ ((strenght/4)-ownFieldValue));
        }
        //System.out.println("now"+storedPoint+"car"+this.getPosition());
        //System.out.println("field value without ownfield"+ ((strenght/4)-ownFieldValue));
        if (fieldValue < (strenght / 2)) {
            if (storedPoint != null) {
                rm.moveTo(this, storedPoint.get(1), time);
                //System.out.println("Too negative gradient value (deliver) " +fieldValue +" position: "+ this.getPosition());

            }
        } else {
            rm.moveTo(this, delivery.getDeliveryLocation(), time);
//			System.out.println("Moving to deliver "+ fieldValue+" position: "+ this.getPosition());

        }
    }

    protected void ParcelmovingWithAvoidCollisionWithGradientField(Parcel closest, TimeLapse time, RoadModel rm) {
        List<Point> p = verifyNotNull(gradientModel).getTargetsFor(this);

        if (p != null) {
            storedPoint = p;
        }
        //System.out.println("now"+storedPoint+"car"+this.getPosition());
        Point getPosition = this.getPosition();
        Point point = new Point(Math.round(this.getPosition().x / 4) * 4, Math.round(this.getPosition().y / 4) * 4);
        List<Point> points = rm.getShortestPathTo(point, closest.getDeliveryLocation());
        Point nextPoint = points.get(0);
        Point thisPoint = points.get(0);
        float fieldValue = 0;
        if (points.size() < 2) {
            nextPoint = points.get(0);
        } else {
            nextPoint = points.get(1);
            Point difference = Point.diff(getPosition, thisPoint);

            fieldValue = verifyNotNull(gradientModel).getField(Point.add(nextPoint, difference), this);
            //System.out.println("FieldValue: "+fieldValue);
            //System.out.println("field value without ownfield "+ ((strenght/4)-ownFieldValue));
        }

        if (fieldValue < (strenght / (2))) {
            // if (fieldValue < (strenght / 4)) {
            boolean correctPostion=true;
            int count=0;
            while (correctPostion)
            {
                if (storedPoint.get(0)!=this.getPosition()) {
                    if (storedPoint.size()==1){
                        movePoint=storedPoint.get(0);
                        break;

                    }else{
                        List<Point> ShortestPath;
                        if(storedPoint.size()==count ) {
                            correctPostion = false;
                            break;
                        }
                        try {
                            ShortestPath = rm.getShortestPathTo(storedPoint.get(count), this.getPosition());
                        } catch (Exception e){
                            System.out.println("vehicle" + this.getPosition());
                            System.out.println("point" + storedPoint);
                            ShortestPath = rm.getShortestPathTo(storedPoint.get(count), this.getPosition());
                        }
                        if (ShortestPath.size() < 5 ||storedPoint.size()==count ) {
                            correctPostion = false;
                            movePoint = ShortestPath.get(0);
                            break;
                        }
                        count++;

                    }


                }
                else{
                    movePoint=this.getPosition();
                    break;
                }
            }

            if (movePoint != null) {
                rm.moveTo(this, movePoint, time);
            }
//            }else {
//                rm.moveTo(this,thisPoint,time);
            /// }
            //System.out.println("Too negative gradient value (cloesests)");
        } else {
            //System.out.println("Moving to closests "+fieldValue+" own "+ownFieldValue +" name"+ this.toString());

            rm.moveTo(this, rm.getPosition(closest), time);
//			System.out.println("Moving to closests");

        }


    }

    public ArrayList<Point> getGarageLocation() {
        return garageLocation;
    }

    public void setGarageLocation(Point garageLocation) {
        this.garageLocation.add(0, garageLocation);
    }

    @Override
    protected void update(TimeLapse time) {
        final RoadModel rm = getRoadModel();
        final PDPModel pm = getPDPModel();
        // Check if we can deliver nearby

        // Otherwise, Check if we can pickup nearby
        final MultiParcel closest = (MultiParcel) RoadModels.findClosestObject(
                rm.getPosition(this), rm, new Predicate<RoadUser>() {
                    @Override
                    public boolean apply(@Nullable RoadUser input) {
                        return input instanceof MultiParcel
                                && pm.getParcelState((MultiParcel) input) == ParcelState.AVAILABLE;
                    }
                });

        if (closest != null
                && Point.distance(rm.getPosition(closest),
                getPosition()) < DISTANCE_THRESHOLD_KM) {
            if (closest != null) {
                if (rm.equalPosition(closest, this)
                        && pm.getTimeWindowPolicy().canPickup(closest.getPickupTimeWindow(),
                        time.getTime(), closest.getPickupDuration())) {
                    closest.setUnregisteredAGVStartLocation(this.garageLocation.get(0));
                    pickUp(closest, time);
                } else {
                    ParcelmovingWithAvoidCollisionWithGradientField(closest, time, rm);
                }
            }


            return;
        }

        if (rm.getObjectsOfType(Parcel.class).isEmpty() &&
                verifyNotNull(pm).getContents(this).
                        size() == 0) {
            rm.moveTo(this, this.getGarageLocation().get(0), time);
            return;
        }

        //		 If none of the above, let the gradient field guide us!
        @Nullable
        Point p1 = verifyNotNull(gradientModel).getTargetFor(this);
        if (p1 != null) {
            rm.moveTo(this, p1, time);
        }

    }

    @Override
    protected void unregister() {
        sim.unregister(this);
        gradientModel.unregister(this);
        getRoadModel().unregister(this);
        getPDPModel().unregister(this);
    }

    @Override
    protected void register() {
        sim.register(this);
    }

    @Override
    protected void semiUnregister() {
        getRoadModel().unregister(this);
        gradientModel.unregister(this);
        getPDPModel().unregister(this);
        System.out.println("unregister semi MultiAGVGRADIENTFEIDL");
    }

    @Override
    protected MultiAggregateAGV createVehicle(Point location, MultiParcel parcelToPickup) {
        MultiGradientModelAggregateAGV newBigVehicle = new MultiGradientModelAggregateAGV(location, (int) parcelToPickup.getNeededCapacity(), this.sim);
        newBigVehicle.setUnregisteredAGVStartLocation((parcelToPickup).getUnregisteredAGVStartLocation());
        return (new MultiGradientModelAggregateAGV(location, (int) parcelToPickup.getNeededCapacity(), this.sim));

    }

    @Override
    protected void afterUpdate(TimeLapse timeLapse) {

    }

    @Override
    public void setModel(GradientModel model) {
        gradientModel = model;
    }

    @Override
    public Point getPosition() {
        Point position = null;
        try {
            position = getRoadModel().getPosition(this);
        } catch (Exception e) {
            System.out.println("");
        }
        return position;
    }

    public Point getRoundedPosition() {
        Point position = null;
        try {
            position = new Point(Math.round(getRoadModel().getPosition(this).x / 4) * 4, Math.round(getRoadModel().getPosition(this).y / 4) * 4);
        } catch (Exception e) {
            System.out.println("");
        }
        return position;
    }

    @Override
    public float getStrength() {
        return strenght;
    }

    public Map<Point, Float> getFields() {
        return verifyNotNull(gradientModel).getFields(this);
    }

    @Override
    public float getStrength(Point vehiclesPosition, Parcel parcel) {
        return 0;
    }


}
