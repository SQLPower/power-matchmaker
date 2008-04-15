/*
 * Copyright (c) 2007, SQL Power Group Inc.
 *
 * This file is part of Power*MatchMaker.
 *
 * Power*MatchMaker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*MatchMaker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

/**
 * 
 */
package ca.sqlpower.matchmaker.swingui.munge;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.CubicCurve2D;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.munge.MungeStepOutput;

/**
 * A class representing the connecting lines in the munge pen. These lines
 * are components, and are stored as children of the munge pen alongside the
 * various munge components.
 */
public class IOConnector extends JComponent implements MouseListener {
	
	private static  final Logger logger = Logger.getLogger(IOConnector.class); 
	
    
    /**
     * The distance from the connection point where the control point for
     * the connector line should be.  For outputs, the control will be this
     * many pixels directly below the connection point; for inputs, it will
     * be this many pixels above the connection point.  Larger values for this
     * parameter make the connectors and wires look more rigid.
     */
    public static final int RIGIDITY = 100;
    
	AbstractMungeComponent parentCom;
	AbstractMungeComponent childCom;
	int parentNumber;
	int childNumber;
	
	
	private Point top;
	private Point bottom;
	
	private Stroke nonSelectedStroke = new BasicStroke(2);
	private Stroke selectedStroke = new BasicStroke(4);
	
	private boolean recentlyResized;
	
    /**
     * The number of pixels around a click that are tested to see if they intersect
     * this IOConnector's path.  A larger number allows for sloppier clicking; smaller
     * numbers require more accurate clicking.
     * 
     * @see #contains(Point)
     */
    private int clickRadius = 3;
    
	/**
	 * The amount of space to add as padding around the line that is to drawn.
	 */
	private int boundsPadding = RIGIDITY;
	
	/**
	 * The amount of downward shift to apply to the line where it meets the plug handle. 
	 */
	private int plugOffset = 2;
	
