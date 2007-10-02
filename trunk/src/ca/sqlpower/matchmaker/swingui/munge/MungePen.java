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

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
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

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchRuleSet;
import ca.sqlpower.matchmaker.event.MatchMakerEvent;
import ca.sqlpower.matchmaker.event.MatchMakerListener;
import ca.sqlpower.matchmaker.munge.DoubleMetaphoneMungeStep;
import ca.sqlpower.matchmaker.munge.LowerCaseMungeStep;
import ca.sqlpower.matchmaker.munge.MetaphoneMungeStep;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.munge.MungeStepOutput;
import ca.sqlpower.matchmaker.munge.RefinedSoundexMungeStep;
import ca.sqlpower.matchmaker.munge.RetainCharactersMungeStep;
import ca.sqlpower.matchmaker.munge.SoundexMungeStep;
import ca.sqlpower.matchmaker.munge.StringSubstitutionMungeStep;
import ca.sqlpower.matchmaker.munge.TranslateWordMungeStep;
import ca.sqlpower.matchmaker.munge.UpperCaseMungeStep;
import ca.sqlpower.matchmaker.munge.WordCountMungeStep;

/**
 * This is a temp spot for generating the Munge step components.
 * This will eventualy be put somewhere where it is easy for the user to 
 * add there own mappings. 
 */
class MungeComponentFactory {
	public static  AbstractMungeComponent getMungeComponent(MungeStep ms) {
		if (ms instanceof UpperCaseMungeStep) {
			return new UpperCaseMungeComponent(ms);
		} else if (ms instanceof LowerCaseMungeStep) {
			return new LowerCaseMungeComponent(ms);
		} else if (ms instanceof SoundexMungeStep) {
			return new SoundexMungeComponent(ms);
		} else if (ms instanceof RefinedSoundexMungeStep) {
			return new RefinedSoundexMungeComponent(ms);
		} else if (ms instanceof MetaphoneMungeStep) {
			return new MetaphoneMungeComponent(ms);
		} else if (ms instanceof DoubleMetaphoneMungeStep) {
			return new DoubleMetaphoneMungeComponent(ms);
		} else if (ms instanceof WordCountMungeStep) {
			return new WordCountMungeComponent(ms);
		} else if (ms instanceof StringSubstitutionMungeStep) {
			return new StringSubstitutionMungeComponent(ms);
		} else if (ms instanceof TranslateWordMungeStep) {
			return new TranslateWordMungeComponent(ms);
		} else if(ms instanceof RetainCharactersMungeStep) {
			return new RetainCharactersMungeComponent(ms);
		} else {
			return null;
		}
			
	}
}

public class MungePen extends JLayeredPane implements Scrollable {
	
	private static  final Logger logger = Logger.getLogger(MungePen.class); 
	
	final MatchRuleSet process;
	
	//The selected component for temp use before the request focus has kicked in
	//only use right after a call to findSelected
	Component recentlySelected;
	
	// The offset from the corner of the component to where the mouse clicked
	Point diff;
	
	
	//holds the info for dragging a connection 
	//between two IOCc
	AbstractMungeComponent start;
	AbstractMungeComponent finish;
	int startNum;
	int finishNum;
	
	//current mouse position
	int mouseX;
	int mouseY;
	
	//the currurrently selected line
	IOConnector selectedLine;
	
	MungePenMungeStepListener mungeStepListener;
	
	
	
	Map<MungeStep,AbstractMungeComponent> modelMap = new HashMap<MungeStep, AbstractMungeComponent>();
	List<IOConnector> lines = new ArrayList<IOConnector>(); 

	/**
	 * Creates a new empty mungepen.
	 * 
	 */
	public MungePen(MatchRuleSet process) {
		process.addMatchMakerListener(new MungePenMatchRuleSetListener());
		mungeStepListener = new MungePenMungeStepListener();
		
		setFocusable(true);
		addMouseListener(new MungePenMouseListener());
		addMouseMotionListener(new MungePenMouseMotionListener());
		addKeyListener(new MungePenKeyListener());
		
		setBackground(Color.WHITE);
		setOpaque(true);
		this.process = process;
		buildComponents(process);
		
	}
	
