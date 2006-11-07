package ca.sqlpower.matchmaker.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JSplitPane;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import ca.sqlpower.matchmaker.Match;

public class NewMatchGroupAction extends AbstractAction {
	final Match parent;
	JSplitPane splitPane;

	public NewMatchGroupAction(Match parent,JSplitPane splitPane) {
	    super("New Match Group");
        if (parent == null) throw new IllegalArgumentException("Parent must be non null");
		this.parent = parent;
		this.splitPane = splitPane;
	}
	
	public void actionPerformed(ActionEvent e) {
            throw new NotImplementedException();
	}

}
