/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*MatchMaker.
 *
 * Power*MatchMaker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*MatchMaker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import ca.sqlpower.matchmaker.swingui.MMSUtils;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.matchmaker.swingui.SwingSessionContext;
import ca.sqlpower.sql.SPDataSource;

/**
 * An action that "refreshes" the session by closing it and creating a new one
 * connected to the same repository in its place. We realize this is a horrible
 * solution to the refresh problem, but Hibernate does not offer us any other
 * way than to recreate the whole session. The long term fix for this problem is
 * to not use hibernate.
 */
public class RefreshAction extends AbstractAction {
    
    private final MatchMakerSwingSession swingSession;

    /**
     * Creates a new instance of this action.
     */
    public RefreshAction(MatchMakerSwingSession swingSession) {
        super("Refresh");
        this.swingSession = swingSession;
    }

    public void actionPerformed(ActionEvent e) {

        // Save information we need in order to recreate the session
        SwingSessionContext context = swingSession.getContext();
        SPDataSource ds = swingSession.getDatabase().getDataSource();
        
        // this will prompt the user to save their work
        swingSession.setCurrentEditorComponent(null);
        
        try {
            // Create the new session just before closing the old one because the context will
            // halt the VM if there are no sessions open
            MatchMakerSwingSession newSession = context.createSession(ds, ds.getUser(), ds.getPass());
            newSession.showGUI();
            swingSession.close();
        } catch (Exception ex) {
            MMSUtils.showExceptionDialog(swingSession.getFrame(), "Refresh failed", ex);
        }
        
    }

}
