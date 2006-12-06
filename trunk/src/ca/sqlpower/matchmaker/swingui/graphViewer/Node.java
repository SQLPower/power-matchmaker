package ca.sqlpower.matchmaker.swingui.graphViewer;

import java.util.List;

import ca.sqlpower.architect.layout.LayoutNode;

/** 
 * A generic node object
 *
 */
public interface Node extends LayoutNode, GraphComponent {


	
	List<Node> getAdjacentNodes();
	List<Diedge> getAdjacentEdges();
	
	/** 
	 * Add an edge to this node the edge must have 
	 * one endpoint being this node
	 */
	void addAdjacentEdge(Diedge edge);
	
	/**
	 * Creates an edge with this node as the tail and
	 * the node passed in
	 */
	void addAdjacentNode(Node node);
	
	/**
	 * Returns true if the Node node shares an edge
	 * with this node
	 */
	boolean isAdjacent(Node node);
}
