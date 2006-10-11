package ca.sqlpower.matchmaker.swingui.action;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.sql.SQLException;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.ButtonBarBuilder;

import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.architect.swingui.CommonCloseAction;
import ca.sqlpower.matchmaker.hibernate.PlMatch;
import ca.sqlpower.matchmaker.swingui.MatchMakerFrame;
import ca.sqlpower.matchmaker.swingui.MatchStatisticsPanel;

public class ShowMatchStatisticInfoAction extends AbstractAction {

	private PlMatch match;

	public ShowMatchStatisticInfoAction(PlMatch match) {
		super("Statistics");
		this.match = match;
	}

	public void actionPerformed(ActionEvent e) {

		if ( match == null )
			return;

		MatchStatisticsPanel p = null;
		try {
			p = new MatchStatisticsPanel(match);
		} catch (SQLException e1) {
			ASUtils.showExceptionDialog(MatchMakerFrame.getMainInstance(),
					"Could not get match statistic information", e1);
		}
		if ( p == null )
			return;

		JDialog d = new JDialog(MatchMakerFrame.getMainInstance());
		JPanel panel = new JPanel(new BorderLayout());

		JButton deleteAllButton = new JButton("Delete All");
		Action closeAction = new CommonCloseAction(d);
		closeAction.putValue(Action.NAME, "Close");
		JButton closeButton = new JButton(closeAction);

		ButtonBarBuilder bbb = new ButtonBarBuilder();
		bbb.addRelatedGap();
		bbb.addGridded(deleteAllButton);
		bbb.addGlue();
		bbb.addGridded(closeButton);
		bbb.addRelatedGap();
		panel.add(bbb.getPanel(),BorderLayout.SOUTH);
		ArchitectPanelBuilder.makeJDialogCancellable(d,closeAction);

		panel.add(p,BorderLayout.CENTER);
		d.add(panel);
		d.setPreferredSize(new Dimension(800,600));
		d.pack();
		d.setVisible(true);

	}

}
