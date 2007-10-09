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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.event.MatchMakerEvent;
import ca.sqlpower.matchmaker.event.MatchMakerListener;
import ca.sqlpower.matchmaker.munge.InputDescriptor;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.munge.MungeStepOutput;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.matchmaker.swingui.MatchMakerTreeModel;
import ca.sqlpower.util.WebColour;
import ca.sqlpower.validation.swingui.FormValidationHandler;

public abstract class AbstractMungeComponent extends JPanel {
	
	
	private static  final Logger logger = org.apache.log4j.Logger.getLogger(AbstractMungeComponent.class); 
		
	/**
	 * A Set of listeners that detect changes in the MungeSteps and redraws them
	 */
	private final MatchMakerListener<MungeStep, MungeStepOutput> stepEventHandler = new MatchMakerListener<MungeStep, MungeStepOutput>() {

		public void mmChildrenInserted(
				MatchMakerEvent<MungeStep, MungeStepOutput> evt) {
			repaint();
		}

		public void mmChildrenRemoved(
				MatchMakerEvent<MungeStep, MungeStepOutput> evt) {
			repaint();
		}

		public void mmPropertyChanged(
				MatchMakerEvent<MungeStep, MungeStepOutput> evt) {
			repaint();
		}

		public void mmStructureChanged(
				MatchMakerEvent<MungeStep, MungeStepOutput> evt) {
			repaint();
		}
		
	};
	
	protected JPanel content;
	private JPanel root;
	
	private final MungeStep step;
	
	private final Color bg;
	private final Color borderColour;
	
	private static final Image MMM_TOP = new ImageIcon(ClassLoader.getSystemResource("icons/mmm_top.png")).getImage(); 
	private static final Image MMM_BOT = new ImageIcon(ClassLoader.getSystemResource("icons/mmm_bot.png")).getImage(); 
	
	private static final ImageIcon EXPOSE_OFF = new ImageIcon(ClassLoader.getSystemResource("icons/expose_off.png"));
	private static final ImageIcon EXPOSE_ON = new ImageIcon(ClassLoader.getSystemResource("icons/expose_on.png"));
	
	private boolean expanded;
	
	private MatchMakerSwingSession session;
	
	private MungeComponentKeyListener mungeComKeyListener;

	private FormValidationHandler handler;
	
	private final JButton hideShow;

