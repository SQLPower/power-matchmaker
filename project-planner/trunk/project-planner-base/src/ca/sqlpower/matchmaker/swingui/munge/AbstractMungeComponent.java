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

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.event.MatchMakerEvent;
import ca.sqlpower.matchmaker.event.MatchMakerListener;
import ca.sqlpower.matchmaker.munge.InputDescriptor;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.munge.MungeStepOutput;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.matchmaker.swingui.MatchMakerTreeModel;
import ca.sqlpower.validation.swingui.FormValidationHandler;

public abstract class AbstractMungeComponent extends JPanel {
	
	private static  final Logger logger = org.apache.log4j.Logger.getLogger(AbstractMungeComponent.class); 
	
	/**
	 * An enumeration that describes the different ways a mouse drag event can affect
	 * this component.
	 */
	private enum DragState {
		MOVING,
		RESIZING
	}
	
	/**
	 * The current mode of dragging.
	 */
	private DragState currentDragState = null;
		
	protected JPanel content;
	protected JPanel contentPlusNames;
	
	private JPanel root;
	
	private final MungeStep step;
	
	/**
	 * The point to auto scroll to
	 */
	private Point autoScrollPoint;
	
	/**
	 * The timer to handle when to autoscroll because always calling it is way too fast.
	 */
	private Timer autoScrollTimer;
	
	/**
	 * How often to call auto scroll
	 */
	public static final int AUTO_SCROLL_TIME = 55;
	
	
	/**
	 * The background colour to use when this component is not selected.
	 */
	protected Color normalBackground = new Color(0xee, 0xee, 0xee);
	
	/**
	 * The shadow colour to use when this component is not selected.
	 */
	private Color normalShadow = new Color(0xdd, 0xdd, 0xdd);
	
	/**
	 * The background colour to use when this component is selected.
	 */
	protected Color selectedBackground = new Color(0xc5, 0xdd, 0xf7);
	
	/**
	 * The shadow colour to use when this component is selected.
	 */
	private Color selectedShadow = new Color(0xb1, 0xc7, 0xdf);
	
	/**
	 * The set of component types that should not have their opaqueness fiddled
	 * with after the UI has been built.  See {@link #deOpaquify(Container)}
	 * for details.
	 */
	protected Set<Class<? extends JComponent>> opaqueComponents = new HashSet<Class<? extends JComponent>>();
	
	private static final Image MMM_TOP = new ImageIcon(AbstractMungeComponent.class.getClassLoader().getResource("icons/mmm_top.png")).getImage(); 
	private static final Image MMM_BOT = new ImageIcon(AbstractMungeComponent.class.getClassLoader().getResource("icons/mmm_bot.png")).getImage(); 
	
	private static final ImageIcon EXPOSE_OFF = new ImageIcon(AbstractMungeComponent.class.getClassLoader().getResource("icons/expose_off.png"));
	private static final ImageIcon EXPOSE_ON = new ImageIcon(AbstractMungeComponent.class.getClassLoader().getResource("icons/expose_on.png"));
	
	private static final ImageIcon PLUS_OFF = new ImageIcon(AbstractMungeComponent.class.getClassLoader().getResource("icons/plus_off.png"));
	private static final ImageIcon PLUS_ON = new ImageIcon(AbstractMungeComponent.class.getClassLoader().getResource("icons/plus_on.png"));
	
	private static final int PLUG_OFFSET = 2;
	public static final int CLICK_TOLERANCE = 15;
	

	/**
	 * The timer interval for the drop down. The length of time to wait before moving
	 * the nib down again.
	 */
	public static final int DROP_TIMER_INTERVAL = 100;
	
	/**
	 * The amount to drop the nib after each interval
	 */
	public static final int DROP_AMOUNT = 5;

	/**
	 * The dimensions of the resize icon in the bottom right of the munge components.
	 */
	public static final Dimension RESIZE_ICON_SIZE = new Dimension(15, 15);
	
	/**
	 * The image to be placed in the bottom right corner of the component to allow users
	 * to drag and resize the component.
	 */
	private static final Image RESIZE_ICON = new ImageIcon(AbstractMungeComponent.class.getClassLoader().getResource("icons/resize_icon.png")).getImage();
	
	private MatchMakerSwingSession session;
	
	private MungeComponentKeyListener mungeComKeyListener;

	private FormValidationHandler handler;
	
	private final int[] connected;
	
	/**
	 * The icon of the nib that is dropping down.
	 */
	private Icon dropNib;
	
	/**
	 * The current offset of the dropping nib, the number of pixles above where it will end up.
	 */
	private int dropNibOffSet;
	
	/**
	 * The index of the currently dropping nib
	 */
	private int dropNibIndex;
	
	/**
	 * The timer running the dropdown. Only one will exist at a time.
	 */
	private Timer dropNibTimer;
	
	/**
	 * The index of an input where the user's mouse is near. The paintComponent method
	 * will paint a semi transparent plug handle here if. Do nothing if it is -1 
	 */
	private int ghostIndex;
	
