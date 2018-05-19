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
package com.github.rinde.rinsim.examples.core;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Map;

import javax.measure.unit.SI;

import com.github.rinde.rinsim.core.Simulator;
import com.github.rinde.rinsim.core.model.Model;
import com.github.rinde.rinsim.core.model.road.CollisionGraphRoadModelImpl;
import com.github.rinde.rinsim.core.model.road.RoadModel;
import com.github.rinde.rinsim.core.model.road.RoadModelBuilders;
import com.github.rinde.rinsim.geom.Graph;
import com.github.rinde.rinsim.geom.Graphs;
import com.github.rinde.rinsim.geom.LengthData;
import com.github.rinde.rinsim.geom.ListenableGraph;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.geom.TableGraph;
import com.github.rinde.rinsim.ui.View;
import com.github.rinde.rinsim.ui.renderers.AGVRenderer;
import com.github.rinde.rinsim.ui.renderers.WarehouseRenderer;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

/**
 * Example showcasing the {@link CollisionGraphRoadModelImpl} with an
 * {@link WarehouseRenderer} and {@link AGVRenderer}.
 * @author Rinde van Lon
 */
public final class WarehouseExample {

  private static final double VEHICLE_LENGTH = 5d;
  private static final int NUM_AGVS = 1;
  private static final long TEST_END_TIME = 10 * 60 * 1000L;
  private static final int TEST_SPEED_UP = 100;

  private WarehouseExample() {}

  /**
   * @param args - No args.
   */
  public static void main(String[] args) {
    run(false);
    
  }

  /**
   * Runs the example.
   * @param testing If <code>true</code> the example will run in testing mode,
   *          automatically starting and stopping itself such that it can be run
   *          from a unit test.
   */
  public static void run(boolean testing) {
    View.Builder viewBuilder = View.builder()
      .with(WarehouseRenderer.builder()
        .withMargin(VEHICLE_LENGTH))
      .with(AGVRenderer.builder()
        .withDifferentColorsForVehicles());

    if (testing) {
      viewBuilder = viewBuilder.withAutoPlay()
        .withAutoClose()
        .withSimulatorEndTime(TEST_END_TIME)
        .withTitleAppendix("TESTING")
        .withSpeedUp(TEST_SPEED_UP);
    } else {
      viewBuilder = viewBuilder.withTitleAppendix("Warehouse Example");
    }

    final Simulator sim = Simulator.builder()
      .addModel(
        //RoadModelBuilders.dynamicGraph(GraphCreator.createSimpleGraph()
        	RoadModelBuilders.dynamicGraph(GraphCreator.createGraph())
          .withCollisionAvoidance()
          .withDistanceUnit(SI.METER)
          .withVehicleLength(VEHICLE_LENGTH))
      .addModel(viewBuilder)
      .build();

    for (int i = 0; i < NUM_AGVS; i++) {
      sim.register(new AGVAgent(sim.getRandomGenerator()));
    }


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

    static ImmutableTable<Integer, Integer, Point> createMatrix(int cols,
        int rows, Point offset) {
      final ImmutableTable.Builder<Integer, Integer, Point> builder =
        ImmutableTable.builder();
      for (int c = 0; c < cols; c++) {
        for (int r = 0; r < rows; r++) {
          builder.put(r, c, new Point(
            offset.x + c * VEHICLE_LENGTH * 2,
            offset.y + r * VEHICLE_LENGTH * 2));
        }
      }
      return builder.build();
    }

    static ListenableGraph<LengthData> createSimpleGraph() {
      final Graph<LengthData> g = new TableGraph<>();

      final Table<Integer, Integer, Point> matrix = createMatrix(8, 6,
        new Point(0, 0));

      for (int i = 0; i < matrix.columnMap().size(); i++) {

        final Iterable<Point> path;
        if (i % 2 == 0) {
          path = Lists.reverse(newArrayList(matrix.column(i).values()));
        } else {
          path = matrix.column(i).values();
        }
        Graphs.addPath(g, path);
      }

      Graphs.addPath(g, matrix.row(0).values());
      Graphs.addPath(g, Lists.reverse(newArrayList(matrix.row(
        matrix.rowKeySet().size() - 1).values())));

      return new ListenableGraph<>(g);
    }

    static ListenableGraph<LengthData> createGraph() {
      final Graph<LengthData> g = new TableGraph<>();

      final Table<Integer, Integer, Point> leftMatrix = createMatrix(5, 10,
        new Point(0, 0));
      for (final Map<Integer, Point> column : leftMatrix.columnMap().values()) {
        Graphs.addBiPath(g, column.values());
      }
      Graphs.addBiPath(g, leftMatrix.row(LEFT_CENTER_U_ROW).values());
      Graphs.addBiPath(g, leftMatrix.row(LEFT_CENTER_L_ROW).values());

      final Table<Integer, Integer, Point> rightMatrix = createMatrix(10, 7,
        new Point(30, 6));
      for (final Map<Integer, Point> row : rightMatrix.rowMap().values()) {
        Graphs.addBiPath(g, row.values());
      }
      Graphs.addBiPath(g, rightMatrix.column(0).values());
      Graphs.addBiPath(g, rightMatrix.column(rightMatrix.columnKeySet().size()
        - 1).values());

      Graphs.addPath(g,
        rightMatrix.get(RIGHT_CENTER_U_ROW, RIGHT_COL),
        leftMatrix.get(LEFT_CENTER_U_ROW, LEFT_COL));
      Graphs.addPath(g,
        leftMatrix.get(LEFT_CENTER_L_ROW, LEFT_COL),
        rightMatrix.get(RIGHT_CENTER_L_ROW, RIGHT_COL));

      return new ListenableGraph<>(g);
    }
  }
}
