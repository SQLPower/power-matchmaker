package ca.sqlpower.matchmaker.swingui.graphViewer;

/**
 * Interface for clients of the GraphViewer which are interested in taking
 * action when nodes and egdes are selected and deselected.
 *
 * @param <V> The type of node managed by the graph
 * @param <E> The type of edge managed by the graph
 */
public interface GraphSelectionListener<V, E> {
    
    /**
     * Called to notify the listener that a node which was selected is not
     * selected any more.
     */
    void nodeDeselected(V node);

    /**
     * Called to notify the listener that a node which was not selected is
     * now selected.
     */
    void nodeSelected(V node);
    
    // TODO edge support
}
