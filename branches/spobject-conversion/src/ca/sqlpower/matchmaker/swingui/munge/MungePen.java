/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of DQguru
 *
 * DQguru is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DQguru is distributed in the hope that it will be useful,
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
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.Autoscroll;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.geom.CubicCurve2D;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.munge.AbstractMungeStep;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.munge.MungeStepOutput;
import ca.sqlpower.matchmaker.swingui.LabelPane;
import ca.sqlpower.matchmaker.swingui.SwingSessionContext;
import ca.sqlpower.matchmaker.swingui.action.AddLabelAction;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.util.TransactionEvent;
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

public class MungePen extends JLayeredPane implements Scrollable, DropTargetListener, Autoscroll {
	
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
	 * The max distance from the side the mouse can be to start an autoscroll
	 */
	public static final int AUTO_SCROLL_INSET = 25; 
	
	/**
	 * The speed at which auto scroll happens
	 */
	private static final int AUTO_SCROLL_SPEED = 10;
	
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
	 * This boolean indicates if the labels can move multiple components at once.
	 */
	private boolean moveMutiples;
	
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
	
	private final MungePenMungeStepListener mungeStepListener = new MungePenMungeStepListener();
	
	private final FormValidationHandler handler;
	
	private Map<MungeStep,AbstractMungeComponent> modelMap = new HashMap<MungeStep, AbstractMungeComponent>();

	/**
	 * Sets the default autoScroll bounds
	 */
	private Insets lockedScrollInsets = new Insets(AUTO_SCROLL_INSET,AUTO_SCROLL_INSET, AUTO_SCROLL_INSET, AUTO_SCROLL_INSET);

	/**
	 * checks if the bounds are locked see {@link #lockAutoScroll(boolean)} for the reason for locking these 
	 */
	private boolean lockScrollInsets = false;
	
	/**
	 * The magic point that tricks the getPreferedSize into thinking that it is a component.
	 * 
	 * This is done to allow the pen to auto scroll to area that does not exist.
	 */
	private Point magic = new Point(0,0);
	
	/**
	 * This is the number that will be used by the JLayeredPane, in order to 
	 * correctly add Labels to the munge pen.
	 */
	public static int LABELS_LAYER = new Integer(Integer.MIN_VALUE);
	
	
	private List<LabelPane> labelsList;
	
    /**
     * Previewer for the process in this editor.
     */
    private final MungePreviewPanel preview;
	
	/**
	 * Creates a new empty mungepen.
	 * 
	 */
	public MungePen(MungeProcess process, FormValidationHandler handler, Project project) throws SQLObjectException {
		this.handler = handler;
		this.process = process;
		
		setFocusable(true);
		setBackground(Color.WHITE);
		setOpaque(true);

		buildComponents(process);
		buildPopup(((SwingSessionContext)process.getSession().getContext()).getStepMap());
	
		normalizing = false;
		
		addMouseListener(new MungePenMouseListener());
		addMouseMotionListener(new MouseMotionAdapter(){
			@Override
			public void mouseMoved(MouseEvent e) {
				//resets the magic point to allow the excess space to be removed
				magic.x = 0;
				magic.y = 0;
				mouseX = e.getX();
				mouseY = e.getY();
				if (start != null || finish != null) {
					repaint();
				}
			}
		});
		process.addSPListener(new MungePenMungeProcessListener());
		
		setDropTarget(new DropTarget(this,this));
		
		addContainerListener(new ContainerAdapter(){
			@Override
			public void componentRemoved(ContainerEvent e) {
				super.componentRemoved(e);
				if (e.getChild() instanceof AbstractMungeComponent) {
					removeAllValidation(((AbstractMungeComponent)e.getChild()).content);
				}
				removeAllValidation(e.getChild());
			}
		});
		
		addKeyListener(new KeyListener() {
			
			public void keyTyped(KeyEvent e) {
			}
		
			public void keyReleased(KeyEvent e) {
				if((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) == 0){
					moveMutiples = false;
				}
			}
		
			public void keyPressed(KeyEvent e) {
				if((e.getModifiersEx() & KeyEvent.ALT_DOWN_MASK) != 0){
					moveMutiples = true;
				}
			}
		
		});
		
		labelsList = new ArrayList<LabelPane>();
		
		preview = new MungePreviewPanel(process, this);
		add(preview.getPanel(), new Integer(1));
	}
	
