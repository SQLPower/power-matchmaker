package ca.sqlpower.matchmaker.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.matchmaker.swingui.MatchMakerTreeModel;

/** An Action o refresh the MatchMaker TreeModel */
public class Refresh extends AbstractAction {
    
    private final MatchMakerSwingSession swingSession;
    
    public Refresh(MatchMakerSwingSession swingSession) {
        super("Refresh");
        this.swingSession = swingSession;
    }

    public void actionPerformed(ActionEvent e) {
        MatchMakerTreeModel m = (MatchMakerTreeModel) swingSession.getTree().getModel();
        m.refresh();
    }

}
