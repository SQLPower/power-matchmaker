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

import junit.framework.TestCase;

import org.apache.log4j.Logger;

public class BooleanToStringMungeStepTest extends TestCase {

	private BooleanToStringMungeStep step;
	
	private final Logger logger = Logger.getLogger("testLogger");
	
	protected void setUp() throws Exception {
		super.setUp();
		step = new BooleanToStringMungeStep();
		step.open(logger);
	}
	
	public void testDefault() throws Exception {
		MungeStepOutput<Boolean> mso = new MungeStepOutput<Boolean>("in",Boolean.class);
		step.connectInput(0, mso);
		String ret;
		
		mso.setData(false);
		step.call();
		ret = (String) step.getOut().getData();
		assertEquals("False", ret);
		
		mso.setData(true);
		step.call();
		ret = (String) step.getOut().getData();
		assertEquals("True", ret);
		
		mso.setData(null);
		step.call();
		ret = (String) step.getOut().getData();
		assertNull(ret);
	}
	
	
	public void testCustom() throws Exception {
		step.setParameter(BooleanToStringMungeStep.FALSE_STRING_PARAMETER_NAME, "F");
		step.setParameter(BooleanToStringMungeStep.TRUE_STRING_PARAMETER_NAME, "T");
		MungeStepOutput<Boolean> mso = new MungeStepOutput<Boolean>("in",Boolean.class);
		step.connectInput(0, mso);
		String ret;
		
		mso.setData(false);
		step.call();
		ret = (String) step.getOut().getData();
		assertEquals("F", ret);
		
		mso.setData(true);
		step.call();
		ret = (String) step.getOut().getData();
		assertEquals("T", ret);
		
		mso.setData(null);
		step.call();
		ret = (String) step.getOut().getData();
		assertNull(ret);
	}
}

