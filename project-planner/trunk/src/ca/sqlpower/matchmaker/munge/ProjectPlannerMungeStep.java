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

package ca.sqlpower.matchmaker.munge;

/**
 * This is the default munge step for the Project Planner. This
 * step will be used for every munge step in the munge pen.
 */
public class ProjectPlannerMungeStep extends AbstractMungeStep {
	
	/**
	 * The parameter key for the text in this munge step.
	 */
	private static final String TEXT_PARAMETER_NAME = "text";

	public ProjectPlannerMungeStep(String name) {
		super(name, true);
		defineIO();
	}

	/**
	 * Sets the first input and output for this munge component
	 * as required to have inputs and outputs.
	 */
	private void defineIO() {
		MungeStepOutput<String> out = new MungeStepOutput<String>("output", String.class);
		addChild(out);
		InputDescriptor desc1 = new InputDescriptor("input", String.class);
		super.addInput(desc1);
	}
	
	/**
	 * This private constructor is required for hibernate to load in 
	 * the project through reflection. 
	 */
	@SuppressWarnings("unused")
	private ProjectPlannerMungeStep() {
		super("", true);
		defineIO();
	}
	
    public void setText(String newValue) {
        setParameter(TEXT_PARAMETER_NAME, newValue);
    }
    
    public String getText() {
    	return getParameter(TEXT_PARAMETER_NAME);
    }

}