	/**
	 * This will return a list of LabelPane objects that have been added to
	 * MungePen. Even thought we add all of these components to the MungePen itself, it
	 * would be nice to have this alternate place to search for the labels, instead of
	 * traversing every component in the MungePen and picking out all of the labels.
	 * This is normally used to switch the z-ordering of the labels in the MungePen,
	 * yet keep them isolated from all of the other munge components.
	 * @return
	 */
	public List<LabelPane> getLabels(){
		return this.labelsList;
	}
	
	/**
	 * Calls cleanup methods on some of its children. Removing listeners is also
	 * done here.
	 */
	public void cleanup() {
		preview.cleanup();
	}
	
	/**
	 * This method will find the highest layer of all the labels in the MungePen.
	 * This method will be used to bring a label to the top of it's own layer in order
	 * to avoid overlapping with other Munge Components in the MungePen.
	 * @return The layer index of the label with the highest z-ordering.
	 */
	public int findHighestLabelLayer() {
		int highest = Integer.MIN_VALUE;
		for(LabelPane label : getLabels()){
			if(getLayer(label) > highest){
				highest = getLayer(label);
			}
		}
		return highest+1;
	}

	
	/**
	 * Tells us if we can move multiple components or not.
	 * @return
	 */
	public boolean getMoveMultiples() {
		return this.moveMutiples;
	}
	
	/**
	 * Tries to remove all of the validation from the removed coponent.
	 * @param com
	 */
	private void removeAllValidation(Component com) {
		if (com instanceof JComponent) {
			JComponent jcom = (JComponent) com;
			this.handler.removeValidateObject(jcom);
		}
		
		if (com instanceof Container) {
			for (Component c : ((Container)com).getComponents()) {
				removeAllValidation(c);
			}
		}
	}
	
	private JPopupMenu buildPopup(Map<Class, StepDescription> stepMap) {
		JPopupMenu pop = new JPopupMenu();
		
		JMenu add = new JMenu("Add Transformer");
		JMenuItem labelMenuItem = new JMenuItem("Add Label");
		//figure out where the user clicked
		
		PointerInfo pi = MouseInfo.getPointerInfo();
        Point startLocation = pi.getLocation();
        SwingUtilities.convertPointFromScreen(startLocation,this);
        labelMenuItem.addActionListener(new AddLabelAction(this, startLocation));
		pop.add(add);
		pop.add(labelMenuItem);
		
		StepDescription[] sds = stepMap.values().toArray(new StepDescription[0]);
		Arrays.sort(sds);
		for (StepDescription sd : sds) {
			JMenuItem item = new JMenuItem(sd.getName(), sd.getIcon());
			item.addActionListener(new AddStepActon(sd.getLogicClass()));
			add.add(item);
		}
		
		return pop;
	}
	
	private class AddStepActon extends AbstractAction {
		Class<? extends MungeStep> logic;
		
		public AddStepActon(Class<? extends MungeStep> logic) {
			this.logic = logic;
		}

