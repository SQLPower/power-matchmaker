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
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ComponentListener;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JLayeredPane;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.event.MatchMakerEvent;
import ca.sqlpower.matchmaker.event.MatchMakerListener;
import ca.sqlpower.matchmaker.munge.ConcatMungeStep;
import ca.sqlpower.matchmaker.munge.DoubleMetaphoneMungeStep;
import ca.sqlpower.matchmaker.munge.LowerCaseMungeStep;
import ca.sqlpower.matchmaker.munge.MetaphoneMungeStep;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.munge.MungeResultStep;
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
import ca.sqlpower.matchmaker.swingui.SwingSessionContext;
import ca.sqlpower.validation.swingui.FormValidationHandler;

/**
 * This class is responsible for maintaining the interactive GUI for a munge
 * process. It is used like any normal swing component and works best inside a
 * scroll pane. Normally the children of this class will be MungeComponents or
 * IOConnectors. There are several convenience methods for listing and removing
 * child components of those types.
 * <p>
 * This component installs itself as a listener to the MungeProcess it's editing,
 * and if the process or any of its munge steps are modified, the GUI will
 * automatically refresh accordingly.
 */

public class MungePen extends JLayeredPane implements Scrollable, DropTargetListener {
	
	private static  final Logger logger = Logger.getLogger(MungePen.class);

	/**
	 * The amount of offset in the X dir to move a newly dropped mungeComponent.
	 * This is needed because it drops them with respect to the top corner, which is transparent
	 * so it looks silly.
	 */
	private static final int COM_DROP_OFFSET_X = -30;

	/**
	 * The amount of offset in the X dir to move a newly dropped mungeComponent.
	 * This is needed because it drops them with respect to the top corner, which is transparent
	 * so it looks silly.
	 */
	private static final int COM_DROP_OFFSET_Y = -30; 
	
	/**
	 * The process this MungePen visualizes and edits.  This MungePen listens for various
	 * events on this process and all its children, and will update the GUI when important
	 * changes take place.
	 */
	private final MungeProcess process;
	
	/**
	 * holds the info for dragging a connection
	 * between two IOCs. This will be null if we are not in 
	 * the process of dragging a connection or the connection 
	 * was started from the input of a mungeComponet.
	 */ 
	private AbstractMungeComponent start;
	/**
	 * holds the info for dragging a connection
	 * between two IOCs. This will be null if we are not in 
	 * the process of dragging a connection or the connection 
	 * was started from the output of a mungeComponet.
	 */ 
	private AbstractMungeComponent finish;
	
	/**
	 * The index of the output that is being dragged. The value is meaningless when
	 * {@link #start} is null.
	 */
	private int startNum;
	
	/**
	 * The index of the input that is being dragged. The value is meaningless when
	 * {@link #finish} is null.
	 */
	private int finishNum;
	
	/**
	 * The current X value of the mouse.  Kept up to date by the mouse motion listener.
	 */
	public int mouseX;
	
	/**
	 * The current X value of the mouse.  Kept up to date by the mouse motion listener.
	 */
	public int mouseY;
	
	/**
	 * True iff the mungePen is in the normalizing.  This flag helps to prevent infinite
	 * recursion in the normalize process.
	 */
	private boolean normalizing;
	
	private final MungePenMungeStepListener mungeStepListener;
	
	private FormValidationHandler handler;
	
	private Map<MungeStep,AbstractMungeComponent> modelMap = new HashMap<MungeStep, AbstractMungeComponent>();
	
	/**
	 * Creates a new empty mungepen.
	 * 
	 */
	public MungePen(MungeProcess process, FormValidationHandler handler, Match match) throws ArchitectException {
		
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
		
		setDropTarget(new DropTarget(this,this));
		
		if (process.getChildCount() == 0) {
			MungeStep inputStep = new SQLInputStep(match.getSourceTable(), process.getSession());
			inputStep.setParameter(AbstractMungeComponent.MUNGECOMPONENT_EXPANDED, new Boolean(true).toString());
			process.addChild(inputStep);
			
			MungeResultStep mungeResultStep = new MungeResultStep(match, inputStep, process.getSession());
			
			String x = new Integer(0).toString();
			String y = new Integer(300).toString();
			
			mungeResultStep.setParameter(AbstractMungeComponent.MUNGECOMPONENT_X, x);
			mungeResultStep.setParameter(AbstractMungeComponent.MUNGECOMPONENT_Y, y);
			process.addChild(mungeResultStep);
			process.setOutputStep(mungeResultStep);
		}
	}
	
