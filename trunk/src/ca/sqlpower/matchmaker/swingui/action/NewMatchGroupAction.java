package ca.sqlpower.matchmaker.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JSplitPane;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.matchmaker.hibernate.PlMatch;
import ca.sqlpower.matchmaker.hibernate.PlMatchGroup;
import ca.sqlpower.matchmaker.swingui.PlMatchGroupPanel;

public class NewMatchGroupAction extends AbstractAction {
	final PlMatch parent;
	JSplitPane splitPane;

	public NewMatchGroupAction(PlMatch parent,JSplitPane splitPane) {
	    super("New Match Group");
        if (parent == null) throw new IllegalArgumentException("Parent must be non null");
		this.parent = parent;
		this.splitPane = splitPane;
	}
	
	public void actionPerformed(ActionEvent e) {
		try {
            PlMatchGroup mg = new PlMatchGroup();
            mg.setPlMatch(parent);
			splitPane.setRightComponent(new PlMatchGroupPanel(mg));
		} catch (ArchitectException e1) {
			throw new ArchitectRuntimeException(e1);
		}
		
	}

}
