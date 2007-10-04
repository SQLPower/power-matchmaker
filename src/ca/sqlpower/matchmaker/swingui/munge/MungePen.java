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
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLayeredPane;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.event.MatchMakerEvent;
import ca.sqlpower.matchmaker.event.MatchMakerListener;
import ca.sqlpower.matchmaker.munge.ConcatMungeStep;
import ca.sqlpower.matchmaker.munge.DoubleMetaphoneMungeStep;
import ca.sqlpower.matchmaker.munge.LowerCaseMungeStep;
import ca.sqlpower.matchmaker.munge.MetaphoneMungeStep;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.munge.MungeStepOutput;
import ca.sqlpower.matchmaker.munge.RefinedSoundexMungeStep;
import ca.sqlpower.matchmaker.munge.RetainCharactersMungeStep;
import ca.sqlpower.matchmaker.munge.SQLInputStep;
import ca.sqlpower.matchmaker.munge.SoundexMungeStep;
import ca.sqlpower.matchmaker.munge.StringSubstitutionMungeStep;
import ca.sqlpower.matchmaker.munge.SubstringByWordMungeStep;
import ca.sqlpower.matchmaker.munge.SubstringMungeStep;
import ca.sqlpower.matchmaker.munge.TranslateWordMungeStep;
import ca.sqlpower.matchmaker.munge.UpperCaseMungeStep;
import ca.sqlpower.matchmaker.munge.WordCountMungeStep;
import ca.sqlpower.validation.swingui.FormValidationHandler;

/**
 * This is a temp spot for generating the Munge step components.
 * This will eventualy be put somewhere where it is easy for the user to 
 * add there own mappings. 
 */
class MungeComponentFactory {
	private static final Type[] CONSTRUCTOR_PARAMS = {MungeStep.class, FormValidationHandler.class, MatchMakerSession.class}; 
	
	public static  AbstractMungeComponent getMungeComponent(MungeStep ms, FormValidationHandler handler, 
		MatchMakerSession session, Map<String, StepDescription> stepProps) {
		
		if (ms instanceof SQLInputStep) {
			return new SQLInputMungeComponent(ms, handler, session);
		}
		
		for (StepDescription sd : stepProps.values()) {
			if (sd.getLogicClass().equals(ms.getClass())) {
				Constructor[] constructors = sd.getGuiClass().getDeclaredConstructors();
				
				for (Constructor con : constructors) {
					Type[] paramTypes = con.getGenericParameterTypes();					
					
					if (arrayEquals(paramTypes,CONSTRUCTOR_PARAMS)) {
						try {
							return (AbstractMungeComponent)con.newInstance(ms, handler, session);
						} catch (Throwable t) {
							throw new RuntimeException("Error generating munge step component check properties file.", t);
						}
					}
				}
			}
		}
		
		throw new NoClassDefFoundError("Error no constructor"  
				+ "(MungeStep, FormValidationHandler, MatchMakerSession) was found for the given munge step:" 
				+ ms.getClass());
	}
	
	private static boolean arrayEquals(Object[] a, Object[] b) {
		if (a.length != b.length) {
			return false;
		}
		
		for (int x = 0; x < a.length; x++) {
			if (!a[x].equals(b[x])) {
				return false;
			}
		}
		return true;
	}
}

public class MungePen extends JLayeredPane implements Scrollable {
	
	private static  final Logger logger = Logger.getLogger(MungePen.class); 
	
	private final MungeProcess process;
	
	//holds the info for dragging a connection 
	//between two IOCc
	private AbstractMungeComponent start;
	private AbstractMungeComponent finish;
	private int startNum;
	private int finishNum;
	
	//current mouse position
	public int mouseX;
	public int mouseY;
	
	private boolean normalizing;
	
	private MungePenMungeStepListener mungeStepListener;
	
	private FormValidationHandler handler;
	
	private Map<MungeStep,AbstractMungeComponent> modelMap = new HashMap<MungeStep, AbstractMungeComponent>();
	