	/**
	 * Attaches this MungePen to the given munge process.  This entails creating a
	 * MungeComponent for every munge step in the process, then creating an IOConnector
	 * for every connected input in each step of the process.  Listeners are attached
	 * to all steps.
	 * <p>
	 * This is a constructor subroutine.  It is incorrect to call this method more
	 * than once (which the constructor will have already done).
	 * 
	 * @param process The process to attach.
	 */
	private void buildComponents(MungeProcess process) {
		for (MungeStep ms : process.getChildren()) {
			ms.addMatchMakerListener(mungeStepListener);
			
			SwingSessionContext ssc = ((SwingSessionContext) process.getSession().getContext());
			AbstractMungeComponent mcom = ssc.getMungeComponent(ms, handler, process.getSession());
			modelMap.put(ms, mcom);
			add(mcom,DEFAULT_LAYER);
			mcom.configureFromStepProperties();
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
					modelMap.get(parent).setConnectOutput(parNum, true);
				}
			}
		}
	}
	
	@Override
	public void moveToFront(Component c) {
		super.moveToFront(c);
		if (c instanceof AbstractMungeComponent) {
			AbstractMungeComponent mcom = (AbstractMungeComponent) c;
			process.getChildren().remove(mcom.getStep());
			process.getChildren().add(getIndexOf(c), mcom.getStep());
		}
	}
	
	/**
	 * Returns true iff there is currently a connection being made by the user.
	 */
	public boolean isConnecting() {
		return (start != null || finish != null);
	}
	
	/**
	 * Returns true iff there is currently a connection being made by the user,
	 * and that connection started from the given output (which means the output peg will
	 * be invisible).
	 * 
	 * @param com The component to check if it's the origin of a connection attempt
	 * @param num The output index of com to check if it's the origin of a connection
	 * attempt.
	 */
	public boolean isConnectingOutput(AbstractMungeComponent com, int num) {
		return (start == com && startNum == num);  
	}

	/**
	 * Intercepts add requests for AbstractMungeComponent implementations and IOConnectors,
	 * performing certain bookkeeping tasks to maintain class invariants.
	 */
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
	
	/**
	 * Paints the connection-in-progress graphics when necessary.  If debugging for
	 * this class is at DEBUG level or lower, also draws a green rectangle just inside the
	 * current clipping area of the given graphics.
	 */
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
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		paintPendingConnection(g);
	}
	
	private void paintPendingConnection(Graphics g) {
		Point end = getClosestInput(new Point(mouseX,mouseY));
		boolean snap = true;
		
		if (end ==null) {
			end = new Point(mouseX,mouseY);
			snap = false;
		}
		
		if (start != null || finish != null) {
			Point fixed;
			Icon plug = null;
			if (start != null) {
				fixed = start.getOutputPosition(startNum);
				fixed.translate(start.getX(), start.getY());
				if (!snap) {
					plug = ConnectorIcon.getFullPlugInstance(start.getStep().getChildren().get(startNum).getType());
				} else {
					plug = ConnectorIcon.getHandleInstance(start.getStep().getChildren().get(startNum).getType());
				}
			} else {
				fixed = finish.getInputPosition(finishNum);
				fixed.translate(finish.getX(), finish.getY());
			}
			g.setColor(Color.BLACK);
			if (plug != null) {
				int dragPlugOffset;
				if (!snap) {
					dragPlugOffset = plug.getIconHeight()/2;
				} else {
					dragPlugOffset = 0;
				}
				g.drawLine(fixed.x,fixed.y, end.x, end.y - dragPlugOffset);
				plug.paintIcon(this, g, end.x, end.y - dragPlugOffset);
			} else {
				g.drawLine(fixed.x,fixed.y, end.x, end.y);
			}
		}
	}

	/**
	 * Returns the list of all IOConnector components that belong to this MungePen.
	 * 
	 * @return A new list of all the IOConnectors in this MungePen. The list can
	 * safely be modified by client code (it is not cached by the MungePen).
	 */
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
	 * Removes the given munge step from this pen. This will remove the given
	 * munge step from its parent process as well as disconnect all its inputs.
	 * 
	 * @param ms
	 *            The step to be removed
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
	

	private void addMungeStep(Class logicClass, Point location) {
		MungeStep ms = ((SwingSessionContext)process.getSession().getContext()).getMungeStep(logicClass, 
				process.getSession());
		String x = new Integer(location.x + COM_DROP_OFFSET_X).toString();
		String y = new Integer(location.y + COM_DROP_OFFSET_Y).toString();
		ms.setParameter(AbstractMungeComponent.MUNGECOMPONENT_X, x);
		ms.setParameter(AbstractMungeComponent.MUNGECOMPONENT_Y, y);
		process.addChild(ms);
	}
	
	/**
	 * Removes the given munge step, but preserves the overall data flow by
	 * connecting the old source step to the old target step. It is an error to
	 * call this method for a munge step that doesn't have exactly one input and
	 * one output.
	 * 
	 * @param ms
	 *            mungestep to delete
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
	
	/**
	 * Called when the user clicks on a plug. This sets the start or finish variable to allow the 
	 * line and plug picture to be drawn while dragging the mouse.
	 * 
	 * @param mcom The MungeComponent that the connection was started with
	 * @param connectionNum The index of input or output that was clicked
	 * @param inputHit Set to true iff an input was clicked on
	 */
	public void startConnection(AbstractMungeComponent mcom, int connectionNum, boolean inputHit) {
		requestFocusInWindow();
		if (inputHit) {
			if (mcom.getInputs().size() > connectionNum && mcom.getInputs().get(connectionNum) != null) {
				for (IOConnector ioc :getConnections()) {
					if (ioc.getChildCom().equals(mcom) && ioc.getChildNumber() == connectionNum) {
						start = ioc.getParentCom();
						startNum = ioc.getParentNumber();
						ioc.remove();
						return;
					}
				}
			}
		}
		
		if (inputHit) {
			finish = mcom;
			finishNum = connectionNum;
		} else {
			start = mcom;
			startNum = connectionNum;
		}
	}
	
	/**
	 * Called when user has finished a connection on an input or output connector.
	 * This checks that the connection made was valid, and if so, the corresponding 
	 * MungeSteps in the munge process will be connected together.
	 * 
	 * @param mcom The MungeComponent that the connection was finished on
	 * @param connectionNum The index of input or output that was dragged to
	 * @param inputHit Set to true iff an input was dragged to
	 */
	public void finishConnection(AbstractMungeComponent mcom, int connectionNum, boolean inputHit) {
		requestFocusInWindow();
		getParent().repaint();
		
		if (inputHit) {
			finish = mcom;
			finishNum = connectionNum;
		} else {
			start = mcom;
			startNum = connectionNum;
		}
		
		//see if a connection is complete
		if (start != null && finish != null && finish.getStep().getInputs().get(finishNum) == null) {
			logger.debug("Connection checking startNum:" + startNum + " EndNum " + finishNum);
			Class startHas = start.getStep().getChildren().get(startNum).getType();
			Class finishWants = finish.getStep().getInputDescriptor(finishNum).getType();
			logger.debug(startHas + " -> " + finishWants);
			if (startHas.equals(finishWants) || finishWants.equals(Object.class)) {
				logger.debug("Connecting");
				finish.getStep().connectInput(finishNum,start.getStep().getChildren().get(startNum));
			}
			stopConnection();
		}
	}
	
	/**
	 * The mouse listener for the MungePen. It does a just in case repaint and 
	 * requests input focus when the user clicks.
	 */
	private class MungePenMouseListener extends MouseAdapter {
		public void mousePressed(MouseEvent e) {	
			logger.debug("Mouse PRess");
			repaint();
			requestFocusInWindow();
		}
	}
	
	/**
	 * Temp method for adding munge steps. This will be added to a toolbar, 
	 * with words so no guessing is needed
	 */
	class MungePenKeyListener extends KeyAdapter {
		//passes key press to selected components
		public void keyPressed(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_1) {
				process.addChild(new UpperCaseMungeStep(process.getSession()));
			} else if (e.getKeyCode() == KeyEvent.VK_2) {
				process.addChild(new LowerCaseMungeStep(process.getSession()));
			} else if (e.getKeyCode() == KeyEvent.VK_3) {
				process.addChild(new SoundexMungeStep(process.getSession()));
			} else if (e.getKeyCode() == KeyEvent.VK_4) {
				process.addChild(new RefinedSoundexMungeStep(process.getSession()));
			} else if (e.getKeyCode() == KeyEvent.VK_5) {
				process.addChild(new MetaphoneMungeStep(process.getSession()));
			} else if (e.getKeyCode() == KeyEvent.VK_6) {
				process.addChild(new DoubleMetaphoneMungeStep(process.getSession()));
			} else if (e.getKeyCode() == KeyEvent.VK_7) {
				process.addChild(new WordCountMungeStep(process.getSession()));
			} else if (e.getKeyCode() == KeyEvent.VK_8) {
				process.addChild(new TranslateWordMungeStep(process.getSession()));
			} else if (e.getKeyCode() == KeyEvent.VK_9) {
				process.addChild(new StringSubstitutionMungeStep(process.getSession()));
			} else if (e.getKeyCode() == KeyEvent.VK_0) {
				process.addChild(new SubstringMungeStep(process.getSession()));
			} else if (e.getKeyCode() == KeyEvent.VK_Q) {
				process.addChild(new RetainCharactersMungeStep(process.getSession()));
			} else if (e.getKeyCode() == KeyEvent.VK_W) {
				process.addChild(new SubstringByWordMungeStep(process.getSession()));
			} else if (e.getKeyCode() == KeyEvent.VK_E) {
				process.addChild(new ConcatMungeStep(process.getSession()));
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
		//return getPreferredSize();
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
	 * <p>
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

	/**
	 * The class for handling all of the actions fired by the MungeProcess. This takes care of adding and removing 
	 * MungeCompoents that are no longer in the process. 
	 */
    private class MungePenMatchRuleSetListener implements MatchMakerListener<MungeProcess, MungeStep> {
		public void mmChildrenInserted(MatchMakerEvent<MungeProcess, MungeStep> evt) {
			
			for (int x : evt.getChangeIndices()) {
				evt.getSource().getChildren().get(x).addMatchMakerListener(mungeStepListener);
				SwingSessionContext ssc = (SwingSessionContext) process.getSession().getContext();
				AbstractMungeComponent mcom = (ssc.getMungeComponent(evt.getSource().getChildren().get(x),
						handler, process.getSession()));
				modelMap.put(evt.getSource().getChildren().get(x), mcom);
				add(mcom);
				logger.debug("Generating positions from properites");
				mcom.configureFromStepProperties();
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
						logger.debug("parent: " + modelMap.get(parent) + "num: " + parNum);
						modelMap.get(parent).setConnectOutput(parNum, true);
					}
				}
			}
		}
	
		public void mmChildrenRemoved(MatchMakerEvent<MungeProcess, MungeStep> evt) {
			
			for (MungeStep ms : evt.getChildren()) {
				AbstractMungeComponent mcom = modelMap.remove(ms);
				ms.removeMatchMakerListener(mungeStepListener);
				removeAllListeners(mcom);
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
    

	/**
	 * The class for handling all of the actions fired by the MungeComponents. This takes care of adding and removing 
	 * IOCs that are no longer connected to anything. 
	 */
    private class MungePenMungeStepListener implements MatchMakerListener<MungeStep, MungeStepOutput> {

		public void mmChildrenInserted(MatchMakerEvent<MungeStep, MungeStepOutput> evt) {

		}

		public void mmChildrenRemoved(MatchMakerEvent<MungeStep, MungeStepOutput> evt) {
			
		}

		public void mmPropertyChanged(MatchMakerEvent<MungeStep, MungeStepOutput> evt) {
			if (evt.getPropertyName().equals("inputs")) {
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
						modelMap.get(parent).setConnectOutput(parNum, true);
					}						
				} else if (evt.getNewValue() == null && evt.getOldValue() != null) {
					if ( evt.getOldValue() instanceof MungeStepOutput) {
						//disconnect input
						MungeStepOutput mso = (MungeStepOutput)evt.getOldValue();
						MungeStep child = evt.getSource();
						MungeStep parent = (MungeStep)mso.getParent();
						int parNum = parent.getChildren().indexOf(mso);
						int childNum = evt.getChangeIndices()[0];
						
						//This stupid loop is needed because remove uses direct comparison
						for (IOConnector con : getConnections()) {
							if (con.getParentCom().equals(modelMap.get(parent)) 
									&& con.getChildCom().equals(modelMap.get(child))
									&& con.getParentNumber() == parNum && con.getChildNumber() == childNum) {
								logger.debug("Found it");
								remove(con);
								con.getParentCom().setConnectOutput(con.getParentNumber(), false);
							}
						}
						logger.debug("Line deleted");
					} 
				} 
			}
		}

		public void mmStructureChanged(MatchMakerEvent<MungeStep, MungeStepOutput> evt) {
			revalidate();
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

	/**
     * Returns the input position for the closest input to the given point.
     * 
     * @param p The given point
     * @return The closest point or null if it is not near any of them 
     */
	public Point getClosestInput(Point p) {
		if (start == null) {
			return null;
		}
		
		AbstractMungeComponent sel = getMungeComponentAt(p);
		if (sel == null) {
			return null;
		}
		
		int index = sel.getClosestIOIndex(p, AbstractMungeComponent.CLICK_TOLERANCE, true);
		
		if (index == -1) {
			return null;
		}
		
		Class startHas = start.getStep().getChildren().get(startNum).getType();
		Class finishWants = sel.getStep().getInputDescriptor(index).getType(); 
		if (sel.getStep().getInputs().get(index) == null && 
				(finishWants.equals(startHas) || finishWants.equals(Object.class))) {
			Point ret = sel.getInputPosition(index);
			ret.translate(sel.getX(), sel.getY());
			return ret;
		}
		return null;
	}
	
	/**
	 * Tells the mungePen that it is no longer connecting mungeSteps and to 
	 * stop drawing that line.
	 */
	public void stopConnection() {
		start = null;
		finish = null;
	}
	
	/**
	 * Assigns input focus to the given munge step's component.
	 * 
	 * @param ms The mungeStep to select.
	 */
	public void setSelectedStep(MungeStep ms) {
		modelMap.get(ms).requestFocusInWindow();
	}
	
	/**
	 * Removes all listeners associated with the given Component. This is useful when removing to to make sure
	 * it does not stick around.
	 */
	public static void removeAllListeners(Component com) {
		for (FocusListener fl : com.getFocusListeners()) {
			com.removeFocusListener(fl);
		}
		
		for (MouseListener ml : com.getMouseListeners()) {
			com.removeMouseListener(ml);
		}
		
		for (MouseMotionListener mml : com.getMouseMotionListeners()) {
			com.removeMouseMotionListener(mml);
		}
		
		for (KeyListener kl : com.getKeyListeners()) {
			com.removeKeyListener(kl);
		}
		
		for (ComponentListener cl : com.getComponentListeners()) {
			com.removeComponentListener(cl);
		}
	}

	public void dragEnter(DropTargetDragEvent dtde) {
	}

	public void dragExit(DropTargetEvent dte) {
	}

	public void dragOver(DropTargetDragEvent dtde) {
	}

	public void drop(DropTargetDropEvent dtde) {
		Transferable t = dtde.getTransferable();
		try {
			StepDescription sd = (StepDescription)t.getTransferData(MungeStepLibrary.STEP_DESC_FLAVOR);
			addMungeStep(sd.getLogicClass(), dtde.getLocation());
			repaint();
			dtde.dropComplete(true);
			return;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (UnsupportedFlavorException e) {
		}
		dtde.dropComplete(false);		
	}

	public void dropActionChanged(DropTargetDragEvent dtde) {
		
	}
}
