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

import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.TestingMatchMakerSession;

public class ConcatMungeStepTest extends AbstractMungeStepTest<ConcatMungeStep> {

	MatchMakerSession session;
	private ConcatMungeStep step;

	public ConcatMungeStepTest(String name) {
		super(name);
	}
	
	private MungeStepOutput testInput;
	
	private final Logger logger = Logger.getLogger("testLogger");
	
	protected void setUp() throws Exception {
		step = new ConcatMungeStep();
		session = new TestingMatchMakerSession();
		step.setSession(session);
		super.setUp();
		MungeProcess process = (MungeProcess) createNewValueMaker(
        		getRootObject(), null).makeNewValue(
        				MungeProcess.class, null, "parent process");
        process.addMungeStep(step, process.getMungeSteps().size());
	}

	public void testCallConcatTwoStrings() throws Exception {
		testInput = new MungeStepOutput<String>("test1", String.class);
		testInput.setData("abc");
		step.connectInput(0, testInput);
		
		testInput = new MungeStepOutput<String>("test2", String.class);
		testInput.setData("123");
		step.connectInput(1, testInput);
		
		step.open(logger);
		step.call();
		List<MungeStepOutput> results = step.getMungeStepOutputs(); 
		
		MungeStepOutput output = results.get(0);
		String result = (String)output.getData();
		assertEquals("abc123", result);
	}

	
	public void testCallOneStringAndOneNull() throws Exception {
		testInput = new MungeStepOutput<String>("test1", String.class);
		testInput.setData("abc");
		step.connectInput(0, testInput);
		
		testInput = new MungeStepOutput<String>("test2", String.class);
		testInput.setData(null);
		step.connectInput(1, testInput);
		
		step.open(logger);
		step.call();
		List<MungeStepOutput> results = step.getMungeStepOutputs(); 
		
		MungeStepOutput output = results.get(0);
		String result = (String)output.getData();
		assertEquals("abc", result);
	}
	
	public void testCallOnNull() throws Exception {
		testInput = new MungeStepOutput<String>("test", String.class);
		testInput.setData(null);
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
    
    public void testDelimiterTwoInputs() throws Exception {
        
        step.setDelimiter("!");
        
        testInput = new MungeStepOutput<String>("test1", String.class);
        testInput.setData("abc");
        step.connectInput(0, testInput);

        MungeStepOutput<String> testInput2;
        testInput2 = new MungeStepOutput<String>("test2", String.class);
        testInput2.setData("def");
        step.connectInput(1, testInput2);

        step.open(logger);
        step.call();
        List<MungeStepOutput> results = step.getMungeStepOutputs(); 
        MungeStepOutput output = results.get(0);
        String result = (String) output.getData();
        assertEquals("abc!def", result);
    }

    public void testDelimiterTwoNullInputs() throws Exception {
        
        step.setDelimiter("!");
        
        testInput = new MungeStepOutput<String>("test1", String.class);
        testInput.setData(null);
        step.connectInput(0, testInput);

        MungeStepOutput<String> testInput2;
        testInput2 = new MungeStepOutput<String>("test2", String.class);
        testInput2.setData(null);
        step.connectInput(1, testInput2);

        step.open(logger);
        step.call();
        List<MungeStepOutput> results = step.getMungeStepOutputs(); 
        MungeStepOutput output = results.get(0);
        String result = (String) output.getData();
        assertEquals("!", result);
    }
    
    public void testDelimiterSecondInputDisconnected() throws Exception {
        
        step.setDelimiter("!");
        
        testInput = new MungeStepOutput<String>("test", String.class);
        testInput.setData("cow");
        step.connectInput(0, testInput);
        step.open(logger);
        step.call();
        List<MungeStepOutput> results = step.getMungeStepOutputs(); 
        MungeStepOutput output = results.get(0);
        String result = (String)output.getData();
        assertEquals("cow", result);
    }

    public void testDelimiterOneInput() throws Exception {
        
        step.setDelimiter("!");
        
        testInput = new MungeStepOutput<String>("test", String.class);
        testInput.setData("cow");
        step.connectInput(0, testInput);
        step.removeInput(1);
        step.open(logger);
        step.call();
        List<MungeStepOutput> results = step.getMungeStepOutputs(); 
        MungeStepOutput output = results.get(0);
        String result = (String)output.getData();
        assertEquals("cow", result);
    }

    public void testDelimiterNoInputs() throws Exception {
        
        step.setDelimiter("!");
        
        step.removeInput(0);
        step.removeInput(0);
        step.open(logger);
        step.call();
        List<MungeStepOutput> results = step.getMungeStepOutputs(); 
        MungeStepOutput output = results.get(0);
        String result = (String)output.getData();
        assertEquals("", result);
    }

    public void testNoInputs() throws Exception {
        step.removeInput(0);
        step.removeInput(0);
        step.open(logger);
        step.call();
        List<MungeStepOutput> results = step.getMungeStepOutputs(); 
        MungeStepOutput output = results.get(0);
        String result = (String)output.getData();
        assertEquals(null, result);
    }

    public void testConnectInput() {
    	testInput = new MungeStepOutput<String>("test1", String.class);
        step.connectInput(0, testInput);
        testInput = new MungeStepOutput<String>("test2", String.class);
        step.connectInput(1, testInput);
        
        assertEquals(3, step.getMSOInputs().size());
        assertEquals(String.class, step.getInputDescriptor(2).getType());
    }

	@Override
	protected ConcatMungeStep getTarget() {
		return step;
	}
	
	@Override
	public void testHasConnectedInputs() {
		// Do nothing
	}
}
