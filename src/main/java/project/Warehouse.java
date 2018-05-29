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
import com.github.rinde.rinsim.geom.*;
import com.github.rinde.rinsim.pdptw.common.RouteRenderer;
import com.github.rinde.rinsim.ui.View;
import com.github.rinde.rinsim.ui.View.Builder;
import com.github.rinde.rinsim.ui.renderers.AGVRenderer;
import com.github.rinde.rinsim.ui.renderers.PDPModelRenderer;
import com.github.rinde.rinsim.ui.renderers.RoadUserRenderer;
import com.github.rinde.rinsim.ui.renderers.WarehouseRenderer;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

import project.visualisers.AntAgentRenderer;
import org.apache.commons.math3.random.RandomGenerator;
import org.eclipse.swt.graphics.RGB;
import project.visualisers.ExplorationAntVisualiser;
import project.visualisers.GoalVisualiser;
import project.visualisers.IntentionAntVisualiser;

import java.util.*;

import javax.measure.unit.SI;

public final class Warehouse {


	private static final double VEHICLE_LENGTH = 2.0D;
	private static final int NUM_AGVS = 3;
	private static final long TEST_END_TIME = 600000L;
	private static final int TEST_SPEED_UP = 1;
	private static final int NUM_PARCEL = 20;
	private static final long SERVICE_DURATION = 0;
	private static final int MAX_CAPACITY = 3;
	private static final int DEPOT_CAPACITY = 100;
	private static final int NUM_DEPOTS = 4;
	private static final int MULTIAGV_CAPACITY = 400;

	private Warehouse() {
	}

	public static void main(String[] args) {
		run(false);
	}

	static View.Builder createGui(	boolean testing) {

		View.Builder view = View.builder()
				.with(WarehouseRenderer.builder().withOneWayStreetArrows().withNodeOccupancy())
                .with(AntAgentRenderer.builder())
				.with(RoadUserRenderer.builder().withToStringLabel()
						.withColorAssociation(MultiParcel.class, new RGB(0, 255, 0))
						.withColorAssociation(MultiDepot.class, new RGB(255, 0, 0))
						.withColorAssociation(
								InfrastructureAgent.class, new RGB(255, 255, 0))
						.withColorAssociation(ExplorationAntVisualiser.class, new RGB(0, 0, 255))
						.withColorAssociation(IntentionAntVisualiser.class, new RGB(0, 255, 255))
						.withColorAssociation(GoalVisualiser.class, new RGB(12, 55, 255)))
				//.with(TaxiRenderer.builder(Language.ENGLISH))
				.with(RouteRenderer.builder())
				.with(PDPModelRenderer.builder())
				.with(AGVRenderer.builder().withDifferentColorsForVehicles());

			view = view
					//.withAutoPlay()
					.withSimulatorEndTime(1000*60*100)
					.withSpeedUp(TEST_SPEED_UP);
		return view;
	}
	public static void run(boolean testing) {
		Builder viewBuilder = View.builder()
				.with(PDPModelRenderer.builder().withDestinationLines())
				.with(AGVRenderer.builder()
						.withDifferentColorsForVehicles());

		if (testing) {
			viewBuilder = viewBuilder.withAutoPlay().withAutoClose().withSimulatorEndTime(TEST_END_TIME).withTitleAppendix("TESTING").withSpeedUp(TEST_SPEED_UP);
		} else {
			viewBuilder = viewBuilder.withTitleAppendix("Warehouse Example");
		}
		final View.Builder view = createGui(testing);

		List<InfrastructureAgent> infrastructureAgents = new ArrayList<>();

		Simulator sim = Simulator.builder()
				.addModel(
				RoadModelBuilders.dynamicGraph(
				       WarehouseDesignWithAnts.GraphCreator.createSimpleGraph(1,infrastructureAgents))
				      	.withCollisionAvoidance()
						.withDistanceUnit(SI.METER)
						.withSpeedUnit(SI.METERS_PER_SECOND)
				   		.withVehicleLength(VEHICLE_LENGTH)
				 )
				.addModel(DefaultPDPModel.builder())
				.addModel(view)
				.build();
		final RandomGenerator rng = sim.getRandomGenerator();

		final RoadModel roadModel = sim.getModelProvider().getModel(
				RoadModel.class);

		for(InfrastructureAgent agent : infrastructureAgents){
			sim.register(agent);
		}

		for(int i = 0; i < NUM_AGVS; ++i) {
			//RoadUser user = new MultiAGV(sim.getRandomGenerator(), sim);
			sim.register(new MultiAGV(roadModel.getRandomPosition(rng),
					MULTIAGV_CAPACITY, sim));
//			sim.register(new MultiAGV(new Point(8,8),
//					MULTIAGV_CAPACITY, sim));
		}

		sim.register(new EvaporationAgent(infrastructureAgents));

		
	    for (int i = 0; i < NUM_DEPOTS; i++) {
	        sim.register(new MultiDepot(roadModel.getRandomPosition(rng), 2, sim));
		}
		for (int i = 0; i < NUM_PARCEL; i++) {

			sim.register(new MultiParcel(
					Parcel.builder(roadModel.getRandomPosition(rng),
							roadModel.getRandomPosition(rng))
							.serviceDuration(SERVICE_DURATION)
							.neededCapacity(1 + rng.nextInt(MAX_CAPACITY))
							.buildDTO(), sim));
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

		GraphCreator() {}

		static ImmutableTable<Integer, Integer, Point> createMatrix(int cols, int rows, Point offset) {
			com.google.common.collect.ImmutableTable.Builder<Integer, Integer, Point> builder = ImmutableTable.builder();

			for(int c = 0; c < cols; ++c) {
				for(int r = 0; r < rows; ++r) {
					builder.put(r, c, new Point(offset.x + (double)c * 2.0D * 2.0D, offset.y + (double)r * 2.0D * 2.0D));
				}
			}

			return builder.build();
		}

		static ListenableGraph<LengthData> createSimpleGraph(List<InfrastructureAgent> list) {
			Graph<LengthData> g = new TableGraph();

			Table<Integer, Integer, Point> matrix = createMatrix(8, 6, new Point(0.0D, 0.0D));

			for(int i = 0; i < matrix.columnMap().size(); ++i) {
				Object path;
				if (i % 2 == 0) {
					path = Lists.reverse(Lists.newArrayList(matrix.column(i).values()));
				} else {
					path = matrix.column(i).values();
				}

				//Graphs.addPath(g, (Iterable) path);
				//GraphHelper.addPath(g, (Iterable) path, new InfrastructureAgent());
				//GraphHelper.addPath(g,(Iterable)path, InfrastructureAgent.class);
				Graphs.addPath(g,(Iterable) path);
			}

//			Graphs.addPath(g, matrix.row(0).values());
//			Graphs.addPath(g, Lists.reverse(Lists.newArrayList(matrix.row(matrix.rowKeySet().size() - 1).values())));
			//GraphHelper.addPath(g, matrix.row(0).values(), InfrastructureAgent.class);
			//GraphHelper.addPath(g, Lists.reverse(Lists.newArrayList(matrix.row(matrix.rowKeySet().size() - 1).values())), InfrastructureAgent.class);
			Graphs.addPath(g, matrix.row(0).values());
			Graphs.addPath(g, Lists.reverse(Lists.newArrayList(matrix.row(matrix.rowKeySet().size() - 1).values())));

			for(Point p : g.getNodes()){
				list.add(new InfrastructureAgent(p));
			}

			return new ListenableGraph(g);
		}

	}
}
