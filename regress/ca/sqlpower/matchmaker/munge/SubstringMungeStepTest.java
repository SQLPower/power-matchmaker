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

import junit.framework.TestCase;

import org.apache.log4j.Logger;

public class SubstringMungeStepTest extends TestCase {

	private SubstringMungeStep step;
	
	private MungeStepOutput testInput;
	
	private final Logger logger = Logger.getLogger("testLogger");
	
	protected void setUp() throws Exception {
		super.setUp();
		step = new SubstringMungeStep();
	}

	public void testCallonNormalString() throws Exception {
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData("abcABCabc");
		step.connectInput(0, testInput);
		step.setParameter(step.BEGIN_PARAMETER_NAME, 3);
		step.setParameter(step.END_PARAMETER_NAME, 9);
		step.open(logger);
		step.call();
		List<MungeStepOutput> results = step.getChildren(); 
		MungeStepOutput output = results.get(0);
		String result = (String)output.getData();
		assertEquals("ABCabc", result);
	}
	
	public void testCallonIndexOutofBounds() throws Exception {
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData("abcABCabc");
		step.connectInput(0, testInput);
		
		step.setParameter(step.BEGIN_PARAMETER_NAME, 3);
		step.setParameter(step.END_PARAMETER_NAME, 100);
		step.open(logger);
		step.call();
		List<MungeStepOutput> results = step.getChildren(); 
		MungeStepOutput output = results.get(0);
		String result = (String)output.getData();
		assertEquals("ABCabc", result);
        step.commit();
        step.mungeClose();
		
		step.setParameter(step.BEGIN_PARAMETER_NAME, 90);
		step.setParameter(step.END_PARAMETER_NAME, 100);
		step.open(logger);
		step.call();
		results = step.getChildren(); 
		output = results.get(0);
		result = (String)output.getData();
		assertEquals("", result);
        step.commit();
        step.close();
		
		step.setParameter(step.BEGIN_PARAMETER_NAME, -100);
		step.setParameter(step.END_PARAMETER_NAME, 100);
		step.open(logger);
		try {
			step.call();
			fail("IndexOutofBoundsException was not thrown as expected");
		} catch (IndexOutOfBoundsException ex) {
			// IndexOutOfBoundsException was thrown as expected
		}
        step.commit();
        step.close();
	}
	
	public void testCallonNull() throws Exception {
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData(null);
		step.setParameter(step.BEGIN_PARAMETER_NAME, 3);
		step.setParameter(step.END_PARAMETER_NAME, 9);
		step.connectInput(0, testInput);
		step.open(logger);
		step.call();
		List<MungeStepOutput> results = step.getChildren(); 
		MungeStepOutput output = results.get(0);
		String result = (String)output.getData();
		assertEquals(null, result);
	}
	
	public void testCallonInteger() throws Exception {
		testInput = new MungeStepOutput<Integer>("test", Integer.class);
		testInput.setData(new Integer(1));
		try {
			step.connectInput(0, testInput);
			fail("UnexpectedDataTypeException was not thrown as expected");
		} catch (UnexpectedDataTypeException ex) {
			// UnexpectedDataTypeException was thrown as expected
		}
	}
}
