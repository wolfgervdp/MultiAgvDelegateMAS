package project;

import com.github.rinde.rinsim.geom.Connection;
import com.github.rinde.rinsim.geom.ConnectionData;
import com.github.rinde.rinsim.geom.Graph;
import com.github.rinde.rinsim.geom.Point;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;

public class GraphHelper {
    public static <E extends ConnectionData> void addPath(Graph<E> graph,
                                                          Iterable<Point> path, E data) {
        final PeekingIterator<Point> it =
                Iterators.peekingIterator(path.iterator());
        while (it.hasNext()) {
            final Point n = it.next();
            if (it.hasNext()) {
                Connection<E> connection = Connection.create(n,it.peek(), data);
                graph.addConnection(connection);
            }
        }
    }
}
