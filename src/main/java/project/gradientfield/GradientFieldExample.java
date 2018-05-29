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

import java.awt.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;
import javax.measure.unit.SI;
import javax.sound.midi.Soundbank;

import com.github.rinde.rinsim.experiment.ExperimentResults;
import com.github.rinde.rinsim.pdptw.common.*;
import org.apache.commons.lang3.builder.Builder;
import org.apache.commons.math3.random.AbstractRandomGenerator;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.SimulatorAPI;
import com.github.rinde.rinsim.core.model.ModelProvider;
import com.github.rinde.rinsim.core.model.ModelReceiver;
import com.github.rinde.rinsim.core.model.pdp.DefaultPDPModel;
import com.github.rinde.rinsim.core.model.pdp.Depot;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.ParcelDTO;
import com.github.rinde.rinsim.core.model.pdp.VehicleDTO;
import com.github.rinde.rinsim.core.model.pdp.TimeWindowPolicy.TimeWindowPolicies;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModelBuilders;
import com.github.rinde.rinsim.event.Event;
import com.github.rinde.rinsim.event.Listener;
import com.github.rinde.rinsim.experiment.Experiment;
import com.github.rinde.rinsim.experiment.MASConfiguration;
import com.github.rinde.rinsim.geom.Graph;
import com.github.rinde.rinsim.geom.Graphs;
import com.github.rinde.rinsim.geom.LengthData;
import com.github.rinde.rinsim.geom.ListenableGraph;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.geom.TableGraph;
import com.github.rinde.rinsim.scenario.Scenario;
import com.github.rinde.rinsim.scenario.StopConditions;
import com.github.rinde.rinsim.scenario.TimeOutEvent;
import com.github.rinde.rinsim.scenario.TimedEventHandler;
import com.github.rinde.rinsim.scenario.gendreau06.Gendreau06Parser;
import com.github.rinde.rinsim.scenario.gendreau06.Gendreau06Scenario;
import com.github.rinde.rinsim.scenario.generator.Depots;
import com.github.rinde.rinsim.scenario.generator.Depots.DepotGenerator;
import com.github.rinde.rinsim.ui.View;
import com.github.rinde.rinsim.ui.renderers.AGVRenderer;
import com.github.rinde.rinsim.ui.renderers.PDPModelRenderer;
import com.github.rinde.rinsim.ui.renderers.PlaneRoadModelRenderer;
import com.github.rinde.rinsim.ui.renderers.RoadUserRenderer;
import com.github.rinde.rinsim.ui.renderers.WarehouseRenderer;
import com.github.rinde.rinsim.util.TimeWindow;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

import project.MultiAGV;
import project.MultiParcel;
import project.Warehouse;
//import project.gradientfield.testExample.ParcelHandler;
//import project.gradientfield.testExample.VehicleHandler;

/**
 * Example of a gradient field MAS for the Gendreau et al. (2006) dataset.
 * <p>
 * If this class is run on MacOS it might be necessary to use
 * -XstartOnFirstThread as a VM argument.
 * @author David Merckx
 * @author Rinde van Lon
 */
public final class GradientFieldExample implements ModelReceiver {
	static final long RANDOM_SEED = 123L;

	static final int TEST_SPEED_UP = 100;
	static final long TEST_END_TIME = 20 * 60 * 1000;



	private static final Point RESOLUTION = new Point(800, 800);
	private static final double VEHICLE_SPEED_KMH = 100;
	private static final double MAX_VEHICLE_SPEED_KMH = 100d;
	private static Point MIN_POINT_1 = new Point(0, 32);
	private static final Point MIN_POINT_2 = new Point(16, 0);
	private static final Point MIN_POINT_3 = new Point(0, 28);
	private static final Point MIN_POINT_4 = new Point(28, 0);

	private static final Point MAX_POINT = new Point(8, 4);
	private static final Point DEPOT_LOC = new Point(5, 2);
	private static final Point P1_PICKUP = new Point(8, 12);
	private static Point P1_DELIVERY = new Point(64, 32);
	private static final Point P2_PICKUP = new Point(4, 16);
	private static final Point P2_DELIVERY = new Point(20, 28);
	private static final Point P3_PICKUP = new Point(12, 12);
	private static final Point P3_DELIVERY = new Point(24, 28);
	private static final Point P4_PICKUP = new Point(28, 8);
	private static final Point P4_DELIVERY = new Point(40, 28);
	private static final Point P5_PICKUP = new Point(12, 28);
	private static final Point P5_DELIVERY = new Point(16, 16);

