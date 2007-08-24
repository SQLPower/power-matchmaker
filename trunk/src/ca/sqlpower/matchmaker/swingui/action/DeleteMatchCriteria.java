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

import ca.sqlpower.matchmaker.MatchMakerCriteria;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;

public class DeleteMatchCriteria extends AbstractAction {
	MatchMakerCriteria criteria;
	MatchMakerSwingSession swingSession;
	
	public DeleteMatchCriteria(MatchMakerSwingSession swingSession, MatchMakerCriteria criteria) {
		super("Delete Criteria");
		this.criteria = criteria;
		this.swingSession = swingSession;
	}

	public void actionPerformed(ActionEvent e) {
		swingSession.delete(criteria);
	}

}
