package project.gradientfield;

import com.github.rinde.rinsim.core.model.pdp.Container;
import com.github.rinde.rinsim.geom.Point;

import java.util.Map;

public interface FieldContainer extends FieldEmitter, Container{
    Map<Point, Float> getFields();
}
