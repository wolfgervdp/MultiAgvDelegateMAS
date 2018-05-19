package project.gradientfield;


import com.github.rinde.rinsim.examples.pdptw.gradientfield.*;
import com.github.rinde.rinsim.core.model.pdp.ParcelDTO;
import com.github.rinde.rinsim.geom.Point;
import project.MultiParcel;

public class MultiParcelGradientField extends MultiParcel implements FieldEmitter{

	public MultiParcelGradientField(ParcelDTO parcelDto) {
		super(parcelDto);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setModel(GradientModel model) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Point getPosition() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public float getStrength() {
		// TODO Auto-generated method stub
		return 0;
	}

}