	private Map<String, StepDescription> stepsProperties;
	
	
	/**
	 * Creates a new empty mungepen.
	 * 
	 */
	public MungePen(MungeProcess process, FormValidationHandler handler, 
			Map stepsProperties) throws ArchitectException {
		this.stepsProperties = stepsProperties;
		process.addMatchMakerListener(new MungePenMatchRuleSetListener());
		mungeStepListener = new MungePenMungeStepListener();
		
		setFocusable(true);
		addMouseListener(new MungePenMouseListener());
		addKeyListener(new MungePenKeyListener());
		
		setBackground(Color.WHITE);
		setOpaque(true);
		this.process = process;
		this.handler = handler;
		buildComponents(process);
		
		normalizing = false;
		
		addMouseMotionListener(new MouseMotionAdapter(){
			@Override
			public void mouseMoved(MouseEvent e) {
				mouseX = e.getX();
				mouseY = e.getY();
				if (start != null || finish != null) {
					repaint();
				}
			}
		});
		
		if (process.getChildCount() == 0) {
			process.addChild(new SQLInputStep(process.getParentMatch().getSourceTable()));
		}
	}
	
	/**
	 * Translates the process' children into the mungePem
	 * 
	 * @param Process
	 */
	private void buildComponents(MungeProcess process) {
		for (MungeStep ms : process.getChildren()) {
			ms.addMatchMakerListener(mungeStepListener);
			AbstractMungeComponent mcom = MungeComponentFactory.getMungeComponent(ms, handler, process.getSession(), stepsProperties);
			modelMap.put(ms, mcom);
			add(mcom,DEFAULT_LAYER);
		}
		
		//This is done in an other loop to ensure that all the MungeComponets have been mapped
		for (MungeStep ms : process.getChildren()) {
			for (int x = 0; x < ms.getInputs().size(); x++) {
				MungeStepOutput link = ms.getInputs().get(x);
				if (link != null) {
					MungeStep parent = (MungeStep)link.getParent();
					int parNum = parent.getChildren().indexOf(link);
					IOConnector ioc = new IOConnector(modelMap.get(parent),parNum,modelMap.get(ms),x);
					add(ioc);
				}
			}
		}
	}
	
	public void bringToFront(Component com) {
		setLayer(com, DRAG_LAYER);
		for (Component tmp : getComponents()) {
			if (!tmp.equals(com)) {
				setLayer(tmp, DEFAULT_LAYER);
			}
		}
		moveToFront(com);
	}
	
