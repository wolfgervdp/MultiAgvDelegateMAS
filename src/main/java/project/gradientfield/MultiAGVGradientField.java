package project.gradientfield;

import static com.google.common.base.Verify.verifyNotNull;

import java.util.Map;

import javax.annotation.Nullable;

import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.Vehicle;
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

public class MultiAGVGradientField extends MultiAGV implements FieldEmitter {

	public MultiAGVGradientField(Point startPosition, int capacity) {
		super(startPosition, capacity);
		// TODO Auto-generated constructor stub
	}

	static final int DISTANCE_THRESHOLD_KM = 10;
	@Nullable
	private GradientModel gradientModel;


	@Override
	protected void tickImpl(TimeLapse time) {
		// Check if we can deliver nearby
		final Parcel delivery = getDelivery(time, 5);

		final RoadModel rm = getRoadModel();
		final PDPModel pm = getPDPModel();

		if (delivery != null) {
			if (delivery.getDeliveryLocation().equals(getPosition())
					&& pm.getVehicleState(this) == VehicleState.IDLE) {
				pm.deliver(this, delivery, time);
			} else {
				rm.moveTo(this, delivery.getDeliveryLocation(), time);
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

				if (newSize <= getCapacity()) {
					pm.pickup(this, closest, time);
				}
			} else {
				rm.moveTo(this, rm.getPosition(closest), time);
			}
			return;
		}

		if (rm.getObjectsOfType(Parcel.class).isEmpty()) {
			rm.moveTo(this, getStartPosition(), time);
			return;
		}

		// If none of the above, let the gradient field guide us!
		@Nullable
		final Point p = verifyNotNull(gradientModel).getTargetFor(this);
		if (p != null) {
			rm.moveTo(this, p, time);
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

	@Override
	public float getStrength() {
		return -1;
	}

	Map<Point, Float> getFields() {
		return verifyNotNull(gradientModel).getFields(this);
	}
}
