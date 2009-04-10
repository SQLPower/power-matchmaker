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

package ca.sqlpower.matchmaker.munge;

import java.util.List;

import org.apache.log4j.Logger;

/**
 * A Mock MungeResultStep that also extends TestingMungeStep
 */
public class TestingResultMungeStep extends TestingMungeStep implements MungeResultStep {

	@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = { "MF_CLASS_MASKS_FIELD" }, 
			justification = "We will always want to use this logger, not the superclass logger")
	Logger logger = Logger.getLogger(TestingResultMungeStep.class);
	
	public TestingResultMungeStep(String name, int inputs) {
		super(name, inputs, 0);
	}

	public void addInputStep(SQLInputStep inputStep) {
		// TODO Auto-generated method stub
		logger.debug("Stub call: MungeResultStep.addInputStep()");
	}

	public List<MungeResult> getResults() {
		// TODO Auto-generated method stub
		logger.debug("Stub call: MungeResultStep.getResults()");
		return null;
	}
}
