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
import java.lang.reflect.Field;
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
import com.github.rinde.rinsim.core.model.pdp.Depot;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.road.RoadModel;

import com.github.rinde.rinsim.geom.Point;
import com.google.auto.value.AutoValue;


import com.google.common.collect.ImmutableList;

import project.gradientfield.GradientFieldExample.DepotHandler;

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
	//private static final int[] X = {0, 4, 0, -4};
	//private static final int[] Y = {4, 0, -4, 0};

	//private static final int[] X = {0, 1, 0, -1};
	//private static final int[] Y = {1, 0, -1, 0};
	//private static final int[] X = {-4, 0, 4, 4, 4, 0, -4, -4};
	//private static final int[] Y = {4, 4, 4, 0, -4, -4, -4, 0};
	private static final int[] X = {0, 0, 4, 8, 0, 0, -4, -8};
	private static final int[] Y = {4, 8, 0, 0, -4, -8, 0, 0};
	//private static final int[] X = {-8, 0, 8, 8, 8, 0, -8, -8};
	//private static final int[] Y = {8, 8, 8, 0, -8, -8, -8, 0};
	private static final Point MIN = new Point(0.0, 4.0);
	private static final Point MAX = new Point(68.0, 32.0);

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

	List<FieldContainer> getFieldContainerFieldEmitters() {
		final List<FieldContainer> MultiAGVGradientFields = new ArrayList<FieldContainer>();

		for (final FieldEmitter emitter : emitters) {
			if (emitter instanceof FieldContainer) {
				MultiAGVGradientFields.add((FieldContainer) emitter);
			}
		}

		return MultiAGVGradientFields;
	}

	@Nullable
	Point getTargetFor(FieldContainer element) {
		float maxField = Float.NEGATIVE_INFINITY;
		Point maxFieldPoint = null;
		//by me
		//if(element.getPosition().x%4==0 && element.getPosition().y%4==0)
		//end by me
		//{

		for (int i = 0; i < X.length; i=i+2) {

			final Point p = new Point(
					Math.round(element.getPosition().x/4)*4 + X[i],
					Math.round(element.getPosition().y/4)*4 + Y[i]);
			//System.out.println(p);
			if (p.x < minX || p.x > maxX || p.y < minY || p.y > maxY) {
				continue;
			}

			final float field = getField(p, element);
			//System.out.println(i+" "+field);

			if (field >= maxField) {
				maxField = field;
				maxFieldPoint = p;
				//System.out.println("maxfield "+field);
				//System.out.println("maxFieldPoint "+p);
			}
			//}
		}
		maxFieldPoint=new Point(Math.round(maxFieldPoint.x/4)*4,Math.round(maxFieldPoint.y/4)*4 );
		return maxFieldPoint;
	}

	float getField(Point in, FieldContainer multiAGVGradientField) {
		float field = 0.0f;
		if(field!=field)
		{
			System.out.println("First NAN check "+ field);	
		}
		//		System.out.println("start");
		int count=0;
		for (final FieldEmitter emitter : emitters) {
			//System.out.println("Count:"+ count+"Dont give up, just debug!!");	
			float emitterStrenght=0;
			int count1=0;
			if((Point.distance(emitter.getPosition(), in)!=0))
			{
				if (emitter instanceof MultiDepotGradientField) {
					for (final Parcel p : verifyNotNull(pdpModel).getContents(multiAGVGradientField)) {
						emitterStrenght = (float) (emitter.getStrength(emitter.getPosition(), p) / (Point.distance(emitter.getPosition(), in)));
						//System.out.println("count: "+count1+" Strength: "+emitterStrenght);
					}
				} else {
					emitterStrenght = (float) (emitter.getStrength() / (Point.distance(emitter.getPosition(), in)));
//				System.out.println("getField method, Emitter:"+emitter.getPosition());
					if (emitterStrenght != emitterStrenght) {
						System.out.println("emitterstrenthgh   Emitter:" + emitter.getPosition() + " Emitter Strength: " + emitter.getStrength() + " First point: " + Point.distance(emitter.getPosition(), in) + "ISNAN: " + (emitter.getStrength() / (Point.distance(emitter.getPosition(), in))));
					}
					if (field != field) {
						System.out.println("Emitter:" + emitter.getPosition() + " Emitter Strength: " + emitter.getStrength() + " First point: " + Point.distance(emitter.getPosition(), in));
					}
				}
			}

			if (emitterStrenght==0){
				emitterStrenght=0;
			}else{
				emitterStrenght=(float)(emitter.getStrength()/ Point.distance(emitter.getPosition(), in));
			}	
			if(emitterStrenght!=emitterStrenght){
			System.out.println("emitterstrenthgh   Emitter:"+emitter.getPosition()+" Emitter Strength: "+emitter.getStrength()+ " First point: "+Point.distance(emitter.getPosition(), in));	
			}
//			System.out.println("Emitter position: "+emitter.getPosition()+"  Strength: "+emitterStrenght+" AllStrength: "+field);
//			if (emitterStrenght<-60)
//			{
//				System.out.println("problem");
//				System.out.println("Emitter position: "+emitter.getPosition()+"  Strength: "+emitterStrenght+" AllStrength: "+field);
//			}
			field = field +emitterStrenght;
			count++;
		}
		if(field!=field)
		{
			System.out.println("This is not a number");
		}
		for (final Parcel p : verifyNotNull(pdpModel).getContents(multiAGVGradientField)) {
			float parcelStrenght=0;
			//System.out.println("continue!"+ "in"+in);

			if (((MultiParcelGradientField)p).getStrength()==0){
				parcelStrenght=0;
			}else{
				parcelStrenght=(float) (((MultiParcelGradientField)p).getStrength() / Point.distance(p.getDeliveryLocation(), in));
			}	
			field = field + parcelStrenght;
			if(field!=field)
			{
				System.out.println("This is not a number");
			}
			//field = field + (float) (((MultiParcelGradientField)p).getStrength() / Point.distance(p.getDeliveryLocation(), in));

			//System.out.println(((MultiParcelGradientField)p).getStrength()+"parcel"+field);

		}
		if(field!=field)
		{
			System.out.println("This is not a number");	
		}
		//		else {
		//			System.out.println("This is a number. Don't worry:) ");	
		//		}
		//		System.out.println(field);
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

	Map<Point, Float> getFields(FieldContainer multiAGVGradientField) {
		final Map<Point, Float> fields = new HashMap<Point, Float>();

		for (int i = 0; i < X.length; i++) {
			final Point p = new Point(multiAGVGradientField.getPosition().x + X[i],
					multiAGVGradientField.getPosition().y + Y[i]);

			if (p.x < minX || p.x > maxX || p.y < minY || p.y > maxY) {
				continue;
			}

			fields.put(new Point(X[i], Y[i]), getField(p, multiAGVGradientField));
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
