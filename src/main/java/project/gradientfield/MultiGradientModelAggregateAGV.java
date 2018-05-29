package project.gradientfield;

import com.github.rinde.rinsim.core.SimulatorAPI;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import org.apache.commons.math3.random.RandomGenerator;
import project.MultiAggregateAGV;
import project.MultiParcel;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Verify.verifyNotNull;

public class MultiGradientModelAggregateAGV extends MultiAggregateAGV  implements FieldContainer{

    private RandomGenerator rng;
    private float strenght = -20;
    private GradientModel gradientModel;
    private Point storedPoint = null;


    public MultiGradientModelAggregateAGV(Point startPosition, int capacity, SimulatorAPI sim) {
        super(startPosition, capacity, sim);
    }

    @Override
    protected void updateImpl(TimeLapse time) {

        final MultiParcel delivery = getDelivery(time, 40);

        //System.out.println(p);
        if (delivery != null) {
            if (delivery.getDeliveryLocation().equals(getPosition())
                    && getPDPModel().getVehicleState(this) == PDPModel.VehicleState.IDLE) {
                deliverParcel(time, delivery);
                return;
            } else {
                //rm.moveTo(this, delivery.getDeliveryLocation(), time);
                movingWithAvoidCollisionWithGradientField(delivery, time, getRoadModel());
            }
            return;
        }
    }

    @Nullable
    MultiParcel getDelivery(TimeLapse time, int distance) {
        MultiParcel target = null;
        double closest = distance;
        final PDPModel pm = getPDPModel();
        for (final Parcel p : pm.getContents(this)) {

            final double dist = Point.distance(getRoadModel().getPosition(this),
                    p.getDeliveryLocation());
            if (dist < closest
                    && pm.getTimeWindowPolicy().canDeliver(p.getDeliveryTimeWindow(),
                    time.getTime(), p.getPickupDuration())) {
                closest = dist;
                target = (MultiParcel) p;
            }
        }
        return target;
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



    @Override
    public void setModel(GradientModel model) {
        gradientModel = model;
    }

    @Override
    public Point getPosition() {
        return getRoadModel().getPosition(this);
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


    @Override
    protected void unregister() {

    }

    @Override
    protected void semiUnregister() {

    }



    @Override
    protected MultiAggregateAGV createVehicle(Point location, double capacity) {
        return null;
    }
}