		public void actionPerformed(ActionEvent e) {
			addMungeStep(logic, new Point(mouseX,mouseY));
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
		for (MungeStep ms : process.getChildren(MungeStep.class)) {
			ms.addSPListener(mungeStepListener);
			
			for(AbstractMungeStep.Input in : ms.getMungeStepInputs()) {
				in.addSPListener(mungeStepListener);
			}
			
			SwingSessionContext ssc = ((SwingSessionContext) process.getSession().getContext());
			AbstractMungeComponent mcom = ssc.getMungeComponent(ms, handler, process.getSession());
			modelMap.put(ms, mcom);
			add(mcom,DEFAULT_LAYER);
		}
		
		//This is done in an other loop to ensure that all the MungeComponets have been mapped
		for (MungeStep ms : process.getChildren(MungeStep.class)) {
			for (int x = 0; x < ms.getMSOInputs().size(); x++) {
				MungeStepOutput link = ms.getMSOInputs().get(x);
				if (link != null) {
					MungeStep parent = (MungeStep)link.getParent();
					int parNum = parent.getChildren().indexOf(link);
					if (parNum < 0) {
					    throw new IllegalStateException("Unable to find munge step output \"" + link + "\" of \"" + parent + "\"");
					}
					IOConnector ioc = new IOConnector(modelMap.get(parent),parNum,modelMap.get(ms),x);
					add(ioc);
					modelMap.get(parent).setConnectOutput(parNum, true);
				}
			}
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
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		//Auto Scroll bounds
		if (logger.isDebugEnabled()) {
			g.drawLine(getWidth() - getAutoscrollInsets().right, 0, getWidth() - getAutoscrollInsets().right, getHeight());
			g.drawLine(getAutoscrollInsets().left, 0, getAutoscrollInsets().left, getHeight());
			g.drawLine(0, getHeight() - getAutoscrollInsets().bottom, getWidth(), getHeight() - getAutoscrollInsets().bottom);
			g.drawLine(0,getAutoscrollInsets().top, getWidth(), getAutoscrollInsets().top);
		}
	}
	
	@Override
	public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		super.paint(g2);
		paintPendingConnection(g2);
		
		MungeStep selectedStep = getSelectedStep();
		if (selectedStep != null && selectedStep != preview.getLastModifiedOrSelectedStep()) {
			preview.stepSelectedOrModified(selectedStep);
		}

	}
	
