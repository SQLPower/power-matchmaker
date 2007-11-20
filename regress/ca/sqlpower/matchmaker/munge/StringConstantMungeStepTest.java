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

import org.apache.log4j.Logger;

public class StringConstantMungeStepTest extends AbstractMungeStepTest {

    StringConstantMungeStep step;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        step = new StringConstantMungeStep();
    }
    
    public void testNullValue() throws Exception {
        step.setValue(null);
        step.open(Logger.getLogger(getClass()));
        step.call();
        assertNull(step.getOut().getData());
    }

    public void testNonNullValue() throws Exception {
        step.setValue("moocow");
        step.open(Logger.getLogger(getClass()));
        step.call();
        assertEquals("moocow", step.getOut().getData());
    }

    /**
     * Ensures the step doesn't update its output value at runtime until after the call().
     */
    public void testChangeValueDuringRun() throws Exception {
        step.setValue("moocow");
        step.open(Logger.getLogger(getClass()));
        step.call();
        assertEquals("moocow", step.getOut().getData());
        step.setValue("woofdog");
        step.call();
        assertEquals("woofdog", step.getOut().getData());
        step.setValue("meowcat");
        assertEquals("woofdog", step.getOut().getData());
        step.call();
        assertEquals("meowcat", step.getOut().getData());
    }
    
    public void testReturnNull() throws Exception {
    	step.setReturningNull(true);
    	step.setValue("moocow");
        step.open(Logger.getLogger(getClass()));
        step.call();
        assertNull(step.getOut().getData());
    	step.setReturningNull(false);
        step.setValue("moocow2");
        step.call();
        assertEquals("moocow2", step.getOut().getData());
    }
   
}
