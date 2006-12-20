package ca.sqlpower.matchmaker.swingui.graphViewer;

import javax.swing.JComponent;


/**
 * 
 *
 * @param <V> the node type 
 */
public interface GraphNodeRenderer<V> {

    /**
     * Returns a JComponent that can be used to render the given node.
     * 
     * @param node
     * @return
     */
    public JComponent getGraphNodeRendererComponent(V node, boolean isSelected, boolean hasFocus);
}
