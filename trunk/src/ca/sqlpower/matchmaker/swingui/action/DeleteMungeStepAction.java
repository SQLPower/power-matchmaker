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
import javax.swing.tree.TreePath;

import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.matchmaker.swingui.MatchMakerTreeModel;

/**
 * A simple action for deleting a munge step.
 */
public class DeleteMungeStepAction extends AbstractAction {
	MungeStep step;
	MatchMakerSwingSession swingSession;
	
	public DeleteMungeStepAction(MatchMakerSwingSession swingSession, MungeStep step) {
		super("Delete Step");
		this.step = step;
		this.swingSession = swingSession;
	}

	public void actionPerformed(ActionEvent e) {
		MungeProcess mp = (MungeProcess) step.getParent();
		mp.removeChildAndInputs(step);
		
		MatchMakerTreeModel treeModel = (MatchMakerTreeModel) swingSession.getTree().getModel();
		TreePath treePath = treeModel.getPathForNode(mp);
		swingSession.getTree().setSelectionPath(treePath);
	}

}