	/**
	 * Translates the process' children into the mungePem
	 * 
	 * @param Process
	 */
	private void buildComponents(MatchRuleSet process) {
		for (MungeStep ms : process.getChildren()) {
			ms.addMatchMakerListener(mungeStepListener);
			AbstractMungeComponent mcom = MungeComponentFactory.getMungeComponent(ms);
			modelMap.put(ms, mcom);
			add(mcom);
		}
		
		//This is done in an other loop to ensure that all the MungeComponets have been mapped
		for (MungeStep ms : process.getChildren()) {
			for (int x = 0; x < ms.getChildren().size(); x++) {
				MungeStepOutput link = ms.getInputs().get(x);
				if (link != null) {
					MungeStep parent = (MungeStep)link.getParent();
					int parNum = parent.getChildren().indexOf(link);
					IOConnector ioc = new IOConnector(modelMap.get(parent),parNum,modelMap.get(ms),x);
					lines.add(ioc);
				}
			}
		}
	}
	
	/**
	 * Moves the given component to the topmost layer in the Pen
	 * 
	 * @param com The component to move
	 */
	private void bringToFront(Component com) {
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
		line.setSelected(true);
	}
	
	/**
	 * Unselects a line if there is one selected, else does nothing 
	 */
	private void unselectLine() {
		if (selectedLine != null) {
			selectedLine.setSelected(false);
		}
		selectedLine = null;
	}
	
	private IOConnector getSelectedLine() {
		return selectedLine;
	}
	
	private Component getSelectedComponent() {
		for (Component com : getComponents()) {
			if (com.hasFocus()) {
				return com;
			}
		}
		return null;
	}
	
	//over ridden to map all the mungesteps to there components
	@Override
	protected void addImpl(Component comp, Object constraints, int index) {
		if (comp instanceof AbstractMungeComponent) {
			AbstractMungeComponent mcom = (AbstractMungeComponent)comp;
			modelMap.put(mcom.getStep(),mcom);
		}
		super.addImpl(comp, constraints, index);
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if (false && logger.isDebugEnabled()) {
			Graphics2D g2 = (Graphics2D)g;
			Rectangle clip = g2.getClipBounds();
			if (clip != null) {
				g2.setColor(Color.green);
				clip.width--;
				clip.height--;
				g2.draw(clip);
				g2.setColor(getForeground());
				logger.debug("Clipping region: "+g2.getClip());
			} else {
				logger.debug("Null clipping region");
			}
		}
		
		for (IOConnector con : lines) {
			con.paint(g);
		}
		
		if (start != null || finish != null) {
			Point fixed;
			if (start != null) {
				fixed = start.getOutputPosition(startNum);
				fixed.translate(start.getX(), start.getY());
			} else {
				fixed = finish.getInputPosition(finishNum);
				fixed.translate(finish.getX(), finish.getY());
			}
			g.setColor(Color.BLACK);
			g.drawLine(fixed.x,fixed.y, mouseX, mouseY);	
		}
	}
	
	/**
	 * Removes a mungestep from the pen. 
	 * This will remove the given mungestep asw ell as disconnect all its inputs.
	 * 
	 * @param ms The step to be removed
	 */
	public void removeMungeStep(MungeStep ms) {
		
		List<IOConnector> killed = new ArrayList<IOConnector>();
		
		for (int x = 0;x <lines.size();x++) {
			IOConnector ioc = lines.get(x);
			if (ioc.getParent().getStep().equals(ms) || ioc.getChild().getStep().equals(ms)) {
				killed.add(ioc);
			}
		}		
		for (IOConnector ioc : killed) {
			lines.remove(ioc);
			ioc.remove();
		}
		process.removeChild(ms);
	}
	
	/**
	 * Removes a munge step and connects the inputs to the output.
	 *  Only call this if there is one input and output
	 *  
	 * @param ms mungestep to delete
	 */
	public void removeMungeStepSingles(MungeStep ms) {
		
		List<IOConnector> killed = new ArrayList<IOConnector>();
		MungeStep parent = null;
		int parNum = 0;
		MungeStep child = null;
		int childNum = 0;
		
		for (int x = 0;x <lines.size();x++) {
			IOConnector ioc = lines.get(x);
			if (ioc.getParent().getStep().equals(ms) || ioc.getChild().getStep().equals(ms)) {
				killed.add(ioc);
				if (ioc.getParent().getStep().equals(ms)) {
					child = ioc.getChild().getStep();
					childNum = ioc.getChildNumber();
				} else {
					parent = ioc.getParent().getStep();
					parNum = ioc.getParentNumber();
				}
			}
		}		
		for (IOConnector ioc : killed) {
			lines.remove(ioc);
			ioc.remove();
		}
		process.removeChild(ms);
		
		if (parent != null && child != null) {
			child.connectInput(childNum, parent.getChildren().get(parNum));
		}
	}
	
	
	private class MungePenMouseListener extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			logger.debug("Mouse PRess");
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
			logger.debug("Mouse released");
			checkForIOConnectors(e);
			
