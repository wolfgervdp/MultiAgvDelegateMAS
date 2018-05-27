package project.gradientfield;

import static com.google.common.base.Verify.verifyNotNull;

import java.sql.SQLOutput;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;import org.apache.commons.math3.random.RandomGenerator;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.SimulatorAPI;
import com.github.rinde.rinsim.core.model.pdp.Depot;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.Vehicle;
import com.github.rinde.rinsim.core.model.pdp.VehicleDTO;
import com.github.rinde.rinsim.core.model.pdp.PDPModel.ParcelState;
import com.github.rinde.rinsim.core.model.pdp.PDPModel.VehicleState;
import com.github.rinde.rinsim.core.model.pdp.PDPObjectImpl;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModels;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.scenario.generator.Vehicles;
import com.google.common.base.Predicate;

import project.MultiAGV;

public class MultiAGVGradientField extends MultiAGV implements FieldEmitter {
	private SimulatorAPI sim;
	private RandomGenerator rng;
	private float strenght=-20;
	private double packagesToDeliver=0;
	public MultiAGVGradientField(Point startPosition, int capacity,SimulatorAPI sim) {
		super(startPosition, capacity,sim);
		this.sim=sim;
		this.rng=sim.getRandomGenerator();
	}
	MultiAGVGradientField(VehicleDTO pDto,SimulatorAPI sim) {
		super(pDto.getStartPosition(),pDto.getCapacity(),sim);
		
	}

	static final int DISTANCE_THRESHOLD_KM = 40;
	@Nullable
	private GradientModel gradientModel;

	protected void movingWithAvoidCollisionWithGradientField(Parcel delivery,TimeLapse time,RoadModel rm)
	{

		Point p = verifyNotNull(gradientModel).getTargetFor(this);
		if (p != null) {
		storedPoint=p;
		}
		
		Point getPosition= this.getPosition();
		Point point=new Point( Math.round(this.getPosition().x/4)*4,Math.round(this.getPosition().y/4)*4);
		List<Point> points=rm.getShortestPathTo(point, delivery.getDeliveryLocation());
		Point nextPoint=points.get(0);	
		Point thisPoint=points.get(0);	
		float fieldValue=0;
		if(points.size()<2){
			 nextPoint=points.get(0);	
		}else {
			 nextPoint=points.get(1);
				Point difference= Point.diff(getPosition,thisPoint);

				fieldValue=verifyNotNull(gradientModel).getField(Point.add(nextPoint,difference), this);
				//System.out.println("deliver FieldValue: "+fieldValue);
				float ownFieldValue=verifyNotNull(gradientModel).getField(thisPoint, this);
				//System.out.println("field value without ownfield "+ ((strenght/4)-ownFieldValue));
		}
		//System.out.println("now"+storedPoint+"car"+this.getPosition());
	//System.out.println("field value without ownfield"+ ((strenght/4)-ownFieldValue));
		if(fieldValue<(strenght/2))
		{
			if (storedPoint != null) {
				rm.moveTo(this, storedPoint, time);
				//System.out.println("Too negative gradient value (deliver) " +fieldValue +" position: "+ this.getPosition());

			}
		}else{
			rm.moveTo(this, delivery.getDeliveryLocation(), time);
//			System.out.println("Moving to deliver "+ fieldValue+" position: "+ this.getPosition());

		}
	}
	protected void ParcelmovingWithAvoidCollisionWithGradientField(Parcel closest,TimeLapse time,RoadModel rm)
	{
		Point p = verifyNotNull(gradientModel).getTargetFor(this);
		if (p != null) {
		storedPoint=p;
		}
		//System.out.println("now"+storedPoint+"car"+this.getPosition());
		Point getPosition= this.getPosition();
		Point point=new Point( Math.round(this.getPosition().x/4)*4,Math.round(this.getPosition().y/4)*4);
		List<Point> points=rm.getShortestPathTo(point, closest.getDeliveryLocation());
		Point nextPoint=points.get(0);	
		Point thisPoint=points.get(0);	
		float fieldValue=0;
		if(points.size()<2){
			 nextPoint=points.get(0);	
		}else {
			 nextPoint=points.get(1);
				Point difference= Point.diff(getPosition,thisPoint);

				fieldValue=verifyNotNull(gradientModel).getField(Point.add(nextPoint,difference), this);
				//System.out.println("FieldValue: "+fieldValue);
				float ownFieldValue=verifyNotNull(gradientModel).getField(thisPoint, this);
				//System.out.println("field value without ownfield "+ ((strenght/4)-ownFieldValue));
		}


			if(fieldValue<(strenght/2))
			{
				if (fieldValue<(strenght/4)) {



					if (storedPoint != null) {
						rm.moveTo(this, storedPoint, time);
					}
				}
				//System.out.println("Too negative gradient value (cloesests)");
			}else{
				rm.moveTo(this, rm.getPosition(closest), time);
//			System.out.println("Moving to closests");

			}



	}

