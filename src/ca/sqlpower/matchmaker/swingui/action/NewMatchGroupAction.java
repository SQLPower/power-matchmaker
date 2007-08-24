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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.i 
 */

package ca.sqlpower.matchmaker.swingui.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchMakerCriteriaGroup;
import ca.sqlpower.matchmaker.swingui.MatchMakerCriteriaGroupEditor;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;

public class NewMatchGroupAction extends AbstractAction {
    
    private final MatchMakerSwingSession swingSession;
	private final Match parent;

	public NewMatchGroupAction(MatchMakerSwingSession swingSession, Match parent) {
	    super("New Match Group");
        this.swingSession = swingSession;
        this.parent = parent;
        if (parent == null) throw new IllegalArgumentException("Parent must be non null");
	}
	
	public void actionPerformed(ActionEvent e) {
		MatchMakerCriteriaGroup g = new MatchMakerCriteriaGroup();
		parent.getMatchCriteriaGroupFolder().addChild(g);
        
        swingSession.setCurrentEditorComponent(new MatchMakerCriteriaGroupEditor(swingSession, parent, g));
	}

}
