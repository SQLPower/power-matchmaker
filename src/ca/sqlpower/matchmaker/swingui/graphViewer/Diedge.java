package ca.sqlpower.matchmaker.swingui.graphViewer;

import ca.sqlpower.architect.layout.LayoutEdge;

public interface Diedge extends LayoutEdge, GraphComponent {
	
	Node getHeadNode();
	Node getTailNode();
	
	void setHeadNode(Node node);
	void setTailNode(Node node);
	/**
	 *  Set the old head as the tail and visa versa
	 */
	void swapDirection();
	/**
	 * remove this edge from both endpoints
	 */
	void cut();
}