	private void paintPendingConnection(Graphics2D g) {
		Point end = getClosestIO(new Point(mouseX,mouseY));
		boolean snap = true;
		
		if (end == null) {
			end = new Point(mouseX,mouseY);
			snap = false;
		}
		
		if (start != null || finish != null) {
			Point fixed;
			ConnectorIcon plug = null;
			if (start != null) {
				fixed = start.getOutputPosition(startNum);
				fixed.translate(start.getX(), start.getY());
				if (!snap) {
					plug = ConnectorIcon.getFullPlugInstance(start.getStep().getChildren(MungeStepOutput.class).get(startNum).getType());
				} else {
					plug = ConnectorIcon.getHandleInstance(start.getStep().getChildren(MungeStepOutput.class).get(startNum).getType());
				}
			} else {
				Icon fixedPlug = ConnectorIcon.getHandleInstance(finish.getStep().getInputDescriptor(finishNum).getType());
				fixed = finish.getInputPosition(finishNum);
				fixed.translate(finish.getX(), finish.getY());
				fixedPlug.paintIcon(this, g, fixed.x, fixed.y);
			}
			g.setColor(Color.BLACK);
			
			if (plug != null) {
				int dragPlugOffset;
				if (!snap) {
					dragPlugOffset = plug.getIconHeight()/2;
				} else {
					dragPlugOffset = 0;
				}
                CubicCurve2D c = IOConnector.createConnectorPath(
                        fixed.x,fixed.y, end.x, end.y - dragPlugOffset);
				g.draw(c);
				plug.paintIcon(this, g, end.x, end.y - dragPlugOffset);
			} else {
                CubicCurve2D c = IOConnector.createConnectorPath(
                        end.x, end.y, fixed.x, fixed.y);
                g.draw(c);
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

	private void addMungeStep(Class<? extends MungeStep> logicClass, Point location) {
		MungeStep ms = ((SwingSessionContext)process.getSession().getContext()).getMungeStep(logicClass);
		int x = location.x + COM_DROP_OFFSET_X;
		int y = location.y + COM_DROP_OFFSET_Y;
		ms.setParameter(MungeStep.MUNGECOMPONENT_X, x);
		ms.setParameter(MungeStep.MUNGECOMPONENT_Y, y);
		process.addChild(ms);
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
		if (start != null && finish != null && finish.getStep().getMSOInputs().get(finishNum) == null) {
			logger.debug("Connection checking startNum:" + startNum + " EndNum " + finishNum);
			Class startHas = start.getStep().getChildren(MungeStepOutput.class).get(startNum).getType();
			Class finishWants = finish.getStep().getInputDescriptor(finishNum).getType();
			logger.debug(startHas + " -> " + finishWants);
			if (startHas.equals(finishWants) || finishWants.equals(Object.class)) {
				logger.debug("Connecting");
				finish.getStep().connectInput(finishNum,start.getStep().getChildren(MungeStepOutput.class).get(startNum));
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
			repaint();
			requestFocusInWindow();
			maybeShowPopup(e);
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {
			maybeShowPopup(e);
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				JPopupMenu pop = buildPopup(((SwingSessionContext)process.getSession().getContext()).getStepMap());
				pop.show(MungePen.this, (int)e.getX(), (int)e.getY());
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
			ppSize = new Dimension(Math.max(magic.x,Math.max(usedSpace.width, vpSize.width)),
					Math.max(magic.y,Math.max(usedSpace.height, vpSize.height)));
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
    private class MungePenMungeProcessListener implements SPListener {
		
    	@Override
		public void childAdded(SPChildEvent e) {
    		
			int x = e.getIndex();
			
			((MungeProcess)e.getSource()).getMungeSteps().get(x).addSPListener(mungeStepListener);
			SwingSessionContext ssc = (SwingSessionContext) process.getSession().getContext();
			AbstractMungeComponent mcom = (ssc.getMungeComponent(e.getSource().getChildren(MungeStep.class).get(x),
					handler, process.getSession()));
			modelMap.put(((MungeProcess)e.getSource()).getMungeSteps().get(x), mcom);
			add(mcom);
			logger.debug("Generating positions from properites");
			
			//This is done in an other loop to ensure that all the MungeComponets have been mapped
			int y = e.getIndex();
			MungeStep ms = ((MungeProcess)e.getSource()).getMungeSteps().get(y);
			for (int z = 0; z < ms.getMSOInputs().size(); z++) {
				MungeStepOutput link = ms.getMSOInputs().get(z);
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

		@Override
		public void childRemoved(SPChildEvent e) {
			MungeStep ms = (MungeStep)e.getChild();
			AbstractMungeComponent mcom = modelMap.remove(ms);
			ms.removeSPListener(mungeStepListener);
			removeAllListeners(mcom);
			remove(mcom);
			repaint();
		}

		@Override
		public void transactionStarted(TransactionEvent e) {
			//no-op
		}

		@Override
		public void transactionEnded(TransactionEvent e) {
			//no-op
		}

		@Override
		public void transactionRollback(TransactionEvent e) {
			//no-op
		}

		@Override
		public void propertyChanged(PropertyChangeEvent evt) {
			//no-op
		}
    }
	///////////////////////////////////// Listener for the MungeStep /////////////////////////
    

	/**
	 * The class for handling all of the actions fired by the MungeComponents. This takes care of adding and removing 
	 * IOCs that are no longer connected to anything. 
	 */
    private class MungePenMungeStepListener implements SPListener {

		@Override
		public void childAdded(SPChildEvent e) { 
			if(e.getSource() instanceof MungeStep && e.getChild() instanceof AbstractMungeStep.Input) {
				((MungeStep)e.getSource()).getMungeStepInputs().get(e.getIndex()).addSPListener(mungeStepListener);
			}
		}

		@Override
		public void childRemoved(SPChildEvent e) {
			if (e.getChild() instanceof AbstractMungeStep.Input &&
					e.getSource() instanceof MungeStepOutput) {
				logger.debug("connection removed");
				//disconnect input
				MungeStepOutput mso = (MungeStepOutput)((AbstractMungeStep.Input)e.getChild()).getCurrent();
				MungeStep child = (MungeStep)e.getSource();
				MungeStep parent = (MungeStep)mso.getParent();
				int parNum = parent.getChildren().indexOf(mso);
				int childNum = e.getIndex();
				
				//This stupid loop is needed because remove uses direct comparison
				for (IOConnector con : getConnections()) {
					if (con.getParentCom().equals(modelMap.get(parent)) 
							&& con.getChildCom().equals(modelMap.get(child))
							&& con.getParentNumber() == parNum && con.getChildNumber() == childNum) {
						remove(con);
						con.getParentCom().setConnectOutput(con.getParentNumber(), false);
					}
				}
			}
			SwingUtilities.invokeLater(new Runnable(){
				public void run() {
					repaint();
				}
			});
		}

		@Override
		public void transactionStarted(TransactionEvent e) {
			//no-op
		}

		@Override
		public void transactionEnded(TransactionEvent e) {
			//no-op
		}

		@Override
		public void transactionRollback(TransactionEvent e) {
			//no-op
		}

		@Override
		public void propertyChanged(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals("current") && evt.getSource() instanceof AbstractMungeStep.Input) {
				logger.debug("connection caught");
				//connected and input
				MungeStepOutput mso = ((AbstractMungeStep.Input)evt.getSource()).getCurrent();
				MungeStep child = (MungeStep)((AbstractMungeStep.Input)evt.getSource()).getParent();
				
				MungeStep parent = mso.getParent();
				int parNum = parent.getMungeStepOutputs().indexOf(mso);
				int childNum = child.getMungeStepInputs().indexOf((AbstractMungeStep.Input)evt.getSource());
				
				
				IOConnector ioc = new IOConnector(modelMap.get(parent),parNum,modelMap.get(child),childNum);
				add(ioc);
				modelMap.get(parent).setConnectOutput(parNum, true);
			}
			SwingUtilities.invokeLater(new Runnable(){
					public void run() {
						repaint();
					}
				});
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
	
	public AbstractMungeComponent getMungeComponent(MungeStep ms) {
		return modelMap.get(ms);
	}

	/**
     * Returns the input position for the closest input to the given point.
     * 
     * @param p The given point
     * @return The closest point or null if it is not near any of them 
     */
	public Point getClosestIO(Point p) {
		AbstractMungeComponent sel = getMungeComponentAt(p);
		if (sel == null) {
			return null;
		}
		
		if (start != null) {			
			int index = sel.getClosestIOIndex(p, AbstractMungeComponent.CLICK_TOLERANCE, true);
			
			if (index == -1) {
				return null;
			}
			
			Class startHas = start.getStep().getChildren(MungeStepOutput.class).get(startNum).getType();
			Class finishWants = sel.getStep().getInputDescriptor(index).getType(); 
			if (sel.getStep().getMSOInputs().get(index) == null && 
					(finishWants.equals(startHas) || finishWants.equals(Object.class))) {
				Point ret = sel.getInputPosition(index);
				ret.translate(sel.getX(), sel.getY());
				return ret;
			}
		} else if (finish != null){
			int index = sel.getClosestIOIndex(p, AbstractMungeComponent.CLICK_TOLERANCE, false);
			
			if (index == -1) {
				return null;
			}
			
			Class startHas = sel.getStep().getChildren(MungeStepOutput.class).get(index).getType();
			Class finishWants = finish.getStep().getInputDescriptor(finishNum).getType(); 
			if (finishWants.equals(startHas) || finishWants.equals(Object.class)) {
				Point ret = sel.getOutputPosition(index);
				ret.translate(sel.getX(), sel.getY());
				return ret;
			}	
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
	 * Returns the selected step in the munge pen. If no step is selected
	 * a null value will be returned.
	 */
	public MungeStep getSelectedStep() {
		for(Map.Entry<MungeStep, AbstractMungeComponent> entry : modelMap.entrySet()) {
			if (entry.getValue().hasFocus()) {
				return entry.getKey();
			}
		}
		return null;
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
		lockAutoScroll(false);
	}

	public void dragExit(DropTargetEvent dte) {
		lockAutoScroll(false);
	}

	public void dragOver(DropTargetDragEvent dtde) {
		lockAutoScroll(false);
	}

	public void drop(DropTargetDropEvent dtde) {
		magic = new Point(0,0);
		lockAutoScroll(false);
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
		magic = new Point(0,0);
	}
	
	/**
	 * Notifiys the pen of an auto scroll being done with a component that is not 
	 * floating. Meaning that it part of the pen, this is done so that the component 
	 * is also moved with the screen and not just left hanging
	 * 
	 * @param cursorLocn move point
	 * @param com Component to move
	 */
	public void autoscroll(Point cursorLocn, Component com) {
		logger.debug("AUTOSCROLL");
		lockAutoScroll(true);
		Insets autoScrollBorder = getAutoscrollInsets();
		Rectangle visRec = getVisibleRect();
		if (cursorLocn.x > getWidth() - autoScrollBorder.right) {
			if (magic.x > 0) {
				magic.x += AUTO_SCROLL_SPEED;
				lockedScrollInsets.right += AUTO_SCROLL_SPEED;
				revalidate();
			} else if (visRec.x + visRec.width + AUTO_SCROLL_SPEED > getWidth()) {
				magic.x = getWidth();
			}
			visRec.x += AUTO_SCROLL_SPEED;
			if (com != null) {
				com.setLocation(com.getX() + AUTO_SCROLL_SPEED, com.getY());
			}
		}
		if (cursorLocn.x < autoScrollBorder.left) {
			if (visRec.x == 0) {
				unNormalize(AUTO_SCROLL_SPEED, 0);
			} else {
				visRec.x -= AUTO_SCROLL_SPEED;
			}
			if (com != null) {
				com.setLocation(com.getX() - AUTO_SCROLL_SPEED, com.getY());
			}
		}
		if (cursorLocn.y < autoScrollBorder.top) {
			if (visRec.y == 0) {
				unNormalize(0, AUTO_SCROLL_SPEED);
			} else {
				visRec.y -= AUTO_SCROLL_SPEED;
			}
			if (com != null) {
				com.setLocation(com.getX(), com.getY() - AUTO_SCROLL_SPEED);
			}
		}
		if (cursorLocn.y > getHeight() - autoScrollBorder.bottom) {
			if (magic.y > 0) {
				magic.y += AUTO_SCROLL_SPEED;
				lockedScrollInsets.bottom += AUTO_SCROLL_SPEED;
				revalidate();
			} else if (visRec.y + visRec.height + AUTO_SCROLL_SPEED > getHeight()) {
				magic.y = getHeight();
			}
			visRec.y += AUTO_SCROLL_SPEED;			
			if (com != null) {
				com.setLocation(com.getX(), com.getY() + AUTO_SCROLL_SPEED);
			}
		}
		scrollRectToVisible(visRec);
		if (com != null) {
			com.repaint();
		}
	}

	public void autoscroll(Point cursorLocn) {
		autoscroll(cursorLocn, null);
	}
	
	/**
	 * moves all the components on the pen (excluding IOC's which take care of them selves)
	 * by the given amount.
	 * 
	 * @param x X!
	 * @param y Y!
	 */
	private void unNormalize(int x, int y) {
		for (Component com : getComponents()) {
			if (!(com instanceof IOConnector)) {
				com.setLocation(com.getX() + x,com.getY() + y);
			}
		}
	}

	public Insets getAutoscrollInsets() {
		if (!lockScrollInsets) {
			setAutoScrollInsets();
		}
		logger.debug(lockedScrollInsets);
		return lockedScrollInsets;
	}
	
	/**
	 * Forces an update to the autoScroll bounds.
	 * See {@link #lockAutoScroll(boolean)}
	 */
	public void setAutoScrollInsets() {
		Rectangle r = getVisibleRect();
		lockedScrollInsets = new Insets(r.y + AUTO_SCROLL_INSET, r.x  + AUTO_SCROLL_INSET, 
				getHeight() - r.y - r.height + AUTO_SCROLL_INSET, getWidth() - r.x - r.width + AUTO_SCROLL_INSET);
	}
	
	/**
	 * Locks/Unlocks the autoscroll insets so that they can not be changed. This is done
	 * because as the pen scroll in one direction, the bounds get adjusted to be only the
	 * {@link #AUTO_SCROLL_INSET} px from the edge, and the screen moves, but the point 
	 * that what ever is handling this is does not change and the bounds are wrong until the 
	 * mouse is moved, by locking the bounds to the old positions it can scroll forever.
	 * 
	 * @param lock Lock the border
	 */
	public void lockAutoScroll(boolean lock) {
		lockScrollInsets = lock;
	}
	
	public void updatePositionsToMMO () {
		List<AbstractMungeComponent> changedComponents = new ArrayList<AbstractMungeComponent>();
		for (Component com : getComponents()) {
			if (com instanceof AbstractMungeComponent) {
				AbstractMungeComponent mcom = (AbstractMungeComponent) com;
				if (mcom.hasUnsavedChanges()) {
					changedComponents.add(mcom);
				}
			}
		}
		if (changedComponents.size() > 0) {
			try {
				process.startCompoundEdit();
				for (AbstractMungeComponent com : changedComponents) {
					com.applyChanges();
				}
			} finally {
				process.endCompoundEdit();
			}
		}
	}
	
	/**
	 * Returns the check box that allows users to enable and disable the preview feature.
	 * @return
	 */
	public JCheckBox getEnablePreviewCheckBox() {
		return preview.getEnablePreviewCheckBox();
	}
}
