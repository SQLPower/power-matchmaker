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

import ca.sqlpower.matchmaker.MatchMakerTestCase;
import ca.sqlpower.object.SPObject;

public class SubstringByWordMungeStepTest extends MatchMakerTestCase<SubstringByWordMungeStep> {

	public SubstringByWordMungeStepTest(String name) {
		super(name);
	}
	private SubstringByWordMungeStep step;
	
	private MungeStepOutput testInput;
	
	private final Logger logger = Logger.getLogger("testLogger");
	
	protected void setUp() throws Exception {
		super.setUp();
		step = new SubstringByWordMungeStep();
		MungeProcess process = (MungeProcess) createNewValueMaker(
        		getRootObject(), null).makeNewValue(
        				MungeProcess.class, null, "parent process");
        process.addMungeStep(step, process.getMungeSteps().size());
	}
	public void testCallonNoOccurrence() throws Exception {
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData("abcdefg");
		step.setBegIndex(1);
		step.setEndIndex(3);
		step.connectInput(0, testInput);
		step.open(logger);
		step.call();
		List<MungeStepOutput> results = step.getMungeStepOutputs(); 
		MungeStepOutput output = results.get(0);
		String result = (String)output.getData();
		assertEquals("bc",result);
	}

	public void testCallonMultipleOccurrences() throws Exception {
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData("abc bcd cde def efg fgh ghi");
		step.setBegIndex(1);
		step.setEndIndex(2);
		step.connectInput(0, testInput);
		step.open(logger);
		step.call();
		List<MungeStepOutput> results = step.getMungeStepOutputs(); 
		MungeStepOutput output = results.get(0);
		String result = (String)output.getData();
		assertEquals("b c d e f g h",result);
	}

	public void testCallonMixedDelimiters() throws Exception {
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData("abc   bcd \ncde def\n efg fgh\n\nghi");
		step.setDelimiter(" \n");
		step.setResultDelim(":");
		step.setBegIndex(1);
		step.setEndIndex(2);
		step.connectInput(0, testInput);
		step.open(logger);
		step.call();
		List<MungeStepOutput> results = step.getMungeStepOutputs(); 
		MungeStepOutput output = results.get(0);
		String result = (String)output.getData();
		assertEquals("b:c:d:e:f:g:h",result);
	}
	
	/**
	 * Tests for a previous bug where special regex characters would
	 * not be taken in as literals even if regex was turned off.
	 */
	public void testCallonRegexInput() throws Exception {
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData("ab\\-+*?()cd[]{}|$^<=de");
		step.setBegIndex(1);
		step.setEndIndex(2);
		step.setDelimiter("\\-+*?()[]{}|$^<=");
		step.connectInput(0, testInput);
		step.open(logger);
		step.call();
		List<MungeStepOutput> results = step.getMungeStepOutputs(); 
		MungeStepOutput output = results.get(0);
		String result = (String)output.getData();
		assertEquals("b d e",result);
	}
	
	public void testCallonNull() throws Exception {
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData(null);
		step.setBegIndex(3);
		step.setEndIndex(9);
		step.connectInput(0, testInput);
		step.open(logger);
		step.call();
		List<MungeStepOutput> results = step.getMungeStepOutputs(); 
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
	
	public void testCallonCaseInsensitive() throws Exception {
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData("abczbcdZcdezdefZefg");
		step.setDelimiter("z");
		step.setBegIndex(1);
		step.setEndIndex(2);
		step.setCaseSensitive(false);
		step.connectInput(0, testInput);
		
		step.open(logger);
		step.call();
		List<MungeStepOutput> results = step.getMungeStepOutputs(); 
		MungeStepOutput output = results.get(0);
		String result = (String)output.getData();
		assertEquals("b c d e f", result);
	}
	@Override
	protected SubstringByWordMungeStep getTarget() {
		return step;
	}
	@Override
	protected Class<? extends SPObject> getChildClassType() {
		// TODO Auto-generated method stub
		logger.debug("Stub call: PersistedSPObjectTest.getChildClassType()");
		return null;
	}
	
	@Override
	public void testAllowedChildTypesField() throws Exception {
		// no=op
	}
}
