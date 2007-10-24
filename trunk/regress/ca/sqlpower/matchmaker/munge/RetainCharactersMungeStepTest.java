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

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.TestingMatchMakerSession;

import junit.framework.TestCase;

public class RetainCharactersMungeStepTest extends TestCase {

	private RetainCharactersMungeStep step;
	private MungeStepOutput testInput;
	
	private final Logger logger = Logger.getLogger("testLogger");
	
	protected void setUp() throws Exception {
		super.setUp();
		step = new RetainCharactersMungeStep(new TestingMatchMakerSession());
	}

	/**
	 * This tests the case where the target string is not present, the output
	 * should just be the same as before the call. 
	 */
	public void testCallonNoOccurrence() throws Exception {
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData("abcdefg");
		step.setParameter(step.RETAIN_CHARACTERS_PARAMETER_NAME, "123");
		step.connectInput(0, testInput);
		
		step.open(logger);
		step.call();
		List<MungeStepOutput> results = step.getChildren(); 
		MungeStepOutput output = results.get(0);
		String result = (String)output.getData();
		assertEquals("", result);
	}

	public void testCallonMultipleOccurrences() throws Exception {
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData("abcdABCabcd");
		step.setParameter(step.RETAIN_CHARACTERS_PARAMETER_NAME, "abc");
		step.connectInput(0, testInput);
		
		step.open(logger);
		step.call();
		List<MungeStepOutput> results = step.getChildren(); 
		MungeStepOutput output = results.get(0);
		String result = (String)output.getData();
		assertEquals("abcabc", result);
	}
	
	public void testCallonCaseInsensitive() throws Exception {
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData("abcdABCabcd");
		step.setParameter(step.RETAIN_CHARACTERS_PARAMETER_NAME, "abc");
		step.setParameter(step.CASE_SENSITIVE_PARAMETER_NAME, "false");
		step.connectInput(0, testInput);
		
		step.open(logger);
		step.call();
		List<MungeStepOutput> results = step.getChildren(); 
		MungeStepOutput output = results.get(0);
		String result = (String)output.getData();
		assertEquals("abcABCabc", result);
	}

	/**
	 * This tests the case where the regular expression option is enabled.
	 */
	public void testCallonUsingRegex() throws Exception {
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData("xxy123xxy!@#xyABC");
		step.connectInput(0, testInput);
		step.setParameter(step.RETAIN_CHARACTERS_PARAMETER_NAME, "[a-zA-z]");
		step.setParameter(step.USE_REGEX_PARAMETER_NAME, "true");
		
		step.open(logger);
		step.call();
		List<MungeStepOutput> results = step.getChildren(); 
		MungeStepOutput output = results.get(0);
		String result = (String)output.getData();
		assertEquals("xxyxxyxyABC", result);
	}
	
	public void testCallonNull() throws Exception {
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData(null);
		step.connectInput(0, testInput);
		step.setParameter(step.RETAIN_CHARACTERS_PARAMETER_NAME, "123");
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