	/**
	 * Creates a AbstractMungeComponent for the given step that will be in the munge pen.
	 * Sets the background and border colours to given colours.
	 * 
	 * @param step The step connected to the UI
	 * @param border The colour for the border around the rectangle
	 * @param bg The background colour to the rectangle
	 */
	private AbstractMungeComponent(MungeStep step, Color border, Color bg) {
		
		this.borderColour = border;
		this.bg = bg;
		this.step = step;
		setVisible(true);
		
		mungeComKeyListener = new MungeComponentKeyListener();
		addKeyListener(mungeComKeyListener);
		
		step.addMatchMakerListener(stepEventHandler);
		setName(step.getName());
		
		int borderTop;
		if (!getStep().canAddInput() && getStep().getInputs().size() == 0) {
			borderTop = MMM_TOP.getHeight(null);
		} else {
			 borderTop = ConnectorIcon.getHandleInstance(Object.class).getIconHeight();
		}
		
		int borderBottom;
		if (getStep().getChildCount() == 0) {
			borderBottom = 0;
		} else {
			borderBottom = ConnectorIcon.getNibInstance(Object.class).getIconHeight();
		}
		setBorder(BorderFactory.createEmptyBorder(borderTop,1,borderBottom,MMM_TOP.getWidth(null)));
				
		
		setOpaque(false);
		setFocusable(true);
		
		Dimension ps = getPreferredSize();
		setBounds(0, 0, ps.width, ps.height);

		
		root = new JPanel();
		root.setLayout(new BorderLayout());
		JPanel tmp = new JPanel( new FlowLayout());
		tmp.add(new JLabel(step.getName()));
		
		content = buildUI();
		
		hideShow = new JButton(new HideShowAction());
		hideShow.setIcon(EXPOSE_OFF);
		
		//returning null will prevent the +/- button form showing up
		if (content != null) {
			JToolBar tb = new JToolBar();
			hideShow.setBorder(null);
			hideShow.addMouseListener(new MouseAdapter(){
				public void mouseEntered(MouseEvent e) {
					hideShow.setIcon(EXPOSE_ON);
					hideShow.setBorder(null);
				}
				
				public void mouseExited(MouseEvent e) {
					hideShow.setIcon(EXPOSE_OFF);
					hideShow.setBorder(null);
				}
			});
			
			tb.setBorder(null);
			tb.add(hideShow);
			tb.setFloatable(false);
			tmp.add(tb);
			content.setBackground(bg);
		}

		root.add(tmp,BorderLayout.NORTH);
		add(root);
		
		root.setBackground(bg);
		tmp.setBackground(bg);
		
		addMouseListener(new MungeComponentMouseListener());
		addMouseMotionListener(new MungeComponentMouseMoveListener());
		
		root.addComponentListener(new ComponentListener(){

			public void componentHidden(ComponentEvent e) {
			}

			public void componentMoved(ComponentEvent e) {
				getParent().repaint();
			}

			public void componentResized(ComponentEvent e) {
				getParent().repaint();
			}

			public void componentShown(ComponentEvent e) {
			}
		});

		addFocusListener(new FocusListener(){
			public void focusGained(FocusEvent e) {
				//if it is being deleted
				if (getParent() != null) {
					logger.debug("Gained focus");
					getParent().repaint();
	                MatchMakerTreeModel treeModel = (MatchMakerTreeModel) session.getTree().getModel();
	    	        TreePath menuPath = treeModel.getPathForNode(getStep());
	    	        session.getTree().setSelectionPath(menuPath);
				}
			}
			public void focusLost(FocusEvent e) {
				logger.debug("Lost focus");
				if (getParent() != null) {
					getParent().repaint();
				}
			}
		});
		
		expanded = false;
		
	}

	/**
	 * This returns the options for the munge step. This must be set individualy for each munge step.
	 * If null is returned no options will be show and there will be no +/- button
	 * 
	 * @return The option panel or null
	 */
	protected abstract JPanel buildUI();
	
	/**
	 * Creates a AbstractMungeComponent for the given step that will be in the munge pen, 
	 * setting default colours
	 * 
	 * @param step The step connecting to the UI
	 */
	public AbstractMungeComponent(MungeStep step, FormValidationHandler handler, MatchMakerSession session) {
		this(step, new WebColour("#dddddd"), new WebColour("#eeeeee"));
		this.session = (MatchMakerSwingSession)session;
		this.handler = handler;
	}
	
	public MatchMakerSession getSession() {
		return session;
	}
	
	public FormValidationHandler getHandler() {
		return handler;
	}
	
	/**
	 * Returns the point where the IOConnector's top part is, for the specified input number.
	 * This point is given relitive to this MungeComponet, to the MungePen
	 * use the translate method of the point to correct it.
	 * 
	 * @param inputNum The number of the IOConnector to find the position of 
	 * @return Point where the IOC is
	 */
	public Point getInputPosition(int inputNum) {
		int inputs = step.getInputs().size();
		
		int xPos = (int) (((double)(inputNum+1)/((double)inputs+1))*getWidth());
		return new Point(xPos,0);
	}
	
	/**
	 * Returns the point where the IOConnector's top part is, for the specified output number.
	 * This point is given relitive to this MungeComponet, to the MungePen
	 * use the translate method of the point to correct it.
	 * 
	 * @param inputNum The number of the IOConnector to find the position of 
	 * @return Point where the IOC is
	 */
	public Point getOutputPosition(int outputNum) {
		int outputs = step.getChildren().size();
		int xPos = (int) (((double)(outputNum+1)/((double)outputs+1))*getWidth());
		return new Point(xPos,getHeight() - getBorder().getBorderInsets(this).bottom);
	}
	
