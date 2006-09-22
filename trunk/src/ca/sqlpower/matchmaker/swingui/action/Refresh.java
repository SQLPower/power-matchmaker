package ca.sqlpower.matchmaker.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import ca.sqlpower.matchmaker.swingui.MatchMakerFrame;
import ca.sqlpower.matchmaker.swingui.MatchMakerTreeModel;

public class Refresh extends AbstractAction{
	public Refresh() {
		super("Refresh");
	}
	public void actionPerformed(ActionEvent e) {
		MatchMakerTreeModel m = (MatchMakerTreeModel)MatchMakerFrame.getMainInstance().getTree().getModel();
		m.refresh();        
	}
	

}
