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
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.ToolTipManager;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.munge.MungeStepOutput;
import ca.sqlpower.validation.swingui.FormValidationHandler;

/**
 * This is a component for a lower case munge step. It has no options at all.
 */
public class SQLInputMungeComponent extends AbstractMungeComponent {

	private JLabel[] labels;
	private JButton showAll;
	private JButton hideAll;
	
	private static final ImageIcon PLUS_OFF = new ImageIcon(ClassLoader.getSystemResource("icons/plus_off.png"));
	private static final ImageIcon PLUS_ON = new ImageIcon(ClassLoader.getSystemResource("icons/plus_on.png"));
	
	public SQLInputMungeComponent(MungeStep step, FormValidationHandler handler, MatchMakerSession session) {
		super(step, handler, session);
	}

	@Override
	protected JPanel buildUI() {
		JPanel content = new JPanel();
		
		try {
			getStep().open();
			getStep().close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		labels = new JLabel[getOutputs().size()];
		
		content.setLayout(new BorderLayout());

		JPanel top = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 10));
		hideAll = new JButton(new AbstractAction("Hide All") {

			public void actionPerformed(ActionEvent e) {
				for (JLabel label: labels) {
					hide(label);
				}
			}
			
		});
		showAll = new JButton(new AbstractAction("Show All") {
			
			public void actionPerformed(ActionEvent e) {
				for (JLabel label: labels) {
					show(label);
				}
			}
		});
		top.add(hideAll);
		top.add(showAll);
		
		JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
		int x = 0;
		for (MungeStepOutput o: getStep().getChildren()) {
			JLabel lab =new JLabel(o.getName());
			lab.setToolTipText(o.getName());
			ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
			toolTipManager.setInitialDelay(0);
			
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
			})
			
			;
			lab.setBorder(BorderFactory.createEtchedBorder());
			bottom.add(lab);
			labels[x++] = lab;
			hide(lab);
		}
		
		content.add(top, BorderLayout.NORTH);
		content.add(bottom, BorderLayout.SOUTH);
		
		return content;
	}
	
	@Override
	public Point getOutputPosition(int inputNum) {
		Point orig = super.getOutputPosition(inputNum);
		if (isExpanded()) {	
			return new Point(labels[inputNum].getX() + labels[inputNum].getWidth()/2,orig.y);
		}
		return orig;
	}
	
	public void hide(JLabel label) {
		label.setText("");
		label.setIcon(PLUS_OFF);
	}

	public void show(JLabel label) {
		label.setIcon(null);
		label.setText(label.getToolTipText());
	}
	
	public void hideShow(JLabel label) {
		if (label.getText().equals("")) {
			show(label);
		} else {
			hide(label);
		}
	}
	
	@Override
	public void remove() {
	}
	
	@Override
	public JPopupMenu getPopupMenu() {
		if (Logger.getLogger(AbstractMungeComponent.class).isDebugEnabled()) {
			return super.getPopupMenu();
		}
		return null;
	}
}
