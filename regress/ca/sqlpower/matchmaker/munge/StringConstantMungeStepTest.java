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

import ca.sqlpower.matchmaker.MatchMakerTestCase;
import ca.sqlpower.object.SPObject;

public class StringConstantMungeStepTest extends MatchMakerTestCase<StringConstantMungeStep> {

    public StringConstantMungeStepTest(String name) {
		super(name);
	}

	StringConstantMungeStep step;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        step = new StringConstantMungeStep();
    }
    
    public void testNullValue() throws Exception {
        step.setOutValue(null);
        step.open(Logger.getLogger(getClass()));
        step.call();
        assertNull(step.getOut().getData());
    }

    public void testNonNullValue() throws Exception {
        step.setOutValue("moocow");
        step.open(Logger.getLogger(getClass()));
        step.call();
        assertEquals("moocow", step.getOut().getData());
    }

    /**
     * Ensures the step doesn't update its output value at runtime until after the call().
     */
    public void testChangeValueDuringRun() throws Exception {
        step.setOutValue("moocow");
        step.open(Logger.getLogger(getClass()));
        step.call();
        assertEquals("moocow", step.getOut().getData());
        step.setOutValue("woofdog");
        step.call();
        assertEquals("woofdog", step.getOut().getData());
        step.setOutValue("meowcat");
        assertEquals("woofdog", step.getOut().getData());
        step.call();
        assertEquals("meowcat", step.getOut().getData());
    }
    
    public void testReturnNull() throws Exception {
    	step.setReturnNull(true);
    	step.setOutValue("moocow");
        step.open(Logger.getLogger(getClass()));
        step.call();
        assertNull(step.getOut().getData());
    	step.setReturnNull(false);
        step.setOutValue("moocow2");
        step.call();
        assertEquals("moocow2", step.getOut().getData());
    }

	@Override
	protected StringConstantMungeStep getTarget() {
		return step;
	}

	@Override
	protected Class<? extends SPObject> getChildClassType() {
		return MungeStepOutput.class;
	}
	
	@Override
	public void testAllowedChildTypesField() throws Exception {
		// no-op
	}
   
}