	private static final long M1 = 0 * 60 * 1000L;
	private static final long M2 = 0 * 60 * 1000L;
	private static final long M3 = 0 * 60 * 1000L;
	private static final long M4 = 0 * 60 * 1000L;
	private static final long M5 = 0 * 60 * 1000L;
	private static final long M1_P1 = 1 * 60 * 1000L;
	private static final long M1_P2 = 10 * 60 * 1000L;
	private static final long M1_D1 = 1 * 60 * 1000L;
	private static final long M1_D2 = 10 * 60 * 1000L;
	private static final long M2_P1 = 1 * 60 * 1000L;
	private static final long M2_P2 = 10 * 60 * 1000L;
	private static final long M2_D1 = 1 * 60 * 1000L;
	private static final long M2_D2 = 10 * 60 * 1000L;
	private static final long M3_P1 = 1 * 60 * 1000L;
	private static final long M3_P2 = 10 * 60 * 1000L;
	private static final long M3_D1 = 1 * 60 * 1000L;
	private static final long M3_D2 = 10 * 60 * 1000L;
	private static final long M4_P1 = 1 * 60 * 1000L;
	private static final long M4_P2 = 10 * 60 * 1000L;
	private static final long M4_D1 = 1 * 60 * 1000L;
	private static final long M4_D2 = 10 * 60 * 1000L;	
	private static final long M5_P1 = 1 * 60 * 1000L;
	private static final long M5_P2 = 10 * 60 * 1000L;
	private static final long M5_D1 = 1 * 60 * 1000L;
	private static final long M5_D2 = 10 * 60 * 1000L;


	private static final long M60 = 1 * 60 * 1000L;
	private GradientFieldExample() {}

	final static RandomGenerator rng = null;

	final static RoadModel roadModel = null;
	/**
	 * Runs the example.
	 * @param args Ignored.
	 */
	public static void main(String[] args) {
		run(false);
	}

	/**
	 * Runs the example.
	 * @param testing If <code>true</code> the example is run in testing mode,
	 *          this means that it will automatically start and stop itself.
	 */
	public static void run(final boolean testing) {
		View.Builder view = View.builder()
				.with(WarehouseRenderer.builder().withOneWayStreetArrows().withNodeOccupancy())
				.with(RoadUserRenderer.builder())
				//										.withColorAssociation(MultiParcel.class, new RGB(0, 255, 0))
				//										.withColorAssociation(Depots.class, new RGB(255, 0, 0)))
				.with(GradientFieldRenderer.builder())
				.with(RouteRenderer.builder())
				.with(PDPModelRenderer.builder().withDestinationLines())
				.with(AGVRenderer.builder().withDifferentColorsForVehicles())
				.with(RoutePanel.builder());
		if (testing) {
			view = view.withAutoClose()
					.withAutoPlay()
					.withSpeedUp(TEST_SPEED_UP)
					.withSimulatorEndTime(TEST_END_TIME);

		} 

		Experiment.builder()
		.withRandomSeed(RANDOM_SEED)
		.addConfiguration(MASConfiguration.builder()
				.setName("GradientFieldConfiguration")
				.addEventHandler(AddVehicleEvent.class, VehicleHandler.INSTANCE)
				.addEventHandler(AddParcelEvent.class, ParcelHandler.INSTANCE)
				.addEventHandler(AddDepotEvent.class, DepotHandler.INSTANCE)
				.addModel(GradientModel.builder())
				.addModel(
						RoadModelBuilders.dynamicGraph(
								WarehouseDesign.GraphCreator.createSimpleGraph(3))
						.withCollisionAvoidance()
						.withDistanceUnit(SI.METER)
						.withVehicleLength(2)
						)
				.build())
		.addScenario(createScenario())
		.withThreads(1)
		.showGui(view)
//		.withRandomSeed(RANDOM_SEED)
		.repeat(1)
		.perform();



	}

	enum VehicleHandler implements TimedEventHandler<AddVehicleEvent> {
		INSTANCE {
			@Override
			public void handleTimedEvent(AddVehicleEvent event, SimulatorAPI sim) {
				sim.register(new MultiAGVGradientField(event.getVehicleDTO(),sim));
			}
		}
	}

