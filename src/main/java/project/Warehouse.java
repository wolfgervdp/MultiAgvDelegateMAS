//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package project;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.model.pdp.DefaultPDPModel;
import com.github.rinde.rinsim.core.model.pdp.Depot;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
//import com.github.rinde.rinsim.core.model.road.NewRoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModelBuilders;
import com.github.rinde.rinsim.event.Event;
import com.github.rinde.rinsim.event.Listener;
import com.github.rinde.rinsim.geom.Graph;
import com.github.rinde.rinsim.geom.Graphs;
import com.github.rinde.rinsim.geom.ListenableGraph;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.geom.TableGraph;
import com.github.rinde.rinsim.ui.View;
import com.github.rinde.rinsim.ui.View.Builder;
import com.github.rinde.rinsim.ui.renderers.AGVRenderer;
import com.github.rinde.rinsim.ui.renderers.PDPModelRenderer;
import com.github.rinde.rinsim.ui.renderers.RoadUserRenderer;
import com.github.rinde.rinsim.ui.renderers.WarehouseRenderer;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import org.apache.commons.math3.random.AbstractRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;
import javax.measure.unit.SI;

public final class Warehouse {


	private static final double VEHICLE_LENGTH = 2.0D;
	private static final int NUM_AGVS = 1;
	private static final long TEST_END_TIME = 600000L;
	private static final int TEST_SPEED_UP = 16;
	private static final int NUM_PARCEL = 10;
	private static final long SERVICE_DURATION = 60000;
	private static final int MAX_CAPACITY = 3;
	private static final int DEPOT_CAPACITY = 100;
	  private static final int NUM_DEPOTS = 4;
	  private static final int MULTIAGV_CAPACITY = 4;

	  


	private Warehouse() {
	}

	public static void main(String[] args) {
		run(false);
	}

	static View.Builder createGui(
			boolean testing,
			@Nullable Display display,
			@Nullable Monitor m,
			@Nullable Listener list) {

		View.Builder view = View.builder()
				.with(WarehouseRenderer.builder())
				.with(RoadUserRenderer.builder()
						.withColorAssociation(MultiParcel.class, new RGB(0, 255, 0))
						.withColorAssociation(Depot.class, new RGB(255, 0, 0))
						.withImageAssociation(
								MultiAGV.class, "/graphics/flat/taxi-32.png"))
				//.with(TaxiRenderer.builder(Language.ENGLISH))
				.withTitleAppendix("Warehouse");

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
		return view;
	}
	public static void run(boolean testing) {
		Builder viewBuilder = View.builder()
				.with(PDPModelRenderer.builder())
				.with(AGVRenderer.builder()
						.withDifferentColorsForVehicles());

		if (testing) {
			viewBuilder = viewBuilder.withAutoPlay().withAutoClose().withSimulatorEndTime(TEST_END_TIME).withTitleAppendix("TESTING").withSpeedUp(TEST_SPEED_UP);
		} else {
			viewBuilder = viewBuilder.withTitleAppendix("Warehouse Example");
		}
		//final View.Builder view = createGui(testing, display, m, list);
		final View.Builder view = createGui(testing, null, null, null);

		Simulator sim = Simulator.builder()
				.addModel(
				RoadModelBuilders.dynamicGraph(
				       Warehouse.GraphCreator.createSimpleGraph())
				      	.withCollisionAvoidance()
						.withDistanceUnit(SI.METER)
				   		.withVehicleLength(VEHICLE_LENGTH)
				 )
				//.addModel(viewBuilder)
				.addModel(DefaultPDPModel.builder())
				.addModel(view)
				.build();

		final RandomGenerator rng = sim.getRandomGenerator();

		final RoadModel roadModel = sim.getModelProvider().getModel(
				RoadModel.class);

		for(int i = 0; i < NUM_AGVS; ++i) {
			//RoadUser user = new MultiAGV(sim.getRandomGenerator(), sim);
			sim.register(new MultiAGV(roadModel.getRandomPosition(rng),
					MULTIAGV_CAPACITY, sim));
		}
		for (int i = 0; i < NUM_PARCEL; i++) {
			sim.register(new MultiParcel(
					Parcel.builder(roadModel.getRandomPosition(rng),
							roadModel.getRandomPosition(rng))
					.serviceDuration(SERVICE_DURATION)
					.neededCapacity(1 + rng.nextInt(MAX_CAPACITY))
					.buildDTO()));
		}
	    for (int i = 0; i < NUM_DEPOTS; i++) {
	        sim.register(new Depot(roadModel.getRandomPosition(rng)));
		}
		//sim.register(new WarehouseUpdater(sim.getModelProvider().getModel(NewRoadModel.class)));

		sim.start();
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

		static ListenableGraph<InfrastructureAgent> createSimpleGraph() {
			Graph<InfrastructureAgent> g = new TableGraph();

			Table<Integer, Integer, Point> matrix = createMatrix(8, 6, new Point(0.0D, 0.0D));

			for(int i = 0; i < matrix.columnMap().size(); ++i) {
				Object path;
				if (i % 2 == 0) {
					path = Lists.reverse(Lists.newArrayList(matrix.column(i).values()));
				} else {
					path = matrix.column(i).values();
				}

				//Graphs.addPath(g, (Iterable) path);
				GraphHelper.addPath(g, (Iterable) path, new InfrastructureAgent());
			}

//			Graphs.addPath(g, matrix.row(0).values());
//			Graphs.addPath(g, Lists.reverse(Lists.newArrayList(matrix.row(matrix.rowKeySet().size() - 1).values())));
			GraphHelper.addPath(g, matrix.row(0).values(), new InfrastructureAgent());
			GraphHelper.addPath(g, Lists.reverse(Lists.newArrayList(matrix.row(matrix.rowKeySet().size() - 1).values())), new InfrastructureAgent());
			return new ListenableGraph(g);
		}

		static ListenableGraph<InfrastructureAgent> createGraph() {
			Graph<InfrastructureAgent> g = new TableGraph();
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
