package project.gradientfield;

import com.github.rinde.rinsim.core.SimulatorAPI;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import org.apache.commons.math3.random.RandomGenerator;
import project.MultiAGV;
import project.MultiAggregateAGV;
import project.MultiParcel;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Verify.verifyNotNull;

public class MultiGradientModelAggregateAGV extends MultiAggregateAGV  implements FieldContainer{

    private RandomGenerator rng;
    private float strenght = -600;
    private GradientModel gradientModel;
    private List<Point> storedPoint = null;
    private Point movePoint = null;
    private ArrayList<Point> unregisteredAGVStartLocation = new ArrayList<Point>();
    private ArrayList<Point> garageLocation = new ArrayList<Point>();


    public MultiGradientModelAggregateAGV(Point startPosition, int capacity, SimulatorAPI sim) {
        super(startPosition, capacity, sim);
    }

    public ArrayList<Point> getUnregisteredAGVStartLocation() {
        return unregisteredAGVStartLocation;
    }

    public void setUnregisteredAGVStartLocation(Point garageLocation) {
        this.unregisteredAGVStartLocation.add(garageLocation);
    }
    public void setUnregisteredAGVStartLocation(ArrayList<Point> garageLocation) {
        this.unregisteredAGVStartLocation=garageLocation;
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

        List<Point> p = verifyNotNull(gradientModel).getTargetsFor(this);

        if (p != null) {
            storedPoint = p;
        }
        //S

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
                        if(storedPoint.size()==count ) {
                            correctPostion = false;
                            break;
                        }
                        List<Point> ShortestPath;
                        try {
                            ShortestPath = rm.getShortestPathTo(storedPoint.get(count), this.getPosition());
                        } catch (Exception e){
                            System.out.println("vehicle" + this.getPosition());
                            System.out.println("point" + storedPoint);
                            System.out.println("count" + count);
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
        Point position = null;
        try {
            position = getRoadModel().getPosition(this);
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
        try {
            return verifyNotNull(gradientModel).getFields(this);

        }catch ( Exception e){
            System.out.println();

        }
        return verifyNotNull(gradientModel).getFields(this);

    }

    @Override
    public float getStrength(Point vehiclesPosition, Parcel parcel) {
        // TODO Auto-generated method stub
        return 0;
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
        gradientModel.unregister(this);
        getRoadModel().unregister(this);
        getPDPModel().unregister(this);
        System.out.println("unregister semi MultiAGVAGGREGATEGRADIENTFEIDL");

    }

    @Override
    protected MultiAGV createVehicle(Point location, MultiParcel parcel) {
        MultiAGVGradientField agv = new MultiAGVGradientField(location, 1, sim);
        agv.setGarageLocation(parcel.getUnregisteredAGVStartLocation().get(0));
        ((MultiParcelGradientField)parcel).removeUnregisteredAGVStartLocation(0);
        return agv;
    }


}
