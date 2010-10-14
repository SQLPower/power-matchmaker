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

public class EmptyStringToNullMungeStepTest extends MatchMakerTestCase<EmptyStringToNullMungeStep> {

	private static final Logger logger = Logger.getLogger("testLogger");

    /**
     * The step instance under test. Gets created in {@link #setUp()}.
     */
    private EmptyStringToNullMungeStep step;

    /**
     * A dummy input that provides values to the step under test.
     */
    private MungeStepOutput testInput;

    public EmptyStringToNullMungeStepTest(String name) {
		super(name);
	}

    protected void setUp() throws Exception {
        step = new EmptyStringToNullMungeStep();
        super.setUp();
        MungeProcess process = (MungeProcess) createNewValueMaker(
        		getRootObject(), null).makeNewValue(
        				MungeProcess.class, null, "parent process");
        process.addMungeStep(step, process.getMungeSteps().size());
    }

    public void testCallOnNonEmptyString() throws Exception {
        testInput = new MungeStepOutput<String>("test", String.class);
        testInput.setData("cows");
        step.connectInput(0, testInput);
        step.open(logger);
        step.call();
        List<MungeStepOutput> results = step.getMungeStepOutputs(); 
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
        List<MungeStepOutput> results = step.getMungeStepOutputs(); 
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
        List<MungeStepOutput> results = step.getMungeStepOutputs(); 
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
        List<MungeStepOutput> results = step.getMungeStepOutputs(); 
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

	@Override
	protected EmptyStringToNullMungeStep getTarget() {
		return step;
	}

	@Override
	protected Class<? extends SPObject> getChildClassType() {
		return MungeStepOutput.class;
	}
    
	@Override
	public void testAllowedChildTypesField() throws Exception {
		// Already in AbstractMungeStep
	}
}
