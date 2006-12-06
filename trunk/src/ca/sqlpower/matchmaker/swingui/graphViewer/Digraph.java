package ca.sqlpower.matchmaker.swingui.graphViewer;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
/**
 * A digraph represents a single possibly disconnected digraph.
 * All subelements should be placed relative to the digraph object's origin
 */
public interface Digraph {
	/** Add a node to the graph */
	public void addNode(Node node);
	
	/** Removes a node to the graph, and all adjacent edges */
	public void removeNode(Node node);
	
	/** get a list of all the nodes */
	public List<Node> getNodes();

	/** Add an edge between to the graph. 
	 * Both endpoints of the edge must be connected to nodes.  
	 * But these endpoints can be the same node.
	 */
	public void addEdge(Diedge edge);
	
	/**
	 * Removes an edge from the graph, its adjacent nodes stay
	 */
	public void removeEdge(Diedge edge);
	
	/** Gets a list of all the nodes. */
	public List<Diedge> getEdges();
	
	/** Gets the total area of the graph */
	public Rectangle getBounds();
	
	/**
	 *  Set the origin of the graph
	 */
	public void setLocation(Point p);
	
	/**
	 * get the component that sits at point p in the parent's
	 * origin.
	 */
	public GraphComponent getGraphComponentAt(Point p);
	
	/** Draws this graph */
	void paint(Graphics g, double zoom);

	public Point getLocation();
}
