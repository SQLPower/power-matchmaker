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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchRuleSet;
import ca.sqlpower.matchmaker.event.MatchMakerEvent;
import ca.sqlpower.matchmaker.event.MatchMakerListener;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.munge.MungeStepOutput;

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
		
		
		step.addMatchMakerListener(stepEventHandler);
		setName(step.getName());
		
		setBorder(BorderFactory.createEmptyBorder(15,1,15,1));
		setOpaque(false);
		setFocusable(true);
		
		Dimension ps = getPreferredSize();
		setBounds(0, 0, ps.width, ps.height);

		
		root = new JPanel();
		root.setLayout(new BorderLayout());
		JPanel tmp = new JPanel( new FlowLayout());
		tmp.add(new JLabel(step.getName()));
		
		content = buildUI();
		
		//returning null will prevent the +/- button form showing up
		if (content != null) {
			JToolBar tb =new JToolBar();
			tb.add(new HideShowAction());
			tb.setFloatable(false);
			tmp.add(tb);
			content.setBackground(bg);
		}

		root.add(tmp,BorderLayout.NORTH);
		add(root);
		
		root.setBackground(bg);
		tmp.setBackground(bg);
				

		root.addComponentListener(new ComponentListener(){

			public void componentHidden(ComponentEvent e) {
				// TODO Auto-generated method stub
				logger.debug("Stub call: .componentHidden()");
				
			}

			public void componentMoved(ComponentEvent e) {
				// TODO Auto-generated method stub
				logger.debug("moved");
			}

			public void componentResized(ComponentEvent e) {
				getParent().repaint();
				logger.debug("Componet resized");
				//getParent().requestFocusInWindow();
			}

			public void componentShown(ComponentEvent e) {
				// TODO Auto-generated method stub
				logger.debug("Stub call: .componentShown()");
				
			}
			
		});

		addFocusListener(new FocusListener(){
			public void focusGained(FocusEvent e) {
				logger.debug("Gained focus");
				repaint();
			}
			public void focusLost(FocusEvent e) {
				logger.debug("Lost focus");
				repaint();
			}
			
		});
		
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
	public AbstractMungeComponent(MungeStep step) {
		this(step, Color.BLACK,Color.WHITE);
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
		return new Point(xPos,getHeight());
		
	}
	
	/**
	 * Returns the step connected to the UI.
	 * @return The step
	 */
	public MungeStep getStep() {
		return step;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		logger.debug("AbstractMungeComponent Repaint");
		g.setColor(Color.BLACK);
		
		if (getPreferredSize().width != getWidth() || getPreferredSize().height != getHeight()) {
			setBounds(getX(), getY(), getPreferredSize().width, getPreferredSize().height);
			revalidate();
		}
		
		int outputs = step.getChildren().size();
		int inputs = step.getInputs().size();
		
		Insets border = getBorder().getBorderInsets(this);
		
		for (int x= 0;x<inputs;x++){
			Point top = getInputPosition(x);
			g.drawLine((int)top.getX(), (int)top.getY(), (int)top.getX(), border.top);
			

			g.setColor(getColor(step.getInputDescriptor(x).getType()));
			g.fillOval(top.x-2, top.y, 4, 4);
			g.setColor(Color.BLACK);
		}
		
		for (int x= 0;x<outputs;x++){
			Point bottom = getOutputPosition(x);
			g.drawLine((int)bottom.getX(), (int)bottom.getY(), (int)bottom.getX(), (int)bottom.getY() - border.bottom);

			g.setColor(getColor(step.getChildren().get(x).getType()));
			g.fillOval(bottom.x-2, bottom.y-5, 4, 4);
			g.setColor(Color.BLACK);
		}

		g = g.create(border.left, border.top, getWidth()-border.right, getHeight()-border.bottom);
		
		Dimension dim = getSize();
		dim.width -= border.left+border.right;
		dim.height -= border.top+border.bottom;

		g.setColor(bg);
		g.fillRect(0, 0, (int)dim.getWidth()-1, (int)dim.getHeight()-1);
		g.setColor(borderColour);
		if (hasFocus()) {
			((Graphics2D)g).setStroke(new BasicStroke(3));
		}
		g.drawRect(0, 0, (int)dim.getWidth()-1, (int)dim.getHeight()-1);
		if (hasFocus()) {
			((Graphics2D)g).setStroke(new BasicStroke(1));
		}
		
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
	 * Returns the appropriate colour for the given type.
	 * This is used to colour code lines and the IOCs.
	 * 
	 * @param c The type of connection
	 * @return The correct colour
	 */
	public static Color getColor(Class c) {
		if (c.equals(String.class)) {
			return Color.red;
		} else if (c.equals(Boolean.class)) {
			return Color.BLUE;
		} else if (c.equals(Integer.class)){
			return Color.GREEN;
		}
		return Color.PINK;
	}
	
	/**
	 * Returns the popup munu to display when this componet is right clicked on.
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
		((MungePen)getParent()).removeMungeStepSingles(getStep());
	}
	
	/** 
	 * Removes this munge step and disconnects all input and output IOCs
	 */
	public void remove() {
		((MungePen)getParent()).removeMungeStep(getStep());
	}
	
	/**
	 * The action to control the +/- button
	 */
	private class HideShowAction extends AbstractAction {
		public HideShowAction() {
			super("+");
		}
		
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("+")) {
				putValue(NAME, "-");
				root.add(content,BorderLayout.CENTER);
			} else {
				putValue(NAME, "+");
				root.remove(content);
			}
			validate();
			getParent().repaint();
			((Component)e.getSource()).requestFocusInWindow();
			logger.debug("Repainted");
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
			getStep().addInput(getStep().getInputDescriptor(0));
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
				for (y=x-1; y>=0 && step.getInputs().get(y) == null; y--);
				y++;
				if (y != x) {
					step.connectInput(y, step.getInputs().get(x));
					step.disconnectInput(x);
				}
			}
			
			for (int x = step.getInputs().size()-1;x>0 && step.getInputs().get(x) == null;x--) {
				step.removeInput(x);
			}
			
			getParent().repaint();
		}
	}
	
	public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
	}
	
	public static void createAndShowGUI() {
		MungePen p = new MungePen(new MatchRuleSet());
		JFrame f = new JFrame("Frame");
		f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		JScrollPane sp = new JScrollPane(p);
		f.setContentPane(sp);
		f.pack();
		f.setVisible(true);
	}


	public Color getBg() {
		return bg;
	}
}

