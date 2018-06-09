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

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.model.ModelProvider;
import com.github.rinde.rinsim.core.model.ModelReceiver;
import com.github.rinde.rinsim.core.model.pdp.DefaultPDPModel;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.TimeWindowPolicy.TimeWindowPolicies;
import com.github.rinde.rinsim.core.model.pdp.VehicleDTO;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModelBuilders;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.pdptw.common.RouteRenderer;
import com.github.rinde.rinsim.scenario.Scenario;
import com.github.rinde.rinsim.ui.View;
import com.github.rinde.rinsim.ui.renderers.AGVRenderer;
import com.github.rinde.rinsim.ui.renderers.PDPModelRenderer;
import com.github.rinde.rinsim.ui.renderers.RoadUserRenderer;
import com.github.rinde.rinsim.ui.renderers.WarehouseRenderer;
import com.github.rinde.rinsim.util.TimeWindow;
import org.apache.commons.math3.random.RandomGenerator;
import project.MultiDepot;
import project.WarehouseDesignWithAnts;
import project.masagents.EvaporationAgent;
import project.masagents.InfrastructureAgent;
import project.masagents.MultiAntAGV;
import project.masagents.MultiAntParcel;

import javax.measure.unit.SI;
import java.util.*;


/**
 * Example of a gradient field MAS for the Gendreau et al. (2006) dataset.
 * <p>
 * If this class is run on MacOS it might be necessary to use
 * -XstartOnFirstThread as a VM argument.
 *
 * @author David Merckx
 * @author Rinde van Lon
 */
public final class GradientFieldExample implements ModelReceiver {
 
	private static final double VEHICLE_LENGTH = 2.0D;
	private static final int[] NUM_AGVS = {1,2,4,6,8,16};
	private static final long TEST_END_TIME = 600000L;
	private static final int TEST_SPEED_UP = 1;
	private static final int[] NUM_PARCEL = {1,2,4,6,8,10};
	private static final long SERVICE_DURATION = 0;
	private static final int MAX_CAPACITY = 1;
	private static final int DEPOT_CAPACITY = 100;
	private static final int NUM_DEPOTS = 4;
	private static final int MULTIAGV_CAPACITY = 1;
	static final long RANDOM_SEED = 123L;
	private static final double VEHICLE_SPEED_KMH = 100;
	private static final long[] SEEDS = {100l, 80l, 90l, 110l,120l};


	private static Point MIN_POINT_1 = new Point(0, 32);
	private static Point P1_DELIVERY = new Point(64, 32);

	private static final long M1 = 0 * 60 * 1000L;
	private static final long M1_P1 = 1 * 60 * 1000L;
	private static final long M1_P2 = 10 * 60 * 1000L;
	private static final long M1_D1 = 1 * 60 * 1000L;
	private static final long M1_D2 = 10 * 60 * 1000L;

	private static final int END_OF_SIMULATION = 50 * 60 * 60 * 1000;

	private static View.Builder view;


	private static final long M60 = 1 * 60 * 1000L;

	private GradientFieldExample() {
	}

	final static RandomGenerator rng = null;

	final static RoadModel roadModel = null;

	/**
	 * Runs the example.
	 *
	 * @param args Ignored.
	 */
//	public static void main(String[] args) {
//		run(false);
//	}

	/**
	 * Runs the example.
	 *
	 * @param testing If <code>true</code> the example is run in testing mode,
	 *                this means that it will automatically start and stop itself.
	 */
	public static void run(final boolean testing) {
		view = View.builder()
				.with(WarehouseRenderer.builder().withOneWayStreetArrows().withNodeOccupancy())
				.with(RoadUserRenderer.builder())
				.with(GradientFieldRenderer.builder())
				.with(RouteRenderer.builder())
				.with(PDPModelRenderer.builder().withDestinationLines())
				.with(AGVRenderer.builder().withDifferentColorsForVehicles().withVehicleCoordinates())
				.withSimulatorEndTime((long)END_OF_SIMULATION)
				.withSpeedUp(TEST_SPEED_UP)
				;


		Map<Integer, Map<Integer,Long>> results = new HashMap<>();

		for (int j =0; j<NUM_AGVS.length; j++)
		{
			Map<Integer,Long> results_parcel = new HashMap<>();
			for (int i =0; i<NUM_PARCEL.length; i++){


				Long averagedTime=0l;
				for (int average_I =0; average_I<5; average_I++) {

					averagedTime=executeExperiment(NUM_AGVS[j],NUM_PARCEL[i],SEEDS[average_I])+averagedTime;

				}
				averagedTime=averagedTime/3;
				results_parcel.put(NUM_PARCEL[i],averagedTime);
			}
			results.put(NUM_AGVS[j],results_parcel);

		}

	}


