/*
 * Copyright (c) 2008, SQL Power Group Inc.
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

package ca.sqlpower.matchmaker.swingui.action;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.sql.SQLException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.swingui.MMSUtils;
import ca.sqlpower.matchmaker.swingui.MatchStatisticsPanel;
import ca.sqlpower.swingui.CommonCloseAction;
import ca.sqlpower.swingui.SPSUtils;

import com.jgoodies.forms.builder.ButtonBarBuilder;

public class ShowMatchStatisticInfoAction extends AbstractAction {

	private final MatchMakerSession swingSession;
	private Project project;
	private JFrame parent;
	private JDialog d;

	public ShowMatchStatisticInfoAction(MatchMakerSession swingSession,
			Project project, JFrame parent) {
		super("Match Statistics...");
		this.project = project;
		this.parent = parent;
		this.swingSession = swingSession;
	}

	public void actionPerformed(ActionEvent e) {

		if (project == null) {
			JOptionPane.showMessageDialog(parent,
					"No project selected",
					"Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		MatchStatisticsPanel p = null;
		try {
			p = new MatchStatisticsPanel(swingSession,project);
		} catch (SQLException e1) {
			MMSUtils.showExceptionDialog(parent,
					"Could not get match statistics information", e1);
			return;
		}
		if ( p == null ) {
			JOptionPane.showMessageDialog(parent,
					"No match statistics available",
					"Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		if (d == null) {
			d = new JDialog(parent);
			JPanel panel = new JPanel(new BorderLayout());
			final MatchStatisticsPanel p2 = p;
			JButton deleteAllButton = new JButton(new AbstractAction(
					"Delete All") {
				public void actionPerformed(ActionEvent e) {
					try {
						p2.deleteAllStatistics();
					} catch (SQLException e1) {
						MMSUtils
								.showExceptionDialog(
										parent,
										"Could not delete match statistics information",
										e1);
					}
				}
			});
			JButton deleteBackwardButton = new JButton(new AbstractAction(
					"Delete Backward") {
				public void actionPerformed(ActionEvent e) {
					try {
						p2.deleteBackwardStatistics();
					} catch (SQLException e1) {
						MMSUtils
								.showExceptionDialog(
										parent,
										"Could not delete match statistics information",
										e1);
					}
				}
			});
			Action closeAction = new CommonCloseAction(d);
			closeAction.putValue(Action.NAME, "Close");
			JButton closeButton = new JButton(closeAction);
			ButtonBarBuilder bbb = new ButtonBarBuilder();
			bbb.addRelatedGap();
			bbb.addGridded(deleteAllButton);
			bbb.addRelatedGap();
			bbb.addGridded(deleteBackwardButton);
			bbb.addGlue();
			bbb.addGridded(closeButton);
			bbb.addRelatedGap();
			panel.add(bbb.getPanel(), BorderLayout.SOUTH);
			SPSUtils.makeJDialogCancellable(d, closeAction);
			panel.add(p, BorderLayout.CENTER);
			panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
			d.add(panel);
			d.setTitle("Match Statistics");
			d.setPreferredSize(new Dimension(800, 600));
			d.pack();
		}
		d.setVisible(true);
	}
}
