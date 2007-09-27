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

package ca.sqlpower.matchmaker.swingui.munge;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLayeredPane;
import javax.swing.JPopupMenu;

import ca.sqlpower.matchmaker.MungeStep;
import ca.sqlpower.matchmaker.MungeStepOutput;

public class MungePen extends JLayeredPane {
	
	Component select;
	Point diff;
	
	Map<MungeStep,MungeComponent> modelMap = new HashMap<MungeStep, MungeComponent>();

	MungePen() {
		addMouseListener(new MungePenMouseListener());
		addMouseMotionListener(new MungePenMouseMotionListener());
	}
	
	public void bringToFront(Component com) {
		remove(com);
		add(com,0);
	}
	
	@Override
	protected void addImpl(Component comp, Object constraints, int index) {
		if (comp instanceof MungeComponent) {
			MungeComponent mcom = (MungeComponent)comp;
			modelMap.put(mcom.getStep(),mcom);
		}
		super.addImpl(comp, constraints, index);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		for (Component com : getComponents()) {
			
			if (com instanceof MungeComponent) {
				MungeComponent child = (MungeComponent)com;
				
				for (int x = 0; x< child.getInputs().size();x++) {
					Point bottom = child.getInputPosition(x);
					
					MungeStepOutput link = child.getInputs().get(x);
					MungeComponent parent = modelMap.get(link.getParent());
					Point top = parent.getOutputPosition(parent.getOutputs().indexOf(link));
					
					top.translate(parent.getX(), parent.getY());
					bottom.translate(child.getX(), child.getY());
					
					g.drawLine((int)top.getX(), (int)top.getY(), (int)bottom.getX(), (int)bottom.getY());
				}
			}
			
		}
	}
	
	
	class MungePenMouseListener extends MouseAdapter {
		@Override
		public void mousePressed(MouseEvent e) {
			select = null;
			for (Component com : MungePen.this.getComponents()) {
				if (com.getBounds().contains(e.getPoint())) {
					
					if (select == null || getLayer(com) < getLayer(select))
					{
						bringToFront(com);
						select = com;
						diff = new Point(e.getX() - com.getX(), e.getY()-com.getY());
					}
				}
			}

			if (e.isPopupTrigger()) {
				if (select != null && select instanceof MungeComponent) {
					JPopupMenu popup = ((MungeComponent) select).getPopupMenu();
					if (popup != null) {
						popup.show(MungePen.this, e.getX(), e.getY());
					}
				}
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			select = null;
			diff = null;
		}
		
	}
	
	class MungePenMouseMotionListener extends MouseMotionAdapter {
		@Override
		public void mouseDragged(MouseEvent e) {
			if (select != null) {
				select.setLocation(new Point((int)(e.getX() - diff.getX()),(int)(e.getY() - diff.getY())));
			}
			getParent().repaint();
		}
	}

}
