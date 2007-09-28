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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLayeredPane;
import javax.swing.JPopupMenu;

import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.munge.MungeStepOutput;

public class MungePen extends JLayeredPane {
	
	//The selected component for moving
	Component selectedMove;
	// The offset from the corner of the component to where the mouse clicked
	Point diff;
	
	
	//holds the info for dragging a connection 
	//between two IOCc
	MungeComponent start;
	MungeComponent finish;
	int startNum;
	int finishNum;
	
	//current mouse position
	int mouseX;
	int mouseY;
	
	//the currurrently selected line
	IOConnector selectedLine;
	
	
	Map<MungeStep,MungeComponent> modelMap = new HashMap<MungeStep, MungeComponent>();
	List<IOConnector> lines = new ArrayList<IOConnector>(); 

	/**
	 * Creates a new empty mungepen.
	 * 
	 */
	public MungePen() {
		setFocusable(true);
		addMouseListener(new MungePenMouseListener());
		addMouseMotionListener(new MungePenMouseMotionListener());
		addKeyListener(new MungePenKeyListener());
		addFocusListener(new FocusAdapter(){
			@Override
			public void focusLost(FocusEvent e) {
				selectedLine = null;
				selectedMove = null;
			}
		});
	}

	/**
	 * Returns the list of IOCs that represents the connections.
	 * 
	 * @return The list
	 */
	public List<IOConnector> getConnectors() {
		return lines;
	}
	
	/**
	 * Moves the given component to the topmost layer in the Pen
	 * 
	 * @param com The component to move
	 */
	public void bringToFront(Component com) {
		remove(com);
		add(com,0);
	}
	
	//over ridden to add any Component 
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
		lines.clear();
		for (Component com : getComponents()) {
			
			if (com instanceof MungeComponent) {
				MungeComponent child = (MungeComponent)com;
				
				for (int x = 0; x< child.getInputs().size();x++) {
					Point bottom = child.getInputPosition(x);
					
					MungeStepOutput link = child.getInputs().get(x);
					if (link != null) {
						MungeComponent parent = modelMap.get(link.getParent());
						
						int parentNum = parent.getOutputs().indexOf(link);					
						
						Point top = parent.getOutputPosition(parentNum);
						
						top.translate(parent.getX(), parent.getY());
						bottom.translate(child.getX(), child.getY());
						
						g.setColor(MungeComponent.getColor(link.getType()));
						g.drawLine((int)top.getX(), (int)top.getY(), (int)bottom.getX(), (int)bottom.getY());
						
						IOConnector curr = new IOConnector(parent,parentNum,child,x);
						if (curr.equals(selectedLine)) {
							Graphics2D g2d = (Graphics2D)g;
							
							int width = 2;
							
							g2d.setStroke(new BasicStroke(width));
							g2d.drawLine((int)top.getX(), (int)top.getY(), (int)bottom.getX(), (int)bottom.getY());
							g2d.setStroke(new BasicStroke(1));
							selectedLine = curr;
						}
						lines.add(curr);
					}
				}
			}
			
		}
		g.setColor(Color.BLACK);
		