	/**
	 * Returns the step connected to the UI.
	 * @return The step
	 */
	public MungeStep getStep() {
		return step;
	}
	
	/**
	 * Returns the munge pen that this munge component is in.
	 * 
	 * @return The pen
	 */
	public MungePen getPen() {
		return (MungePen)getParent();
	}
	
	public boolean isExpanded() {
		return expanded;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		
		
		if (getPreferredSize().width != getWidth() || getPreferredSize().height != getHeight()) {
			setBounds(getX(), getY(), getPreferredSize().width, getPreferredSize().height);
			revalidate();
		}
		
		Insets border = getBorder().getBorderInsets(this);
		Dimension dim = getSize();
		dim.width -= border.left+border.right;
		dim.height -= border.top+border.bottom;
		
		int[] x = {0,MMM_TOP.getHeight(null)-1,dim.width-1,dim.width-1};
		int[] y = {MMM_TOP.getHeight(null)-1,0,0,MMM_TOP.getHeight(null)-1};
		g.setColor(borderColour);
		g.translate(0, border.top - MMM_TOP.getHeight(null));
		g.fillPolygon(x, y, 4);
		g.translate(0, -(border.top - MMM_TOP.getHeight(null)));
		g.drawImage(MMM_TOP, getWidth()-border.right-1, border.top - MMM_TOP.getHeight(null), null);
		g.drawImage(MMM_BOT, getWidth()-border.right-1, getHeight() - MMM_BOT.getHeight(null) - border.bottom - 1, null);

		
		
		
		for (int i = 0; i< getStep().getInputs().size(); i++) {
			int xPos = getInputPosition(i).x;
			Icon port = ConnectorIcon.getFemaleInstance(getStep().getInputDescriptor(i).getType());

			port.paintIcon(this, g, xPos, border.top - port.getIconHeight());
			
			if (getStep().getInputs().get(i) != null) {
				Icon handle = ConnectorIcon.getHandleInstance(getStep().getInputs().get(i).getType());
				handle.paintIcon(this, g, xPos, 0);
			}
		}
		
		for (int i = 0; i < getStep().getChildCount(); i++) {
			int xPos = getOutputPosition(i).x;
			Icon nib = ConnectorIcon.getNibInstance(getStep().getChildren().get(i).getType());
			nib.paintIcon(this, g, xPos, getHeight() - border.bottom - ConnectorIcon.NIB_OVERLAP);
		}
		
		g = g.create(border.left, border.top, getWidth()-border.right, getHeight()-border.bottom);
		g.setColor(bg);
		g.fillRect(0, 0, dim.width-1, dim.height-1);
		
	}
	
	/**
	 * Returns the list for Inputs from the step object.
	 * 
	 * @return the list
	 */
	public List<MungeStepOutput> getInputs() {
		return step.getInputs();
	}
	
	/**
	 * Returns the list for Outputs from the step object.
	 * 
	 * @return the list
	 */
	public List<MungeStepOutput> getOutputs() {
		return step.getChildren();
	}
	
	
	
	/**
	 * Returns the popup menu to display when this component is right clicked on.
	 * Defaults to just having a remove action, but can be over ridden to include more actions.
	 * 
	 * @return The popup menu
	 */
	public JPopupMenu getPopupMenu() {
		JPopupMenu ret = new JPopupMenu();
		JMenuItem rm = new JMenuItem(new AbstractAction(){

			public void actionPerformed(ActionEvent e) {
				remove();
			}
			
		});
		
		rm.setText("Delete (del)");
		ret.add(rm);
		return ret;
	}
	
