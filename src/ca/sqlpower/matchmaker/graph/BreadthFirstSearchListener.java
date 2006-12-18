package ca.sqlpower.matchmaker.graph;

/**
 * Listener interface for clients of the BreadthFirstSearch.  Allows
 * client code to take action during specific phases of the search.
 *
 * @param <V> The type of vertices in the graph being searched
 */
public interface BreadthFirstSearchListener<V> {

    /**
     * Called when a node is first discovered by the search.
     * 
     * @param node The node that was discovered
     */
    void nodeDiscovered(V node);
}