		if (start != null || finish != null) {
			Point fixed;
			if (start != null) {
				fixed = start.getOutputPosition(startNum);
				fixed.translate(start.getX(), start.getY());
			} else {
				fixed = finish.getInputPosition(finishNum);
				fixed.translate(finish.getX(), finish.getY());
			}
			g.drawLine(fixed.x,fixed.y, mouseX, mouseY);	
		}
	}
	
	class MungePenMouseListener extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			requestFocusInWindow();
			
			//finds if the user has selected a line
			selectedLine = null;
			for (IOConnector line : lines) {
				if (line.clicked(e.getPoint())) {
					selectedLine = line;
					break;
				}
			}
	
			findSelected(e);
			if (!maybeShowPopup(e)) {
				checkForIOConnectors(e);
			}
		}
		
		public void mouseReleased(MouseEvent e) {
			findSelected(e);
			checkForIOConnectors(e);
			diff = null;
			start = null;
			finish = null;
			
			mouseX = e.getX();
			mouseY = e.getY();
			
			findSelected(e);
			maybeShowPopup(e);
			getParent().repaint();
		}
		
		public void mouseClicked(MouseEvent e) {
			findSelected(e);
			maybeShowPopup(e);
		}
		
		public boolean maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				if (selectedMove != null && selectedMove instanceof MungeComponent) {
					JPopupMenu popup = ((MungeComponent) selectedMove).getPopupMenu();
					if (popup != null) {
						popup.show(MungePen.this, e.getX(), e.getY());
						selectedMove = null;
						return true;
					}
				} else if (selectedLine != null && e.isPopupTrigger()) {
					JPopupMenu popup = selectedLine.getPopup();
					getParent().repaint();
					popup.show(MungePen.this,e.getX(),e.getY());
					return true;
				}
			}
			return false;
		}

		public void findSelected(MouseEvent e) {
			selectedMove = null;
			for (Component com : MungePen.this.getComponents()) {
				if (com.getBounds().contains(e.getPoint())) {
					
					if (selectedMove == null || getLayer(com) < getLayer(selectedMove))
					{
						bringToFront(com);
						selectedMove = com;
						selectedLine = null;
						diff = new Point(e.getX() - com.getX(), e.getY()-com.getY());
					}
				}
			}
		}
		
		public void checkForIOConnectors(MouseEvent e) {
			//used to define the range of which a hit is counted as
			int tolerance = 15;

			if (selectedMove instanceof MungeComponent) {
				MungeComponent mcom = (MungeComponent) selectedMove;
				int inputs = mcom.getStep().getInputs().size();

				for (int x = 0;x<inputs;x++) {
					Point p = mcom.getInputPosition(x);
					p.translate(mcom.getX(), mcom.getY());
					if (Math.abs(p.x - e.getX()) < tolerance && Math.abs(p.y - e.getY()) < tolerance) {
						connectionHit(mcom, x, true);
					}
				}
				
				for (int x = 0;x<mcom.getOutputs().size();x++) {
					Point p = mcom.getOutputPosition(x);
					p.translate(mcom.getX(), mcom.getY());
					if (Math.abs(p.x - e.getX()) < tolerance && Math.abs(p.y - e.getY()) < tolerance) {
						connectionHit(mcom, x, false);
					}
				}
			}
		}
		
		public void connectionHit(MungeComponent mcom, int connectionNum, boolean inputHit) {
			selectedMove = null;
			diff = null;
			selectedLine = null;
			
			getParent().repaint();

			if (inputHit) {
				if (mcom.getInputs().size() > connectionNum && mcom.getInputs().get(connectionNum) != null) {
					return;
				}
			}
			
			if (inputHit) {
				finish = mcom;
				finishNum = connectionNum;
			} else {
				start = mcom;
				startNum = connectionNum;
			}
			
			//see if a connection is complete
			if (start != null && finish != null) {
				Class startHas = start.getStep().getChildren().get(startNum).getType();
				Class finishWants = finish.getStep().getInputDescriptor(finishNum).getType();
				if (startHas.equals(finishWants)) {
					finish.getStep().connectInput(finishNum,start.getStep().getChildren().get(startNum));
				} else {
					if (inputHit) {
						finish = null;
					} else {
						start = null;
					}
				}
				start = null;
				finish = null;
			}
		}
	}
	 
	class MungePenMouseMotionListener extends MouseMotionAdapter {
		@Override
		public void mouseDragged(MouseEvent e) {
			if (getBounds().contains(e.getPoint())) {
				if (selectedMove != null) {
					selectedMove.setLocation(new Point((int)(e.getX() - diff.getX()),(int)(e.getY() - diff.getY())));
					getParent().repaint();
				} else if (start != null || finish != null) {
					getParent().repaint();
				}
			}
			mouseX = e.getX();
			mouseY = e.getY();
		}
		
		@Override
		public void mouseMoved(MouseEvent e) {
			super.mouseMoved(e);
			mouseX = e.getX();
			mouseY = e.getY();
		}
	}
	
	class MungePenKeyListener extends KeyAdapter {
		//passes key press to selected components
		public void keyPressed(KeyEvent e) {
			if (selectedLine != null) {
				selectedLine.keyPressed(e);
			} else if (selectedMove != null && selectedMove instanceof MungeComponent) {
				((MungeComponent)selectedMove).keyPressed(e);
			}
		}
	}
}
