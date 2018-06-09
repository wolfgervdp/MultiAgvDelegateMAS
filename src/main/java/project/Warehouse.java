//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package project;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.model.Model;
import com.github.rinde.rinsim.core.model.pdp.DefaultPDPModel;
import com.github.rinde.rinsim.core.model.pdp.PDPModel;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModelBuilders;
import com.github.rinde.rinsim.core.model.time.TickListener;
import com.github.rinde.rinsim.core.model.time.TimeLapse;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.pdptw.common.RouteRenderer;
import com.github.rinde.rinsim.scenario.Scenario;
import com.github.rinde.rinsim.ui.View;
import com.github.rinde.rinsim.ui.View.Builder;
import com.github.rinde.rinsim.ui.renderers.AGVRenderer;
import com.github.rinde.rinsim.ui.renderers.PDPModelRenderer;
import com.github.rinde.rinsim.ui.renderers.RoadUserRenderer;
import com.github.rinde.rinsim.ui.renderers.WarehouseRenderer;
import com.github.rinde.rinsim.util.TimeWindow;
import org.apache.commons.math3.random.RandomGenerator;
import org.eclipse.swt.graphics.RGB;
import org.omg.CORBA.INTERNAL;

import project.masagents.EvaporationAgent;
import project.masagents.InfrastructureAgent;
import project.masagents.MultiAntAGV;
import project.masagents.MultiAntParcel;
import project.visualisers.AntAgentRenderer;
import project.visualisers.ExplorationAntVisualiser;
import project.visualisers.GoalVisualiser;
import project.visualisers.IntentionAntVisualiser;

import javax.measure.unit.SI;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;

//import com.github.rinde.rinsim.core.model.road.NewRoadModel;

public final class Warehouse {

//
//    private static final double VEHICLE_LENGTH = 2.0D;
//    private static final int[] NUM_AGVS = {1,2,4,6,8,16};
//    private static final int NUM_AVERAGE_RUNS = 5;
//    private static final int[] NUM_PARCEL = {1,2,4,6,8,10};

    private static final double VEHICLE_LENGTH = 2.0D;
    private static final int[] NUM_AGVS = {1,2};
    private static final int NUM_AVERAGE_RUNS = 2;
    private static final int[] NUM_PARCEL = {1};

    private static final long TEST_END_TIME = 600000L;
    private static final int TEST_SPEED_UP = 1;

    private static final long SERVICE_DURATION = 0;
    private static final int MAX_CAPACITY = 1;
    private static final int DEPOT_CAPACITY = 100;
    private static final int NUM_DEPOTS = 4;
    private static final int MULTIAGV_CAPACITY = 1;
    static final long RANDOM_SEED = 90L;
    private static final double VEHICLE_SPEED_KMH = 100;
    private static final long[] SEEDS = {100l, 80l, 90l, 110l,120l};

    private static Point MIN_POINT_1 = new Point(0, 32);
    private static Point P1_DELIVERY = new Point(64, 32);

    private static final long M1 = 0 * 60 * 1000L;
    private static final long M1_P1 = 1 * 60 * 1000L;
    private static final long M1_P2 = 10 * 60 * 1000L;
    private static final long M1_D1 = 1 * 60 * 1000L;
    private static final long M1_D2 = 10 * 60 * 1000L;

    private static final int END_OF_SIMULATION = 24 * 60 * 60 * 1000;

    private static View.Builder view;


    private static final long M60 = 1 * 60 * 1000L;

    private Warehouse() { }

    public static void main(String[] args) {

        if(args.length >= 1 && args[0].equals("gradientfield")){
            GradientFieldExample.run(false);
        }else{
            run(false);
        }

    }


