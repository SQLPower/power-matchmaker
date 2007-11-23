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

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.munge.CleanseResultStep;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.munge.SQLInputStep;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.matchmaker.swingui.munge.MungePen;

/**
 * A simple action to adds a new munge process to the swing session and
 * opens up the editor for the new munge process.
 */
public class NewMungeProcessAction extends AbstractAction {
	private static final Logger logger = Logger.getLogger(NewMungeProcessAction.class);
    private final MatchMakerSwingSession swingSession;
	private final Project project;

	public NewMungeProcessAction(MatchMakerSwingSession swingSession, Project parent) {
	    super("New Munge Process");
        this.swingSession = swingSession;
        this.project = parent;
        if (parent == null) throw new IllegalArgumentException("Parent must be non null");
	}
	
	public void actionPerformed(ActionEvent e) {
		MungeProcess process = new MungeProcess();
		int count;
    	for (count = 1; project.getMungeProcessByName("New Munge Process " + count) != null ; count++);
    	process.setName("New Munge Process " + count);
    	project.addMungeProcess(process);
    	SQLInputStep inputStep = new SQLInputStep();
		inputStep.setParameter(MungeStep.MUNGECOMPONENT_EXPANDED, true);
		process.addChild(inputStep);
		
		try {
			inputStep.open(logger);
            inputStep.rollback();
			inputStep.close();
		} catch (Exception ex) {
			throw new RuntimeException("Could not set up the input munge step!", ex);
		}
		
		MungeStep mungeResultStep;
		try {
			mungeResultStep = inputStep.getOutputStep(project);
		} catch (ArchitectException e1) {
			throw new RuntimeException(e1);
		}
		
		String x = new Integer(MungePen.AUTO_SCROLL_INSET + 5).toString();
		String y = new Integer(300).toString();
		
		//sets the input one just outside of the autoscroll bounds
		inputStep.setParameter(MungeStep.MUNGECOMPONENT_X, x);
		inputStep.setParameter(MungeStep.MUNGECOMPONENT_Y, x);
		
		//sets the location of the result step (resonalibly arbatrary location)
		mungeResultStep.setParameter(MungeStep.MUNGECOMPONENT_X, x);
		mungeResultStep.setParameter(MungeStep.MUNGECOMPONENT_Y, y);
		
		process.addChild(mungeResultStep);
		
		if (mungeResultStep instanceof CleanseResultStep) {
			try {
				((CleanseResultStep)mungeResultStep).open(logger);
                mungeResultStep.rollback();
				mungeResultStep.close();
			} catch (Exception ex) {
				throw new RuntimeException("Could not set up the result munge step!", ex);
			}
		}
    	swingSession.save(process);
	}

}
