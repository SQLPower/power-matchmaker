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
import javax.swing.JOptionPane;

import ca.sqlpower.matchmaker.MatchMakerTranslateGroup;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;


public class DeleteTranslateGroupAction extends AbstractAction {

    private final MatchMakerSwingSession swingSession;
    private final MatchMakerTranslateGroup group;

	public DeleteTranslateGroupAction(MatchMakerSwingSession swingSession,
			MatchMakerTranslateGroup group) {
		super("Delete Translate Group");
        this.swingSession = swingSession;
		this.group = group;
	}

	public void actionPerformed(ActionEvent e) {
		int response = JOptionPane.showConfirmDialog(swingSession.getFrame(),
		"Are you sure you want to delete the translate group \"" + group.getName() + "\"?");
		if (response != JOptionPane.YES_OPTION)
			return;
		
		if (swingSession.getTranslations().isInUseInBusinessModel(group)) {
			JOptionPane.showMessageDialog(swingSession.getFrame(),
				"This translation group is in use, and cannot be deleted.");
		} else {
			swingSession.delete(group);
		}
	}
}