    public static void run(boolean testing) {

        view = View.builder()
                .with(WarehouseRenderer.builder().withOneWayStreetArrows().withNodeOccupancy())
                .with(AntAgentRenderer.builder())
                .with(RoadUserRenderer.builder().withToStringLabel()
                        .withColorAssociation(MultiParcel.class, new RGB(0, 255, 0))
                        .withColorAssociation(MultiDepot.class, new RGB(255, 0, 0))
                        .withColorAssociation(MultiAGV.class, new RGB(0,0,0))
                        .withColorAssociation(
                                InfrastructureAgent.class, new RGB(255, 255, 255))
                        .withColorAssociation(ExplorationAntVisualiser.class, new RGB(0, 0, 255))
                        .withColorAssociation(IntentionAntVisualiser.class, new RGB(0, 255, 255))
                        .withColorAssociation(GoalVisualiser.class, new RGB(12, 55, 255))
                )
                .with(RouteRenderer.builder())
                .with(PDPModelRenderer.builder())
                .with(AGVRenderer.builder().withDifferentColorsForVehicles().withVehicleCoordinates())
                .withSpeedUp(TEST_SPEED_UP)
                .withSimulatorEndTime(END_OF_SIMULATION);



        ArrayList<ArrayList<Long>> results = new ArrayList<>();
        ArrayList<ArrayList<Integer>> deadlocks = new ArrayList<>();

        for (int j =0; j<NUM_AGVS.length; j++)
        {
            ArrayList<Long> results_parcel = new ArrayList<>();
            ArrayList<Integer> result_deadlock_parcel = new ArrayList<>();

            for (int i =0; i<NUM_PARCEL.length; i++){


                Long averagedTime=0l;
                int numDeadlocks = 0;
                for (int average_I =0; average_I<NUM_AVERAGE_RUNS; average_I++) {
                    long currentTime = executeExperiment(NUM_AGVS[j],NUM_PARCEL[i],SEEDS[average_I]);
                    if(currentTime != END_OF_SIMULATION){
                        averagedTime=currentTime+averagedTime;
                    }else{
                        numDeadlocks++;
                    }


                }
                averagedTime=averagedTime/(NUM_AVERAGE_RUNS - numDeadlocks);
                results_parcel.add(averagedTime);
                result_deadlock_parcel.add(numDeadlocks);
            }
            results.add(results_parcel);
            deadlocks.add(result_deadlock_parcel);



        }
        PrintWriter writer = null;
        PrintWriter writer_deadlock = null;
        try {
            writer = new PrintWriter("results", "UTF-8");
            writer_deadlock = new PrintWriter("results_deadlock", "UTF-8");

            for (ArrayList<Long> results_inner : results) {
                for (Long l : results_inner) {
                    writer.print(l + ";");
                }
                writer.println();
            }
            for (ArrayList<Integer> deadlocks_inner : deadlocks) {
                for (Integer l : deadlocks_inner) {
                    writer_deadlock.print(l + ";");
                }
                writer_deadlock.println();
            }
            writer.close();
            writer_deadlock.close();
        } catch (FileNotFoundException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    static long executeExperiment(int numAgvs, int numParcels, long seed){
        List<InfrastructureAgent> infrastructureAgents = new ArrayList<>();
        Simulator sim = Simulator.builder()
                .addModel(
                        RoadModelBuilders.dynamicGraph(
                                WarehouseDesignWithAnts.GraphCreator.createSimpleGraph(3, infrastructureAgents))
                                .withCollisionAvoidance()
                                .withDistanceUnit(SI.METER)
                                .withSpeedUnit(SI.METERS_PER_SECOND)
                                .withVehicleLength(VEHICLE_LENGTH)
                )
                .addModel(DefaultPDPModel.builder())
                .addModel(view)
                .build();

        for (InfrastructureAgent agent : infrastructureAgents) {
            sim.register(agent);
        }

        sim.register(new EvaporationAgent(infrastructureAgents));

        ArrayList<Point> possibleParcels = createPossibleParcelLocations();
        ArrayList<Point> possibleAGVs = createPossibleAGVLocations();
        ArrayList<Point> possibleDepot = createPossibleDepotLocations();
        TestWithMultiplePackageAtTheBeginning(numAgvs,numParcels,seed,possibleDepot, possibleParcels, possibleAGVs, sim);
        //sim.register(new WarehouseUpdater(sim.getModelProvider().getModel(NewRoadModel.class)));
        sim.register(new TickListener() {
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
        boolean succeeded = false;
        while(!succeeded){
            try {
                sim.start();
                succeeded = true;
            }catch (Exception e){
                System.out.println(e);
            }
        }


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