	public boolean isConnecting() {
		return (start != null || finish != null);
	}

	
	//over ridden to map all the mungesteps to there components
	@Override
	protected void addImpl(Component comp, Object constraints, int index) {
		if (comp instanceof AbstractMungeComponent) {
			AbstractMungeComponent mcom = (AbstractMungeComponent)comp;
			modelMap.put(mcom.getStep(),mcom);
		}
		
		if (comp instanceof IOConnector) {
			IOConnector ioc = (IOConnector)comp;
			addMouseListener(ioc);
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
	
	public List<IOConnector> getConnections() {
		List<IOConnector> lines = new ArrayList<IOConnector>();
		for (Component com: getComponents()) {
			if (com instanceof IOConnector) {
				lines.add((IOConnector)com);
			}
		}
		return lines;
	}
	
	/**
	 * Removes a mungestep from the pen. 
	 * This will remove the given mungestep as well as disconnect all its inputs.
	 * 
	 * @param ms The step to be removed
	 */
	public void removeMungeStep(MungeStep ms) {
		
		List <IOConnector> lines = getConnections();
		List<IOConnector> killed = new ArrayList<IOConnector>();
		
		for (int x = 0;x <lines.size();x++) {
			IOConnector ioc = lines.get(x);
			if (ioc.getParentCom().getStep().equals(ms) || ioc.getChildCom().getStep().equals(ms)) {
				killed.add(ioc);
			}
		}		
		for (IOConnector ioc : killed) {
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
		
		List <IOConnector> lines = getConnections();
		List<IOConnector> killed = new ArrayList<IOConnector>();
		MungeStep parent = null;
		int parNum = 0;
		MungeStep child = null;
		int childNum = 0;
		
		for (int x = 0;x <lines.size();x++) {
			IOConnector ioc = lines.get(x);
			if (ioc.getParentCom().getStep().equals(ms) || ioc.getChildCom().getStep().equals(ms)) {
				killed.add(ioc);
				if (ioc.getParentCom().getStep().equals(ms)) {
					child = ioc.getChildCom().getStep();
					childNum = ioc.getChildNumber();
				} else {
					parent = ioc.getParentCom().getStep();
					parNum = ioc.getParentNumber();
				}
			}
		}		
		for (IOConnector ioc : killed) {
			ioc.remove();
		}
		process.removeChild(ms);
		
		if (parent != null && child != null) {
			child.connectInput(childNum, parent.getChildren().get(parNum));
		}
	}
	
	
	//The mouse was near an IOC point
	public void connectionHit(AbstractMungeComponent mcom, int connectionNum, boolean inputHit) {
		logger.debug("Connection hit");
		requestFocusInWindow();
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
			logger.debug("Connection checking startNum:" + startNum + " EndNum " + finishNum);
			Class startHas = start.getStep().getChildren().get(startNum).getType();
			Class finishWants = finish.getStep().getInputDescriptor(finishNum).getType();
			logger.debug(startHas + " -> " + finishWants);
			if (startHas.equals(finishWants)) {
				logger.debug("Connecting");
				finish.getStep().connectInput(finishNum,start.getStep().getChildren().get(startNum));
			}
			stopConnection();
		}
		requestFocusInWindow();
	}
	
	private class MungePenMouseListener extends MouseAdapter {
		public void mousePressed(MouseEvent e) {	
			logger.debug("Mouse PRess");
			repaint();
			requestFocusInWindow();
		}
	}
	
	class MungePenKeyListener extends KeyAdapter {
		//passes key press to selected components
		public void keyPressed(KeyEvent e) {
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
				process.addChild(new SubstringMungeStep());
			} else if (e.getKeyCode() == KeyEvent.VK_Q) {
				process.addChild(new RetainCharactersMungeStep());
			} else if (e.getKeyCode() == KeyEvent.VK_W) {
				process.addChild(new SubstringByWordMungeStep());
			} else if (e.getKeyCode() == KeyEvent.VK_E) {
				process.addChild(new ConcatMungeStep());
			}
		}
	}
	
	
	
	/////////////////////////////////////////////////////////////////
	//        Code to handle the scrollPane                       //
	//    Most of it was shamelessly stolen from the playpen     //
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
    
    /**
	 * If some mungepen components get dragged into a negative range all of them are then shifted
	 * so that the lowest x and y values are 0.  The components will retain their relative location.
	 *
	 * If this function is moved into a layout manager it causes problems with undo because we do
	 * no know when this gets called.
	 */
	public void normalize() {
		if (normalizing) return;
		normalizing=true;
		int minX = 0;
		int minY = 0;

		for (Component com : getComponents()) {
			if (!(com instanceof IOConnector)) {
				minX = Math.min(minX, com.getX());
				minY = Math.min(minY, com.getY());
			}
		}

		//Readjust
		if ( minX < 0 || minY < 0 ) {
			for (Component com : getComponents()) {
				if (!(com instanceof IOConnector)) {
					com.setLocation(com.getX()-minX, com.getY()-minY);
				}
			}
			revalidate();
		}
		normalizing = false;
	}

    
    ///////////////////////////Listener for MatchMaker Rule Set /////////////////////////////////

    private class MungePenMatchRuleSetListener implements MatchMakerListener<MungeProcess, MungeStep> {
		public void mmChildrenInserted(MatchMakerEvent<MungeProcess, MungeStep> evt) {
			
			for (int x : evt.getChangeIndices()) {
				evt.getSource().getChildren().get(x).addMatchMakerListener(mungeStepListener);
				AbstractMungeComponent mcom = MungeComponentFactory.getMungeComponent(evt.getSource().getChildren().get(x), 
						handler, process.getSession(), stepsProperties);
				modelMap.put(evt.getSource().getChildren().get(x), mcom);
				add(mcom);
			}
			
			//This is done in an other loop to ensure that all the MungeComponets have been mapped
			for (int y : evt.getChangeIndices()) {
				MungeStep ms = evt.getSource().getChildren().get(y);
				for (int x = 0; x < ms.getInputs().size(); x++) {
					MungeStepOutput link = ms.getInputs().get(x);
					if (link != null) {
						MungeStep parent = (MungeStep)link.getParent();
						int parNum = parent.getChildren().indexOf(link);
						IOConnector ioc = new IOConnector(modelMap.get(parent),parNum,modelMap.get(ms),x);
						add(ioc);

					}
				}
			}
		}
	
		public void mmChildrenRemoved(MatchMakerEvent<MungeProcess, MungeStep> evt) {
			
			for (MungeStep ms : evt.getChildren()) {
				AbstractMungeComponent mcom = modelMap.remove(ms);
				ms.removeMatchMakerListener(mungeStepListener);
				mcom.removeAllListeners();
				remove(mcom);
			}
			
			repaint();
		}
	
		public void mmPropertyChanged(MatchMakerEvent<MungeProcess, MungeStep> evt) {
			repaint();
		}
	
		public void mmStructureChanged(MatchMakerEvent<MungeProcess, MungeStep> evt) {
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
					if (evt.getNewValue() instanceof MungeStepOutput) {
						logger.debug("connection Caught");
						//connected and input
						MungeStepOutput mso = (MungeStepOutput)evt.getNewValue();
						
						logger.debug("mos " + mso);
						
						MungeStep child = evt.getSource();
						MungeStep parent = (MungeStep)mso.getParent();
						int parNum = parent.getChildren().indexOf(mso);
						int childNum = evt.getChangeIndices()[0];
						
						logger.debug("parNum: " + parNum + " childNum " + childNum);
						
						
						IOConnector ioc = new IOConnector(modelMap.get(parent),parNum,modelMap.get(child),childNum);
						add(ioc);

					} else {
						//assuming it is of type input
												
					}
				} else if (evt.getNewValue() == null && evt.getOldValue() != null) {
					if ( evt.getOldValue() instanceof MungeStepOutput) {
						//disconnect input
						MungeStepOutput mso = (MungeStepOutput)evt.getOldValue();
						MungeStep child = evt.getSource();
						MungeStep parent = (MungeStep)mso.getParent();
						int parNum = parent.getChildren().indexOf(mso);
						int childNum = evt.getChangeIndices()[0];
						IOConnector ioc = new IOConnector(modelMap.get(parent),parNum,modelMap.get(child),childNum);
						
						//This stupid loop is needed because remove uses direct comparision
						for (IOConnector con : getConnections()) {
							if (con.equals(ioc)) {
								remove(con);
							}
						}
						logger.debug("Line deleted");
					} else {
						//assuming it is of type input
						
					}

				} 
			}
			repaint();
		}

		public void mmStructureChanged(MatchMakerEvent<MungeStep, MungeStepOutput> evt) {
			repaint();
		}
    }


    /**
     * Returns the mungeComponet at the given point.
     * 
     * @param p The given point
     * @return The MungeComponent
     */
	public AbstractMungeComponent getMungeComponentAt(Point p) {
		AbstractMungeComponent sel = null;
		for (Component com : MungePen.this.getComponents()) {
			if (com.getBounds().contains(p) && com instanceof AbstractMungeComponent) {
				if (sel == null || getLayer(com) > getLayer(sel))
				{
					sel = (AbstractMungeComponent)com;
				}
			}
		}
		return sel;
	}

	public void stopConnection() {
		start = null;
		finish = null;
	}
	
}
