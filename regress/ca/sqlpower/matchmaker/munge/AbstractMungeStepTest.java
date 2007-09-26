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

import java.util.List;

import junit.framework.TestCase;
import ca.sqlpower.matchmaker.TestingAbstractMungeStep;

public class AbstractMungeStepTest extends TestCase {

	TestingAbstractMungeStep mungeStep;
	
	protected void setUp() throws Exception {
		super.setUp();
		mungeStep = new TestingAbstractMungeStep();
	}

	public void testAddInput() {
		MungeStepOutput out = new MungeStepOutput<Object>("test", Object.class);
		mungeStep.addInput(out);
		assertTrue(mungeStep.getInputs().contains(out));
	}

	public void testGetInputs() {
		fail("Not yet implemented");
	}

	public void testGetOutputs() {
		List<MungeStepOutput> list = mungeStep.getChildren();
		try {
			list.add(new MungeStepOutput<Object>("test", Object.class));
			fail("Trying to modify output list should have thrown an exception");
		} catch (UnsupportedOperationException ex) {
			// UnsupportedOperationException thrown as expected
		}
		
	}

	public void testGetParameter() {
		fail("Not yet implemented");
	}

	public void testRemoveInput() {
		fail("Not yet implemented");
	}

	public void testSetParameter() {
		fail("Not yet implemented");
	}

	public void testGetParent() {
		fail("Not yet implemented");
	}

}