	/**
	 * Passes a key event to the AbstractMungeComponent, this is only passed if this
	 * AbstractMungeComponent is selected.
	 * 
	 * @param e The event
	 */
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_DELETE) {
			remove();
		}
	}
	
	/**
	 * Removes the this MC and all connected lines. And connects the ends.
	 * This should only be called of there is one input and one output 
	 */
	public void removeSingle() {
		getPen().removeMungeStepSingles(getStep());
	}
	
	/** 
	 * Removes this munge step and disconnects all input and output IOCs
	 */
	public void removeNormal() {
		getPen().removeMungeStep(getStep());
	}
	
	/**
	 * The remove action for the component. This defaults to normal deletion, but
	 * can be over ridden to use the delete single or a custom action (not recommended).
	 */
	public void remove() {
		removeNormal();
		MungePen.removeAllListeners(hideShow);
	}
	
	
	/**
	 * The action to control the +/- button
	 */
	private class HideShowAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			if (!expanded) {
				root.add(content,BorderLayout.CENTER);
				expanded = true;
			} else {
				root.remove(content);
				expanded = false;
			}
			getPen().normalize();
			validate();
			root.updateUI();
		}	
	}

	
	/**
	 * An action that can be added to the JPanel in buildUI that will add an input to the mungeStep.
	 * This should only be used if there is a variable number of inputs allowed
	 */
	protected class AddInputAction extends AbstractAction {
		
		/**
		 * Constructs the action.
		 * 
		 * @param title The string on the button
		 */
		public AddInputAction(String title) {
			super(title);
		}

		public void actionPerformed(ActionEvent e) {
			InputDescriptor ref = getStep().getInputDescriptor(0);
			getStep().addInput(new InputDescriptor(ref.getName(),ref.getType()));
			getParent().repaint();
		}
	}
	
	/**
	 * An action that can be added to the JPanel in buildUI that will remove all unused outputs from the mungeStep.
	 * This should only be used if there is a variable number of inputs allowed
	 */
	protected class RemoveUnusedInputAction extends AbstractAction {
		
		/**
		 * Constructs the action.
		 * 
		 * @param title The string on the button
		 */
		RemoveUnusedInputAction(String title) {
			super(title);
		}

		public void actionPerformed(ActionEvent e) {
			MungeStep step = getStep();
			
			for (int x = 0; x< step.getInputs().size();x++) {
				int y;
				if (step.getInputs().get(x) != null) {
					for (y=x-1; y>=0 && step.getInputs().get(y) == null; y--);
					y++;
					if (y != x) {
						step.connectInput(y, step.getInputs().get(x));						
						List<IOConnector> lines = getPen().getConnections();
						for (IOConnector ioc : lines) {
							if (ioc.getChildCom().equals(AbstractMungeComponent.this) && ioc.getChildNumber() == x) {
								ioc.remove();
							}
						}
					}
				}
			}
			
			for (int x = step.getInputs().size()-1;x>0 && step.getInputs().get(x) == null;x--) {
				step.removeInput(x);
			}			
			
			//cleans up the lines, Some of the IOCs may have made their bounds smaller during the call
			//and that bit of the line might still be on the screen, this gets rig of it. May other things 
			//were tried to get rid of it before this was used.
			SwingUtilities.invokeLater(new Runnable(){
				public void run() {
					getPen().repaint();
				}
			});
		}
	}
	
	private class MungeComponentKeyListener extends KeyAdapter {
		@Override
		public void keyPressed(KeyEvent e) {
			super.keyPressed(e);
			
			if (e.getKeyCode() == KeyEvent.VK_DELETE) {
				remove();
			}
		}
	}
	private Point diff;
	
	private class MungeComponentMouseListener implements MouseListener {

		
		public void mouseClicked(MouseEvent e) {
			maybeShowPopup(e);
		}

		public void mouseEntered(MouseEvent e) {
			
		}

		public void mouseExited(MouseEvent e) {
		}

		public void mousePressed(MouseEvent e) {
			logger.debug("MousePressed");
			getPen().bringToFront(AbstractMungeComponent.this);
			if (!maybeShowPopup(e)) {
				diff = new Point((int)(e.getPoint().getX() - getX()), (int)(e.getPoint().getY() - getY()));			
				diff.translate(getX(), getY());
				if (!checkForIOConnectors(new Point(e.getX() + getX(),e.getY()+getY()))) {
					requestFocusInWindow();
				}
			}
		}

		public void mouseReleased(MouseEvent e) {
			if (!maybeShowPopup(e) && getPen().isConnecting()) {
				Point abs = new Point(e.getX() + getX(),e.getY()+getY());
				AbstractMungeComponent amc = getPen().getMungeComponentAt(abs);
				if (amc == null || !amc.checkForIOConnectors(abs)) {
					getPen().requestFocusInWindow();
				}
				getPen().repaint();
			}
			getPen().normalize();
			getPen().stopConnection();
			getPen().revalidate();
		}
		
	}
	
	/**
	 * Possibly show the popup.
	 * 
	 * @param e The mouse event
	 * @return true iff the popup was shown
	 */
	public boolean maybeShowPopup(MouseEvent e) {
		if (e.isPopupTrigger()) {
			JPopupMenu pop = getPopupMenu();
			if (pop != null) {
				pop.show(AbstractMungeComponent.this, e.getX(), e.getY());
				requestFocusInWindow();
			}
			return true;
		}
		return false;
	}
	
	private class MungeComponentMouseMoveListener extends MouseMotionAdapter {
		@Override
		public void mouseDragged(MouseEvent e) {			
			Point mouse = e.getPoint();
			mouse.x += getX();
			mouse.y += getY();
			
			MungePen parent = (MungePen)getParent();
			if (mouse.x < 0 || mouse.y < 0) {
				return;
			}
			
			if (!parent.isConnecting()) {
				e.translatePoint(getX(), getY());
				setLocation((int)(e.getX() - diff.getX()), (int)(e.getY()-diff.getY()));
			} else {
				parent.mouseX = e.getX() + getX();
				parent.mouseY = e.getY() + getY();
			}
			parent.repaint();
		}
		
		@Override
		public void mouseMoved(MouseEvent e) {
			getPen().mouseX = e.getX() + getX();
			getPen().mouseY = e.getY() + getY();
		}
	}

	public Color getBg() {
		return bg;
	}
	
	//checks to see if the mouse was near an IOC point
	public boolean checkForIOConnectors(Point mousePoint) {
		logger.debug("Checking for IOConnections");
		
		// this is the maximum distance squared where a connection
		// would be considered as a hit. having it squared avoids
		// square rooting in the loop.
		int tolerance = 225;
	
		int inputs = getStep().getInputs().size();

		MungePen parent = getPen();
		
		int minDist = tolerance;
		int minNum = -1;
		
		for (int x = 0;x<inputs;x++) {
			Point p = getInputPosition(x);
			p.translate(getX(), getY());
			logger.debug("\nPoint: " + p + "\nMouse: " + mousePoint);
			int dist = Math.abs(p.x - mousePoint.x)*Math.abs(p.x - mousePoint.x) + Math.abs(p.y - mousePoint.y)*Math.abs(p.y - mousePoint.y); 
			if (dist < tolerance && dist < minDist) {
				minDist = dist;
				minNum = x;

			}
		}
		
		if (minNum != -1) {
			parent.connectionHit(this, minNum, true);
			return true;
		}
		
		for (int x = 0;x<getOutputs().size();x++) {
			Point p = getOutputPosition(x);
			p.translate(getX(), getY());
			logger.debug("\n" + p + "\n" + mousePoint);
			int dist = Math.abs(p.x - mousePoint.x)*Math.abs(p.x - mousePoint.x) + Math.abs(p.y - mousePoint.y)*Math.abs(p.y - mousePoint.y); 
			if (dist < tolerance && dist < minDist) {
				minDist = dist;
				minNum = x;

			}
		}
		
		if (minNum != -1) {
			parent.connectionHit(this, minNum, false);
			return true;
		}

		return false;
	}	
}

