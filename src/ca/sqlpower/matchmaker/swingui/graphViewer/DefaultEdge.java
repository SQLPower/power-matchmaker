package ca.sqlpower.matchmaker.swingui.graphViewer;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.swingui.graphViewer.event.GraphComponentListener;

public class DefaultEdge implements Diedge {
	private final static Logger logger = Logger.getLogger(DefaultEdge.class);
	private Node headNode;
	private Node tailNode;

	public DefaultEdge(Node head,Node tail){
		headNode = head;
		tailNode = tail;
	}
	
	public void cut() {
		headNode.getAdjacentEdges().remove(this);
		tailNode.getAdjacentEdges().remove(this);
	}

	public void addGraphComponentListener(GraphComponentListener l) {
		// TODO Auto-generated method stub
		logger.debug("Stub call: DefaultEdge.addGraphComponentListener()");

	}

	public Rectangle getBounds(Rectangle b) {
		return null;
	}

	public Rectangle getBounds() {
		Rectangle rect = new Rectangle();
		return getBounds(rect);
	}

	public int getHeight() {
		// TODO Auto-generated method stub
		logger.debug("Stub call: DefaultEdge.getHeight()");
		return 0;
	}

	public String getLabel() {
		// TODO Auto-generated method stub
		logger.debug("Stub call: DefaultEdge.getLabel()");
		return null;
	}

	public Point getLocation() {
		// TODO Auto-generated method stub
		logger.debug("Stub call: DefaultEdge.getLocation()");
		return null;
	}

	public String getToolTip() {
		// TODO Auto-generated method stub
		logger.debug("Stub call: DefaultEdge.getToolTip()");
		return null;
	}

	public int getWidth() {
		// TODO Auto-generated method stub
		logger.debug("Stub call: DefaultEdge.getWidth()");
		return 0;
	}

	public int getX() {
		// TODO Auto-generated method stub
		logger.debug("Stub call: DefaultEdge.getX()");
		return 0;
	}

	public int getY() {
		// TODO Auto-generated method stub
		logger.debug("Stub call: DefaultEdge.getY()");
		return 0;
	}

	public void paint(Graphics g, double zoom) {
		// TODO Auto-generated method stub
		logger.debug("Stub call: DefaultEdge.paint()");

	}

	public void removeGraphComponentListener(GraphComponentListener l) {
		// TODO Auto-generated method stub
		logger.debug("Stub call: DefaultEdge.removeGraphComponentListener()");

	}
	
	public Node getHeadNode() {
		return headNode;
	}

	public void setHeadNode(Node headNode) {
		this.headNode = headNode;
	}

	public Node getTailNode() {
		return tailNode;
	}

	public void setTailNode(Node tailNode) {
		this.tailNode = tailNode;
	}

	public void swapDirection() {
		// TODO Auto-generated method stub
		logger.debug("Stub call: DefaultEdge.swapDirection()");
		
	}

	public void setBounds(int x, int i, int width, int height) {
		// TODO Auto-generated method stub
		logger.debug("Stub call: DefaultEdge.setBounds()");
		
	}

	public void setLocation(int i, int j) {
		// TODO Auto-generated method stub
		logger.debug("Stub call: DefaultEdge.setLocation()");
		
	}


}
