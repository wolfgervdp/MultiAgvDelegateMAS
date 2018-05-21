package project.gradientfield;


import com.github.rinde.rinsim.core.model.pdp.ParcelDTO;
import com.github.rinde.rinsim.core.model.pdp.PDPModel.ParcelState;
import com.github.rinde.rinsim.geom.Point;
import project.MultiParcel;

public class MultiParcelGradientField extends MultiParcel implements FieldEmitter{

	static final float AVAILABLE_STRENGTH = 3.0f;
	  private final Point pos;

	  MultiParcelGradientField(ParcelDTO pDto) {
	    super(pDto);
	    pos = pDto.getPickupLocation();
	  }

	  @Override
	  public void setModel(GradientModel model) {}

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
}
