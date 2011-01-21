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

import ca.sqlpower.matchmaker.MatchMakerTranslateGroupDAOStub;
import ca.sqlpower.matchmaker.TestingMatchMakerSession;

public class TranslateWordMungeStepTest extends TestCase {

	private TranslateWordMungeStep step;
	
	private MungeStepOutput testInput;
	
	private final Logger logger = Logger.getLogger("testLogger");
	
	/**
	 * Translate group is not set here in the test but when the munge step
	 * is opened, a call to {@link MatchMakerTranslateGroupDAOStub#findByOID(long)}
	 * is made and that method just returns a default translate group
	 * despite any given oid. Check the method for translate words.
	 */
	protected void setUp() throws Exception {
		super.setUp();
		step = new TranslateWordMungeStep();
		step.setSession(new TestingMatchMakerSession());
	}

	/**
	 * This tests the case where the target string is not present, the output
	 * should just be the same as before the call. 
	 */
	public void testCallonNoOccurrence() throws Exception {
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData("efg");
		step.setParameter(step.TRANSLATE_GROUP_PARAMETER_NAME, "123");
		step.connectInput(0, testInput);
		
		step.open(logger);
		step.call();
		List<MungeStepOutput> results = step.getChildren(); 
		MungeStepOutput output = results.get(0);
		String result = (String)output.getData();
		assertEquals("efg", result);
	}

	public void testCallonMultipleOccurrences() throws Exception {
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData("abcdABCabcd");
		step.setParameter(step.TRANSLATE_GROUP_PARAMETER_NAME, "123");
		step.connectInput(0, testInput);
		
		step.open(logger);
		step.call();
		List<MungeStepOutput> results = step.getChildren(); 
		MungeStepOutput output = results.get(0);
		String result = (String)output.getData();
		assertEquals("1234ABC1234", result);
	}

	/**
	 * This tests a previous design error where the munge step would
	 * translate consecutive occurrences together as one. 
	 */
	public void testCallonConsecutiveOccurrences() throws Exception {
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData("ababcdcd");
		step.setParameter(step.TRANSLATE_GROUP_PARAMETER_NAME, "123");
		step.connectInput(0, testInput);
		
		step.open(logger);
		step.call();
		List<MungeStepOutput> results = step.getChildren(); 
		MungeStepOutput output = results.get(0);
		String result = (String)output.getData();
		assertEquals("12123434", result);
	}
	
	/**
	 * This tests a previous design error where the munge step would
	 * substitute even if the data had the correct characters but in
	 * the wrong order. 
	 */
	public void testCallonWrongOrder() throws Exception {
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData("abcdbadc");
		step.setParameter(step.TRANSLATE_GROUP_PARAMETER_NAME, "123");
		step.connectInput(0, testInput);
		
		step.open(logger);
		step.call();
		List<MungeStepOutput> results = step.getChildren(); 
		MungeStepOutput output = results.get(0);
		String result = (String)output.getData();
		assertEquals("1234badc", result);
	}
	
	/**
	 * Tests for a previous bug where special regex characters would
	 * not be taken in as literals even if regex was turned off.
	 */
	public void testCallonRegexInput() throws Exception {
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData("ab\\-+*?()[]{}|$^<=cd");
		step.setParameter(step.TRANSLATE_GROUP_PARAMETER_NAME, "123");
		step.connectInput(0, testInput);
		
		step.open(logger);
		step.call();
		List<MungeStepOutput> results = step.getChildren(); 
		MungeStepOutput output = results.get(0);
		String result = (String)output.getData();
		assertEquals("12\\-+*?()[]{}|$^<=34", result);
	}

	/**
	 * This tests the case where the regular expression option is enabled.
	 */
	public void testCallonUsingRegex() throws Exception {
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData("xxyfooxxyfooxyfooy");
		step.connectInput(0, testInput);
		step.setParameter(step.TRANSLATE_GROUP_PARAMETER_NAME, "123");
		step.setParameter(step.USE_REGEX_PARAMETER_NAME, true);
		
		step.open(logger);
		step.call();
		List<MungeStepOutput> results = step.getChildren(); 
		MungeStepOutput output = results.get(0);
		String result = (String)output.getData();
		assertEquals("-foo-foo-foo-", result);
	}
	
	public void testCallonCaseInsensitive() throws Exception {
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData("abcdABCD");
		step.connectInput(0, testInput);
		step.setParameter(step.TRANSLATE_GROUP_PARAMETER_NAME, "123");
		step.setParameter(step.CASE_SENSITIVE_PARAMETER_NAME, false);
		
		step.open(logger);
		step.call();
		List<MungeStepOutput> results = step.getChildren(); 
		MungeStepOutput output = results.get(0);
		String result = (String)output.getData();
		assertEquals("12341234", result);
	}
	
	public void testCallonNull() throws Exception {
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData(null);
		step.connectInput(0, testInput);
		step.setParameter(step.TRANSLATE_GROUP_PARAMETER_NAME, "123");
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
