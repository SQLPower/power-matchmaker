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
import java.awt.geom.Line2D;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.munge.MungeStepOutput;

/**
 *Used to store a line between the IOConnections.
 *This does not draw the line but holds its position.
 *Each IOC is recreated every time the munge pen is redrawn 
 */
public class IOConnector extends JComponent implements MouseListener {
	
	private static  final Logger logger = Logger.getLogger(IOConnector.class); 
	
	AbstractMungeComponent parentCom;
	AbstractMungeComponent childCom;
	int parentNumber;
	int childNumber;
	
	
	private Point top;
	private Point bottom;
	
	private Stroke nonSelectedStroke = new BasicStroke(2);
	private Stroke selectedStroke = new BasicStroke(3);
	
	private boolean recentlyResized;
	
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
	 * Checks if the current line has been clicked. 
	 * 
	 * @param click The point where the click was directed
	 * @return will return true iff the click was within 6 pixles of the
	 * click
	 */
	public boolean clicked(Point click) {
		Point p1 = parentCom.getOutputPosition(parentNumber);
		p1.translate(parentCom.getX(), parentCom.getY());
		Point p2 = childCom.getInputPosition(childNumber);
		p2.translate(childCom.getX(), childCom.getY());
		double dist = Line2D.ptSegDist(p1.x,p1.y,p2.x,p2.y,click.x,click.y);
		return dist < 6;
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
		top = parentCom.getOutputPosition(parentNumber);
		top.translate(parentCom.getX(), parentCom.getY());
		bottom.translate(childCom.getX(), childCom.getY());
		
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
				bottom = new Point(width,height);
				top = new Point(0,0);
			} 
		}
		
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
			g2.drawLine((int)top.getX(), (int)top.getY(), (int)bottom.getX(), (int)bottom.getY());
		} else {
			logger.debug("Error line not existant");
		}
		
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
