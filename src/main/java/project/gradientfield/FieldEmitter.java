package project.gradientfield;


import com.github.rinde.rinsim.geom.Point;

interface FieldEmitter {

  void setModel(GradientModel model);

  Point getPosition();

  float getStrength();
}
