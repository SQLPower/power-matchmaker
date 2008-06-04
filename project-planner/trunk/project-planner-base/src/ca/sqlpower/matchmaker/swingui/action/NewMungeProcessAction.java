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

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;

/**
 * A simple action to adds a new munge process to the swing session and
 * opens up the editor for the new munge process.
 */
public class NewMungeProcessAction extends AbstractAction {
	private static final Logger logger = Logger.getLogger(NewMungeProcessAction.class);
    private final MatchMakerSwingSession swingSession;
	private final Project project;

	public NewMungeProcessAction(MatchMakerSwingSession swingSession, Project parent) {
	    super("New Workflow");
        this.swingSession = swingSession;
        this.project = parent;
        if (parent == null) throw new IllegalArgumentException("Parent must be non null");
	}
	
	public void actionPerformed(ActionEvent e) {
		MungeProcess process = new MungeProcess();
		int count;
    	for (count = 1; project.getMungeProcessByName("New Workflow " + count) != null ; count++);
    	process.setName("New Workflow " + count);
    	project.addChild(process);
		
    	swingSession.save(process);
	}

}
