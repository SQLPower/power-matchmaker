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

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.geom.Line2D;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 *Used to store a line between the IOConnections.
 *This does not draw the line but holds its position.
 *Each IOC is recreated every time the munge pen is redrawn 
 */
public class IOConnector {
	MungeComponent parent;
	MungeComponent child;
	int parentNumber;
	int childNumber;
	
	/**
	 * Creates a new IOConnector that represents a connection line in the mungePen
	 *  
	 * @param parent The MungeComponent where the output is from
	 * @param parentNum The number of the IOC in the parent
	 * @param child The MungeComponent where the inputs goes 
	 * @param childNum The number of the IOC in the child
	 */
	public IOConnector(MungeComponent parent, int parentNum, MungeComponent child, int childNum) {
		this.parent = parent;
		this.child = child;
		parentNumber = parentNum;
		childNumber = childNum;
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
		JMenuItem test = new JMenuItem("Test");
		pop.add(test);
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
	 * deletes this IOC and the link in the mungeSteps 
	 */
	public void remove() {
		child.getStep().disconnectInput(childNumber);
		child.getParent().repaint();
	}
}
