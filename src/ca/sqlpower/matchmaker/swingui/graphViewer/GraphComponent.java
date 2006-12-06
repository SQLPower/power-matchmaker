package ca.sqlpower.matchmaker.swingui.graphViewer;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import ca.sqlpower.matchmaker.swingui.graphViewer.event.GraphComponentListener;

public interface GraphComponent {
	String getLabel();
	String getToolTip();
	
	void paint(Graphics g, double zoom);
	
    int getX();
    int getY();
    int getWidth();
    int getHeight();
    Rectangle getBounds(Rectangle b);
    Rectangle getBounds();
    void setBounds(int x, int i, int width, int height);
    Point getLocation();
    void setLocation(int i, int j);
    
    void addGraphComponentListener(GraphComponentListener l);
    void removeGraphComponentListener(GraphComponentListener l);
}
