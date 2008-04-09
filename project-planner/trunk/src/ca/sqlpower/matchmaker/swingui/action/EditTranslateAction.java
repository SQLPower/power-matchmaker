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
import javax.swing.tree.TreePath;

import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.matchmaker.swingui.MatchMakerTreeModel;

/**
 * This action creates a TranslatePanel and puts it in a popup dialog with an OK button.
 */
public class EditTranslateAction extends AbstractAction {

    private final MatchMakerSwingSession swingSession;

	public EditTranslateAction(MatchMakerSwingSession swingSession) {
		super("Translate Words Manager");
        this.swingSession = swingSession;
	}
	
	public void actionPerformed(ActionEvent e) {
		MatchMakerTreeModel treeModel = (MatchMakerTreeModel) swingSession.getTree().getModel();
		TreePath menuPath = treeModel.getPathForNode(swingSession.getTranslations());
		swingSession.getTree().setSelectionPath(menuPath);
	}

}
