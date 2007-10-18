/*
 * Copyright (c) 2007, SQL Power Group Inc.
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
import javax.swing.JOptionPane;

import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.swingui.MMSUtils;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;

/**
 * Simple action that deletes a match from the swing session
 */
public class DeleteProjectAction extends AbstractAction {

	private MatchMakerSwingSession swingSession;
	private Match match;
	private boolean calledFromTopMenu = true;
	
    /**
     * Creates a new instance of this action which is parented by the given swingSession.
     * match is null when an instance is created with this constructor and will be 
     * configured to decide which Match to delete by checking for the selected item 
     * in the tree.
     * @param swingSession The GUI session this action lives in.
     */
	public DeleteProjectAction(MatchMakerSwingSession swingSession) {
		super("Delete Project");
		this.swingSession = swingSession;
	}
	
    /**
     * Creates a new instance of this action with a call to the above constructor. In
     * addition, this match is set to the given parameter.
     * @param swingSession The GUI session this action lives in.
     */
	public DeleteProjectAction(MatchMakerSwingSession swingSession, Match match) {
		this(swingSession);
		this.match = match;
		this.calledFromTopMenu = false;
	}
	
	/**
	 * Performs the delete action if a Match is selected.
	 */
	public void actionPerformed(ActionEvent e) {
		if (calledFromTopMenu) {
			this.match = MMSUtils.getTreeObject(this.swingSession.getTree(),Match.class);
			if (match == null) return;
		}
		int responds = JOptionPane.showConfirmDialog(swingSession.getFrame(),
				"Are you sure you want to delete the match [" +
				match.getName() + "]?",
				"Confirm Delete",
				JOptionPane.YES_NO_OPTION);
		if ( responds != JOptionPane.YES_OPTION )
			return;
		swingSession.delete(match);
	}
	
}
