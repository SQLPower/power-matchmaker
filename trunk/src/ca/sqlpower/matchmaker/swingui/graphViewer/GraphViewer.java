package ca.sqlpower.matchmaker.swingui.graphViewer;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.Scrollable;

import org.apache.log4j.Logger;

public class GraphViewer extends JPanel implements Scrollable {
	private static final int SCROLL_UNIT = 50;

	private static final Logger logger = Logger.getLogger(GraphViewer.class);
	
	/**
	 * The visual magnification factor for this graph.
	 */
	private double zoom;

	private Digraph graph;
	
	@Override
	public void paint(Graphics g) {
		Rectangle r = graph.getBounds();
		graph.paint(g.create(r.x,r.y,r.width,r.height),zoom);
	}

	@Override
	public Dimension getPreferredSize() {
		Rectangle g = graph.getBounds();
		Dimension d = new Dimension((int)(g.height*zoom), (int)(g.width*zoom));
		return d;
	}
	
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		return SCROLL_UNIT;
	}

	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

	public boolean getScrollableTracksViewportWidth() {
		return false;
	}

	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		return SCROLL_UNIT;
	}

	public Digraph getGraph() {
		return graph;
	}

	public void setGraph(Digraph graph) {
		this.graph = graph;
	}

	public double getZoom() {
		return zoom;
	}

	public void setZoom(double zoom) {
		this.zoom = zoom;
	}

}
