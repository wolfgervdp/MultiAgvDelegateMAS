//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package project;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.model.pdp.DefaultPDPModel;
import com.github.rinde.rinsim.core.model.pdp.Depot;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.VehicleDTO;
import com.github.rinde.rinsim.core.model.road.RoadModel;
//import com.github.rinde.rinsim.core.model.road.NewRoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModelBuilders;
import com.github.rinde.rinsim.geom.*;
import com.github.rinde.rinsim.pdptw.common.RouteRenderer;
import com.github.rinde.rinsim.scenario.Scenario;
import com.github.rinde.rinsim.ui.View;
import com.github.rinde.rinsim.ui.View.Builder;
import com.github.rinde.rinsim.ui.renderers.AGVRenderer;
import com.github.rinde.rinsim.ui.renderers.PDPModelRenderer;
import com.github.rinde.rinsim.ui.renderers.RoadUserRenderer;
import com.github.rinde.rinsim.ui.renderers.WarehouseRenderer;
import com.github.rinde.rinsim.util.TimeWindow;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

import project.gradientfield.MultiAGVGradientField;
import project.gradientfield.MultiDepotGradientField;
import project.gradientfield.MultiParcelGradientField;
import project.masagents.EvaporationAgent;
import project.masagents.InfrastructureAgent;
import project.masagents.MultiAntAGV;
import project.masagents.MultiAntParcel;
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
    private static final int NUM_AGVS = 1;
    private static final long TEST_END_TIME = 600000L;
    private static final int TEST_SPEED_UP = 1;
    private static final int NUM_PARCEL = 20;
    private static final long SERVICE_DURATION = 0;
    private static final int MAX_CAPACITY = 1;
    private static final int DEPOT_CAPACITY = 100;
    private static final int NUM_DEPOTS = 4;
    private static final int MULTIAGV_CAPACITY = 1;
    static final long RANDOM_SEED = 123L;
    private static final double VEHICLE_SPEED_KMH = 100;

    private static Point MIN_POINT_1 = new Point(0, 32);
    private static Point P1_DELIVERY = new Point(64, 32);

    private static final long M1 = 0 * 60 * 1000L;
    private static final long M1_P1 = 1 * 60 * 1000L;
    private static final long M1_P2 = 10 * 60 * 1000L;
    private static final long M1_D1 = 1 * 60 * 1000L;
    private static final long M1_D2 = 10 * 60 * 1000L;

    private static final int END_OF_SIMULATION = 10 * 60 * 60 * 1000;


    private static final long M60 = 1 * 60 * 1000L;

    private Warehouse() {
    }

    public static void main(String[] args) {
        run(false);
    }


    public static void run(boolean testing) {
        Builder viewBuilder = View.builder()
                .with(PDPModelRenderer.builder().withDestinationLines())
                .with(AGVRenderer.builder()
                        .withDifferentColorsForVehicles());

        View.Builder view = View.builder()
                .with(WarehouseRenderer.builder().withOneWayStreetArrows().withNodeOccupancy())
                .with(AntAgentRenderer.builder())
                .with(RoadUserRenderer.builder().withToStringLabel()
                        .withColorAssociation(MultiParcel.class, new RGB(0, 255, 0))
                        .withColorAssociation(MultiDepot.class, new RGB(255, 0, 0))
                        .withColorAssociation(
                                InfrastructureAgent.class, new RGB(255, 255, 255))
                        .withColorAssociation(ExplorationAntVisualiser.class, new RGB(0, 0, 255))
                        .withColorAssociation(IntentionAntVisualiser.class, new RGB(0, 255, 255))
                        .withColorAssociation(GoalVisualiser.class, new RGB(12, 55, 255)))
                //.with(TaxiRenderer.builder(Language.ENGLISH))
                .with(RouteRenderer.builder())
                .with(PDPModelRenderer.builder())
                .with(AGVRenderer.builder().withDifferentColorsForVehicles())
                .withSimulatorEndTime(1000 * 60 * 100);
                //.withSpeedUp(TEST_SPEED_UP);

        List<InfrastructureAgent> infrastructureAgents = new ArrayList<>();

        Simulator sim = Simulator.builder()
                .addModel(
                        RoadModelBuilders.dynamicGraph(
                                WarehouseDesignWithAnts.GraphCreator.createSimpleGraph(1, infrastructureAgents))
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

        for (InfrastructureAgent agent : infrastructureAgents) {
            sim.register(agent);
        }

/*		for(int i = 0; i < NUM_AGVS; ++i) {
			//RoadUser user = new MultiAGV(sim.getRandomGenerator(), sim);
			sim.register(new MultiAntAGV(roadModel.getRandomPosition(rng),
					MULTIAGV_CAPACITY, sim));
//			sim.register(new MultiAGV(new Point(8,8),
//					MULTIAGV_CAPACITY, sim));
		}*/

        sim.register(new EvaporationAgent(infrastructureAgents));


/*	    for (int i = 0; i < NUM_DEPOTS; i++) {
	        sim.register(new MultiDepot(roadModel.getRandomPosition(rng), sim));
		}
		for (int i = 0; i < NUM_PARCEL; i++) {

			sim.register(new MultiAntParcel(
					Parcel.builder(roadModel.getRandomPosition(rng),
							roadModel.getRandomPosition(rng))
							.serviceDuration(SERVICE_DURATION)
							.neededCapacity(1 + rng.nextInt(MAX_CAPACITY))
							.buildDTO(), sim));
		}*/

        ArrayList<Point> possibleParcels = createPossibleParcelLocations();
        ArrayList<Point> possibleAGVs = createPossibleAGVLocations();
        ArrayList<Point> possibleDepot = createPossibleDepotLocations();
        TestWithMultiplePackageAtTheBeginning(possibleDepot, possibleParcels, possibleAGVs, sim);
        //sim.register(new WarehouseUpdater(sim.getModelProvider().getModel(NewRoadModel.class)));

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

    static void TestWithMultiplePackageAtTheBeginning(ArrayList<Point> possibleDepot, ArrayList<Point> possibleParcels, ArrayList<Point> possibleVehicles, Simulator sim) {

        for (int i = 0; i < 4; i++) {
            sim.register(new MultiDepot(possibleDepot.get(i), sim));
        }
        for (int i = 0; i < 1; i++) {   //max84
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
            int randomCapacity = rp.nextInt((4));
            sim.register(new MultiAntParcel(Parcel.builder(MIN_POINT_1, P1_DELIVERY)
                    .neededCapacity(2)
                    .orderAnnounceTime(M1)
                    .pickupTimeWindow(TimeWindow.create(M1_P1, M1_P2))
                    .deliveryTimeWindow(TimeWindow.create(M1_D1, M1_D2))
                    .buildDTO(), sim));
        }


        for (int i = 0; i <2; i++) {   //max48
            Random r = new Random();
            r.setSeed(RANDOM_SEED);
            int random = r.nextInt((2 * 13) - i - 0);
            MIN_POINT_1 = possibleVehicles.get(random);
            possibleVehicles.remove(random);
            sim.register(new MultiAntAGV((MIN_POINT_1), 1, sim));
        }
    }

    static void TestWithOnePackageAtTheBeginning(ArrayList<Point> possibleDepot, ArrayList<Point> possibleParcels, ArrayList<Point> possibleVehicles, Simulator sim) {

        for (int i = 0; i < 4; i++) {
            sim.register(new MultiDepot(possibleDepot.get(i), sim));
        }
        for (int i = 0; i < 1; i++) {   //max84
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
            int randomOrderAnnouncement = rp.nextInt((int) (END_OF_SIMULATION * 0.9));
            int randomCapacity = rp.nextInt((4));
            sim.register(new MultiParcel(Parcel.builder(MIN_POINT_1, P1_DELIVERY)
                    .neededCapacity(randomCapacity)
                    .orderAnnounceTime(randomOrderAnnouncement)
                    .pickupTimeWindow(TimeWindow.create(M1_P1 + randomOrderAnnouncement, M1_P2 + randomOrderAnnouncement))
                    .deliveryTimeWindow(TimeWindow.create(M1_D1 + randomOrderAnnouncement, M1_D2 + randomOrderAnnouncement))
                    .buildDTO()));
        }
        for (int i = 0; i < 4; i++) {   //max48
            Random r = new Random();
            r.setSeed(RANDOM_SEED);
            int random = r.nextInt((2 * 13) - i - 0);
            MIN_POINT_1 = possibleVehicles.get(random);
            possibleVehicles.remove(random);
            sim.register(new MultiAntAGV(MIN_POINT_1, 1, sim));
        }
    }
}