	/**
	 * A panel that goes on top and labels the inputs
	 */
	private JPanel inputNames;
	
	/**
	 * True iff a top panel is wanted displaying the names
	 */
	private boolean showInputNames;
	
	/**
	 * Holds the lables with the names of the inputs.
	 */
	private CoolJLabel[] inputLabels;
	
	
	/**
	 * A panel that goes on bottom and labels the outputs
	 */
	private JPanel outputNames;
	
	/**
	 * True iff a bottom panel is wanted displaying the names
	 */
	private boolean showOutputNames;
	
	/**
	 * Holds the lables with the names of the outputs.
	 */
	private CoolJLabel[] outputLabels;
	
	/**
	 * Creates a AbstractMungeComponent for the given step that will be in the munge pen.
	 * Sets the background and border colours to given colours.
	 * 
	 * @param step The step connected to the UI
	 * @param mainIcon the main icon for this munge component. May be null.
	 */
	private AbstractMungeComponent(MungeStep step, Icon mainIcon) {
        if (step == null) throw new NullPointerException("Null step");
		this.step = step;
		this.mainIcon = mainIcon;
		setVisible(true);
		setBackground(normalBackground);
		
		autoScrollTimer = new Timer(AUTO_SCROLL_TIME, new AbstractAction(){

			public void actionPerformed(ActionEvent e) {
				logger.debug("TIMER GO!!!");
				getPen().autoscroll(autoScrollPoint, AbstractMungeComponent.this);
			}});
		
		autoScrollTimer.stop();
		autoScrollTimer.setRepeats(true);
		
		dropNibIndex = -1;
		connected = new int[step.getChildCount()];
		for (int x = 0; x < connected.length;x++) {
			connected[x] = 0;
		}
		
		ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
		toolTipManager.setInitialDelay(0);
		
		bustGhost();
		
		mungeComKeyListener = new MungeComponentKeyListener();
		addKeyListener(mungeComKeyListener);
		
		step.addMatchMakerListener(new StepEventHandler());
		setName(step.getName());
		
		int borderTop;
		if (!getStep().canAddInput() && getStep().getMSOInputs().size() == 0) {
			borderTop = MMM_TOP.getHeight(null);
		} else {
			 borderTop = ConnectorIcon.getHandleInstance(Object.class).getIconHeight() + PLUG_OFFSET;
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
		root.setBackground(Color.GREEN);
		root.setLayout(new BorderLayout());
		
		
		JPanel tmp = new JPanel( new FlowLayout());
		tmp.setBackground(Color.BLUE);
		
		setupOpaqueComponents();
		content = buildUI();
		
		root.add(tmp,BorderLayout.CENTER);
		add(root);
		
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
				// weird part to prevent some weird behaviour from labels
				Object selectedObj = session.getTree().getSelectionPath().getLastPathComponent();
				if (getParent() != null && selectedObj.equals(getStep())) {
					getParent().repaint();
	                MatchMakerTreeModel treeModel = (MatchMakerTreeModel) session.getTree().getModel();
	    	        TreePath menuPath = treeModel.getPathForNode(getStep());
	    	        session.getTree().setSelectionPath(menuPath);
				}
			}
			public void focusLost(FocusEvent e) {
				if (getParent() != null) {
					getParent().repaint();
				} 
				if (autoScrollTimer.isRunning()) {
					autoScrollTimer.stop();
				}
			}
		});
		
		root.setOpaque(false);
		tmp.setOpaque(false);
		
		buildInputNamesPanel();
		buildOutputNamesPanel();
		
		if (content != null) {
			contentPlusNames = new JPanel(new BorderLayout());
			contentPlusNames.add(content, BorderLayout.CENTER);
			contentPlusNames.add(outputNames,BorderLayout.SOUTH);
			contentPlusNames.setOpaque(false);
			content.setOpaque(false);
		} else {
			contentPlusNames = null;
		}
		
		// Note, this does not take care of the content panel; only the basic
		// stuff added here in the constructor (most importantly, the +/- button)
		deOpaquify(this);
		deOpaquify(inputNames);
		setExpanded(true);
	}
	
	private void buildInputNamesPanel() {
		if (isExpanded() && showInputNames) {
			root.remove(inputNames);
		}
		inputNames = new JPanel();
		inputNames.setOpaque(false);
		inputNames.setLayout(new FlowLayout());

		inputLabels = new CoolJLabel[step.getMSOInputs().size()];
		
		for (int x = 0; x < inputLabels.length; x++) {
			InputDescriptor id = step.getInputDescriptor(x);
            inputLabels[x] = new CoolJLabel(id.getName(), id.getType());
			inputLabels[x].collapse();
			inputNames.add(inputLabels[x]);
			inputLabels[x].setOpaque(false);
		}
		
		if (isExpanded() && showInputNames) {
			root.add(inputNames, BorderLayout.NORTH);
			revalidate();
		}	
	}
	
    private void buildOutputNamesPanel() {
		outputNames = new JPanel();
		outputNames.setOpaque(false);
		outputNames.setLayout(new FlowLayout());

		outputLabels = new CoolJLabel[step.getChildCount()];
		
		for (int x = 0; x < outputLabels.length; x++) {
			MungeStepOutput out = step.getChildren().get(x);
            outputLabels[x] = new CoolJLabel(out.getName(), out.getType());
			outputLabels[x].collapse();
			outputNames.add(outputLabels[x]);
			outputLabels[x].setOpaque(false);
		}
	}

    /**
     * Returns the unqualified name of the given class (no package name prefix).
     */
    private String shortClassName(Class type) {
        if (type == null) return null;
        return type.getName().substring(type.getName().lastIndexOf('.') + 1);
    }

    /**
     * a label that can expand or collapse
     */
    private class CoolJLabel extends JLabel {
    	public CoolJLabel(String id, Class type) {
    		super(id);
    		setToolTipText(id + " (" + shortClassName(type) + ")");
    		addMouseListener(new MouseAdapter(){
    			@Override
    			public void mouseClicked(MouseEvent e) {
    				changeState();
    			}
    			
    			@Override
    			public void mouseEntered(MouseEvent e) {
    				if (getText().equals("")) {
    					setIcon(PLUS_ON);
    				}
    			}
    			
    			@Override
    			public void mouseExited(MouseEvent e) {
    				if (getText().equals("")) {
    					setIcon(PLUS_OFF);
    				}
    			}
    		});
    		
    		setBorder(BorderFactory.createEtchedBorder());
    	}
    	
    	public void collapse() {
    		setText("");
    		setIcon(PLUS_OFF);
    	}
    	
    	public void expand() {
    		setIcon(null);
    		setText(getToolTipText());
    	}
    	
    	public void changeState() {
    		if (getText().equals("")) {
    			expand();
    		} else {
    			collapse();
    		}
    		validate();
    	}
    }
    
	private JLabel getCoolJLabel(String id, Class type) {
		JLabel lab = new JLabel(id);
		lab.setToolTipText(id + " (" + shortClassName(type) + ")");
		
		
		return lab;
	}
	
	


	/**
	 * Adds the default set of component types that should not be made non-opaque
	 * to the {@link #opaqueComponents} set.  If your munge component uses other component
	 * types that should also not be made non-opaque, override this method and add
	 * your types to that set.  Don't forget to call super.setupOpaqueComponents()
	 * if you want to have the default set too.
	 */
	protected void setupOpaqueComponents() {
		opaqueComponents.add(JTextField.class);
		opaqueComponents.add(JTextArea.class);
		opaqueComponents.add(JFormattedTextField.class);
	}
	
	/**
	 * Resets the location and expandedness to the values in the step.
	 */
	private void setDefaults() {
		setExpanded(isExpanded());
		configureXFromMMO();
		configureYFromMMO();
		configureWidthFromMMO();
		configureHeightFromMMO();
		logger.debug("default size is " + getSize());
	}
	
	private void configureXFromMMO() {
		setLocation(getXFromMMO(), getY());
	}
	
	private void configureYFromMMO() {
		setLocation(getX(), getYFromMMO());
	}

	private void configureWidthFromMMO() {
		// both size and preferred size need to be changed for labels to be sized correctly
		content.setSize(getWidthFromMMO(), content.getHeight());
		content.setPreferredSize(new Dimension(getWidthFromMMO(), content.getHeight()));
		setSize(getPreferredSize());
	}
	
	private void configureHeightFromMMO() {
		// both size and preferred size need to be changed for labels to be sized correctly
		content.setSize(content.getWidth(), getHeightFromMMO());
		content.setPreferredSize(new Dimension(content.getWidth(), getHeightFromMMO()));
		setSize(getPreferredSize());
	}
	
	/**
	 * Set the x y parameter to the current value if needed.
	 * Also sets the width and height parameters.
	 */
	public void applyChanges() {
		MungeStep step = getStep();
		if (hasPositionChanged()) step.setPosition(getX(), getY());
		logger.debug("If the size has changed (" + hasSizeChanged() + ") then we will store the size " + content.getWidth() + ", " + content.getHeight());
		if (hasSizeChanged()) step.setSize(content.getWidth(), content.getHeight());
	}
	
	public boolean hasPositionChanged() {
		return getXFromMMO() != getX() || getYFromMMO() != getY();
	}
	
	public boolean hasSizeChanged() {
		return getWidthFromMMO() != content.getWidth() || getHeightFromMMO() != content.getHeight();
	}

	/**
	 * Walks the tree of components rooted at c, setting all of the components
	 * that can and should be flagged as non-opaque as such.
	 * <p>
	 * Components that should not be made non-opaque (such as JTextField, because
	 * that looks silly) will be left alone.  The exact set of component types
	 * that will be left with their existing opaqueness setting is controlled by
	 * the contents of the {@link #opaqueComponents} set.  If your munge component
	 * implementation has a preferences component that's getting made non-opaque,
	 * just add its class to that set in your {@link #setupOpaqueComponents()} method.
	 * 
	 * @param c
	 */
	private void deOpaquify(Container c) {
		for (int i = 0; i < c.getComponentCount(); i++) {
			Component cc = c.getComponent(i);
			if (cc instanceof JComponent && !opaqueComponents.contains(cc.getClass())) {
				((JComponent) cc).setOpaque(false);
			}
			if (cc instanceof Container) {
				deOpaquify((Container) cc);
			}
		}
	}

	/**
	 * This returns the user interface for your munge step options. This method
	 * must be implemented individualy for each munge step. If your munge step
	 * doesn't have any options, you should return null from this method, and
	 * there will be no +/- button on your component.
	 * <p>
	 * Important note about opaqueness: The munge component's background colour
	 * will change when it is selected.  For this effect to work properly, most
	 * of your components will have to be non-opaque.  This is a pain for you
	 * to remember and actually do for every munge component, so the AbstractMungeComponent
	 * will walk through the panel returned by this method and set most of the contained
	 * components to non-opaque.  Some components, though, look bad when they're not
	 * opaque, so those are left alone.  If you're adding a custom (or unusual) component
	 * to your preferences panel, and it's being made non-opaque against your wishes,
	 * add its class to the {@link #opaqueComponents} set in your {@link #setupOpaqueComponents()}
	 * method.
	 * <p>
	 * Important note for resizing: The panel returned here will have its size set
	 * as the component is being resized. The minimum size of the panel will also
	 * be used to determine how small the component can be shrunk. Override
	 * {@link JPanel#setSize(int, int)} and {@link JPanel#getMinimumSize()} as required.
	 * 
	 * @return The option panel or null
	 */
	protected abstract JPanel buildUI();
	
	/**
	 * The icon that will appear in the munge component itself when an image
	 * on the munge component is desired.
	 */
	private Icon mainIcon;
	
	/**
	 * Creates a AbstractMungeComponent for the given step that will be in the munge pen, 
	 * setting default colours
	 * 
	 * @param step The step connecting to the UI
	 */
	public AbstractMungeComponent(MungeStep step, FormValidationHandler handler, MatchMakerSession session, Icon mainIcon) {
		this(step, mainIcon);
		this.session = (MatchMakerSwingSession)session;
		this.handler = handler;
		setDefaults();
	}
	
	protected FormValidationHandler getHandler() {
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
		int inputs = step.getMSOInputs().size();
		
		if (!isExpanded() || !showInputNames) {
			int xPos = (int) (((double)(inputNum+1)/((double)inputs+1))*getWidth());
			return new Point(xPos,0);
		}
		
		int xPos = inputLabels[inputNum].getX() + inputLabels[inputNum].getWidth()/2;
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
		Point orig =  new Point(xPos,getHeight() - getBorder().getBorderInsets(this).bottom);
		
		if (isExpanded() && showOutputNames) {
			orig.x = outputLabels[outputNum].getX() + outputLabels[outputNum].getWidth()/2;
		}
		return orig;
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
	protected MungePen getPen() {
		return (MungePen)getParent();
	}
	
	private boolean isExpanded() {
		return getStepParameter(MungeStep.MUNGECOMPONENT_EXPANDED, true);
	}
	
	/**
	 * note that this is not the true position value, it is the
	 * positions stored in the MMO, which is not synchronized 
	 * until a mouse release event is fired.
	 */
	private int getXFromMMO() {
		return getStepParameter(MungeStep.MUNGECOMPONENT_X, 0);
	}
	private int getYFromMMO() {
		return getStepParameter(MungeStep.MUNGECOMPONENT_Y, 0);
	}
	
	private int getWidthFromMMO() {
		return getStepParameter(MungeStep.MUNGECOMPONENT_WIDTH, (int)content.getMinimumSize().getWidth());
	}
	private int getHeightFromMMO() {
		return getStepParameter(MungeStep.MUNGECOMPONENT_HEIGHT, (int)content.getMinimumSize().getHeight());
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		
		Insets border = getBorder().getBorderInsets(this);
		Dimension dim = getSize();
		dim.width -= border.left+border.right;
		dim.height -= border.top+border.bottom;
		
		g.drawImage(MMM_TOP, getWidth()-border.right-1, border.top - MMM_TOP.getHeight(null)+2, null);
		g.drawImage(MMM_BOT, getWidth()-border.right-1, getHeight() - MMM_BOT.getHeight(null) - border.bottom - 1, null);
		
		int[] x = {0,						MMM_TOP.getHeight(null)-1,	dim.width + MMM_TOP.getWidth(null) -1,	dim.width-1};
		int[] y = {MMM_TOP.getHeight(null),	1,							1,										MMM_TOP.getHeight(null)};
		if (!hasFocus()) {
			g.setColor(normalShadow);
		} else {
			g.setColor(selectedShadow);
		}
		g.translate(0, border.top - MMM_TOP.getHeight(null));
		g.fillPolygon(x, y, 4);
		g.translate(0, -(border.top - MMM_TOP.getHeight(null)));
		
		for (int i = 0; i< getStep().getMSOInputs().size(); i++) {
			int xPos = getInputPosition(i).x;
			Icon port = ConnectorIcon.getFemaleInstance(getStep().getInputDescriptor(i).getType());

			port.paintIcon(this, g, xPos, border.top - port.getIconHeight());
			
			if (getStep().getMSOInputs().get(i) != null || ghostIndex == i) {
				ConnectorIcon handle = ConnectorIcon.getHandleInstance(getStep().getInputDescriptor(i).getType());
				
				Graphics2D g2 = (Graphics2D)g.create();
				if (ghostIndex == i) {
					AlphaComposite alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
					g2.setComposite(alpha);
				} 
				
				handle.paintIcon(this, g2, xPos, 0);
			}
		}
		
		for (int i = 0; i < getStep().getChildCount(); i++) {
			if (!isOutputConnected(i) || (dropNib != null && dropNibIndex == i)) {
				int xPos = getOutputPosition(i).x;
				Icon nib;
				
				int droppingOffset;
				if (dropNib == null || dropNibIndex != i) {
					nib = ConnectorIcon.getNibInstance(getStep().getChildren().get(i).getType());
					droppingOffset = 0;
				} else {
					nib = dropNib;
					droppingOffset = dropNibOffSet;
				}
					
				if (!getPen().isConnectingOutput(this,i)) {
					nib.paintIcon(this, g, xPos, getHeight() - border.bottom - ConnectorIcon.NIB_OVERLAP - droppingOffset);
				}
			}
		}
		
		g = g.create(border.left, border.top, getWidth()-border.right, getHeight()-border.bottom);
		if (!hasFocus()) {
			g.setColor(normalBackground);
		} else {
			g.setColor(selectedBackground);
		}
		g.fillRect(0, 0, dim.width-1, dim.height-1);
		
		g.drawImage(RESIZE_ICON, dim.width - RESIZE_ICON_SIZE.width, dim.height - RESIZE_ICON_SIZE.height, null);
	}
	
	/**
	 * Returns the list for Inputs from the step object.
	 * 
	 * @return the list
	 */
	public List<MungeStepOutput> getInputs() {
		return step.getMSOInputs();
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
	protected JPopupMenu getPopupMenu() {
		JPopupMenu ret = new JPopupMenu();
		JMenuItem rm = new JMenuItem(new AbstractAction("Delete (del)") {

			public void actionPerformed(ActionEvent e) {
				remove();
			}
			
		});
		ret.add(rm);
		
		if (logger.isDebugEnabled()) {
			ret.addSeparator();
			ret.add(new AbstractAction("Show Components") {

				public void actionPerformed(ActionEvent e) {
					JTextArea ta = new JTextArea(listContents(AbstractMungeComponent.this, 0));
					JOptionPane.showMessageDialog(AbstractMungeComponent.this, new JScrollPane(ta));
				}
				
				private String listContents(Container c, int level) {
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < c.getComponentCount(); i++) {
						Component cc = c.getComponent(i);
						for (int j = 0; j < level; j++) {
							sb.append("  ");
						}
						sb.append(cc.getClass().getName());
						sb.append(": ");
						sb.append(cc.getBackground());
						sb.append("; opaque=").append(cc.isOpaque());
						sb.append("\n");
						if (cc instanceof Container) {
							sb.append(listContents((Container) cc, level + 1));
						}
					}
					return sb.toString();
				}
			});
		}
		return ret;
	}

	/**
	 * The remove action for the component. 
	 */
	protected void remove() {
		MungeStep step = getStep();
		MatchMakerObject mmo = step.getParent();
		if (mmo instanceof MungeProcess) {
			((MungeProcess)mmo).removeChildAndInputs(getStep());
		}
	}
	
	/**
	 * Hides or expands the component.
	 */
	protected void setExpanded(boolean expanded) {
		if (expanded) {
			if (showInputNames) {
				root.add(inputNames, BorderLayout.NORTH);
			}
			if (showOutputNames) {
				root.add(contentPlusNames,BorderLayout.SOUTH);
			} else {
				if (content != null ) {
				root.add(content,BorderLayout.SOUTH);
				}
			}
		} else {
			if (content != null) {
				root.remove(content);
				root.remove(inputNames);
				root.remove(contentPlusNames);
			}
		}
		if (getPen() != null) {
			getPen().normalize();
		}
		validate();
		root.updateUI();
	}
	
	/**
	 * A Set of listeners that detect changes in the MungeSteps and redraws them
	 */
	private class StepEventHandler implements MatchMakerListener<MungeStep, MungeStepOutput> {
		public void mmChildrenInserted(
				MatchMakerEvent<MungeStep, MungeStepOutput> evt) {}

		public void mmChildrenRemoved(
				MatchMakerEvent<MungeStep, MungeStepOutput> evt) {}

		public void mmPropertyChanged(
				MatchMakerEvent<MungeStep, MungeStepOutput> evt) {
			if (evt.getPropertyName().equals(MungeStep.MUNGECOMPONENT_EXPANDED)) {
				setExpanded(Boolean.parseBoolean((String)evt.getNewValue()));
			} else if (evt.getPropertyName().equals(MungeStep.MUNGECOMPONENT_X)) {
				configureXFromMMO();
			} else if (evt.getPropertyName().equals(MungeStep.MUNGECOMPONENT_Y)) {
				configureYFromMMO();
			} else if (evt.getPropertyName().equals(MungeStep.MUNGECOMPONENT_WIDTH)) {
				configureWidthFromMMO();
			} else if (evt.getPropertyName().equals(MungeStep.MUNGECOMPONENT_HEIGHT)) {
				configureHeightFromMMO();
			} else if (evt.getPropertyName().equals("addInputs")){
				repaint();
			} else if (!evt.getPropertyName().equals("inputs")
					&& evt.isUndoEvent()) {
				reload();
			}
		}

		public void mmStructureChanged(
				MatchMakerEvent<MungeStep, MungeStepOutput> evt) {}
	}
	
	/**
	 * The action to control the +/- button
	 */
	private class HideShowAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			step.setParameter(MungeStep.MUNGECOMPONENT_EXPANDED, !isExpanded());
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
		}
	}
	
	/**
	 * An action that can be added to a munge step that when used will
	 * change all of the inputs or output labels to the plus icon or
	 * the actual label.
	 */
	protected class HideShowAllLabelsAction extends AbstractAction {
		
		/**
		 * control variables
		 */
		boolean input;
		boolean show;
		
		/**
		 * Constructor to set the title
		 * 
		 * @param The title of the action
		 */
		public HideShowAllLabelsAction(String title, boolean input, boolean show) {
			super(title);
			this.input = input;
			this.show = show;
		}
		
		
		public void actionPerformed(ActionEvent e) {
			CoolJLabel[] labels = input ? inputLabels : outputLabels;
			for (CoolJLabel l : labels) {
				if (show) l.expand(); else l.collapse();
			}
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
			step.removeUnusedInput();
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
		Point startPoint;
		
		public void mouseClicked(MouseEvent e) {
			boolean showPopup = maybeShowPopup(e);
			bustGhost();
			autoScrollTimer.stop();
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
			nibDropStop();
			bustGhost();
			repaint();
		}

		public void mousePressed(MouseEvent e) {
			getPen().moveToFront(AbstractMungeComponent.this);
			if (!maybeShowPopup(e)) {
				diff = new Point((int)(e.getPoint().getX() - getX()), (int)(e.getPoint().getY() - getY()));			
				diff.translate(getX(), getY());
				if (!checkForIOConnectors(new Point(e.getX() + getX(),e.getY()+getY()))) {
					requestFocusInWindow();
				}
				startPoint = getLocation();
				
				if(diff.getX() > getWidth() - RESIZE_ICON_SIZE.getWidth() - getInsets().right 
						&& diff.getY() > getHeight() - RESIZE_ICON_SIZE.getHeight() - getInsets().bottom) {
					currentDragState = DragState.RESIZING;
				} else {
					currentDragState = DragState.MOVING;
				}
			}
		}

		public void mouseReleased(MouseEvent e) {
			//resets the auto scroll bounds for the munge pen
			MungePen mp = getPen();
			mp.lockAutoScroll(false);
			autoScrollTimer.stop();
			logger.debug("Mouse released: connecting? " + (!maybeShowPopup(e) && mp.isConnecting()));
			if (!maybeShowPopup(e) && mp.isConnecting()) {
				Point abs = new Point(e.getX() + getX(),e.getY()+getY());
				AbstractMungeComponent amc = mp.getMungeComponentAt(abs);
				if (amc == null || !amc.checkForIOConnectors(abs)) {
					mp.requestFocusInWindow();
				}
				mp.repaint();
			} else {
				mp.updatePositionsToMMO();
			}
			mp.normalize();
			mp.stopConnection();
			mp.revalidate();
			
			currentDragState = null;
		}
	}
	
	/**
	 * Possibly show the popup.
	 * 
	 * @param e The mouse event
	 * @return true iff the popup was shown
	 */
	private boolean maybeShowPopup(MouseEvent e) {
		if (e.isPopupTrigger()) {
			JPopupMenu pop = getPopupMenu();
			if (pop != null) {
				diff = null;
				pop.show(AbstractMungeComponent.this, e.getX(), e.getY());
				requestFocusInWindow();
			}
			return true;
		}
		return false;
	}
	
	private void bustGhost() {
		if (ghostIndex != -1) {
			ghostIndex = -1;		
			repaint();
		}
	}
	
	private void setGhost(int ghost) {
		ghostIndex = ghost;
	}

	private class MungeComponentMouseMoveListener extends MouseMotionAdapter {
		@Override
		public void mouseDragged(MouseEvent e) {
			nibDropStop();
			Point mouse = e.getPoint();
			mouse.x += getX();
			mouse.y += getY();
			
			MungePen parent = (MungePen)getParent();
			if (mouse.x < 0 || mouse.y < 0) {
				return;
			}
			
			if (!parent.isConnecting()) {
				//diff is set to null for a right click to prevent dragging 
				if (diff != null) {
					//unlocks the bounds (because the pen has no idea when the mouse is moving
					getPen().lockAutoScroll(false);
					e.translatePoint(getX(), getY());
					if (currentDragState.equals(DragState.RESIZING)) {
						int xModifier = (int)(e.getX() - diff.getX() - getX());
						int yModifier = (int)(e.getY()-diff.getY() - getY());
						
						// both size and preferred size need to be changed for the labels to be sized correctly
						Dimension dim = new Dimension();
						if (content.getMinimumSize().getWidth() < content.getWidth() + xModifier) {
							dim.setSize(content.getWidth() + xModifier, content.getHeight());
							content.setSize(dim);
							content.setPreferredSize(dim);
							diff.setLocation(diff.getX() + xModifier, diff.getY());
						} else {
							dim.setSize((int)content.getMinimumSize().getWidth(), content.getHeight());
							content.setSize(dim);
							content.setPreferredSize(dim);
							diff.setLocation(getPreferredSize().getWidth() - getInsets().right, diff.getY());
						}
						
						logger.debug("Minimum height is " + content.getMinimumSize().getHeight() + ", user changing to " + content.getHeight() + " + " + yModifier);
						if (content.getMinimumSize().getHeight() < content.getHeight() + yModifier) {
							dim.setSize(content.getWidth(), content.getHeight() + yModifier);
							content.setSize(dim);
							content.setPreferredSize(dim);
							diff.setLocation(diff.getX(), diff.getY() + yModifier);
						} else {
							dim.setSize(content.getWidth(), (int)content.getMinimumSize().getHeight());
							content.setSize(dim);
							content.setPreferredSize(dim);
							diff.setLocation(diff.getX(), getPreferredSize().getHeight() - getInsets().bottom);
						}
						
						setSize(getPreferredSize());
						revalidate();
					} else if (currentDragState.equals(DragState.MOVING)){
						setLocation((int)(e.getX() - diff.getX()), (int)(e.getY()-diff.getY()));						
					


						//checks if auto scrolling is a good idea at the present time
						MungePen pen = getPen();
						Insets autoScroll = pen.getAutoscrollInsets();
						//does not use the mouse point because this looks better
						autoScrollPoint = new Point(pen.getWidth()/2, pen.getHeight()/2);
						boolean asChanged = false;
						if (getX() + getWidth() > pen.getWidth() - autoScroll.right) {
							autoScrollPoint.x = getX() + getWidth();
							asChanged = true;
						}
						if (getY() + getHeight() > pen.getHeight() - autoScroll.bottom) {
							autoScrollPoint.y = getY() + getHeight();
							asChanged = true;
						}
						if (getX() < autoScroll.left) {
							autoScrollPoint.x = getX();
							asChanged = true;
						}
						if (getY() < autoScroll.top) {
							autoScrollPoint.y = getY();
							asChanged = true;
						}

						if (!asChanged) {
							autoScrollTimer.stop();
						} else {
							autoScrollTimer.restart();
						}
					}
					
				}
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
			Point p = e.getPoint();
			p.translate(getX(), getY());
			int selected = getClosestIOIndex(p, CLICK_TOLERANCE, false);			
			
			if (dropNibIndex != selected && selected != -1 && isOutputConnected(selected)) {
				if (dropNibTimer != null) {
					dropNibTimer.stop();
				}
				dropNibTimer = new Timer(DROP_TIMER_INTERVAL,new DropDownAction(selected,DROP_AMOUNT));
				dropNibTimer.start();
			} else if (dropNibIndex != selected) {
				nibDropStop();
			}
			
			selected = getClosestIOIndex(p, CLICK_TOLERANCE, true);
			if (selected != -1 && getStep().getMSOInputs().get(selected) == null) {
				if (ghostIndex != selected) {
					setGhost(selected);
				}
			} else {
				bustGhost();
			}
			repaint();
		}
	}
	
	//checks to see if the mouse was near an IOC point
	private boolean checkForIOConnectors(Point mousePoint) {
		MungePen parent = getPen();
		
		int inputIndex = getClosestIOIndex(mousePoint, CLICK_TOLERANCE, true);
		
		if (inputIndex != -1) {
			if (parent.isConnecting()) {
				parent.finishConnection(this, inputIndex, true);
			} else {
				parent.startConnection(this, inputIndex, true);
			}
			return true;
		}
		
		
		inputIndex = getClosestIOIndex(mousePoint, CLICK_TOLERANCE, false);
		
		if (inputIndex != -1) {
			if (parent.isConnecting()) {
				parent.finishConnection(this, inputIndex, false);
			} else {
				parent.startConnection(this, inputIndex, false);
			}
			return true;
		}
		
		return false;
		
	}	
	
	/**
	 * Returns the input or output number that is the closest to the given point 
	 * within a tolerance.
	 * 
	 * @param mousePoint The given point
	 * @param tol The tolerance
	 * @param checkInputs Set to true if you are checking inputs, false if you are looking at outputs
	 * @return The index of the closest input to the mouse event or -1 if no 
	 *   inputs are close
	 */
	public int getClosestIOIndex(Point mousePoint, int tol, boolean checkInputs) {
		
		int count;
		if (checkInputs) {
			count = getStep().getMSOInputs().size();
		} else {
			count = getStep().getChildren().size();
		}
		
		//squared because it should be faster then using the sqrt method later
		tol = tol*tol;
		int minDist = tol;
		int minNum = -1;
		
		for (int x = 0;x<count;x++) {
			Point p;
			if (checkInputs) {
				p = getInputPosition(x);
			} else {
				p = getOutputPosition(x);
			}
			
			p.translate(getX(), getY());
			int dist = Math.abs(p.x - mousePoint.x)*Math.abs(p.x - mousePoint.x) + Math.abs(p.y - mousePoint.y)*Math.abs(p.y - mousePoint.y); 
			if (dist < tol && dist < minDist) {
				minDist = dist;
				minNum = x;
			}
		}
		
		return minNum;
	}
	
	/**
	 * Returns true iff the output for the given index of this component is in use.
	 * 
	 * @param index The index
	 */
	private boolean isOutputConnected(int index) {
		return connected[index] > 0;
	}

	/**
	 * Tells this mungecomponent that the output at the given index is in use;
	 * 
	 * @param index the index
	 * @param con True iff the component is in use.
	 */
	public void setConnectOutput(int index, boolean con) {
		logger.debug("index: " + index + " -> " + con);
		if (con) {
			connected[index]++;
		} else {
			connected[index]--;
			if (connected[index] < 0) {
				connected[index] = 0;
			}
		}
	}
	
	/**
	 * Stops dropping the nib.
	 */
	private void nibDropStop() {
		if (dropNibTimer != null && dropNibTimer.isRunning()) {
			dropNib = null;			
			dropNibTimer.stop();
			dropNibIndex = -1;
			repaint();
		}
	}
	
	/**
	 * The action to be added to the timer to drop the nib.
	 */
	private class DropDownAction extends AbstractAction {
		int dropAmount;
		public DropDownAction(int index, int dropAmount) {
			this.dropAmount = dropAmount;
			dropNib = ConnectorIcon.getNibInstance(getStep().getChildren().get(index).getType());
			dropNibOffSet = dropNib.getIconHeight();
			dropNibIndex = index;
		}
		
		public void actionPerformed(ActionEvent e) {
			dropNibOffSet -= dropAmount;
			
			if (dropNibOffSet < 0) {
				dropNibOffSet = 0;
			}
			repaint();
		}
	}
	
	/**
	 * If set to true the input names will be displayed if the user expands the component.
	 *  
	 * @param b The value to set it to
	 */
	protected void setInputShowNames(Boolean b) {
		showInputNames = b;
	}
	
	/**
	 * If set to true the output names will be displayed if the user expands the component.
	 *  
	 * @param b The value to set it to
	 */
	protected void setOutputShowNames(Boolean b) {
		showOutputNames = b;
	}
	
	/**
	 * reloads the content pane from the matchmaker object
	 */
	protected void reload() {
		if (isExpanded()) {
			setExpanded(false);
		}
		content = buildUI();
		content.setOpaque(false);
		deOpaquify(content);
		if (isExpanded()) {
			setExpanded(true);
		}
		requestFocus();
	}
	
	/**
	 * helper methods called by the subclasses
	 * **************************************************************
	 */
	protected int getStepParameter(String key, int defaultValue) {
		String val = step.getParameter(key);
		if (val == null) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(val);
		} catch (NumberFormatException ex) {
			logger.warn("Invalid integer value \"" + val + "\" for parameter \"" + key + "\" in step " + step);
			return defaultValue;
		}
	}

	protected boolean getStepParameter(String key, boolean defaultValue) {
		String val = step.getParameter(key);
		if (val == null) {
			return defaultValue;
		} else {
			return Boolean.parseBoolean(val);
		}
	}

	protected String getStepParameter(String key, String defaultValue) {
		String val = step.getParameter(key);
		if (val == null) {
			return defaultValue;
		} else {
			return val;
		}
	}
	
	private Point getDifferencePoint() {
		return this.diff;
	}

	public Icon getMainIcon() {
		return mainIcon;
	}
}
