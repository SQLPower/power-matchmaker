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

package ca.sqlpower.matchmaker.munge;

import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

public class SortWordsMungeStepTest extends TestCase {

	private SortWordsMungeStep step;
	
	private MungeStepOutput testInput;
	
	private final Logger logger = Logger.getLogger("testLogger");
	
	protected void setUp() throws Exception {
		super.setUp();
		step = new SortWordsMungeStep();
	}

	public void testCallOnNull() throws Exception {
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData(null);
		step.connectInput(0, testInput);
		step.open(logger);
		step.call();
		List<MungeStepOutput> results = step.getChildren(); 
		MungeStepOutput output = results.get(0);
		String result = (String) output.getData();
		assertNull(result);
	}
	
	public void testCallOnInteger() throws Exception {
		testInput = new MungeStepOutput<Integer>("test", Integer.class);
		testInput.setData(new Integer(1));
		try {
			step.connectInput(0, testInput);
			fail("UnexpectedDataTypeException was not thrown as expected");
		} catch (UnexpectedDataTypeException ex) {
			// UnexpectedDataTypeException was thrown as expected
		}
	}

	public void testAlreadySortedInput() throws Exception {
	    testInput = new MungeStepOutput<String>("test", String.class);
	    testInput.setData("alpha bravo charlie");
	    step.connectInput(0, testInput);
	    step.open(logger);
	    step.call();
	    List<MungeStepOutput> results = step.getChildren();
	    MungeStepOutput output = results.get(0);
	    String result = (String) output.getData();
	    assertEquals("alpha bravo charlie", result);
	}

    public void testMultipleSpacesWithDefaultDelimiter() throws Exception {
        testInput = new MungeStepOutput<String>("test", String.class);
        testInput.setData("bravo     alpha charlie");
        step.connectInput(0, testInput);
        step.open(logger);
        step.call();
        List<MungeStepOutput> results = step.getChildren();
        MungeStepOutput output = results.get(0);
        String result = (String) output.getData();
        assertEquals("alpha bravo charlie", result);
    }

    public void testLiteralDelimiter() throws Exception {
        testInput = new MungeStepOutput<String>("test", String.class);
        testInput.setData("abc+def+aab+aba+ab+z");
        step.setDelimiterRegex(false);
        step.setDelimiter("b+");
        step.connectInput(0, testInput);
        step.open(logger);
        step.call();
        List<MungeStepOutput> results = step.getChildren();
        MungeStepOutput output = results.get(0);
        String result = (String) output.getData();
        assertEquals("aba+a abc+def+aa z", result);
    }

    public void testRegexDelimiter() throws Exception {
        testInput = new MungeStepOutput<String>("test", String.class);
        testInput.setData("abc+def+aab+aba+ab+z");
        step.setDelimiterRegex(true);
        step.setDelimiter("b+");
        step.connectInput(0, testInput);
        step.open(logger);
        step.call();
        List<MungeStepOutput> results = step.getChildren();
        MungeStepOutput output = results.get(0);
        String result = (String) output.getData();
        assertEquals("+a +z a a+a c+def+aa", result);
    }
    
    public void testSpacesBeforeOnlyWord() throws Exception {
        testInput = new MungeStepOutput<String>("test", String.class);
        testInput.setData("      alpha");
        step.connectInput(0, testInput);
        step.open(logger);
        step.call();
        List<MungeStepOutput> results = step.getChildren();
        MungeStepOutput output = results.get(0);
        String result = (String) output.getData();
        assertEquals("alpha", result);
    }
    
    public void testSpacesAfterOnlyWord() throws Exception {
        testInput = new MungeStepOutput<String>("test", String.class);
        testInput.setData("alpha             ");
        step.connectInput(0, testInput);
        step.open(logger);
        step.call();
        List<MungeStepOutput> results = step.getChildren();
        MungeStepOutput output = results.get(0);
        String result = (String) output.getData();
        assertEquals("alpha", result);
    }

    public void testSpacesMultiWords() throws Exception {
        testInput = new MungeStepOutput<String>("test", String.class);
        testInput.setData("      alpha   crix      bravo           ");
        step.setDelimiter(" ");
        step.connectInput(0, testInput);
        step.open(logger);
        step.call();
        List<MungeStepOutput> results = step.getChildren();
        MungeStepOutput output = results.get(0);
        String result = (String) output.getData();
        assertEquals("alpha bravo crix", result);
    }

    public void testCaseSensitiveDelimiter() throws Exception {
        testInput = new MungeStepOutput<String>("test", String.class);
        testInput.setData("AalphaAAAAcrixAAbravo");
        step.setDelimiter("A");
        step.setDelimiterCaseSensitive(true);
        step.connectInput(0, testInput);
        step.open(logger);
        step.call();
        List<MungeStepOutput> results = step.getChildren();
        MungeStepOutput output = results.get(0);
        String result = (String) output.getData();
        assertEquals("alpha bravo crix", result);
    }

    public void testCaseInsensitiveDelimiter() throws Exception {
        testInput = new MungeStepOutput<String>("test", String.class);
        testInput.setData("AalphaAAAAcrixAAbravo");
        step.setDelimiter("A");
        step.setDelimiterCaseSensitive(false);
        step.connectInput(0, testInput);
        step.open(logger);
        step.call();
        List<MungeStepOutput> results = step.getChildren();
        MungeStepOutput output = results.get(0);
        String result = (String) output.getData();
        assertEquals("br crix lph vo", result);
    }
    
    public void testResultDelimiter() throws Exception {
        testInput = new MungeStepOutput<String>("test", String.class);
        testInput.setData("AalphaAAAAcrixAAbravo");
        step.setDelimiter("A");
        step.setDelimiterCaseSensitive(true);
        step.setResultDelimiter("\\");
        step.connectInput(0, testInput);
        step.open(logger);
        step.call();
        List<MungeStepOutput> results = step.getChildren();
        MungeStepOutput output = results.get(0);
        String result = (String) output.getData();
        assertEquals("alpha\\bravo\\crix", result);
    }
    
}
