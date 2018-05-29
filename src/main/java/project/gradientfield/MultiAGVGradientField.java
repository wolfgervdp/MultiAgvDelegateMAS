package project.gradientfield;

import static com.google.common.base.Verify.verifyNotNull;

import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.math3.random.RandomGenerator;

import com.github.rinde.rinsim.core.SimulatorAPI;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.VehicleDTO;
import com.github.rinde.rinsim.core.model.pdp.PDPModel.ParcelState;
import com.github.rinde.rinsim.core.model.pdp.PDPModel.VehicleState;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModels;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.base.Predicate;

import project.MultiAGV;
import project.MultiAggregateAGV;
import project.MultiParcel;


public class MultiAGVGradientField extends MultiAGV implements FieldContainer {


    private RandomGenerator rng;
    private float strenght = -20;


    static final int DISTANCE_THRESHOLD_KM = 40;
    @Nullable
    private GradientModel gradientModel;
    private Point storedPoint = null;

    public MultiAGVGradientField(Point startPosition, int capacity, SimulatorAPI sim) {
        super(startPosition, capacity, sim);
        this.rng = sim.getRandomGenerator();
    }

    MultiAGVGradientField(VehicleDTO pDto, SimulatorAPI sim) {
        super(pDto.getStartPosition(), pDto.getCapacity(), sim);
        this.sim = sim;
    }

    protected void movingWithAvoidCollisionWithGradientField(Parcel delivery, TimeLapse time, RoadModel rm) {

        Point p = verifyNotNull(gradientModel).getTargetFor(this);
        if (p != null) {
            storedPoint = p;
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
                rm.moveTo(this, storedPoint, time);
                //System.out.println("Too negative gradient value (deliver) " +fieldValue +" position: "+ this.getPosition());

            }
        } else {
            rm.moveTo(this, delivery.getDeliveryLocation(), time);
//			System.out.println("Moving to deliver "+ fieldValue+" position: "+ this.getPosition());

        }
    }

    protected void ParcelmovingWithAvoidCollisionWithGradientField(Parcel closest, TimeLapse time, RoadModel rm) {
        Point p = verifyNotNull(gradientModel).getTargetFor(this);
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
            float ownFieldValue = verifyNotNull(gradientModel).getField(thisPoint, this);
            //System.out.println("field value without ownfield "+ ((strenght/4)-ownFieldValue));
        }


        if (fieldValue < (strenght / 2)) {
            if (fieldValue < (strenght / 4)) {


                if (storedPoint != null) {
                    rm.moveTo(this, storedPoint, time);
                }
            }
            //System.out.println("Too negative gradient value (cloesests)");
        } else {
            rm.moveTo(this, rm.getPosition(closest), time);
//			System.out.println("Moving to closests");

        }


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
                    pickUp(closest, time);
                } else {
                    //rm.moveTo(this, rm.getPosition(closest), time);

                    ParcelmovingWithAvoidCollisionWithGradientField(closest, time, rm);

                    //rm.moveTo(this, rm.getPosition(closest), time);
                }
            }


            return;
        }
        //		if (rm.getObjectsOfType(Parcel.class).isEmpty()) {
        //			rm.moveTo(this, getStartPosition(), time);
        //			return;
        //		}
        //

        if (rm.getObjectsOfType(Parcel.class).isEmpty() &&
                verifyNotNull(pm).getContents(this).
                        size() == 0){
            rm.moveTo(this, getStartPosition(), time);
            //System.out.println(getStrenght());
            return;
        }

        //		if ((rm.getObjectsOfType(Parcel.class).isEmpty())) {
//			//System.out.println(delivery);
//			rm.moveTo(this, getStartPosition(), time);
//			this.strenght=0;
//			//rm.moveTo(this, p, time);
//			return;
//		}
        //		 If none of the above, let the gradient field guide us!
        @Nullable
        Point p1 = verifyNotNull(gradientModel).getTargetFor(this);
        if (p1 != null)

        {
            rm.moveTo(this, p1, time);
        }

    }

    @Override
    protected void unregister() {
        getRoadModel().unregister(this);
        sim.unregister(this);
        gradientModel.unregister(this);
        getPDPModel().unregister(this);
    }

    @Override
    protected void semiUnregister() {
        getRoadModel().unregister(this);
        gradientModel.unregister(this);
        getPDPModel().unregister(this);
    }

    @Override
    protected MultiAggregateAGV createVehicle(Point location, double capacity) {
        return (new MultiGradientModelAggregateAGV(location, (int) capacity, this.sim));
    }

    @Override
    public void setModel(GradientModel model) {
        gradientModel = model;
    }

    @Override
    public Point getPosition() {
        return getRoadModel().getPosition(this);
    }

    public Point getRoundedPosition() {
        Point position = new Point(Math.round(getRoadModel().getPosition(this).x / 4) * 4, Math.round(getRoadModel().getPosition(this).y / 4) * 4);
        return position;
    }

    @Override
    public float getStrength() {
        return strenght;
    }

    Map<Point, Float> getFields() {
        return verifyNotNull(gradientModel).getFields(this);
    }

    @Override
    public float getStrength(Point vehiclesPosition, Parcel parcel) {
        // TODO Auto-generated method stub
        return 0;
    }


}
