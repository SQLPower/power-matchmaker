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
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Line2D;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import ca.sqlpower.matchmaker.munge.MungeStepOutput;

/**
 *Used to store a line between the IOConnections.
 *This does not draw the line but holds its position.
 *Each IOC is recreated every time the munge pen is redrawn 
 */
public class IOConnector {
	AbstractMungeComponent parent;
	AbstractMungeComponent child;
	int parentNumber;
	int childNumber;
	
	boolean selected;
	
	/**
	 * Creates a new IOConnector that represents a connection line in the mungePen
	 *  
	 * @param parent The AbstractMungeComponent where the output is from
	 * @param parentNum The number of the IOC in the parent
	 * @param child The AbstractMungeComponent where the inputs goes 
	 * @param childNum The number of the IOC in the child
	 */
	public IOConnector(AbstractMungeComponent parent, int parentNum, AbstractMungeComponent child, int childNum) {
		this.parent = parent;
		this.child = child;
		parentNumber = parentNum;
		childNumber = childNum;
		selected = false;
	}
	
	
	/**
	 * Checks if the current line has been clicked. 
	 * 
	 * @param click The point where the click was directed
	 * @return will return true iff the click was within 6 pixles of the
	 * click
	 */
	public boolean clicked(Point click) {
		Point p1 = parent.getOutputPosition(parentNumber);
		p1.translate(parent.getX(), parent.getY());
		Point p2 = child.getInputPosition(childNumber);
		p2.translate(child.getX(), child.getY());
		double dist = Line2D.ptLineDist(p1.x,p1.y,p2.x,p2.y,click.x,click.y);
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
			return ioc.parent == parent && ioc.child == child && ioc.parentNumber == parentNumber && ioc.childNumber == childNumber;
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

	/**
	 * paints this IOC as a line on the specified Graphics
	 * 
	 * @param g The place to paint
	 */
	public void paint(Graphics g) {
		if (selected) {
			((Graphics2D)g).setStroke(new BasicStroke(2));
		}
		
		Point bottom = child.getInputPosition(childNumber);
		Point top = parent.getOutputPosition(parentNumber);
		
		top.translate(parent.getX(), parent.getY());
		bottom.translate(child.getX(), child.getY());
		
		MungeStepOutput link = child.getInputs().get(childNumber);
		g.setColor(AbstractMungeComponent.getColor(link.getType()));
		
		g.drawLine((int)top.getX(), (int)top.getY(), (int)bottom.getX(), (int)bottom.getY());
		
		if (selected) {
			((Graphics2D)g).setStroke(new BasicStroke(1));
		}
	}
	
	/**
	 * Sets this IOC to be selected so that it can be drawn diffrently
	 * 
	 * @param sel Set to true to select this component, false otherwise 
	 */
	public void setSelected(boolean sel) {
		selected = sel;
	}
	
	/**
	 * deletes this IOC and the link in the mungeSteps 
	 */
	public void remove() {
		child.getStep().disconnectInput(childNumber);
	}


	public AbstractMungeComponent getParent() {
		return parent;
	}

	public AbstractMungeComponent getChild() {
		return child;
	}


	public int getParentNumber() {
		return parentNumber;
	}

	public int getChildNumber() {
		return childNumber;
	}
}
