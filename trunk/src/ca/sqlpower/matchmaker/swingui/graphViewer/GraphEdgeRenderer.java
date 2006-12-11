package ca.sqlpower.matchmaker.swingui.graphViewer;

import javax.swing.JComponent;

public interface GraphEdgeRenderer<E> {

    /**
     * Returns a JComponent instance that can be used to render the
     * given edge in the graph.
     * <p>
     * We will add more parameters to this method in the future to deal with
     * stuff like selection, focus, etc.
     * 
     * @param edge The edge to render.
     * @return a JComponent that can be used to paint the edge.
     */
    public JComponent getGraphEdgeRendererComponent(E edge);
    
}
