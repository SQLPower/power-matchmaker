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

package ca.sqlpower.matchmaker.undo;

import java.beans.PropertyChangeEvent;

import junit.framework.TestCase;
import ca.sqlpower.matchmaker.munge.AbstractMungeStep;
import ca.sqlpower.matchmaker.munge.DeDupeResultStep;
import ca.sqlpower.matchmaker.munge.MungeStepOutput;
import ca.sqlpower.matchmaker.munge.StringConstantMungeStep;

public class MMOPropertyChangeUndoableEditTest extends TestCase {

	/**
	 * The munge step inputs being set to a null value is a special case. When a munge step
	 * input is set to null the step's input needs to be disconnected. This also checks
	 * that the step's input list is not being set to null for regression testing.
	 */
	public void testSetMungeStepInputToNull() {
		StringConstantMungeStep stringConstantStep = new StringConstantMungeStep();
		DeDupeResultStep resultStep = new DeDupeResultStep();
		assertEquals("Wrong amount of inputs", 1, resultStep.getInputCount());
		resultStep.connectInput(0, stringConstantStep.getChildren(MungeStepOutput.class).get(0));
		assertEquals("Wrong amount of inputs", 2, resultStep.getInputCount());
		
		PropertyChangeEvent inputChangeEvent = 
			new PropertyChangeEvent(resultStep.getChildren(AbstractMungeStep.Input.class).get(0), "current", null, stringConstantStep.getChildren(MungeStepOutput.class).get(0));
		MMOPropertyChangeUndoableEdit edit = new MMOPropertyChangeUndoableEdit(inputChangeEvent);

		assertNotNull("Your input should be connected", resultStep.getMSOInputs().get(0));
		
		edit.undo();
		assertNotNull("You should still have inputs", resultStep.getInputs());
		assertEquals("You should have all inputs still there" ,2, resultStep.getInputCount());
		assertNull("Your original input should be disconnected", resultStep.getMSOInputs().get(0));
	}
}
