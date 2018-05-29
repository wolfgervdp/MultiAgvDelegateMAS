package project.antsystems;

import com.github.rinde.rinsim.core.model.DependencyProvider;
import com.github.rinde.rinsim.core.model.ModelBuilder;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadUser;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.ui.renderers.CanvasRenderer;
import com.github.rinde.rinsim.ui.renderers.ViewPort;
import com.google.auto.value.AutoValue;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;

import java.util.*;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;

import com.github.rinde.rinsim.core.model.DependencyProvider;
import com.github.rinde.rinsim.core.model.ModelBuilder.AbstractModelBuilder;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.ui.renderers.CanvasRenderer.AbstractCanvasRenderer;
import com.github.rinde.rinsim.ui.renderers.ViewPort;
import com.google.auto.value.AutoValue;

public class AntAgentRenderer extends CanvasRenderer.AbstractCanvasRenderer{
    /*
     * Copyright (C) 2011-2017 Rinde van Lon, imec-DistriNet, KU Leuven
     *
     * Licensed under the Apache License, Version 2.0 (the "License");
     * you may not use this file except in compliance with the License.
     * You may obtain a copy of the License at
     *
     *         http://www.apache.org/licenses/LICENSE-2.0
     *
     * Unless required by applicable law or agreed to in writing, software
     * distributed under the License is distributed on an "AS IS" BASIS,
     * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     * See the License for the specific language governing permissions and
     * limitations under the License.
     */



        static final double DIAMETER_MUL = 10d;
        static final RGB GREEN = new RGB(0, 255, 0);
        static final RGB RED = new RGB(255, 0, 0);

    RoadModel roadModel;

    AntAgentRenderer(RoadModel rm) {
        roadModel = rm;
        }

        @Override
        public void renderStatic(GC gc, ViewPort vp) {}

        @Override
        public void renderDynamic(GC gc, ViewPort vp, long time) {

            Set<AntVisualiser> setAntVisulizer= roadModel.getObjectsOfType(AntVisualiser.class);

            for(AntVisualiser object : setAntVisulizer){

                    final int x = vp.toCoordX(object.position.x);
                    final int y = vp.toCoordY(object.position.y);
                    final int ox = vp.toCoordX(object.originalAntPosition.x);
                    final int oy = vp.toCoordY(object.originalAntPosition.y);


                RGB color = null;
                color = new RGB(0,0,255);
                gc.setForeground(new Color(gc.getDevice(), color));
                gc.setLineWidth(4);
                gc.drawLine(x, y, ox, oy);
                gc.setLineWidth(1);


            }
           // synchronized (trucks) {
              //  for (final AntVisualiser t : trucks) {
                  //  final Point tp = t.position;

                    //final Map<Point, Float> fields = t.getFields();

                    float max = Float.NEGATIVE_INFINITY;
                    float min = Float.POSITIVE_INFINITY;

                  /*  for (final Map.Entry<Point, Float> p : fields.entrySet()) {
                        max = Math.max(max, p.getValue());
                        min = Math.min(min, p.getValue());
                    }
                    int dia;
                    RGB color = null;
                    for (final Map.Entry<Point, Float> entry : fields.entrySet()) {
                        final Point p = entry.getKey();
                        final float field = entry.getValue();
                        //divide by to to get the drawing

                        final int x = vp.toCoordX(tp.x + p.x/2);
                        final int y = vp.toCoordY(tp.y + p.y/2);

                        if (field < 0) {
                            dia = (int) ((int) (field / -min * DIAMETER_MUL));
                            color = RED;
                        } else {
                            dia = (int) ((int) (field / max * DIAMETER_MUL));
                            color = GREEN;
                        }
                        gc.setBackground(new Color(gc.getDevice(), color));
                        gc.fillOval(x, y, dia, dia);*/

                    //}
              //  }
            }


    public static Builder builder() {
        return new project.antsystems.AutoValue_AntAgentRendere_Builder();
    }


        @AutoValue
        abstract static class Builder extends
                ModelBuilder.AbstractModelBuilder<project.antsystems.AntAgentRenderer, Void> {

            Builder() {
                setDependencies(RoadModel.class);
            }

            @Override
            public project.antsystems.AntAgentRenderer build(DependencyProvider dependencyProvider) {
                final RoadModel rm = dependencyProvider.get(RoadModel.class);
                return new project.antsystems.AntAgentRenderer(rm);
            }
        }


}
