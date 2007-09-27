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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import ca.sqlpower.matchmaker.event.MatchMakerEvent;
import ca.sqlpower.matchmaker.event.MatchMakerListener;
import ca.sqlpower.matchmaker.munge.AbstractMungeStep;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.munge.MungeStepOutput;

public class MungeComponent extends JPanel {

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
	
	private final MungeStep step;
	
	private final Color bg;
	private final Color borderColour;
	
	public MungeComponent(MungeStep step, Color border, Color bg) {
		this.borderColour = border;
		this.bg = bg;
		this.step = step;
		step.addMatchMakerListener(stepEventHandler);
		setName(step.getName());
		setBorder(BorderFactory.createEmptyBorder(15,1,15,1));
		setOpaque(false);
		setFocusable(true);
	}
	
	public Point getInputPosition(int inputNum) {
		int inputs = step.getInputCount();
		
		if (inputs == MungeStep.UNLIMITED_INPUTS) {
			inputs = step.getInputs().size() + 1;
		}
		
		int xPos = (int) (((double)(inputNum+1)/((double)inputs+1))*getWidth());
		return new Point(xPos,0);
	}
	
	public Point getOutputPosition(int outputNum) {
		int outputs = step.getChildren().size();
		int xPos = (int) (((double)(outputNum+1)/((double)outputs+1))*getWidth());
		return new Point(xPos,getHeight());
		
	}
	
	
	public MungeComponent(MungeStep step) {
		this(step, Color.BLACK,Color.WHITE);
	}
	
	public MungeStep getStep() {
		return step;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		g.setColor(Color.BLACK);
		
		setBounds(getX(), getY(), getPreferredSize().width, getPreferredSize().height);
		
		int outputs = step.getChildren().size();
		int maxInputs = step.getInputCount();
		if (maxInputs == MungeStep.UNLIMITED_INPUTS) {
			maxInputs = step.getInputs().size() +1;
		}
		
		Insets border = getBorder().getBorderInsets(this);
		
		for (int x= 0;x<maxInputs;x++){
			Point top = getInputPosition(x);
			g.drawLine((int)top.getX(), (int)top.getY(), (int)top.getX(), border.top);
			
			g.setColor(getColor(step.getInputType(x)));
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
		g.drawRect(0, 0, (int)dim.getWidth()-1, (int)dim.getHeight()-1);
	}
	
	public List<MungeStepOutput> getInputs() {
		return step.getInputs();
	}
	
	
	public static Color getColor(Class c) {
		if (c.equals(String.class)) {
			return Color.red;
		} else if (c.equals(Boolean.class)) {
			return Color.BLUE;
		} else if (c.equals(Integer.class)){
			return Color.MAGENTA;
		}
		return Color.PINK;
	}
	
	public JPopupMenu getPopupMenu() {
		JPopupMenu ret = new JPopupMenu();
		ret.add(new JMenuItem("HELLO"));
		ret.add(new JMenuItem("HELLO"));
		ret.add(new JMenuItem("HELLO"));
		ret.add(new JMenuItem("HELLO"));
		return ret;
	}
	
	
	
	
	static class MungeStepTest extends AbstractMungeStep {

		public MungeStepTest(int numOutputs) {
			for (int i = 0; i < numOutputs; i++) {
				addChild(new MungeStepOutput<String>("Output", String.class));
			}
		}
		
		@Override
		public boolean equals(Object obj) {
			return this == obj;
		}

		@Override
		public int hashCode() {
			return System.identityHashCode(this);
		}

		public List<MungeStepOutput> call() throws Exception {
			return null;
		}

		public int getMaxInputs() {
			return -1;
		}

		public int getInputCount() {
			// TODO Auto-generated method stub
			return 3;
		}

		public Class getInputType(int inputNumber) {
			switch (inputNumber) {
			case 0:
				return String.class;
			case 1:
				return Integer.class;
			}
			return Boolean.class;
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
		MungePen p = new MungePen();
		
		MungeStep parent = new MungeStepTest(2);
		
		MungeStep child = new MungeStepTest(55);
		
		MungeStep child2 = new MungeStepTest(2);
		
		child.addInput(parent.getChildren().get(1));
		child2.addInput(parent.getChildren().get(0));
		child2.addInput(child.getChildren().get(0));
		
		parent.setName("parent");
		child.setName("child");
		MungeComponent mc = new MungeComponent(parent,Color.BLACK,Color.RED);
		MungeComponent mc2 = new MungeComponent(child,Color.BLACK,Color.BLUE);
		mc.add(new JLabel("parent"));
		mc.add(new JTextField("Cow"));
		mc.add(new JCheckBox("Moo"));
		mc.add(new JCheckBox("Work Properly", true));
		
		p.add(mc);
		
		MungeComponent mc3 = new MungeComponent(child2, Color.BLACK,Color.GREEN);
		mc3.add(new JLabel("child2"));
		mc3.add(new JTextField("Cow"));
		mc3.add(new JCheckBox("Moo"));
		mc3.add(new JCheckBox("Work Properly", true));
		p.add(mc3);

		mc2.add(new JLabel("child"));
		mc2.add(new JTextField("Cow"));
		mc2.add(new JCheckBox("Moo"));
		mc2.add(new JCheckBox("Work Properly", true));
		p.add(mc2);
		
		
		Dimension ps = mc.getPreferredSize();
		mc.setBounds(0, 0, ps.width, ps.height);
		
		ps = mc2.getPreferredSize();
		mc2.setBounds(0, 0, ps.width, ps.height);
		
		ps = mc3.getPreferredSize();
		mc3.setBounds(0, 0, ps.width, ps.height);
		
		p.setPreferredSize(new Dimension(500, 500));
		p.setBackground(Color.WHITE);
		p.setOpaque(true);
		JFrame f = new JFrame("Frame");
		f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		f.setContentPane(p);
		f.pack();
		f.setVisible(true);
		mc.repaint();
	}

	public List<MungeStepOutput> getOutputs() {
		return step.getChildren();
	}
}
