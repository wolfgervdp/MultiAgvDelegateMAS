package project.gradientfield;

import com.github.rinde.rinsim.core.model.pdp.Depot;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.geom.Point;

public class MultiDepotGradientField extends Depot implements FieldEmitter  {

	static final float AVAILABLE_STRENGTH =0.0f;
	private final Point pos;

	MultiDepotGradientField(Point pDto) {
		super(pDto);
		pos = pDto;    
	}

	@Override
	public void setModel(GradientModel model) {}

	@Override
	public Point getPosition() {
		return pos;
	}

	@Override
	public float getStrength() {
		return  AVAILABLE_STRENGTH;
	}
	@Override
	public float getStrength(Point vehiclesPosition, Parcel parcel) {
		float strength=0;
		if(vehiclesPosition.equals(parcel.getDeliveryLocation()))
		{
			strength=0.0f;
		}else
		{
			strength=0.0f;
		}
		return strength;
	}



}
