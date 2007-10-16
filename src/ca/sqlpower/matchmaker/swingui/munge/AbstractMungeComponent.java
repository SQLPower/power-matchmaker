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
import javax.swing.JButton;
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
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.ToolTipManager;
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
	protected JPanel contentPlusNames;
	
	private JPanel root;
	
	private final MungeStep step;
	
	/**
	 * The background colour to use when this component is not selected.
	 */
	private Color normalBackground = new Color(0xee, 0xee, 0xee);
	
	/**
	 * The shadow colour to use when this component is not selected.
	 */
	private Color normalShadow = new Color(0xdd, 0xdd, 0xdd);
	
	/**
	 * The background colour to use when this component is selected.
	 */
	private Color selectedBackground = new Color(0xc5, 0xdd, 0xf7);
	
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
	
	private static final Image MMM_TOP = new ImageIcon(ClassLoader.getSystemResource("icons/mmm_top.png")).getImage(); 
	private static final Image MMM_BOT = new ImageIcon(ClassLoader.getSystemResource("icons/mmm_bot.png")).getImage(); 
	
	private static final ImageIcon EXPOSE_OFF = new ImageIcon(ClassLoader.getSystemResource("icons/expose_off.png"));
	private static final ImageIcon EXPOSE_ON = new ImageIcon(ClassLoader.getSystemResource("icons/expose_on.png"));
	
	private static final ImageIcon PLUS_OFF = new ImageIcon(ClassLoader.getSystemResource("icons/plus_off.png"));
	private static final ImageIcon PLUS_ON = new ImageIcon(ClassLoader.getSystemResource("icons/plus_on.png"));
	
	private static final int PLUG_OFFSET = 2;
	public static final int CLICK_TOLERANCE = 15;
	
	public static final String MUNGECOMPONENT_X = "x";
	public static final String MUNGECOMPONENT_Y = "y";
	public static final String MUNGECOMPONENT_EXPANDED ="expanded";

	/**
	 * The timer interval for the drop down. The length of time to wait before moving
	 * the nib down again.
	 */
	public static final int DROP_TIMER_INTERVAL = 100;
	
	/**
	 * The amount to drop the nib after each interval
	 */
	public static final int DROP_AMOUNT = 5;
	
	private boolean expanded;
	
	private MatchMakerSwingSession session;
	
	private MungeComponentKeyListener mungeComKeyListener;

	private FormValidationHandler handler;
	
	private final JButton hideShow;

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
	private JLabel[] inputLables;
	
	
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
	private JLabel[] outputLables;
	
	/**
	 * Creates a AbstractMungeComponent for the given step that will be in the munge pen.
	 * Sets the background and border colours to given colours.
	 * 
	 * @param step The step connected to the UI
	 * @param border The colour for the border around the rectangle
	 * @param bg The background colour to the rectangle
	 */
	private AbstractMungeComponent(MungeStep step) {
		this.step = step;
		setVisible(true);
		setBackground(normalBackground);
		
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
		
		step.addMatchMakerListener(stepEventHandler);
		setName(step.getName());
		
		int borderTop;
		if (!getStep().canAddInput() && getStep().getInputs().size() == 0) {
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
		tmp.add(new JLabel(step.getName()));
		
		
		hideShow = new JButton(new HideShowAction());
		hideShow.setIcon(EXPOSE_OFF);
		
		setupOpaqueComponents();
		content = buildUI();
		//returning null will prevent the +/- button form showing up
		if (content != null) {
			deOpaquify(content);
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
		}
		
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
				//if it is being deleted
				if (getParent() != null) {
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
			}
		});
		
		
		root.setOpaque(false);
		tmp.setOpaque(false);
		expanded = false;
		
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
	}
	
	private void buildInputNamesPanel() {
		if (expanded && showInputNames) {
			root.remove(inputNames);
		}
		inputNames = new JPanel();
		inputNames.setOpaque(false);
		inputNames.setLayout(new FlowLayout());

		inputLables = new JLabel[step.getInputs().size()];
		
		for (int x = 0; x < inputLables.length; x++) {
			inputLables[x] = getCoolJLabel(step.getInputDescriptor(x).getName());
			hide(inputLables[x]);
			inputNames.add(inputLables[x]);
			inputLables[x].setOpaque(false);
		}
		
		if (expanded && showInputNames) {
			root.add(inputNames, BorderLayout.NORTH);
			revalidate();
		}	
	}
	
	private void buildOutputNamesPanel() {
		outputNames = new JPanel();
		outputNames.setOpaque(false);
		outputNames.setLayout(new FlowLayout());

		outputLables = new JLabel[step.getChildCount()];
		
		for (int x = 0; x < outputLables.length; x++) {
			outputLables[x] = getCoolJLabel(step.getChildren().get(x).getName());
			hide(outputLables[x]);
			outputNames.add(outputLables[x]);
			outputLables[x].setOpaque(false);
		}
	}

	private JLabel getCoolJLabel(String id) {
		JLabel lab =new JLabel(id);
		lab.setToolTipText(id);
		
		lab.addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent e) {
				hideShow((JLabel)e.getSource());
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				if (((JLabel)e.getSource()).getText().equals("")) {
					((JLabel)e.getSource()).setIcon(PLUS_ON);
				}
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				if (((JLabel)e.getSource()).getText().equals("")) {
					((JLabel)e.getSource()).setIcon(PLUS_OFF);
				}
			}
		});
		
		lab.setBorder(BorderFactory.createEtchedBorder());
		return lab;
			
	}
	
	private void hide(JLabel label) {
		label.setText("");
		label.setIcon(PLUS_OFF);
	}

	private void show(JLabel label) {
		label.setIcon(null);
		label.setText(label.getToolTipText());
	}
	
	private void hideShow(JLabel label) {
		if (label.getText().equals("")) {
			show(label);
		} else {
			hide(label);
		}
		validate();
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
	public void configureFromStepProperties() {
		int x = getStepParameter(MUNGECOMPONENT_X, 0);
		int y = getStepParameter(MUNGECOMPONENT_Y, 0);
		boolean exp = getStepParameter(MUNGECOMPONENT_EXPANDED, false);
		setLocation(new Point(x,y));
		setExpand(exp);
	}
	
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

	/**
	 * Tells the steps properties so that it can reload the GUI bits if needed.
	 */
	public void updateStepProperties() {
		getStep().setParameter(MUNGECOMPONENT_EXPANDED, new Boolean(expanded).toString());
		getStep().setParameter(MUNGECOMPONENT_X, new Integer(getX()).toString());
		getStep().setParameter(MUNGECOMPONENT_Y, new Integer(getY()).toString());
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
		this(step);
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
		
		if (!expanded || !showInputNames) {
			int xPos = (int) (((double)(inputNum+1)/((double)inputs+1))*getWidth());
			return new Point(xPos,0);
		}
		
		int xPos = inputLables[inputNum].getX() + inputLables[inputNum].getWidth()/2;
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
		
		if (expanded && showOutputNames) {
			orig.x = outputLables[outputNum].getX() + outputLables[outputNum].getWidth()/2;
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
		
		g.drawImage(MMM_TOP, getWidth()-border.right-1, border.top - MMM_TOP.getHeight(null)+1, null);
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

		
		
		
		for (int i = 0; i< getStep().getInputs().size(); i++) {
			int xPos = getInputPosition(i).x;
			Icon port = ConnectorIcon.getFemaleInstance(getStep().getInputDescriptor(i).getType());

			port.paintIcon(this, g, xPos, border.top - port.getIconHeight());
			
			if (getStep().getInputs().get(i) != null || ghostIndex == i) {
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
		MungePen.removeAllListeners(hideShow);
	}
	
	/** 
	 * Removes this munge step and disconnects all input and output IOCs
	 */
	public void removeNormal() {
		getPen().removeMungeStep(getStep());
		MungePen.removeAllListeners(hideShow);
	}
	
	/**
	 * The remove action for the component. This defaults to normal deletion, but
	 * can be over ridden to use the delete single or a custom action (not recommended).
	 */
	public void remove() {
		removeNormal();
	}
	
	/**
	 * Hides or expands the component.
	 */
	public void hideShow() {
		expanded = !expanded;
		if (expanded) {
			if (showInputNames) {
				root.add(inputNames, BorderLayout.NORTH);
			}
			if (showOutputNames) {
				root.add(contentPlusNames,BorderLayout.SOUTH);
			} else {
				root.add(content,BorderLayout.SOUTH);
			}
		} else {
			root.remove(content);
			root.remove(inputNames);
			root.remove(contentPlusNames);
		}
		getPen().normalize();
		validate();
		root.updateUI();
	}
	
	public void setExpand(boolean exp) {
		if (expanded != exp) {
			hideShow();
		}
	}
	
	/**
	 * The action to control the +/- button
	 */
	private class HideShowAction extends AbstractAction {
		public void actionPerformed(ActionEvent e) {
			hideShow();
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
			buildInputNamesPanel();
			
			//cleans up the lines because they were being stupid
			SwingUtilities.invokeLater(new Runnable(){
				public void run() {
					revalidate();
					getPen().repaint();
				}
			});
		}
	}
	
	/**
	 * An action that can be added to a munge step that when used will
	 * change all of the inputs labels to the plus icon.
	 */
	protected class HideAllInputNamesAction extends AbstractAction {
		
		/**
		 * Constructor to set the title
		 * 
		 * @param The title of the action
		 */
		public HideAllInputNamesAction(String title) {
			super(title);
		}

		public void actionPerformed(ActionEvent e) {
			for (JLabel l : inputLables) {
				hide(l);
			}
		}
	}
	
	/**
	 * An action that can be added to a munge step that when used will
	 * change all of the inputs labels to the plus icon.
	 */
	protected class HideAllOutputNamesAction extends AbstractAction {
		
		/**
		 * Constructor to set the title
		 * 
		 * @param The title of the action
		 */
		public HideAllOutputNamesAction(String title) {
			super(title);
		}

		public void actionPerformed(ActionEvent e) {
			for (JLabel l : outputLables) {
				hide(l);
			}
		}
	}
	
	/**
	 * An action that can be added to a munge step that when used will
	 * change all of the inputs labels to the plus icon.
	 */
	protected class ShowAllInputNamesAction extends AbstractAction {
		
		/**
		 * Constructor to set the title
		 * 
		 * @param The title of the action
		 */
		public ShowAllInputNamesAction(String title) {
			super(title);
		}

		public void actionPerformed(ActionEvent e) {
			for (JLabel l : inputLables) {
				show(l);
			}
		}
	}
	
	/**
	 * An action that can be added to a munge step that when used will
	 * change all of the inputs labels to the plus icon.
	 */
	protected class ShowAllOutputNamesAction extends AbstractAction {
		
		/**
		 * Constructor to set the title
		 * 
		 * @param The title of the action
		 */
		public ShowAllOutputNamesAction(String title) {
			super(title);
		}

		public void actionPerformed(ActionEvent e) {
			for (JLabel l : outputLables) {
				show(l);
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
			
			buildInputNamesPanel();
			
			//cleans up the lines, Some of the IOCs may have made their bounds smaller during the call
			//and that bit of the line might still be on the screen, this gets rig of it. May other things 
			//were tried to get rid of it before this was used.
			SwingUtilities.invokeLater(new Runnable(){
				public void run() {
					revalidate();
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
			bustGhost();
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
				diff = null;
				pop.show(AbstractMungeComponent.this, e.getX(), e.getY());
				requestFocusInWindow();
			}
			return true;
		}
		return false;
	}
	
	public void bustGhost() {
		if (ghostIndex != -1) {
			ghostIndex = -1;		
			repaint();
		}
	}
	
	public void setGhost(int ghost) {
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
				//diff is set to null for a right click to stop dragging 
				if (diff != null) {
					e.translatePoint(getX(), getY());
					setLocation((int)(e.getX() - diff.getX()), (int)(e.getY()-diff.getY()));
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
			if (selected != -1 && getStep().getInputs().get(selected) == null) {
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
	public boolean checkForIOConnectors(Point mousePoint) {
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
			count = getStep().getInputs().size();
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
	public boolean isOutputConnected(int index) {
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
	public void setInputShowNames(Boolean b) {
		showInputNames = b;
	}
	
	/**
	 * If set to true the output names will be displayed if the user expands the component.
	 *  
	 * @param b The value to set it to
	 */
	public void setOutputShowNames(Boolean b) {
		showOutputNames = b;
	}
	
}

