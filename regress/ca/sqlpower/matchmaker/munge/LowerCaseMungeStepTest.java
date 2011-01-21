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

public class LowerCaseMungeStepTest extends TestCase {

	private LowerCaseMungeStep step;
	
	private MungeStepOutput testInput;
	
	private final Logger logger = Logger.getLogger("testLogger");
	
	protected void setUp() throws Exception {
		super.setUp();
		step = new LowerCaseMungeStep();
	}

	public void testCallonUpperCaseString() throws Exception {
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData("ABCDEFG");
		step.connectInput(0, testInput);
		step.open(logger);
		step.call();
		List<MungeStepOutput> results = step.getChildren(); 
		MungeStepOutput output = results.get(0);
		String result = (String)output.getData();
		assertEquals("abcdefg", result);
	}

	public void testCallonMixedString() throws Exception {
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData("abcDEF!@#$%^&*");
		step.connectInput(0, testInput);
		step.open(logger);
		step.call();
		List<MungeStepOutput> results = step.getChildren(); 
		MungeStepOutput output = results.get(0);
		String result = (String)output.getData();
		assertEquals("abcdef!@#$%^&*", result);
	}

	
	public void testCallonNull() throws Exception {
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData(null);
		step.connectInput(0, testInput);
		step.open(logger);
		step.call();
		List<MungeStepOutput> results = step.getChildren(); 
		MungeStepOutput output = results.get(0);
		String result = (String)output.getData();
		assertEquals(null, result);
	}
	
	public void testConnectIntegerInput() throws Exception {
		testInput = new MungeStepOutput<Integer>("test", Integer.class);
		testInput.setData(new Integer(1));
		try {
			step.connectInput(0, testInput);
			fail("UnexpectedDataTypeException was not thrown as expected");
		} catch (UnexpectedDataTypeException ex) {
			// UnexpectedDataTypeException was thrown as expected
		}
	}
    
    public void testRemoveInput() throws Exception {
        try {
            step.removeInput(0);
            fail("UnsupportedOperationException should have been thrown");
        } catch (UnsupportedOperationException ex) {
            // good
        }
    }
    
    public void testAddInput() throws Exception {
        try {
            step.addInput(new InputDescriptor("This should not work", String.class));
            fail("UnsupportedOperationException should have been thrown");
        } catch (UnsupportedOperationException ex) {
            // good
        }
    }

}
