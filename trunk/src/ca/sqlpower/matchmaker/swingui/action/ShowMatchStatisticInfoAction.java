package ca.sqlpower.matchmaker.swingui.action;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.sql.SQLException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.CommonCloseAction;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.swingui.MatchStatisticsPanel;

import com.jgoodies.forms.builder.ButtonBarBuilder;

public class ShowMatchStatisticInfoAction extends AbstractAction {

	private Match match;
	private JFrame parent;

	public ShowMatchStatisticInfoAction(Match match, JFrame parent) {
		super("Statistics");
		this.match = match;
		this.parent = parent;
	}

	public void actionPerformed(ActionEvent e) {

		if (match == null) {
			JOptionPane.showMessageDialog(parent,
					"No match selected",
					"Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		MatchStatisticsPanel p = null;
		try {
			p = new MatchStatisticsPanel(match);
		} catch (SQLException e1) {
			ASUtils.showExceptionDialog(parent,
					"Could not get match statistic information", e1);
			return;
		}
		if ( p == null ) {
			JOptionPane.showMessageDialog(parent,
					"No match statistics available",
					"Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		JDialog d = new JDialog(parent);
		JPanel panel = new JPanel(new BorderLayout());
		final MatchStatisticsPanel p2 = p;

		JButton deleteAllButton = new JButton(new AbstractAction("Delete All"){
			public void actionPerformed(ActionEvent e) {
				try {
					p2.deleteAllStatistics();
				} catch (SQLException e1) {
					ASUtils.showExceptionDialog(parent,
							"Could not delete match statistic information", e1);
				}
			}});
		JButton deleteBackwardButton = new JButton(new AbstractAction("Delete Backward"){
			public void actionPerformed(ActionEvent e) {
				try {
					p2.deleteBackwardStatistics();
				} catch (SQLException e1) {
					ASUtils.showExceptionDialog(parent,
							"Could not delete match statistic information", e1);
				}
			}});
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
		panel.add(bbb.getPanel(),BorderLayout.SOUTH);
		ASUtils.makeJDialogCancellable(d,closeAction);

		panel.add(p,BorderLayout.CENTER);
		d.add(panel);
		d.setPreferredSize(new Dimension(800,600));
		d.pack();
		d.setVisible(true);

	}

}