	static long executeExperiment(int numAgvs, int numParcels, long seed){
		Simulator sim = Simulator.builder()
				.addModel(
						RoadModelBuilders.dynamicGraph(
								WarehouseDesign.GraphCreator.createSimpleGraph(3))
								.withCollisionAvoidance()
								.withDistanceUnit(SI.METER)
								.withSpeedUnit(SI.METERS_PER_SECOND)
								.withVehicleLength(VEHICLE_LENGTH)
				)
				.addModel(DefaultPDPModel.builder())
				.addModel(view)
				.build();

		//for (InfrastructureAgent agent : infrastructureAgents) {
	//		sim.register(agent);
	//	}

		//sim.register(new EvaporationAgent(infrastructureAgents));

		ArrayList<Point> possibleParcels = createPossibleParcelLocations();
		ArrayList<Point> possibleAGVs = createPossibleAGVLocations();
		ArrayList<Point> possibleDepot = createPossibleDepotLocations();
		TestWithMultiplePackageAtTheBeginning(numAgvs,numParcels,seed,possibleDepot, possibleParcels, possibleAGVs, sim);
		//sim.register(new WarehouseUpdater(sim.getModelProvider().getModel(NewRoadModel.class)));
		sim.start();
		sim.addTickListener(new TickListener() {
			@Override
			public void tick(TimeLapse timeLapse) {
				if(sim.getModelProvider().getModel(PDPModel.class).getParcels(PDPModel.ParcelState.DELIVERED).size() == numParcels){
					sim.stop();
				}
			}

			@Override
			public void afterTick(TimeLapse timeLapse) {

			}
		});

		return sim.getCurrentTime();
	}

	static ArrayList<Point> createPossibleParcelLocations() {
		ArrayList<Point> possibleParcels = new ArrayList<Point>();
		for (int i = 2; i < 8; i++) {
			for (int j = 2; j < 16; j++) {
				possibleParcels.add(new Point(4 * j, 4 * i));
			}
		}
		return possibleParcels;
	}

	static ArrayList<Point> createPossibleAGVLocations() {
		int[] firstAndLastRow = {0, 9};
		ArrayList<Point> possibleVehicles = new ArrayList<Point>();
		for (int i : firstAndLastRow) {
			for (int j = 2; j < 15; j++) {
				possibleVehicles.add(new Point(4 * j, 4 * i));
			}
		}
		return possibleVehicles;
	}

	static ArrayList<Point> createPossibleDepotLocations() {
		ArrayList<Point> possibleDepot = new ArrayList<Point>();
		possibleDepot.add(new

				Point(68, 12));
		possibleDepot.add(new

				Point(0, 24));
		possibleDepot.add(new

				Point(0, 12));
		possibleDepot.add(new

				Point(68, 24));
		Scenario.Builder b = Scenario.builder();
		return possibleDepot;
	}

