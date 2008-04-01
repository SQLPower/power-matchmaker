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

public class EmptyStringToNullTest extends TestCase {

    private static final Logger logger = Logger.getLogger("testLogger");

    /**
     * The step instance under test. Gets created in {@link #setUp()}.
     */
    private EmptyStringToNullMungeStep step;

    /**
     * A dummy input that provides values to the step under test.
     */
    private MungeStepOutput testInput;


    protected void setUp() throws Exception {
        super.setUp();
        step = new EmptyStringToNullMungeStep();
    }

    public void testCallOnNonEmptyString() throws Exception {
        testInput = new MungeStepOutput<String>("test", String.class);
        testInput.setData("cows");
        step.connectInput(0, testInput);
        step.open(logger);
        step.call();
        List<MungeStepOutput> results = step.getChildren(); 
        MungeStepOutput output = results.get(0);
        String result = (String) output.getData();
        assertEquals("cows", result);
    }

    public void testCallOnEmptyString() throws Exception {
        testInput = new MungeStepOutput<String>("test", String.class);
        testInput.setData("");
        step.connectInput(0, testInput);
        step.open(logger);
        step.call();
        List<MungeStepOutput> results = step.getChildren(); 
        MungeStepOutput output = results.get(0);
        String result = (String)output.getData();
        assertNull(result);
    }

    public void testCallOnAllWhitespaceString() throws Exception {
        testInput = new MungeStepOutput<String>("test", String.class);
        testInput.setData("   ");
        step.connectInput(0, testInput);
        step.open(logger);
        step.call();
        List<MungeStepOutput> results = step.getChildren(); 
        MungeStepOutput output = results.get(0);
        String result = (String) output.getData();
        assertEquals("   ", result);
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
}