	/**
	 * Creates a new IOConnector that represents a connection line in the mungePen
	 *  
	 * @param parent The AbstractMungeComponent where the output is from
	 * @param parentNum The number of the IOC in the parent
	 * @param child The AbstractMungeComponent where the inputs goes 
	 * @param childNum The number of the IOC in the child
	 */
	public IOConnector(AbstractMungeComponent parent, int parentNum, AbstractMungeComponent child, int childNum) {
		this.parentCom = parent;
		this.childCom = child;
		parentNumber = parentNum;
		childNumber = childNum;
		
		setOpaque(false);
		
		setFocusable(true);
		
		addFocusListener(new FocusListener(){
			public void focusGained(FocusEvent e) {
				//a problem it the line was removed and this IOC has not been told that yet.
				if (getPen() != null) {
					logger.debug("Gained focus");
					requestFocusInWindow();
					getParent().repaint();
				}
			}
			
			public void focusLost(FocusEvent e) {
				if (getPen() != null) {
					logger.debug("lost focus");
					getParent().repaint();
				}
			}
		});
		
		addKeyListener(new KeyAdapter(){
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					remove();
				}
			}
		});
		
		recentlyResized = false;
		setBounds();
	}

	public MungePen getPen() {
		return (MungePen)getParent();
	}
	
    /**
     * Checks, with a certain degree of fuzziness, if the given point intersects
     * the visible line of this IOConnector.  The amount of fuzziness is controlled
     * by the {@link #clickRadius} property.
     * 
     * @param p The point to test, in the parent component's coordinate space
     */
	public boolean clicked(Point p) {
        Point p1 = parentCom.getOutputPosition(parentNumber);
        p1.translate(parentCom.getX(), parentCom.getY());
        Point p2 = childCom.getInputPosition(childNumber);
        p2.translate(childCom.getX(), childCom.getY());
        CubicCurve2D c = createConnectorPath(p1.x, p1.y, p2.x, p2.y);
        return c.intersects(p.x - clickRadius, p.y - clickRadius, clickRadius * 2, clickRadius * 2);
	}

	/**
	 * Returns the popup menu for the currrent IOConnector
	 * 
	 * @return The popup
	 */
	public JPopupMenu getPopup() {
		JPopupMenu pop = new JPopupMenu();
		JMenuItem remove = new JMenuItem(new AbstractAction(){
			public void actionPerformed(ActionEvent e) {
				remove();
			}
		});
		remove.setText("Delete (del)");
		pop.add(remove);
		return pop;
	}
	
	/**
	 * Checks equality based on the parents children and parent and child number
	 */
	public boolean equals(Object obj) {
		if (obj instanceof IOConnector) {
			IOConnector ioc = (IOConnector) obj;
			return ioc.parentCom == parentCom && ioc.childCom == childCom && ioc.parentNumber == parentNumber && ioc.childNumber == childNumber;
		}
		return false;
	}
	
	/**
	 * Passes a key event to the IOC, this is only passed if this
	 * IOC is selected.
	 * 
	 * @param e The event
	 */
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_DELETE) {
			remove();
		}
	}
	
	public void setBounds() {

		if (recentlyResized) {
			return;
		}
		
		recentlyResized = true;
		
		bottom = childCom.getInputPosition(childNumber);
		bottom.translate(childCom.getX(), childCom.getY());

        top = parentCom.getOutputPosition(parentNumber);
		top.translate(parentCom.getX(), parentCom.getY());
		
		int width;
		int height;
		int x;
		int y;
		
		width = Math.abs(bottom.x - top.x);
		height = Math.abs(bottom.y - top.y);
		
		
		if (top.getY() < bottom.getY()) {
			if (top.getX() < bottom.getX()) {
				x = top.x;
				y = top.y;
				bottom = new Point(width,height);
				top = new Point(0,0);
			} else {
				x = bottom.x;
				y = top.y;
				bottom = new Point(0, height);
				top = new Point(width, 0);
			} 
		} else {
			if (top.getX() < bottom.getX()) {
				x = top.x;
				y = bottom.y;
				bottom = new Point(width, 0);
				top = new Point(0, height);
			} else {
				x = bottom.x;
				y = bottom.y;
				bottom = new Point(0,0);
				top = new Point(width,height);
			} 
		}
		
		width += boundsPadding;
		height += boundsPadding;
		x -= boundsPadding/2;
		y -= boundsPadding/2;

		top.x += boundsPadding/2;
		top.y += boundsPadding/2;
		bottom.x += boundsPadding/2;
		bottom.y += boundsPadding/2 + plugOffset;
		
		
		setBounds(x,y,width+1,height+1);
	}
	

	
	@Override
	public int getWidth() {
		setBounds();
		return super.getWidth();
	}
	
	@Override
	public int getHeight() {
		setBounds();
		return super.getHeight();
	}
	
	@Override
	public int getX() {
		setBounds();
		return super.getX();
	}
	
	@Override
	public int getY() {
		setBounds();
		return super.getY();
	}


	
	/**
	 * paints this IOC as a line on the specified Graphics
	 * 
	 * @param g The place to paint
	 */
	public void paintComponent(Graphics g) {
		recentlyResized = false;
		
		Graphics2D g2 = (Graphics2D) g.create();
		
		if (hasFocus()) {
			g2.setStroke(selectedStroke);
		} else {
			g2.setStroke(nonSelectedStroke);
		}
		
		
		MungeStepOutput link = childCom.getInputs().get(childNumber);
		
		//If it is drawing in the time period between the child removed 
		//and the event being finished,
		if (link != null) {
			g2.setColor(ConnectorIcon.getColor(link.getType()));
            CubicCurve2D c = createConnectorPath((int)top.getX(), (int)top.getY(), (int)bottom.getX(), (int)bottom.getY());
			g2.draw(c);
		} else {
			logger.debug("Error line not existant");
		}
		
		if (logger.isDebugEnabled()) {
			g.setColor(Color.RED);
			g.drawRect(0, 0, getWidth() -1, getHeight()-1);
			g.drawLine(top.x, top.y, top.x, top.y);
			g.drawLine(bottom.x, bottom.y, bottom.x, bottom.y);
			g.setColor(Color.BLACK);
		}
		
	}
	
	public static CubicCurve2D createConnectorPath(int x1, int y1, int x2, int y2) {
        int rigidity = RIGIDITY;
        if (Math.abs(y2 - y1) < RIGIDITY) {
            rigidity = Math.abs(y2 - y1);
        }
        return new CubicCurve2D.Double(
                x1, y1,
                x1, y1 + rigidity,
                x2, y2 - rigidity,
                x2, y2);
    }

    /**
	 * deletes this IOC and the link in the mungeSteps 
	 */
	public void remove() {
		getParent().removeMouseListener(this);
		MungePen.removeAllListeners(this);
		childCom.getStep().disconnectInput(childNumber);
	}


	public AbstractMungeComponent getParentCom() {
		return parentCom;
	}

	public AbstractMungeComponent getChildCom() {
		return childCom;
	}


	public int getParentNumber() {
		return parentNumber;
	}

	public int getChildNumber() {
		return childNumber;
	}

	//mouseListener Stuff, each IOC is a mouse listener of
	//the MungePen to allow it to properly handle the overlap
	
	public void mouseClicked(MouseEvent e) {
		if (clicked(e.getPoint())) {
			maybeShowPopup(e);
		}
	}

	public void mouseEntered(MouseEvent e) {
	}

	public void mouseExited(MouseEvent e) {
		
	}

	public void mousePressed(MouseEvent e) {
		logger.debug(e.getPoint());
		if (clicked(e.getPoint())) {
			requestFocusInWindow();
			maybeShowPopup(e);
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (clicked(e.getPoint())) {
			maybeShowPopup(e);
		}
	}
	
	public void maybeShowPopup(MouseEvent e) {
		if (e.isPopupTrigger()) {
			logger.debug("ShowPopop");
			getPopup().show(getParent(), e.getX(), e.getY());
			requestFocusInWindow();
		}
	}
}