			diff = null;
			start = null;
			finish = null;
			
			mouseX = e.getX();
			mouseY = e.getY();
			
			maybeShowPopup(e);
			revalidate();
			repaint();
		}
		
		public void mouseClicked(MouseEvent e) {
			logger.debug("Mouse Click");
			maybeShowPopup(e);
		}
		
		public boolean maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				findSelected(e);
				if (getSelectedComponent() != null && getSelectedComponent() instanceof AbstractMungeComponent) {
					JPopupMenu popup = ((AbstractMungeComponent) getSelectedComponent()).getPopupMenu();
					if (popup != null) {
						popup.show(MungePen.this, e.getX(), e.getY());
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
			recentlySelected = null;
			for (Component com : MungePen.this.getComponents()) {
				if (com.getBounds().contains(e.getPoint())) {
					
					if (recentlySelected == null || getLayer(com) < getLayer(recentlySelected))
					{
						recentlySelected = com;
						unselectLine();
						diff = new Point(e.getX() - com.getX(), e.getY()-com.getY());
					}
				}
			}
			if (recentlySelected == null) {
				requestFocusInWindow();
			} else if (!recentlySelected.hasFocus()) {
				bringToFront(recentlySelected);
				recentlySelected.requestFocusInWindow();
			}
		}
		
		//checks to see if the mouse was near an IOC point
		public void checkForIOConnectors(MouseEvent e) {
			logger.debug("Checking for IOConnections");
			
			//used to define the range of which a hit is counted as
			int tolerance = 15;
			
			Component sel = null;
			for (Component com : MungePen.this.getComponents()) {
				if (com.getBounds().contains(e.getPoint())) {
					if (sel == null || getLayer(com) < getLayer(sel))
					{
						sel = com;
					}
				}
			}

			if (sel instanceof AbstractMungeComponent) {
				AbstractMungeComponent mcom = (AbstractMungeComponent) sel;
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
		
		//The mouse was near an IOC point
		public void connectionHit(AbstractMungeComponent mcom, int connectionNum, boolean inputHit) {
			logger.debug("Connection hit");
			
			unselectLine();
			
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
				}
				start = null;
				finish = null;
			}
			
			recentlySelected = null;
			requestFocusInWindow();
		}
	}
	 
	private class MungePenMouseMotionListener extends MouseMotionAdapter {
		@Override
		public void mouseDragged(MouseEvent e) {
			logger.debug("Mouse dragged");
			if (getBounds().contains(e.getPoint())) {
				if (recentlySelected != null) {
					recentlySelected.setLocation(new Point((int)(e.getX() - diff.getX()),(int)(e.getY() - diff.getY())));
					repaint();
				} else if (start != null || finish != null) {
					repaint();
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
			} else if (getSelectedComponent() != null && getSelectedComponent() instanceof AbstractMungeComponent) {
				((AbstractMungeComponent)getSelectedComponent()).keyPressed(e);
			}
			
			if (e.getKeyCode() == KeyEvent.VK_1) {
				process.addChild(new UpperCaseMungeStep());
			} else if (e.getKeyCode() == KeyEvent.VK_2) {
				process.addChild(new LowerCaseMungeStep());
			} else if (e.getKeyCode() == KeyEvent.VK_3) {
				process.addChild(new SoundexMungeStep());
			} else if (e.getKeyCode() == KeyEvent.VK_4) {
				process.addChild(new RefinedSoundexMungeStep());
			} else if (e.getKeyCode() == KeyEvent.VK_5) {
				process.addChild(new MetaphoneMungeStep());
			} else if (e.getKeyCode() == KeyEvent.VK_6) {
				process.addChild(new DoubleMetaphoneMungeStep());
			} else if (e.getKeyCode() == KeyEvent.VK_7) {
				process.addChild(new WordCountMungeStep());
			} else if (e.getKeyCode() == KeyEvent.VK_8) {
				process.addChild(new TranslateWordMungeStep(process.getSession()));
			} else if (e.getKeyCode() == KeyEvent.VK_9) {
				process.addChild(new StringSubstitutionMungeStep());
			} else if (e.getKeyCode() == KeyEvent.VK_0) {
				process.addChild(new RetainCharactersMungeStep());
			}
		}
	}
	
	
	
	/////////////////////////////////////////////////////////////////
	//        Code to handle the scrollPane                       //
	//         Most of it was stolen from the playpen             //
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
    
    ///////////////////////////Listener for MatchMaker Rule Set /////////////////////////////////

    private class MungePenMatchRuleSetListener implements MatchMakerListener<MatchRuleSet, MungeStep> {
		public void mmChildrenInserted(MatchMakerEvent<MatchRuleSet, MungeStep> evt) {
			
			for (int x : evt.getChangeIndices()) {
				evt.getSource().getChildren().get(x).addMatchMakerListener(mungeStepListener);
				AbstractMungeComponent mcom = MungeComponentFactory.getMungeComponent(evt.getSource().getChildren().get(x));
				modelMap.put(evt.getSource().getChildren().get(x), mcom);
				add(mcom);
			}
			
			//This is done in an other loop to ensure that all the MungeComponets have been mapped
			for (int y : evt.getChangeIndices()) {
				MungeStep ms = evt.getSource().getChildren().get(y);
				for (int x = 0; x < ms.getChildren().size(); x++) {
					MungeStepOutput link = ms.getInputs().get(x);
					if (link != null) {
						MungeStep parent = (MungeStep)link.getParent();
						int parNum = parent.getChildren().indexOf(link);
						IOConnector ioc = new IOConnector(modelMap.get(parent),parNum,modelMap.get(ms),x);
						lines.add(ioc);
					}
				}
			}
		}
	
		public void mmChildrenRemoved(MatchMakerEvent<MatchRuleSet, MungeStep> evt) {
			
			for (MungeStep ms : evt.getChildren()) {
				AbstractMungeComponent mcom = modelMap.remove(ms);
				ms.removeMatchMakerListener(mungeStepListener);
				remove(mcom);
			}
			
			repaint();
		}
	
		public void mmPropertyChanged(MatchMakerEvent<MatchRuleSet, MungeStep> evt) {
			repaint();
		}
	
		public void mmStructureChanged(MatchMakerEvent<MatchRuleSet, MungeStep> evt) {
			repaint();
		}
    }
	///////////////////////////////////// Listener for the MungeStep /////////////////////////
    
    private class MungePenMungeStepListener implements MatchMakerListener<MungeStep, MungeStepOutput> {

		public void mmChildrenInserted(MatchMakerEvent<MungeStep, MungeStepOutput> evt) {

		}

		public void mmChildrenRemoved(MatchMakerEvent<MungeStep, MungeStepOutput> evt) {
			
		}

		public void mmPropertyChanged(MatchMakerEvent<MungeStep, MungeStepOutput> evt) {
			if (evt.getPropertyName().equals("inputs")) {
				//changed indices =
				if (evt.getOldValue() == null && evt.getNewValue() != null) {
					//connected and input
					MungeStepOutput mso = (MungeStepOutput)evt.getNewValue();
					MungeStep child = evt.getSource();
					MungeStep parent = (MungeStep)mso.getParent();
					int parNum = parent.getChildren().indexOf(mso);
					int childNum = child.getInputs().indexOf(mso);
					
					
					IOConnector ioc = new IOConnector(modelMap.get(parent),parNum,modelMap.get(child),childNum);
					lines.add(ioc);
					
				} else if (evt.getNewValue() == null && evt.getOldValue() != null) {
					//disconnect input
					MungeStepOutput mso = (MungeStepOutput)evt.getOldValue();
					MungeStep child = evt.getSource();
					MungeStep parent = (MungeStep)mso.getParent();
					int parNum = parent.getChildren().indexOf(mso);
					int childNum = child.getInputs().indexOf(mso);
					
					IOConnector ioc = new IOConnector(modelMap.get(parent),parNum,modelMap.get(child),childNum);
					lines.remove(ioc);
				} 
			}
			repaint();
		}

		public void mmStructureChanged(MatchMakerEvent<MungeStep, MungeStepOutput> evt) {
			repaint();
		}
    }
	
}
