package project.gradientfield;


import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.geom.Point;

interface FieldEmitter {

  void setModel(GradientModel model);

  Point getPosition();

  float getStrength();
  float getStrength(Point vehiclesPosition,Parcel parcel);


}