	enum ParcelHandler implements TimedEventHandler<AddParcelEvent> {
		INSTANCE {
			@Override
			public void handleTimedEvent(AddParcelEvent event, SimulatorAPI sim) {
				// all parcels are accepted by default
				sim.register(new MultiParcelGradientField(event.getParcelDTO()));
			}
		}
	}

	enum DepotHandler implements TimedEventHandler<AddDepotEvent> {
		INSTANCE {
			@Override
			public void handleTimedEvent(AddDepotEvent event, SimulatorAPI sim) {
				// all parcels are accepted by default
				sim.register(new MultiDepotGradientField(event.getPosition()) {
				});
			}
		}
	}

	static Scenario createScenario() {
		ArrayList<Point> possibleParcels = new ArrayList<Point>();
		for( int i=2; i<8; i++)
		{
			for( int j=2; j<16; j++)
			{				
				possibleParcels.add( new Point(4*j,4*i));
			}
		}

		int[] firstAndLastRow= {0,9};
		ArrayList<Point> possibleVehicles = new ArrayList<Point>();
		for (int i : firstAndLastRow) {	
			for( int j=2; j<15; j++)
			{				
				possibleVehicles.add( new Point(4*j,4*i));
			}
		}
//		int[] firstAndLastColoumn= {0,17};
//		for (int j : firstAndLastColoumn) {	
//			for( int i=1; i<9; i++)
//			{				
//				possibleVehicles.add( new Point(4*j,4*i));
//			}
//		}
		ArrayList<Point> possibleDepot = new ArrayList<Point>();		
		possibleDepot.add(new Point(68, 12) );
		possibleDepot.add(new Point(0, 24) );
		possibleDepot.add(new Point(0, 12) );
		possibleDepot.add(new Point(68,24 ) );
		Scenario.Builder b = Scenario.builder();


		for (int i = 0; i <4 ; i++) {
			b.addEvent(AddDepotEvent.create(-1,possibleDepot.get(i)));
		}
		for (int i = 0; i < 1; i++) {//max84
			Random rp = new Random();
			rp.setSeed(RANDOM_SEED);
			int randomParcel=rp.nextInt(6*14-i-0);
			MIN_POINT_1=possibleParcels.get(randomParcel);
			possibleParcels.remove(randomParcel);
			double distance=Double.MAX_VALUE;
			//			P1_DELIVERY=possibleDepot.get(randomDepot);		

			for (int iCount = 0; iCount < 4; iCount++) {
				if (distance <Point.distance(MIN_POINT_1, possibleDepot.get(iCount)))					{
				}else {
					distance=Point.distance(MIN_POINT_1, possibleDepot.get(iCount));
					P1_DELIVERY=possibleDepot.get(iCount);
				}		
			}

			//			Random rd = new Random();
			//			int randomDepot=rd.nextInt(4);
			b.addEvent(AddParcelEvent.create(Parcel.builder(MIN_POINT_1, P1_DELIVERY)
					.neededCapacity(2)
					.orderAnnounceTime(M1)
					.pickupTimeWindow(TimeWindow.create(M1_P1, M1_P2))
					.deliveryTimeWindow(TimeWindow.create(M1_D1, M1_D2))
					.buildDTO()));			
		}

		for (int i = 0; i < 4;i++) {//max48
			Random r = new Random();
            r.setSeed(RANDOM_SEED);
            int random=r.nextInt((2*13)-i-0);
			MIN_POINT_1=possibleVehicles.get(random);
			possibleVehicles.remove(random);
			b.addEvent(AddVehicleEvent.create(-1, VehicleDTO.builder()
					.speed(VEHICLE_SPEED_KMH)
					.startPosition(MIN_POINT_1)
					.capacity(1)				
					.build()));
		}	
		return b.scenarioLength(M60)
				.addModel(DefaultPDPModel.builder().withTimeWindowPolicy(TimeWindowPolicies.TARDY_ALLOWED))

				.setStopCondition(StopConditions.or(
						StatsStopConditions.timeOutEvent(),
						StatsStopConditions.vehiclesDoneAndBackAtDepot()))
				.build(); 
	}
	@Override
	public void registerModelProvider(ModelProvider mp) {
		mp.getModel(RoadModel.class).getRandomPosition(rng);
		RoadModel roadModel = mp.getModel(RoadModel.class);
		// TODO Auto-generated method stub

	}

}