	static void TestWithMultiplePackageAtTheBeginning(int numAgvs,int numParcels,long randomSeed,ArrayList<Point> possibleDepot, ArrayList<Point> possibleParcels, ArrayList<Point> possibleVehicles, Simulator sim) {

		for (int i = 0; i < 4; i++) {
			sim.register(new MultiDepot(possibleDepot.get(i), sim));
		}
		for (int i = 0; i < numParcels; i++) {   //max84
			Random rp = new Random();
			rp.setSeed(randomSeed);
			int randomParcel = rp.nextInt(6 * 14 - i - 0);
			MIN_POINT_1 = possibleParcels.get(randomParcel);
			possibleParcels.remove(randomParcel);
			double distance = Double.MAX_VALUE;
			//P1_DELIVERY=possibleDepot.get(randomDepot);

			for (int iCount = 0; iCount < 4; iCount++) {
				if (distance < Point.distance(MIN_POINT_1, possibleDepot.get(iCount))) {
				} else {
					distance = Point.distance(MIN_POINT_1, possibleDepot.get(iCount));
					P1_DELIVERY = possibleDepot.get(iCount);
				}
			}
			int randomCapacity = rp.nextInt((4));
			sim.register(new MultiAntParcel(Parcel.builder(MIN_POINT_1, P1_DELIVERY)
					.neededCapacity(1)
					.orderAnnounceTime(M1)
					.pickupTimeWindow(TimeWindow.create(M1_P1, M1_P2))
					.deliveryTimeWindow(TimeWindow.create(M1_D1, M1_D2))
					.buildDTO(), sim));
		}


		for (int i = 0; i < numAgvs; i++) {   //max48

			Random r = new Random();
			r.setSeed(randomSeed);
			int random = r.nextInt((2 * 13) - i - 0);
			MIN_POINT_1 = possibleVehicles.get(random);
			possibleVehicles.remove(random);
			sim.register(new MultiAntAGV((MIN_POINT_1), 1, sim));
		}
//        for (int i = 0; i < 10; i++) {   //max48
//            sim.register(new MultiAntAGV((new Point(8,(i*4)+8)), 1, sim));
//        }
	}
	static void TestWithOnePackageAtTheBeginning(ArrayList<Point> possibleDepot, ArrayList<Point> possibleParcels, ArrayList<Point> possibleVehicles, Simulator sim) {

		for (int i = 0; i < 4; i++)        {
			sim.register(new MultiDepotGradientField(possibleDepot.get(i)));
		}
		for (int i = 0; i < 10; i++)
		{   //max84
			Random rp = new Random();
			rp.setSeed(RANDOM_SEED);
			int randomParcel = rp.nextInt(6 * 14 - i - 0);
			MIN_POINT_1 = possibleParcels.get(randomParcel);
			possibleParcels.remove(randomParcel);
			double distance = Double.MAX_VALUE;
			//P1_DELIVERY=possibleDepot.get(randomDepot);

			for (int iCount = 0; iCount < 4; iCount++) {
				if (distance < Point.distance(MIN_POINT_1, possibleDepot.get(iCount))) {
				} else {
					distance = Point.distance(MIN_POINT_1, possibleDepot.get(iCount));
					P1_DELIVERY = possibleDepot.get(iCount);
				}
			}

			int randomOrderAnnouncement = (END_OF_SIMULATION/15);

			int randomCapacity = rp.nextInt((3))+1
					;
			sim.register( new MultiParcelGradientField(Parcel.builder(MIN_POINT_1, P1_DELIVERY)
					.neededCapacity(randomCapacity)
					.orderAnnounceTime(M1_P1)
					.pickupTimeWindow(TimeWindow.create(M1_P1+randomOrderAnnouncement*i, M1_P2+randomOrderAnnouncement*i))
					.deliveryTimeWindow(TimeWindow.create(M1_D1+randomOrderAnnouncement*i, M1_D2+randomOrderAnnouncement*i))
					.buildDTO()));
		}
		for (int i = 0; i < 6; i++)
		{   //max48
			Random r = new Random();
			r.setSeed(RANDOM_SEED);
			int random = r.nextInt((2 * 13) - i - 0);
			MIN_POINT_1 = possibleVehicles.get(random);
			possibleVehicles.remove(random);
			sim.register(new MultiAGVGradientField((VehicleDTO.builder()
					.speed(VEHICLE_SPEED_KMH)
					.startPosition(MIN_POINT_1)
					.capacity(1)
					.build()),sim));
		}
	}
	@Override
	public void registerModelProvider(ModelProvider mp) {
		mp.getModel(RoadModel.class).getRandomPosition(rng);
		RoadModel roadModel = mp.getModel(RoadModel.class);
	}

}
