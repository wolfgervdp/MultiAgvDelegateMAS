package project.visualisers;

import com.github.rinde.rinsim.geom.Point;

public abstract class ConnectedVisualiser extends Visualiser {

    Point originalAntPosition;

    public ConnectedVisualiser(Point position, Point originalAntPosition) {
        super(position);
        this.originalAntPosition = originalAntPosition;
    }
}
