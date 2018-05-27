package project;

import com.github.rinde.rinsim.core.SimulatorAPI;
import com.github.rinde.rinsim.geom.Connection;
import com.github.rinde.rinsim.geom.ConnectionData;
import com.github.rinde.rinsim.geom.Graph;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.reverse;

public class GraphHelper {
    public static <E extends ConnectionData> void addPath(Graph<E> graph,
                                                          Iterable<Point> path, Class<E> classToSpawn) {
        final PeekingIterator<Point> it =
                Iterators.peekingIterator(path.iterator());
        while (it.hasNext()) {
            final Point n = it.next();
            if (it.hasNext()) {
                try{

                    Connection<E> connection = Connection.create(n,it.peek(), classToSpawn.newInstance());
                    graph.addConnection(connection);
                }catch(InstantiationException| IllegalAccessException e){
                    System.out.println("Uh oh");
                }
            }
        }
    }
    public static <E extends ConnectionData> void addBiPath(Graph<E> graph,
                                                            Iterable<Point> path, Class<E> classToSpawn) {
        addPath(graph, path, classToSpawn);
        addPath(graph, reverse(newArrayList(path)), classToSpawn);
    }

}

