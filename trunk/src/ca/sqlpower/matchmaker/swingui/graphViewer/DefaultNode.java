package ca.sqlpower.matchmaker.swingui.graphViewer;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.layout.LayoutEdge;
import ca.sqlpower.matchmaker.swingui.graphViewer.event.GraphComponentEventSupport;
import ca.sqlpower.matchmaker.swingui.graphViewer.event.GraphComponentListener;

public class DefaultNode implements Node {

	private static final Logger logger = Logger.getLogger(DefaultNode.class);
	private GraphComponentEventSupport eventSupport;
	private List<Diedge> edges;
	private String label;
	private EdgeFactory edgeFactory;

	public DefaultNode(String label, EdgeFactory edgeFactory){
		this.label = label;
		edges = new ArrayList<Diedge>();
		eventSupport = new GraphComponentEventSupport(this);
		this.edgeFactory = edgeFactory;
	}		

	
	public void addAdjacentEdge(Diedge edge) {
		if (!edges.contains(edge)){
			edges.add(edge);
			eventSupport.fireNewEdgeAddedToNode(edge);
		}
	}

	public void addAdjacentNode(Node node) {
		Diedge edge = edgeFactory.createEdge(node, this);
		edges.add(edge);
		node.addAdjacentEdge(edge);
		eventSupport.fireNewEdgeAddedToNode(edge);
	}

	public List<Diedge> getAdjacentEdges() {
		return edges;
	}

	public List<Node> getAdjacentNodes() {
		List<Node> adjacentNodes = new ArrayList<Node>();
        for (Diedge e : edges){
            if (e.getHeadNode() == this){
                adjacentNodes.add(e.getTailNode());
            } else if (e.getTailNode() == this) {
                adjacentNodes.add(e.getHeadNode());
            } else {
                throw new IllegalStateException("One of the adjacent edge is not attached to the node");
            }
        }
        return adjacentNodes;
	}

	public boolean isAdjacent(Node node) {
		// TODO Auto-generated method stub
		logger.debug("Stub call: DefaultNode.isAdjacent()");
		return false;
	}

	public Rectangle getBounds(Rectangle b) {
		// TODO Auto-generated method stub
		logger.debug("Stub call: DefaultNode.getBounds()");
		return null;
	}

	public Rectangle getBounds() {
		// TODO Auto-generated method stub
		logger.debug("Stub call: DefaultNode.getBounds()");
		return null;
	}

	public int getHeight() {
		// TODO Auto-generated method stub
		logger.debug("Stub call: DefaultNode.getHeight()");
		return 0;
	}

	public List<LayoutEdge> getInboundEdges() {
		// TODO Auto-generated method stub
		logger.debug("Stub call: DefaultNode.getInboundEdges()");
		return null;
	}

	public Point getLocation() {
		// TODO Auto-generated method stub
		logger.debug("Stub call: DefaultNode.getLocation()");
		return null;
	}

	public String getNodeName() {
		// TODO Auto-generated method stub
		logger.debug("Stub call: DefaultNode.getNodeName()");
		return null;
	}

	public List<LayoutEdge> getOutboundEdges() {
		// TODO Auto-generated method stub
		logger.debug("Stub call: DefaultNode.getOutboundEdges()");
		return null;
	}

	public int getWidth() {
		// TODO Auto-generated method stub
		logger.debug("Stub call: DefaultNode.getWidth()");
		return 0;
	}

	public int getX() {
		// TODO Auto-generated method stub
		logger.debug("Stub call: DefaultNode.getX()");
		return 0;
	}

	public int getY() {
		// TODO Auto-generated method stub
		logger.debug("Stub call: DefaultNode.getY()");
		return 0;
	}

	public void setBounds(int x, int i, int width, int height) {
		// TODO Auto-generated method stub
		logger.debug("Stub call: DefaultNode.setBounds()");

	}

	public void setLocation(int i, int j) {
		// TODO Auto-generated method stub
		logger.debug("Stub call: DefaultNode.setLocation()");

	}

	public void setLocation(Point pos) {
		// TODO Auto-generated method stub
		logger.debug("Stub call: DefaultNode.setLocation()");

	}

	public void addGraphComponentListener(GraphComponentListener l) {
		// TODO Auto-generated method stub
		logger.debug("Stub call: DefaultNode.addGraphComponentListener()");

	}

	public String getLabel() {
		// TODO Auto-generated method stub
		logger.debug("Stub call: DefaultNode.getLabel()");
		return null;
	}

	public String getToolTip() {
		// TODO Auto-generated method stub
		logger.debug("Stub call: DefaultNode.getToolTip()");
		return null;
	}

	public void paint(Graphics g, double zoom) {
		// TODO Auto-generated method stub
		logger.debug("Stub call: DefaultNode.paint()");

	}

	public void removeGraphComponentListener(GraphComponentListener l) {
		// TODO Auto-generated method stub
		logger.debug("Stub call: DefaultNode.removeGraphComponentListener()");

	}

}
