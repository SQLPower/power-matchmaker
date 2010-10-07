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
import javax.swing.JOptionPane;

import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.swingui.MMSUtils;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;

/**
 * Simple action that deletes a project from the swing session
 */
public class DeleteProjectAction extends AbstractAction {

	private MatchMakerSwingSession swingSession;
	private Project project;
	private boolean calledFromTopMenu = true;
	
    /**
     * Creates a new instance of this action which is parented by the given swingSession.
     * project is null when an instance is created with this constructor and will be 
     * configured to decide which Project to delete by checking for the selected item 
     * in the tree.
     * @param swingSession The GUI session this action lives in.
     */
	public DeleteProjectAction(MatchMakerSwingSession swingSession) {
		super("Delete Project");
		this.swingSession = swingSession;
	}
	
    /**
     * Creates a new instance of this action with a call to the above constructor. In
     * addition, this project is set to the given parameter.
     * @param swingSession The GUI session this action lives in.
     */
	public DeleteProjectAction(MatchMakerSwingSession swingSession, Project project) {
		this(swingSession);
		this.project = project;
		this.calledFromTopMenu = false;
	}
	
	/**
	 * Performs the delete action if a Project is selected.
	 */
	public void actionPerformed(ActionEvent e) {
		if (calledFromTopMenu) {
			this.project = MMSUtils.getTreeObject(this.swingSession.getTree(),Project.class);
			if (project == null) return;
		}
		int responds = JOptionPane.showConfirmDialog(swingSession.getFrame(),
				"Are you sure you want to delete the project [" +
				project.getName() + "]?",
				"Confirm Delete",
				JOptionPane.YES_NO_OPTION);
		if ( responds != JOptionPane.YES_OPTION )
			return;
		swingSession.delete(project);
	}
	
}
