package project;

import com.github.rinde.rinsim.geom.*;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import project.masagents.InfrastructureAgent;

import java.util.List;


public class WarehouseDesignWithAnts {



        public static class GraphCreator {

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

            static ListenableGraph<LengthData> createSimpleGraph(int version, List<InfrastructureAgent> agents) {
                Graph<LengthData> g = new TableGraph();
                Table<Integer, Integer, Point> matrix = createMatrix(18, 10, new Point(0.0D, 0.0D));

                if (version==1)
                {
                    for(int i = 0; i < matrix.columnMap().size(); i++) {
                        Object path ;

                        for(int j = 0; j < matrix.rowMap().size(); j++) {

                            if(j == 0 && i == 0){
                                path = Lists.newArrayList(matrix.get(j+1, i),matrix.get(j, i));
                                Graphs.addPath(g, (Iterable)path);
                            }else if (j == matrix.rowMap().size()-1 &&i == 0) {
                                path = Lists.newArrayList(matrix.get(j, i),matrix.get(j-1, i));
                                Graphs.addPath(g, (Iterable)path);
                            }else if (i == matrix.columnMap().size()-1 &&j ==0) {
                                path = Lists.newArrayList(matrix.get(j, i),matrix.get(j+1, i));
                                Graphs.addPath(g, (Iterable)path);
                            }else if (i == matrix.columnMap().size()-1 &&j ==matrix.rowMap().size()-1) {
                                path = Lists.newArrayList(matrix.get(j-1, i),matrix.get(j, i));
                                Graphs.addPath(g, (Iterable)path);
                            }else if (j == 1 &&i == 1) {
                                path = Lists.reverse(Lists.newArrayList(matrix.get(j-1, i),matrix.get(j-1, i-1)));
                                Graphs.addPath(g, (Iterable)path);
                                path = Lists.newArrayList(matrix.get(j-1, i),matrix.get(j, i));
                                Graphs.addPath(g, (Iterable)path);
                            }else if (j == matrix.rowMap().size()-2 &&i == 1) {
                                path = Lists.newArrayList(matrix.get(j+1, i),matrix.get(j+1, i-1));
                                Graphs.addPath(g, (Iterable)path);
                                path = Lists.newArrayList(matrix.get(j, i),matrix.get(j+1, i));
                                Graphs.addPath(g, (Iterable)path);
                                path = Lists.newArrayList(matrix.get(j-1, i),matrix.get(j, i));
                                Graphs.addPath(g, (Iterable)path);
                                path = Lists.newArrayList(matrix.get(j, i+1),matrix.get(j, i));
                                Graphs.addPath(g, (Iterable)path);
                            }else if (i == matrix.columnMap().size()-2 &&j ==1) {
                                path = Lists.newArrayList(matrix.get(j-1, i),matrix.get(j-1, i+1));
                                Graphs.addPath(g, (Iterable)path);
                                path = Lists.newArrayList(matrix.get(j, i),matrix.get(j-1, i));
                                Graphs.addPath(g, (Iterable)path);
                                path = Lists.newArrayList(matrix.get(j+1, i),matrix.get(j, i));
                                Graphs.addPath(g, (Iterable)path);
                                path = Lists.newArrayList(matrix.get(j, i-1),matrix.get(j, i));
                                Graphs.addPath(g, (Iterable)path);
                            }else if (i == matrix.columnMap().size()-2 &&j ==matrix.rowMap().size()-2) {
                                path = Lists.reverse(Lists.newArrayList(matrix.get(j+1, i),matrix.get(j+1, i+1)));
                                Graphs.addPath(g, (Iterable)path);
                                path = Lists.newArrayList(matrix.get(j+1, i),matrix.get(j, i));
                                Graphs.addPath(g, (Iterable)path);
                            }else if (i == matrix.columnMap().size()-2 && j>1 && j<matrix.rowMap().size()-1) {
//							path = Lists.newArrayList(matrix.get(j, i),matrix.get(j, i+1));
//							Graphs.addBiPath(g, (Iterable)path);
                                path = Lists.newArrayList(matrix.get(j+1, i),matrix.get(j, i));
                                Graphs.addPath(g, (Iterable)path);
                            }else if (j == matrix.rowMap().size()-2 && i>1 && i<matrix.columnMap().size()-1) {
                                path = Lists.newArrayList(matrix.get(j, i),matrix.get(j+1, i));
                                Graphs.addBiPath(g, (Iterable)path);
                                path = Lists.reverse(Lists.newArrayList(matrix.get(j, i),matrix.get(j, i+1)));
                                Graphs.addPath(g, (Iterable)path);
                            }else if (j == 1 && i>1 && i<matrix.columnMap().size()-1) {
                                path = Lists.newArrayList(matrix.get(j, i),matrix.get(j-1, i));
                                Graphs.addBiPath(g, (Iterable)path);
                                path = Lists.newArrayList(matrix.get(j, i-1),matrix.get(j, i));
                                Graphs.addPath(g, (Iterable)path);
                            }else if (i == 1 && j>1 && j<matrix.rowMap().size()-1){
//							path = Lists.newArrayList(matrix.get(j, i),matrix.get(j, i-1));
//							Graphs.addPath(g, (Iterable)path);
                                path = Lists.newArrayList(matrix.get(j-1, i),matrix.get(j, i));
                                Graphs.addPath(g, (Iterable)path);
                            }else
                            {
                            }
                        }
                        if(i>1 && i<matrix.columnMap().size()-2) {
                            if (i % 2 == 0) {
                                path = Lists.reverse(Lists.newArrayList(matrix.get(1, i),matrix.get(2, i),
                                        matrix.get(3, i),matrix.get(4, i),
                                        matrix.get(5, i),matrix.get(6, i),
                                        matrix.get(7, i),matrix.get(8, i)));
                            } else {
                                path = Lists.newArrayList(matrix.get(1, i),matrix.get(2, i),
                                        matrix.get(3, i),matrix.get(4, i),
                                        matrix.get(5, i),matrix.get(6, i),
                                        matrix.get(7, i),matrix.get(8, i));
                            }

                            Graphs.addPath(g, (Iterable)path);
                        }
                        if(i<1 || i>matrix.columnMap().size()-2) {
                            if (i % 2 == 0) {
                                path = Lists.reverse(Lists.newArrayList(matrix.get(1, i),matrix.get(2, i),
                                        matrix.get(3, i),matrix.get(4, i),
                                        matrix.get(5, i),matrix.get(6, i),
                                        matrix.get(7, i),matrix.get(8, i)));
                            } else {
                                path = Lists.newArrayList(matrix.get(1, i),matrix.get(2, i),
                                        matrix.get(3, i),matrix.get(4, i),
                                        matrix.get(5, i),matrix.get(6, i),
                                        matrix.get(7, i),matrix.get(8, i));
                            }

                            Graphs.addPath(g, (Iterable)path);
                        }

                    }

                }else if(version==2) {
                    for(int i = 0; i < matrix.columnMap().size(); i++) {
                        Object path ;

                        for(int j = 0; j < matrix.rowMap().size(); j++) {

                            if(j == 0 && i == 0){
                                path = Lists.newArrayList(matrix.get(j+1, i),matrix.get(j, i));
                                Graphs.addPath(g, (Iterable)path);
                            }else if (j == matrix.rowMap().size()-1 &&i == 0) {
                                path = Lists.newArrayList(matrix.get(j, i),matrix.get(j-1, i));
                                Graphs.addPath(g, (Iterable)path);
                            }else if (i == matrix.columnMap().size()-1 &&j ==0) {
                                path = Lists.newArrayList(matrix.get(j, i),matrix.get(j+1, i));
                                Graphs.addPath(g, (Iterable)path);
                            }else if (i == matrix.columnMap().size()-1 &&j ==matrix.rowMap().size()-1) {
                                path = Lists.newArrayList(matrix.get(j-1, i),matrix.get(j, i));
                                Graphs.addPath(g, (Iterable)path);
                            }else if (j == 1 &&i == 1) {
                                path = Lists.reverse(Lists.newArrayList(matrix.get(j-1, i),matrix.get(j-1, i-1)));
                                Graphs.addPath(g, (Iterable)path);
                                path = Lists.newArrayList(matrix.get(j-1, i),matrix.get(j, i));
                                Graphs.addPath(g, (Iterable)path);
                            }else if (j == matrix.rowMap().size()-2 &&i == 1) {
                                path = Lists.newArrayList(matrix.get(j+1, i),matrix.get(j+1, i-1));
                                Graphs.addPath(g, (Iterable)path);
                                path = Lists.newArrayList(matrix.get(j, i),matrix.get(j+1, i));
                                Graphs.addPath(g, (Iterable)path);
                                path = Lists.newArrayList(matrix.get(j-1, i),matrix.get(j, i),matrix.get(j, i+1));
                                Graphs.addBiPath(g, (Iterable)path);
                            }else if (i == matrix.columnMap().size()-2 &&j ==1) {
                                path = Lists.newArrayList(matrix.get(j-1, i),matrix.get(j-1, i+1));
                                Graphs.addPath(g, (Iterable)path);
                                path = Lists.newArrayList(matrix.get(j, i),matrix.get(j-1, i));
                                Graphs.addPath(g, (Iterable)path);
                                path = Lists.newArrayList(matrix.get(j+1, i),matrix.get(j, i),matrix.get(j, i-1));
                                Graphs.addBiPath(g, (Iterable)path);
                            }else if (i == matrix.columnMap().size()-2 &&j ==matrix.rowMap().size()-2) {
                                path = Lists.reverse(Lists.newArrayList(matrix.get(j+1, i),matrix.get(j+1, i+1)));
                                Graphs.addPath(g, (Iterable)path);
                                path = Lists.newArrayList(matrix.get(j+1, i),matrix.get(j, i));
                                Graphs.addPath(g, (Iterable)path);
                            }else if (i == matrix.columnMap().size()-2 && j>1 && j<matrix.rowMap().size()-1) {
//							path = Lists.newArrayList(matrix.get(j, i),matrix.get(j, i+1));
//							Graphs.addBiPath(g, (Iterable)path);
                                path = Lists.newArrayList(matrix.get(j, i),matrix.get(j+1, i));
                                Graphs.addBiPath(g, (Iterable)path);
                            }else if (j == matrix.rowMap().size()-2 && i>1 && i<matrix.columnMap().size()-1) {
                                path = Lists.newArrayList(matrix.get(j, i),matrix.get(j+1, i));
                                Graphs.addBiPath(g, (Iterable)path);
                                path = Lists.reverse(Lists.newArrayList(matrix.get(j, i),matrix.get(j, i+1)));
                                Graphs.addBiPath(g, (Iterable)path);
                            }else if (j == 1 && i>1 && i<matrix.columnMap().size()-1) {
                                path = Lists.newArrayList(matrix.get(j, i),matrix.get(j-1, i));
                                Graphs.addBiPath(g, (Iterable)path);
                                path = Lists.newArrayList(matrix.get(j, i),matrix.get(j, i-1));
                                Graphs.addBiPath(g, (Iterable)path);
                            }else if (i == 1 && j>1 && j<matrix.rowMap().size()-1){
//							path = Lists.newArrayList(matrix.get(j, i),matrix.get(j, i-1));
//							Graphs.addPath(g, (Iterable)path);
                                path = Lists.newArrayList(matrix.get(j, i),matrix.get(j-1, i));
                                Graphs.addBiPath(g, (Iterable)path);
                            }else
                            {
                            }
                        }
                        if(i>1 && i<matrix.columnMap().size()-2) {
                            if (i % 2 == 0) {
                                path = Lists.reverse(Lists.newArrayList(matrix.get(1, i),matrix.get(2, i),
                                        matrix.get(3, i),matrix.get(4, i),
                                        matrix.get(5, i),matrix.get(6, i),
                                        matrix.get(7, i),matrix.get(8, i)));
                            } else {
                                path = Lists.newArrayList(matrix.get(1, i),matrix.get(2, i),
                                        matrix.get(3, i),matrix.get(4, i),
                                        matrix.get(5, i),matrix.get(6, i),
                                        matrix.get(7, i),matrix.get(8, i));
                            }

                            Graphs.addPath(g, (Iterable)path);
                        }
                        if(i<1 || i>matrix.columnMap().size()-2) {
                            if (i % 2 == 0) {
                                path = Lists.reverse(Lists.newArrayList(matrix.get(1, i),matrix.get(2, i),
                                        matrix.get(3, i),matrix.get(4, i),
                                        matrix.get(5, i),matrix.get(6, i),
                                        matrix.get(7, i),matrix.get(8, i)));
                            } else {
                                path = Lists.newArrayList(matrix.get(1, i),matrix.get(2, i),
                                        matrix.get(3, i),matrix.get(4, i),
                                        matrix.get(5, i),matrix.get(6, i),
                                        matrix.get(7, i),matrix.get(8, i));
                            }

                            Graphs.addPath(g, (Iterable)path);
                        }

                    }




                }else if(version==3) {
                    for(int i = 0; i < matrix.columnMap().size(); i++) {
                        Object path ;

                        for(int j = 0; j < matrix.rowMap().size(); j++) {

                            if(j == 0 && i == 0){
                                path = Lists.newArrayList(matrix.get(j+1, i),matrix.get(j, i));
                                Graphs.addPath(g, (Iterable)path);
                            }else if (j == matrix.rowMap().size()-1 &&i == 0) {
                                path = Lists.newArrayList(matrix.get(j, i),matrix.get(j-1, i));
                                Graphs.addPath(g, (Iterable)path);
                            }else if (i == matrix.columnMap().size()-1 &&j ==0) {
                                path = Lists.newArrayList(matrix.get(j, i),matrix.get(j+1, i));
                                Graphs.addPath(g, (Iterable)path);
                            }else if (i == matrix.columnMap().size()-1 &&j ==matrix.rowMap().size()-1) {
                                path = Lists.newArrayList(matrix.get(j-1, i),matrix.get(j, i));
                                Graphs.addPath(g, (Iterable)path);
                            }else if (j == 1 &&i == 1) {
                                path = Lists.reverse(Lists.newArrayList(matrix.get(j-1, i),matrix.get(j-1, i-1)));
                                Graphs.addPath(g, (Iterable)path);
                                path = Lists.newArrayList(matrix.get(j-1, i),matrix.get(j, i));
                                Graphs.addPath(g, (Iterable)path);
                            }else if (j == matrix.rowMap().size()-2 &&i == 1) {
                                path = Lists.newArrayList(matrix.get(j+1, i),matrix.get(j+1, i-1));
                                Graphs.addPath(g, (Iterable)path);
                                path = Lists.newArrayList(matrix.get(j, i),matrix.get(j+1, i));
                                Graphs.addPath(g, (Iterable)path);
                                path = Lists.newArrayList(matrix.get(j-1, i),matrix.get(j, i),matrix.get(j, i+1));
                                Graphs.addBiPath(g, (Iterable)path);
                            }else if (i == matrix.columnMap().size()-2 &&j ==1) {
                                path = Lists.newArrayList(matrix.get(j-1, i),matrix.get(j-1, i+1));
                                Graphs.addPath(g, (Iterable)path);
                                path = Lists.newArrayList(matrix.get(j, i),matrix.get(j-1, i));
                                Graphs.addPath(g, (Iterable)path);
                                path = Lists.newArrayList(matrix.get(j+1, i),matrix.get(j, i),matrix.get(j, i-1));
                                Graphs.addBiPath(g, (Iterable)path);
                            }else if (i == matrix.columnMap().size()-2 &&j ==matrix.rowMap().size()-2) {
                                path = Lists.reverse(Lists.newArrayList(matrix.get(j+1, i),matrix.get(j+1, i+1)));
                                Graphs.addPath(g, (Iterable)path);
                                path = Lists.newArrayList(matrix.get(j+1, i),matrix.get(j, i));
                                Graphs.addPath(g, (Iterable)path);
                            }else if (i == matrix.columnMap().size()-2 && j>1 && j<matrix.rowMap().size()-1) {
//							path = Lists.newArrayList(matrix.get(j, i),matrix.get(j, i+1));
//							Graphs.addBiPath(g, (Iterable)path);
                                path = Lists.newArrayList(matrix.get(j, i),matrix.get(j+1, i));
                                Graphs.addBiPath(g, (Iterable)path);
                            }else if (j == matrix.rowMap().size()-2 && i>1 && i<matrix.columnMap().size()-1) {
                                path = Lists.newArrayList(matrix.get(j, i),matrix.get(j+1, i));
                                Graphs.addBiPath(g, (Iterable)path);
                                path = Lists.reverse(Lists.newArrayList(matrix.get(j, i),matrix.get(j, i+1)));
                                Graphs.addBiPath(g, (Iterable)path);
                            }else if (j == 1 && i>1 && i<matrix.columnMap().size()-1) {
                                path = Lists.newArrayList(matrix.get(j, i),matrix.get(j-1, i));
                                Graphs.addBiPath(g, (Iterable)path);
                                path = Lists.newArrayList(matrix.get(j, i),matrix.get(j, i-1));
                                Graphs.addBiPath(g, (Iterable)path);
                            }else if (i == 1 && j>1 && j<matrix.rowMap().size()-1){
//							path = Lists.newArrayList(matrix.get(j, i),matrix.get(j, i-1));
//							Graphs.addPath(g, (Iterable)path);
                                path = Lists.newArrayList(matrix.get(j, i),matrix.get(j-1, i));
                                Graphs.addBiPath(g, (Iterable)path);
                            }else
                            {
                            }
                        }
                        if(i>1 && i<matrix.columnMap().size()-2) {
                            if (i % 2 == 0) {
                                path = Lists.reverse(Lists.newArrayList(matrix.get(1, i),matrix.get(2, i),
                                        matrix.get(3, i),matrix.get(4, i),
                                        matrix.get(5, i),matrix.get(6, i),
                                        matrix.get(7, i),matrix.get(8, i)));
                            } else {
                                path = Lists.newArrayList(matrix.get(1, i),matrix.get(2, i),
                                        matrix.get(3, i),matrix.get(4, i),
                                        matrix.get(5, i),matrix.get(6, i),
                                        matrix.get(7, i),matrix.get(8, i));
                            }

                            Graphs.addBiPath(g, (Iterable)path);
                        }
                        if(i<1 || i>matrix.columnMap().size()-2) {
                            if (i % 2 == 0) {
                                path = Lists.reverse(Lists.newArrayList(matrix.get(1, i),matrix.get(2, i),
                                        matrix.get(3, i),matrix.get(4, i),
                                        matrix.get(5, i),matrix.get(6, i),
                                        matrix.get(7, i),matrix.get(8, i)));
                            } else {
                                path = Lists.newArrayList(matrix.get(1, i),matrix.get(2, i),
                                        matrix.get(3, i),matrix.get(4, i),
                                        matrix.get(5, i),matrix.get(6, i),
                                        matrix.get(7, i),matrix.get(8, i));
                            }

                            Graphs.addPath(g, (Iterable)path);
                        }

                    }
                }
                for(Point p : g.getNodes()){
                    agents.add(new InfrastructureAgent(p));
                }

                return new ListenableGraph<LengthData>(g);
            }


        }

    }


