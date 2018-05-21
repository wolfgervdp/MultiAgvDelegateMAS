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
package project.gradientfield;

import static com.google.common.base.Verify.verifyNotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.annotation.Nullable;

import com.github.rinde.rinsim.core.model.DependencyProvider;
import com.github.rinde.rinsim.core.model.Model.AbstractModel;
import com.github.rinde.rinsim.core.model.ModelBuilder.AbstractModelBuilder;
import com.github.rinde.rinsim.core.model.ModelProvider;
import com.github.rinde.rinsim.core.model.ModelReceiver;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.road.RoadModel;

import com.github.rinde.rinsim.examples.pdptw.gradientfield.*;
import com.github.rinde.rinsim.geom.Point;
import com.google.auto.value.AutoValue;


import com.google.common.collect.ImmutableList;

/**
 * Model for gradient field implementation.
 * @author David Merckx
 * @author Rinde van Lon
 */
public class GradientModel
    extends AbstractModel<FieldEmitter>
    implements ModelReceiver {
  /**
   * Possibilities (-1,1) (0,1) (1,1) (-1,0) (1,0 (-1,-1) (0,-1) (1,-1).
   */
  private static final int[] X = {-1, 0, 1, 1, 1, 0, -1, -1};
  private static final int[] Y = {1, 1, 1, 0, -1, -1, -1, 0};
	private static final Point MIN = new Point(0.0, 0.0);
	private static final Point MAX = new Point(28.0, 28.0);
  
  private final List<FieldEmitter> emitters;
  private double minX;
  private double maxX;
  private double minY;
  private double maxY;
  @Nullable
  private PDPModel pdpModel;

  GradientModel() {
    emitters = new CopyOnWriteArrayList<FieldEmitter>();
  }

  List<FieldEmitter> getEmitters() {
    return emitters;
  }

  List<MultiAGVGradientField> getMultiAGVGradientFieldEmitters() {
    final List<MultiAGVGradientField> MultiAGVGradientFields = new ArrayList<MultiAGVGradientField>();

    for (final FieldEmitter emitter : emitters) {
      if (emitter instanceof MultiAGVGradientField) {
        MultiAGVGradientFields.add((MultiAGVGradientField) emitter);
      }
    }

    return MultiAGVGradientFields;
  }

  @Nullable
  Point getTargetFor(MultiAGVGradientField element) {
    float maxField = Float.NEGATIVE_INFINITY;
    Point maxFieldPoint = null;

    for (int i = 0; i < X.length; i++) {
      final Point p = new Point(element.getPosition().x + X[i],
        element.getPosition().y + Y[i]);

      if (p.x < minX || p.x > maxX || p.y < minY || p.y > maxY) {
        continue;
      }

      final float field = getField(p, element);
      if (field >= maxField) {
        maxField = field;
        maxFieldPoint = p;
      }
    }

    return maxFieldPoint;
  }

  float getField(Point in, MultiAGVGradientField MultiAGVGradientField) {
    float field = 0.0f;
    for (final FieldEmitter emitter : emitters) {
      field = field + (float) (emitter.getStrength()
        / Point.distance(emitter.getPosition(), in));
    }

    for (final Parcel p : verifyNotNull(pdpModel).getContents(MultiAGVGradientField)) {
      field = field + (float) (2 / Point.distance(p.getDeliveryLocation(), in));
    }
    return field;
  }

  @Override
  public boolean register(FieldEmitter element) {
    emitters.add(element);
    element.setModel(this);
    return true;
  }

  @Override
  public boolean unregister(FieldEmitter element) {
    emitters.remove(element);
    return false;
  }

  Map<Point, Float> getFields(MultiAGVGradientField MultiAGVGradientField) {
    final Map<Point, Float> fields = new HashMap<Point, Float>();

    for (int i = 0; i < X.length; i++) {
      final Point p = new Point(MultiAGVGradientField.getPosition().x + X[i],
        MultiAGVGradientField.getPosition().y + Y[i]);

      if (p.x < minX || p.x > maxX || p.y < minY || p.y > maxY) {
        continue;
      }

      fields.put(new Point(X[i], Y[i]), getField(p, MultiAGVGradientField));
    }

    float avg = 0;
    for (final Float f : fields.values()) {
      avg += f;
    }
    avg /= fields.size();
    for (final Entry<Point, Float> entry : fields.entrySet()) {
      fields.put(entry.getKey(), entry.getValue() - avg);
    }
    return fields;
  }

  @Override
  public void registerModelProvider(ModelProvider mp) {
    pdpModel = mp.tryGetModel(PDPModel.class);
//    final ImmutableList<Point> bs = mp.getModel(RoadModel.class)
//      .getBounds();

   

    minX = MIN.x;
    maxX = MAX.x;
    minY = MIN.y;
    maxY = MAX.y;
  }

  @Override
  public <U> U get(Class<U> clazz) {
    return clazz.cast(this);
  }

  static Builder builder() {
    return new AutoValue_GradientModel_Builder();
  }

  @AutoValue
  abstract static class Builder
      extends AbstractModelBuilder<GradientModel, FieldEmitter>
      implements Serializable {

    private static final long serialVersionUID = 4464819196521333718L;

    Builder() {
      setProvidingTypes(GradientModel.class);
    }

    @Override
    public GradientModel build(DependencyProvider dependencyProvider) {
      return new GradientModel();
    }
  }
}
