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
import com.github.rinde.rinsim.core.model.pdp.DefaultPDPModel;
import com.github.rinde.rinsim.core.model.pdp.Depot;
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
import com.github.rinde.rinsim.pdptw.common.AddParcelEvent;
import com.github.rinde.rinsim.pdptw.common.AddVehicleEvent;
import com.github.rinde.rinsim.pdptw.common.RouteRenderer;
import com.github.rinde.rinsim.scenario.TimedEventHandler;
import com.github.rinde.rinsim.scenario.gendreau06.Gendreau06Parser;
import com.github.rinde.rinsim.scenario.gendreau06.Gendreau06Scenario;
import com.github.rinde.rinsim.ui.View;
import com.github.rinde.rinsim.ui.renderers.PDPModelRenderer;
import com.github.rinde.rinsim.ui.renderers.PlaneRoadModelRenderer;
import com.github.rinde.rinsim.ui.renderers.RoadUserRenderer;
import com.github.rinde.rinsim.ui.renderers.WarehouseRenderer;
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
public final class test_SIM {
	static final long RANDOM_SEED = 123L;

	static final int TEST_SPEED_UP = 64;
	static final long TEST_END_TIME = 20 * 60 * 1000;

	private test_SIM() {}

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
						        .withImageAssociation(
						          MultiAGVGradientField.class, "/graphics/perspective/bus-44.png")
						        .withImageAssociation(
						          Depot.class, "/graphics/flat/warehouse-32.png")
						        .withImageAssociation(
						          MultiParcelGradientField.class, "/graphics/flat/hailing-cab-32.png"))
				.with(GradientFieldRenderer.builder())						     
				.with(RouteRenderer.builder())
				.with(PDPModelRenderer.builder())	;

		@Nullable Display display=null;
		@Nullable Monitor m=null;
		@Nullable Listener list=null;
		if (testing) {
			view = view.withAutoClose()
					.withAutoPlay()
					.withSimulatorEndTime(10)
					.withSpeedUp(TEST_SPEED_UP);
		} else if (m != null && list != null && display != null) {
			view = view.withMonitor(m)
					.withSpeedUp(1)
					.withResolution(m.getClientArea().width, m.getClientArea().height)
					.withDisplay(display)
					.withCallback(list)
					.withAsync()
					.withAutoPlay()
					.withAutoClose();
		}


		final Gendreau06Scenario scenario = Gendreau06Parser
				.parser().addFile(GradientFieldExample.class
						.getResourceAsStream("/data/gendreau06/req_rapide_1_240_24"),
						"req_rapide_1_240_24")
				.allowDiversion()
				.parse().get(0);

	    Experiment.builder()
	      .withRandomSeed(RANDOM_SEED)
	      .withThreads(1)
	      .addConfiguration(MASConfiguration.builder()
	        .setName("GradientFieldConfiguration")
	        .addEventHandler(AddVehicleEvent.class, VehicleHandler.INSTANCE)
	        .addEventHandler(AddParcelEvent.class, ParcelHandler.INSTANCE)
	        .addModel(GradientModel.builder())
	        //.addModel(
			//		RoadModelBuilders.dynamicGraph(
			//				GradientFieldExample.GraphCreator.createSimpleGraph())
			//		.withCollisionAvoidance()
			//		.withDistanceUnit(SI.METER)
			//		.withVehicleLength(3)
			//		)
			//.addModel(DefaultPDPModel.builder())
	        .build())
	      .addScenario(scenario)
	      .showGui(view)
	      .repeat(1)
	      .perform();
	    
	    
		Simulator sim = Simulator.builder()
				.addModel(
				RoadModelBuilders.dynamicGraph(
				       GraphCreator.createSimpleGraph())
				      .withCollisionAvoidance()
				     .withDistanceUnit(SI.METER)
				   .withVehicleLength(2)
				 )
				.addModel(DefaultPDPModel.builder())
				
				//.addModel(GradientModel.builder())
				
				.addModel(view)
				.build();
		sim.start();

	}

	enum VehicleHandler implements TimedEventHandler<AddVehicleEvent> {
		INSTANCE {
			@Override
			public void handleTimedEvent(AddVehicleEvent event, SimulatorAPI sim) {
				sim.register(new MultiAGVGradientField(event.getVehicleDTO(), sim));
			}
		}
	}

	enum ParcelHandler implements TimedEventHandler<AddParcelEvent> {
		INSTANCE {
			@Override
			public void handleTimedEvent(AddParcelEvent event, SimulatorAPI sim) {
				// all parcels are accepted by default
				sim.register(new MultiParcelGradientField(event.getParcelDTO(), sim));
			}
		}
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
			Table<Integer, Integer, Point> matrix = createMatrix(8, 6, new Point(0.0D, 0.0D));

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
			return new ListenableGraph(g);
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

}
