package ca.sqlpower.matchmaker.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;

public class NewMatchGroupAction extends AbstractAction {
    
    private final MatchMakerSwingSession swingSession;
	private final Match parent;

	public NewMatchGroupAction(MatchMakerSwingSession swingSession, Match parent) {
	    super("New Match Group");
        this.swingSession = swingSession;
        this.parent = parent;
        if (parent == null) throw new IllegalArgumentException("Parent must be non null");
	}
	
	public void actionPerformed(ActionEvent e) {
	    throw new NotImplementedException();
	}

}