	private Point storedPoint=null;

	@Override

	protected void tickImpl(TimeLapse time) {
		// Check if we can deliver nearby
		final Parcel delivery = getDelivery(time, 40);
		final RoadModel rm = getRoadModel();
		final PDPModel pm = getPDPModel();

		//		 If none of the above, let the gradient field guide us!

		//System.out.println(p);
		if (delivery != null) {
			if (delivery.getDeliveryLocation().equals(getPosition())
					&& pm.getVehicleState(this) == VehicleState.IDLE) {
				pm.deliver(this, delivery, time);
			}
			else
			{
				//rm.moveTo(this, delivery.getDeliveryLocation(), time);

				movingWithAvoidCollisionWithGradientField(delivery,time,rm);
			}
			return;
		}

		// Otherwise, Check if we can pickup nearby
		final Parcel closest = (Parcel) RoadModels.findClosestObject(
				rm.getPosition(this), rm, new Predicate<RoadUser>() {
					@Override
					public boolean apply(@Nullable RoadUser input) {
						return input instanceof Parcel
								&& pm.getParcelState((Parcel) input) == ParcelState.AVAILABLE;
					}
				});

		if (closest != null
				&& Point.distance(rm.getPosition(closest),
						getPosition()) < DISTANCE_THRESHOLD_KM) {
			if (rm.equalPosition(closest, this)
					&& pm.getTimeWindowPolicy().canPickup(closest.getPickupTimeWindow(),
							time.getTime(), closest.getPickupDuration())) {
				final double newSize = getPDPModel().getContentsSize(this)
						+ closest.getNeededCapacity();
				if (newSize <=getCapacity()) {
					pm.pickup(this, closest, time);
				}
			} else {
				//rm.moveTo(this, rm.getPosition(closest), time);

				ParcelmovingWithAvoidCollisionWithGradientField(closest, time, rm);
				
				//rm.moveTo(this, rm.getPosition(closest), time);
			}
			return;
		} 
		//		if (rm.getObjectsOfType(Parcel.class).isEmpty()) {
		//			rm.moveTo(this, getStartPosition(), time);
		//			return;
		//		}
		//

		if (rm.getObjectsOfType(Parcel.class).isEmpty() && verifyNotNull(pm).getContents(this).size()==0) {
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
		if (p1 != null) {
			rm.moveTo(this, p1, time);
		}
	}

	@Nullable
	Parcel getDelivery(TimeLapse time, int distance) {
		Parcel target = null;
		double closest = distance;
		final PDPModel pm = getPDPModel();
		for (final Parcel p : pm.getContents(this)) {

			final double dist = Point.distance(getRoadModel().getPosition(this),
					p.getDeliveryLocation());
			if (dist < closest
					&& pm.getTimeWindowPolicy().canDeliver(p.getDeliveryTimeWindow(),
							time.getTime(), p.getPickupDuration())) {
				closest = dist;
				target = p;
			}
		}

		return target;
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
		Point position=new Point(Math.round(getRoadModel().getPosition(this).x/4)*4, Math.round(getRoadModel().getPosition(this).y/4)*4);
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
