package ca.sqlpower.matchmaker.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JSplitPane;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.matchmaker.hibernate.PlMatch;
import ca.sqlpower.matchmaker.swingui.PlMatchGroupPanel;

public class NewMatchGroupAction extends AbstractAction {
	PlMatch parent;
	JSplitPane splitPane;
	
	public NewMatchGroupAction(PlMatch parent,JSplitPane splitPane) {
		super("New Match Group");
		this.parent = parent;
		this.splitPane = splitPane;
	}
	
	public void actionPerformed(ActionEvent e) {
		try {
			splitPane.setRightComponent(new PlMatchGroupPanel(parent));
		} catch (ArchitectException e1) {
			throw new ArchitectRuntimeException(e1);
		}
		
	}

}
