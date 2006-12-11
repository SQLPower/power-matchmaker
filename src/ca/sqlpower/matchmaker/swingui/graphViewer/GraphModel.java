package ca.sqlpower.matchmaker.swingui.graphViewer;

import java.util.Collection;

/**
 * Provides the model of a graph's topology.  Does not deal with positioning,
 * layout, selection, rendering, or any other visual aspect of the graph.
 *
 * @param <V> The node (vertex) type
 * @param <E> The edge type
 */
public interface GraphModel<V, E> {

    Collection<E> getEdges();
    Collection<V> getNodes();
    Collection<V> getAdjacentNodes(V node);
    Collection<E> getInboundEdges(V node);
    Collection<E> getOutboundEdges(V node);
}
