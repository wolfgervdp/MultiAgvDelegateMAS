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

import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;
import javax.measure.unit.SI;

import org.apache.commons.math3.random.AbstractRandomGenerator;
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
import com.github.rinde.rinsim.pdptw.common.AddDepotEvent;
import com.github.rinde.rinsim.pdptw.common.AddParcelEvent;
import com.github.rinde.rinsim.pdptw.common.AddVehicleEvent;
import com.github.rinde.rinsim.pdptw.common.RouteRenderer;
import com.github.rinde.rinsim.pdptw.common.StatsStopConditions;
import com.github.rinde.rinsim.scenario.Scenario;
import com.github.rinde.rinsim.scenario.StopConditions;
import com.github.rinde.rinsim.scenario.TimeOutEvent;
import com.github.rinde.rinsim.scenario.TimedEventHandler;
import com.github.rinde.rinsim.scenario.gendreau06.Gendreau06Parser;
import com.github.rinde.rinsim.scenario.gendreau06.Gendreau06Scenario;
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
import project.gradientfield.testExample.ParcelHandler;
import project.gradientfield.testExample.VehicleHandler;

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

	static final int TEST_SPEED_UP = 64;
	static final long TEST_END_TIME = 20 * 60 * 1000;



	private static final Point RESOLUTION = new Point(800, 800);
	private static final double VEHICLE_SPEED_KMH = 30d;
	private static final double MAX_VEHICLE_SPEED_KMH = 50d;
	private static final Point MIN_POINT = new Point(4.0, 1.0);
	private static final Point MIN_POINT_1 = new Point(0, 1);
	private static final Point MIN_POINT_2 = new Point(1, 0);


	private static final Point MAX_POINT = new Point(8, 4);
	private static final Point DEPOT_LOC = new Point(5, 2);
	private static final Point P1_PICKUP = new Point(1, 2);
	private static final Point P1_DELIVERY = new Point(4, 2);
	private static final Point P2_PICKUP = new Point(1, 1);
	private static final Point P2_DELIVERY = new Point(4, 1);
	private static final Point P3_PICKUP = new Point(1, 3);
	private static final Point P3_DELIVERY = new Point(4, 3);

	private static final long M1 = 60 * 1000L;
	private static final long M4 = 4 * 60 * 1000L;
	private static final long M5 = 5 * 60 * 1000L;
	private static final long M7 = 7 * 60 * 1000L;
	private static final long M10 = 10 * 60 * 1000L;
	private static final long M12 = 12 * 60 * 1000L;
	private static final long M13 = 13 * 60 * 1000L;
	private static final long M18 = 18 * 60 * 1000L;
	private static final long M20 = 20 * 60 * 1000L;
	private static final long M25 = 25 * 60 * 1000L;
	private static final long M30 = 30 * 60 * 1000L;
	private static final long M40 = 40 * 60 * 1000L;
	private static final long M60 = 60 * 60 * 1000L;
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
				.with(WarehouseRenderer.builder())
				.with(RoadUserRenderer.builder()
						.withColorAssociation(MultiParcel.class, new RGB(0, 255, 0))
						.withColorAssociation(Depot.class, new RGB(255, 0, 0))
						.withImageAssociation(
								MultiAGV.class, "/graphics/flat/taxi-32.png"))
				.with(GradientFieldRenderer.builder())
				.with(RouteRenderer.builder())
				.with(PDPModelRenderer.builder())
				.with(AGVRenderer.builder().withDifferentColorsForVehicles());

				if (testing) {
					view = view.withAutoClose()
							.withAutoPlay()
							.withSpeedUp(TEST_SPEED_UP)
							.withSimulatorEndTime(TEST_END_TIME);

				} 

				Experiment.builder()
				.withRandomSeed(RANDOM_SEED)
				.withThreads(1)
				.addConfiguration(MASConfiguration.builder()
						.setName("GradientFieldConfiguration")
						.addEventHandler(AddVehicleEvent.class, VehicleHandler.INSTANCE)
						.addEventHandler(AddParcelEvent.class, ParcelHandler.INSTANCE)
						.addModel(GradientModel.builder())
						.addModel(
								RoadModelBuilders.dynamicGraph(
										GradientFieldExample.GraphCreator.createSimpleGraph())
								.withCollisionAvoidance()
								.withDistanceUnit(SI.METER)
								.withVehicleLength(2)
								)
						.build())
				.addScenario(createScenario())
				.showGui(view)
				.repeat(1)
				.perform();


	}

	enum VehicleHandler implements TimedEventHandler<AddVehicleEvent> {
		INSTANCE {
			@Override
			public void handleTimedEvent(AddVehicleEvent event, SimulatorAPI sim) {
				sim.register(new MultiAGVGradientField(new Point(0, 8),
						2));
				sim.register(new MultiAGVGradientField(new Point(8, 8),
						2));
			}
		}
	}

	enum ParcelHandler implements TimedEventHandler<AddParcelEvent> {
		INSTANCE {
			@Override
			public void handleTimedEvent(AddParcelEvent event, SimulatorAPI sim) {
				// all parcels are accepted by default
				//sim.register(new MultiParcelGradientField(event.getParcelDTO()));
				sim.register(new MultiParcelGradientField(
						Parcel.builder(new Point(0, 0),
								new Point(4, 0))
						.serviceDuration(1)
						.buildDTO()));
			}
		}
	}
	static Scenario createScenario() {
		// In essence a scenario is just a list of events. The events must implement
		// the TimedEvent interface. You are free to construct any object as a
		// TimedEvent but keep in mind that implementations should be immutable.
		return Scenario.builder()



				// Adds one depot.
				//.addEvent(AddDepotEvent.create(-1, DEPOT_LOC))

				// Adds one vehicle.
				.addEvent(AddVehicleEvent.create(-1, VehicleDTO.builder()
						.speed(VEHICLE_SPEED_KMH)
						.startPosition(MIN_POINT_1)
						.build()))
				//				.addEvent(AddVehicleEvent.create(-1, VehicleDTO.builder()
				//						.speed(VEHICLE_SPEED_KMH)
				//						.startPosition(MIN_POINT_2)
				//						.build()))
				// Three add parcel events are added. They are announced at different
				// times and have different time windows.
				.addEvent(
						AddParcelEvent.create(Parcel.builder(P1_PICKUP, P1_DELIVERY)
								.neededCapacity(0)
								.orderAnnounceTime(M1)
								.pickupTimeWindow(TimeWindow.create(M1, M20))
								.deliveryTimeWindow(TimeWindow.create(M4, M30))
								.buildDTO()))

				.addEvent(
						AddParcelEvent.create(Parcel.builder(P2_PICKUP, P2_DELIVERY)
								.neededCapacity(0)
								.orderAnnounceTime(M5)
								.pickupTimeWindow(TimeWindow.create(M10, M25))
								.deliveryTimeWindow(
										TimeWindow.create(M20, M40))
								.buildDTO()))

				.addEvent(
						AddParcelEvent.create(Parcel.builder(P3_PICKUP, P3_DELIVERY)
								.neededCapacity(0)
								.orderAnnounceTime(M7)
								.pickupTimeWindow(TimeWindow.create(M12, M18))
								.deliveryTimeWindow(
										TimeWindow.create(M13, M60))
								.buildDTO()))

				// Signals the end of the scenario. Note that it is possible to stop the
				// simulation before or after this event is dispatched, that depends on
				// the stop condition (see below).
				//.addEvent(TimeOutEvent.create(M60))
				.scenarioLength(M60)

				// Adds a plane road model as this is part of the problem

				// Adds the pdp model
				.addModel(
						DefaultPDPModel.builder()
						.withTimeWindowPolicy(TimeWindowPolicies.TARDY_ALLOWED))
				//				.addModel(
				//						RoadModelBuilders.dynamicGraph(
				//								GradientFieldExample.GraphCreator.createSimpleGraph())
				//						.withCollisionAvoidance()
				//						.withDistanceUnit(SI.METER)
				//						.withVehicleLength(3)
				//						)
				// The stop condition indicates when the simulator should stop the
				// simulation. Typically this is the moment when all tasks are performed.
				// Custom stop conditions can be created by implementing the StopCondition
				// interface.
				.setStopCondition(StopConditions.or(
						StatsStopConditions.timeOutEvent(),
						StatsStopConditions.vehiclesDoneAndBackAtDepot()))
				.build();
	}


	static class GraphCreator {
		static final int LEFT_CENTER_U_ROW = 4;
		static final int LEFT_CENTER_L_ROW = 5;
		static final int LEFT_COL = 4;
		static final int RIGHT_CENTER_U_ROW = 2;
		static final int RIGHT_CENTER_L_ROW = 4;
		static final int RIGHT_COL = 0;

		GraphCreator() {
		}

		static ImmutableTable<Integer, Integer, Point> createMatrix(int cols, int rows, Point offset) {
			com.google.common.collect.ImmutableTable.Builder<Integer, Integer, Point> builder = ImmutableTable.builder();

			for(int c = 0; c < cols; ++c) {
				for(int r = 0; r < rows; ++r) {
					builder.put(r, c, new Point(offset.x + (double)c * 2.0D * 2.0D, offset.y + (double)r * 2.0D * 2.0D));
				}
			}

			return builder.build();
		}

		static ListenableGraph<LengthData> createSimpleGraph() {
			Graph<LengthData> g = new TableGraph();
			Table<Integer, Integer, Point> matrix = createMatrix(8, 8, new Point(0.0D, 0.0D));

			for(int i = 0; i < matrix.columnMap().size(); ++i) {
				Object path;
				if (i % 2 == 0) {
					path = Lists.reverse(Lists.newArrayList(matrix.column(i).values()));
				} else {
					path = matrix.column(i).values();
				}

				Graphs.addPath(g, (Iterable)path);
			}

			Graphs.addPath(g, matrix.row(0).values());
			Graphs.addPath(g, Lists.reverse(Lists.newArrayList(matrix.row(matrix.rowKeySet().size() - 1).values())));
			return new ListenableGraph<LengthData>(g);
		}

		static ListenableGraph<LengthData> createGraph() {
			Graph<LengthData> g = new TableGraph();
			Table<Integer, Integer, Point> leftMatrix = createMatrix(5, 10, new Point(0.0D, 0.0D));
			Iterator var2 = leftMatrix.columnMap().values().iterator();

			while(var2.hasNext()) {
				Map<Integer, Point> column = (Map)var2.next();
				Graphs.addBiPath(g, column.values());
			}

			Graphs.addBiPath(g, leftMatrix.row(4).values());
			Graphs.addBiPath(g, leftMatrix.row(5).values());
			Table<Integer, Integer, Point> rightMatrix = createMatrix(10, 7, new Point(30.0D, 6.0D));
			Iterator var6 = rightMatrix.rowMap().values().iterator();

			while(var6.hasNext()) {
				Map<Integer, Point> row = (Map)var6.next();
				Graphs.addBiPath(g, row.values());
			}

			Graphs.addBiPath(g, rightMatrix.column(0).values());
			Graphs.addBiPath(g, rightMatrix.column(rightMatrix.columnKeySet().size() - 1).values());
			Graphs.addPath(g, new Point[]{(Point)rightMatrix.get(2, 0), (Point)leftMatrix.get(4, 4)});
			Graphs.addPath(g, new Point[]{(Point)leftMatrix.get(5, 4), (Point)rightMatrix.get(4, 0)});
			final ListenableGraph graph = new ListenableGraph(g);


			graph.getEventAPI().addListener(new Listener() {
				@Override
				public void handleEvent(Event event) {
					Point[] test = new Point[0];
					RandomGenerator g = new AbstractRandomGenerator() {
						@Override
						public void setSeed(long l) {

						}
						@Override
						public double nextDouble() {
							Random r = new Random();
							return r.nextDouble();
						}
					};
					System.out.println("Being called");
					graph.addConnection(graph.getRandomNode(g), graph.getRandomNode(g));
				}
			});
			return graph;
		}
	}


	@Override
	public void registerModelProvider(ModelProvider mp) {
		mp.getModel(RoadModel.class).getRandomPosition(rng);
		RoadModel roadModel = mp.getModel(RoadModel.class);
		// TODO Auto-generated method stub

	}

}
