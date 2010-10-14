/*
 * Copyright (c) 2010, SQL Power Group Inc.
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

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;

/**
 * An action that saves a workspace to the swing sessions's
 * {@link MatchMakerSwingSession.savePoint savePoint} if it exists and prompts
 * to select a savePoint otherwise.
 */
public class SaveWorkspaceAction extends AbstractAction {

	private static final Logger logger = Logger.getLogger(SaveWorkspaceAsAction.class);
	
	private final MatchMakerSwingSession session;
	
	public SaveWorkspaceAction(MatchMakerSwingSession session) {
		super("Save");
		this.session = session;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (session.getSavePoint() != null) {
			SaveWorkspaceAsAction.doSaveAs(session.getSavePoint(), session);
		} else {
			session.setSavePoint(SaveWorkspaceAsAction.selectFileName(session));
			if (session.getSavePoint() != null) {
				SaveWorkspaceAsAction.doSaveAs(session.getSavePoint(), session);
			}
		}
	}

}
