package project.antsystems;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.ui.renderers.CanvasRenderer;
import com.github.rinde.rinsim.ui.renderers.ViewPort;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.internal.win32.POINT;
import project.gradientfield.MultiAGVGradientField;

import java.util.List;
import java.util.Map;

public class AntVisualiser  implements RoadUser {

    Point position;
    Point originalAntPosition;

    public AntVisualiser(Point position, Point originalAntPosition) {
        this.position = position;
        this.originalAntPosition = originalAntPosition;
    }

    @Override
    public void initRoadUser(RoadModel model) {
        model.addObjectAt(this, position);
    }

    @Override
    public String toString() {
        return "";
    }

  /*  @Override
    public void renderStatic(GC gc, ViewPort vp) {

    }

    @Override
    public void renderDynamic(GC gc, ViewPort vp, long time) {
        synchronized (this) {
            final Point tp = originalAntPosition;

            final Point p= this.position;

            int dia;

            final int x = vp.toCoordX(tp.x);
            final int y = vp.toCoordY(tp.y);

            final int xa = vp.toCoordX(p.x );
            final int ya = vp.toCoordY(p.y );

            RGB color = new RGB(0,255,0) ;

            gc.setBackground(new Color(gc.getDevice(), color));
            gc.drawLine(x,y,xa,ya);


        }
    }*/
}
