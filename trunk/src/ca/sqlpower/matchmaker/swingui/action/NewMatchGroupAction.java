package ca.sqlpower.matchmaker.swingui.action;

import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JDialog;

import ca.sqlpower.architect.swingui.ArchitectPanelBuilder;
import ca.sqlpower.matchmaker.hibernate.PlMatch;
import ca.sqlpower.matchmaker.swingui.NewMatchGroupPanel;

public class NewMatchGroupAction extends AbstractAction {
	PlMatch parent;
	private Window window;
	public NewMatchGroupAction(PlMatch parent, Window parentWindow) {
		super("New Match Group");
		this.parent = parent;
		this.window = parentWindow;
	}
	
	public void actionPerformed(ActionEvent e) {
		
		JDialog d = ArchitectPanelBuilder.createArchitectPanelDialog(new NewMatchGroupPanel(parent,window), window, "New Match Group", "Create Match Group");
		d.setVisible(true);
	}

}
