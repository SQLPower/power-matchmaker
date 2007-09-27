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

package ca.sqlpower.matchmaker.munge;

import junit.framework.TestCase;
import ca.sqlpower.matchmaker.event.MatchMakerEventCounter;

public class AbstractMungeStepTest extends TestCase {

	TestingAbstractMungeStep mungeStep;
	
	protected void setUp() throws Exception {
		super.setUp();
		mungeStep = new TestingAbstractMungeStep();
	}

	/**
	 * Test to ensure that add input adds the MungeStepOuput, and 
	 * that it fires a property changed event
	 */
	public void testAddInput() {
		MungeStepOutput out = new MungeStepOutput<Object>("test", Object.class);
		MatchMakerEventCounter<MungeStep, MungeStepOutput> mml =
			new MatchMakerEventCounter<MungeStep, MungeStepOutput>();
		mungeStep.addMatchMakerListener(mml);
		mungeStep.addInput(out);
		assertTrue(mungeStep.getInputs().contains(out));
		assertEquals("Did not get any events",1,mml.getPropertyChangedCount());
		mungeStep.removeMatchMakerListener(mml);
		mungeStep.addInput(out);
		assertEquals("Got extra events",1,mml.getAllEventCounts());
	}

	/**
	 * Test to ensure that remove input removes the MungeStepOutput, and 
	 * that it fires a property changed event
	 */
	public void testRemoveInput() {
		MungeStepOutput out = new MungeStepOutput<Object>("test", Object.class);
		mungeStep.addInput(out);
		MatchMakerEventCounter<MungeStep, MungeStepOutput> mml =
			new MatchMakerEventCounter<MungeStep, MungeStepOutput>();
		mungeStep.addMatchMakerListener(mml);
		mungeStep.removeInput(out);
		assertFalse(mungeStep.getInputs().contains(out));
		assertEquals("Did not get any events",1,mml.getPropertyChangedCount());
	}

	/**
	 * Test to ensure setParameter properly sets a munge step parameter, and 
	 * that it fires a property changed event
	 */
	public void testSetParameter() {
		MatchMakerEventCounter<MungeStep, MungeStepOutput> mml =
			new MatchMakerEventCounter<MungeStep, MungeStepOutput>();
		mungeStep.addMatchMakerListener(mml);
		mungeStep.setParameter("test", "test");
		assertEquals("Did not get any events",1,mml.getPropertyChangedCount());
		assertEquals("Parameter cannot be found", "test", mungeStep.getParameter("test"));
	}
}
