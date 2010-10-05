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

import org.apache.log4j.Logger;

public class BooleanConstantMungeStepTest extends AbstractMungeStepTest<BooleanConstantMungeStep> {

	BooleanConstantMungeStep step;
	
	private final Logger logger = Logger.getLogger("testLogger");

	public BooleanConstantMungeStepTest(String name) {
		super(name);
	}
	
    @Override
    protected void setUp() throws Exception {
        step = new BooleanConstantMungeStep();
        super.setUp();
        MungeProcess process = (MungeProcess) createNewValueMaker(
        		getRootObject(), null).makeNewValue(
        				MungeProcess.class, null, "parent process");
        process.addMungeStep(step, process.getMungeSteps().size());
    }
    
    public void testNull() throws Exception {
    	step.setConstant(null);
    	step.open(Logger.getLogger(getClass()));
        step.call();
        assertNull(step.getOut().getData());
    }
    
    public void testTrue() throws Exception {
    	step.setConstant(true);
    	step.open(Logger.getLogger(getClass()));
        step.call();
        MungeStepOutput<Boolean> out = step.getOut();
        assertTrue(out.getData().booleanValue());
    }

    public void testFalse() throws Exception {
    	step.setConstant(false);
    	step.open(Logger.getLogger(getClass()));
        step.call();
        MungeStepOutput<Boolean> out = step.getOut();
        assertFalse(out.getData().booleanValue());
    }

	@Override
	protected BooleanConstantMungeStep getTarget() {
		return step;
	}
   
}
