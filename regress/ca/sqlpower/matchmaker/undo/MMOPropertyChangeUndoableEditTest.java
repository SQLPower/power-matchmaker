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

package ca.sqlpower.matchmaker.undo;

import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.event.MatchMakerEvent;
import ca.sqlpower.matchmaker.munge.DeDupeResultStep;
import ca.sqlpower.matchmaker.munge.StringConstantMungeStep;
import junit.framework.TestCase;

public class MMOPropertyChangeUndoableEditTest extends TestCase {

	/**
	 * The munge step inputs being set to a null value is a special case. When a munge step
	 * input is set to null the step's input needs to be disconnected. This also checks
	 * that the step's input list is not being set to null for regression testing.
	 */
	public void testSetMungeStepInputToNull() {
		StringConstantMungeStep stringConstantStep = new StringConstantMungeStep();
		DeDupeResultStep resultStep = new DeDupeResultStep();
		assertEquals(1, resultStep.getInputCount());
		resultStep.connectInput(0, stringConstantStep.getChildren().get(0));
		assertEquals(2, resultStep.getInputCount());
		
		MatchMakerEvent<DeDupeResultStep, MatchMakerObject> inputChangeEvent = new MatchMakerEvent<DeDupeResultStep, MatchMakerObject>();
		inputChangeEvent.setSource(resultStep);
		inputChangeEvent.setOldValue(null);
		inputChangeEvent.setPropertyName("inputs");
		int[] changedIndices = { 0 };
		inputChangeEvent.setChangeIndices(changedIndices);
		MMOPropertyChangeUndoableEdit edit = new MMOPropertyChangeUndoableEdit(inputChangeEvent);
		
		edit.undo();
		assertNotNull(resultStep.getInputs());
		assertEquals(2, resultStep.getInputCount());
		assertNull(resultStep.getMSOInputs().get(0));
	}
}
