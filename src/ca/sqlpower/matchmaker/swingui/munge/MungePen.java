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
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
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
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.munge.MungeStepOutput;

public class MungePen extends JLayeredPane implements Scrollable {
	
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
				unselectLine();
				unselectCom();
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
	
	/**
	 * Selects the given line
	 * 
	 * @param line The line to select
	 */
	private void selectLine(IOConnector line) {
		selectedLine = line;
	}
	
	/**
	 * Unselects a line if there is one selected, else does nothing 
	 */
	private void unselectLine() {
		selectedLine = null;
	}
	
	/**
	 * Selects the given MungeComponent
	 * 
	 * @param mcom The MingeComponent to select
	 */
	private void selectCom(Component com) {
		boolean redraw = selectedMove != com;
		selectedMove = com;
		if (com instanceof MungeComponent) {
			((MungeComponent)com).setSelect(true);
		}
		if (redraw) {
			repaint();
		}
	}
	
	/**
	 * Unselects a MungeComponent if there is one selected, else does nothing 
	 */
	private void unselectCom() {
		if (selectedMove instanceof MungeComponent) {
			((MungeComponent)selectedMove).setSelect(false);
		}
		selectedMove = null;
		diff = null;
		repaint();
	}
	
	
	private IOConnector getSelectedLine() {
		return selectedLine;
	}
	
	private Component getSelectedComponent() {
		return selectedMove;
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
						if (curr.equals(getSelectedLine())) {
							Graphics2D g2d = (Graphics2D)g;
							
							int width = 2;
							
							g2d.setStroke(new BasicStroke(width));
							g2d.drawLine((int)top.getX(), (int)top.getY(), (int)bottom.getX(), (int)bottom.getY());
							g2d.setStroke(new BasicStroke(1));
							selectLine(curr);
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
			unselectLine();
			for (IOConnector line : lines) {
				if (line.clicked(e.getPoint())) {
					selectLine(line);
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
			
			maybeShowPopup(e);
			revalidate();
		}
		
		public void mouseClicked(MouseEvent e) {
			maybeShowPopup(e);
		}
		
		public boolean maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				findSelected(e);
				if (getSelectedComponent() != null && getSelectedComponent() instanceof MungeComponent) {
					JPopupMenu popup = ((MungeComponent) getSelectedComponent()).getPopupMenu();
					if (popup != null) {
						popup.show(MungePen.this, e.getX(), e.getY());
						unselectCom();
						return true;
					}
				} else if (getSelectedLine() != null && e.isPopupTrigger()) {
					JPopupMenu popup = getSelectedLine().getPopup();
					getParent().repaint();
					popup.show(MungePen.this,e.getX(),e.getY());
					return true;
				}
			}
			return false;
		}

		public void findSelected(MouseEvent e) {
			unselectCom();
			for (Component com : MungePen.this.getComponents()) {
				if (com.getBounds().contains(e.getPoint())) {
					
					if (getSelectedComponent() == null || getLayer(com) < getLayer(getSelectedComponent()))
					{
						bringToFront(com);
						selectCom(com);
						unselectLine();
						diff = new Point(e.getX() - com.getX(), e.getY()-com.getY());
					}
				}
			}
		}
		
		public void checkForIOConnectors(MouseEvent e) {
			//used to define the range of which a hit is counted as
			int tolerance = 15;

			if (getSelectedComponent() instanceof MungeComponent) {
				MungeComponent mcom = (MungeComponent) getSelectedComponent();
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
			unselectLine();
			unselectCom();
			
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
				if (getSelectedComponent() != null) {
					getSelectedComponent().setLocation(new Point((int)(e.getX() - diff.getX()),(int)(e.getY() - diff.getY())));
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
			if (getSelectedLine() != null) {
				getSelectedLine().keyPressed(e);
			} else if (getSelectedComponent() != null && getSelectedComponent() instanceof MungeComponent) {
				((MungeComponent)getSelectedComponent()).keyPressed(e);
			}
		}
	}
	
	
	
	/////////////////////////////////////////////////////////////////
	//        Code to handle the scrollPane                       //
	//         Most of it was taken from the playpen             //
	//////////////////////////////////////////////////////////////
	/**
	 * Calculates the smallest rectangle that will completely
	 * enclose the visible components.
	 *
	 * This is then compared to the viewport size, one dimension
	 * at a time.  To ensure the whole playpen is "live", always
	 * choose the larger number in each Dimension.
	 */
	public Dimension getPreferredSize() {

		Dimension usedSpace = getUsedArea();
		Dimension vpSize = getViewportSize();
		Dimension ppSize = null;

		// viewport seems to never come back as null, but protect anyways...
		if (vpSize != null) {
			ppSize = new Dimension(Math.max(usedSpace.width, vpSize.width),
					Math.max(usedSpace.height, vpSize.height));
		}

		if (ppSize != null) {
			return ppSize;
		} else {
			return usedSpace;
		}
	}
	
	// get the size of the viewport that we are sitting in (return null if there isn't one);
	public Dimension getViewportSize() {
		Container c = SwingUtilities.getAncestorOfClass(JViewport.class, this);
		if (c != null) {
			JViewport jvp = (JViewport) c;
			return jvp.getSize();
		} else {
			return null;
		}
	}
	
	public Dimension getUsedArea() {
		Rectangle cbounds = null;
		int minx = 0, miny = 0, maxx = 0, maxy = 0;
		for (int i = 0; i < getComponentCount(); i++) {
			Component c = getComponent(i);
			cbounds = c.getBounds(cbounds);
			minx = Math.min(cbounds.x, minx);
			miny = Math.min(cbounds.y, miny);
			maxx = Math.max(cbounds.x + cbounds.width , maxx);
			maxy = Math.max(cbounds.y + cbounds.height, maxy);
		}
		return new Dimension(Math.max(maxx - minx, getMinimumSize().width), Math.max(maxy - miny, getMinimumSize().height));
	}

 	public Dimension getPreferredScrollableViewportSize() {
		// return getPreferredSize();
		return new Dimension(800,600);
	}

    public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		if (orientation == SwingConstants.HORIZONTAL) {
			return visibleRect.width;
		} else { // SwingConstants.VERTICAL
			return visibleRect.height;
		}
	}

	public boolean getScrollableTracksViewportHeight() {
		return false;
	}

    public boolean getScrollableTracksViewportWidth() {
		return false;
	}

    public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		if (orientation == SwingConstants.HORIZONTAL) {
			return visibleRect.width/5;
		} else { // SwingConstants.VERTICAL
			return visibleRect.height/5;
		}
	}
}
