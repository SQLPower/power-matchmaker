package ca.sqlpower.matchmaker.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JSplitPane;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.swingui.MatchEditor;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;

/**
 * Creates a new Match object and a GUI editor for it, then puts that editor in the split pane.
 */
public final class NewMatchAction extends AbstractAction {

    private final MatchMakerSwingSession swingSession;
	private final PlFolder folder;
	private final JSplitPane splitPane;

	public NewMatchAction(
            MatchMakerSwingSession swingSession,
            String name,
            PlFolder folder,
            JSplitPane displayArea) {
		super(name);
        this.swingSession = swingSession;
		this.folder = folder;
		splitPane = displayArea;
	}

	public void actionPerformed(ActionEvent e) {
	    MatchEditor me;
		try {
			// FIXME no user 
			me = new MatchEditor(swingSession, new Match(), folder, splitPane);
		} catch (ArchitectException e1) {
			throw new ArchitectRuntimeException(e1);
		}
		splitPane.setRightComponent(me.getPanel());
	}
}
