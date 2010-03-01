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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.Project.ProjectMode;
import ca.sqlpower.matchmaker.munge.AddressCorrectionMungeStep;
import ca.sqlpower.matchmaker.munge.CleanseResultStep;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.munge.SQLInputStep;
import ca.sqlpower.matchmaker.swingui.ColorScheme;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.matchmaker.swingui.munge.MungePen;
import ca.sqlpower.sqlobject.SQLObjectException;

/**
 * A simple action to adds a new munge process to the swing session and
 * opens up the editor for the new munge process.
 */
public class NewMungeProcessAction extends AbstractAction {
	private static final Logger logger = Logger.getLogger(NewMungeProcessAction.class);
    private final MatchMakerSwingSession swingSession;
	private final Project project;
	
	/**
	 * The default distance between the upper y co-ordinate of the input step
	 * and the upper y co-ordinate of the result step when the new Munge Process
	 * gets created.
	 */
	private static final int DISTANCE_BETWEEN_INPUT_AND_RESULT_STEP = 350;

	public NewMungeProcessAction(MatchMakerSwingSession swingSession, Project parent) {
	    super("New Transformation");
        this.swingSession = swingSession;
        this.project = parent;
        if (parent == null) throw new IllegalArgumentException("Parent must be non null");
	}
	
	public void actionPerformed(ActionEvent e) {
		MungeProcess process = new MungeProcess();
		int count;
    	for (count = 1; project.getMungeProcessByName("New Transformation " + count) != null ; count++);
    	process.setName("New Transformation " + count);
    	
    	// default not to reuse colours
    	List<Color> usedColors = new ArrayList<Color>();
    	for (MungeProcess mp : project.getMungeProcesses()) {
    		usedColors.add(mp.getColour());
    	}
    	Color color = null;
    	for (Color c : ColorScheme.BREWER_SET19) {
    		if (!usedColors.contains(c)) {
    			color = c;
    			break;
    		}
    	}
    	// if all colours were used, use the default
    	if (color == null) {
    		color = MungeProcess.DEFAULT_COLOR;
    	}
    	process.setColour(color);
    	
    	project.addMungeProcess(process);
    	SQLInputStep inputStep = new SQLInputStep();
		inputStep.setParameter(MungeStep.MUNGECOMPONENT_EXPANDED, true);
		process.addChild(inputStep);
		
		try {
			inputStep.refresh(logger);
		} catch (Exception ex) {
			throw new RuntimeException("Could not set up the input munge step!", ex);
		}
		
		MungeStep mungeResultStep;
		try {
			mungeResultStep = inputStep.getOutputStep(project);
		} catch (SQLObjectException ex) {
			throw new RuntimeException("Could not find or set up the result munge step!", ex);
		}
		
		String x = Integer.toString(MungePen.AUTO_SCROLL_INSET + 5);
		String y = Integer.toString(DISTANCE_BETWEEN_INPUT_AND_RESULT_STEP);
		
		//sets the input one just outside of the autoscroll bounds
		inputStep.setParameter(MungeStep.MUNGECOMPONENT_X, x);
		inputStep.setParameter(MungeStep.MUNGECOMPONENT_Y, x);
		
		//sets the location of the result step (resonalibly arbatrary location)
		mungeResultStep.setParameter(MungeStep.MUNGECOMPONENT_X, x);
		mungeResultStep.setParameter(MungeStep.MUNGECOMPONENT_Y, y);
		
		process.addChild(mungeResultStep);
		
		if (mungeResultStep instanceof CleanseResultStep) {
			try {
				mungeResultStep.setParameter(MungeStep.MUNGECOMPONENT_EXPANDED, true);
				mungeResultStep.open(logger);
                mungeResultStep.rollback();
				mungeResultStep.close();
			} catch (Exception ex) {
				throw new RuntimeException("Could not set up the cleanse result munge step!", ex);
			}
		}
		
		if (project.getType() == ProjectMode.ADDRESS_CORRECTION) {
			AddressCorrectionMungeStep addressStep = new AddressCorrectionMungeStep();
			addressStep.setInputStep(inputStep);
			addressStep.setParameter(MungeStep.MUNGECOMPONENT_EXPANDED, true);
			addressStep.setParameter(MungeStep.MUNGECOMPONENT_X, x);
			addressStep.setParameter(MungeStep.MUNGECOMPONENT_Y, Integer.toString(DISTANCE_BETWEEN_INPUT_AND_RESULT_STEP / 2));
			process.addChild(addressStep);
		}
		
    	swingSession.save(process);
	}

}
