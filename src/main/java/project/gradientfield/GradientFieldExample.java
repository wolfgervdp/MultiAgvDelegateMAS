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
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.TimeWindowPolicy.TimeWindowPolicies;
import com.github.rinde.rinsim.core.model.pdp.VehicleDTO;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModelBuilders;
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

import javax.measure.unit.SI;
import java.util.ArrayList;
import java.util.Random;
//import project.gradientfield.testExample.ParcelHandler;
//import project.gradientfield.testExample.VehicleHandler;

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
	static final long RANDOM_SEED = 90L;
	private static final double VEHICLE_SPEED_KMH = 100 ;
	private static final int SPEED_UP = 5 ;

	private static Point MIN_POINT_1 = new Point(0, 32);
	private static Point P1_DELIVERY = new Point(64, 32);

	private static final long M1 = 0 * 60 * 1000L;
	private static final long M1_P1 = 1 * 60 * 1000L;
	private static final long M1_P2 = 10 * 60 * 1000L;
	private static final long M1_D1 = 1 * 60 * 1000L;
	private static final long M1_D2 = 10 * 60 * 1000L;
	private static final double VEHICLE_LENGTH = 2.0D;
	private static final int NUM_AGVS = 2;
	private static final int END_OF_SIMULATION=10*60*60*1000;


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
		View.Builder view = View.builder()
				.with(WarehouseRenderer.builder().withOneWayStreetArrows().withNodeOccupancy())
				.with(RoadUserRenderer.builder())
				.with(GradientFieldRenderer.builder())
				.with(RouteRenderer.builder())
				.with(PDPModelRenderer.builder().withDestinationLines())
				.with(AGVRenderer.builder().withDifferentColorsForVehicles())
				.withSimulatorEndTime((long)END_OF_SIMULATION)
				.withSpeedUp(SPEED_UP)
				;


		Simulator sim = Simulator.builder()
				.addModel(
						RoadModelBuilders.dynamicGraph(
								WarehouseDesign.GraphCreator.createSimpleGraph(3))
								.withCollisionAvoidance()
								.withDistanceUnit(SI.METER)
								.withSpeedUnit(SI.METERS_PER_SECOND)
								.withVehicleLength(2))
				.addModel(GradientModel.builder())

				.addModel(DefaultPDPModel.builder().withTimeWindowPolicy(TimeWindowPolicies.TARDY_ALLOWED))
				.addModel(view)
				.build();

		ArrayList<Point> possibleParcels = createPossibleParcelLocations();
		ArrayList<Point> possibleAGVs = createPossibleAGVLocations();
		ArrayList<Point> possibleDepot = createPossibleDepotLocations();
		TestWithMultiplePackageAtTheBeginning(possibleDepot,possibleParcels, possibleAGVs, sim);

		//TestWithOnePackageAtTheBeginning(possibleDepot,possibleParcels, possibleAGVs, sim);

		sim.start();
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
		for (int i : firstAndLastRow)
		{
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

	static void TestWithMultiplePackageAtTheBeginning(ArrayList<Point> possibleDepot, ArrayList<Point> possibleParcels, ArrayList<Point> possibleVehicles, Simulator sim) {

		for (int i = 0; i < 4; i++)        {
			sim.register(new MultiDepotGradientField(possibleDepot.get(i)));
		}
		for (int i = 0; i < 7; i++)
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
			int randomCapacity = rp.nextInt((3))+1;
			sim.register( new MultiParcelGradientField(Parcel.builder(MIN_POINT_1, P1_DELIVERY)
					.neededCapacity(2)
					.orderAnnounceTime(M1)
					.pickupTimeWindow(TimeWindow.create(M1_P1, M1_P2))
					.deliveryTimeWindow(TimeWindow.create(M1_D1, M1_D2))
					.buildDTO()));
		}


		for (int i = 0; i <6; i++)
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
	static void TestWithOnePackageAtTheBeginning(ArrayList<Point> possibleDepot, ArrayList<Point> possibleParcels, ArrayList<Point> possibleVehicles, Simulator sim) {

		for (int i = 0; i < 4; i++)        {
			sim.register(new MultiDepotGradientField(possibleDepot.get(i)));
		}
		for (int i = 0; i < 1; i++)
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
			int randomOrderAnnouncement = rp.nextInt((int)(END_OF_SIMULATION*0.9));
			int randomCapacity = rp.nextInt((4));
			sim.register( new MultiParcelGradientField(Parcel.builder(MIN_POINT_1, P1_DELIVERY)
					.neededCapacity(randomCapacity)
					.orderAnnounceTime(randomOrderAnnouncement)
					.pickupTimeWindow(TimeWindow.create(M1_P1+randomOrderAnnouncement, M1_P2+randomOrderAnnouncement))
					.deliveryTimeWindow(TimeWindow.create(M1_D1+randomOrderAnnouncement, M1_D2+randomOrderAnnouncement))
					.buildDTO()));
		}
		for (int i = 0; i < 2; i++)
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
