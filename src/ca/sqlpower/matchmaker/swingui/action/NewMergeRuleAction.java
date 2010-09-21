/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of DQguru
 *
 * DQguru is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DQguru is distributed in the hope that it will be useful,
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
import javax.swing.JDialog;

import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.matchmaker.swingui.NewTableMergeRuleChooserPane;
import ca.sqlpower.swingui.DataEntryPanelBuilder;

/**
 * This action opens a dialog for the user to choose the SQLTable for
 * a new merge rule which is then added to the swing session and the editor 
 * for the new merge rule is opened.
 */
public class NewMergeRuleAction extends AbstractAction {
    
    private final MatchMakerSwingSession swingSession;
	private final Project parent;

	public NewMergeRuleAction(MatchMakerSwingSession swingSession, Project parent) {
	    super("New Merge Rule");
        this.swingSession = swingSession;
        this.parent = parent;
        
        if (parent == null) throw new IllegalArgumentException("Parent must be non null");
	}
	
	public void actionPerformed(ActionEvent e) {
		NewTableMergeRuleChooserPane chooserPane = new NewTableMergeRuleChooserPane(swingSession, parent);
		JDialog d = DataEntryPanelBuilder.createDataEntryPanelDialog(
				chooserPane, 
				swingSession.getFrame(), 
				"Choose the table and index for the new merge rule", 
				"OK");
        d.pack();
		d.setLocationRelativeTo(swingSession.getFrame());
        d.setVisible(true);
	}
}
