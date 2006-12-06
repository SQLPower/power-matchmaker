package ca.sqlpower.matchmaker.swingui.graphViewer;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class DefaultDigraph implements Digraph {
	List<Diedge> edges;
	List<Node>  nodes;
	Point origin;

	public DefaultDigraph() {
		edges = new ArrayList<Diedge>();
		nodes = new ArrayList<Node>();
		origin = new Point();
	}
	
	public void addEdge(Diedge edge) {
		edges.add(edge);
	}

	public void addNode(Node node) {
		nodes.add(node);
	}

	public Rectangle getBounds() {
		Rectangle rect = new Rectangle();
		rect.x = origin.x;
		rect.y = origin.y;
		for(Node v:nodes){
			rect.height = Math.min(rect.width,v.getWidth()+v.getX() );
			rect.height = Math.min(rect.height,v.getHeight()+v.getY() );
		}
		return rect;
	}

	public List<Diedge> getEdges() {
		return edges;
	}

	public List<Node> getNodes() {
		return nodes;
	}

	public void paint(Graphics g, double zoom) {
		// Paint the edges below the nodes
		for(Diedge e: edges){
			e.paint(g, zoom);
		}
		// Paint the nodes on top
		for(Node v: nodes){
			v.paint(g, zoom);
		}
	}

	public void removeEdge(Diedge edge) {
		edges.remove(edge);
	}

	public void removeNode(Node node) {
		List<Diedge> adjacentEdges = node.getAdjacentEdges();
		edges.removeAll(adjacentEdges);
		nodes.remove(node);
		while(node.getAdjacentEdges().size() >0){
			node.getAdjacentEdges().get(0).cut();
		}
	}
	
	public void setLocation(Point p) {
		origin = new Point(p);
	}

	public Point getLocation() {
		return origin;
	}

	public GraphComponent getGraphComponentAt(Point p) {
		Point localPoint = new Point(p);
		localPoint.translate(-origin.x, -origin.y);
		if (localPoint.x <0 || localPoint.y < 0){
			return null;
		}
		for (Node v :nodes){
			if (v.getBounds().contains(p)){
				return v;
			}
		}
		for (Diedge e :edges){
			if (e.getBounds().contains(p)){
				return e;
			}
		}
		return null;
	}

}
