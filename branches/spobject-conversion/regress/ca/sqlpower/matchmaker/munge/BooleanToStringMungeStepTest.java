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

public class BooleanToStringMungeStepTest extends AbstractMungeStepTest<BooleanToStringMungeStep> {

	private BooleanToStringMungeStep step;
	
	public BooleanToStringMungeStepTest(String name) {
		super(name);
	}
	
	private final Logger logger = Logger.getLogger("testLogger");
	
	protected void setUp() throws Exception {
		step = new BooleanToStringMungeStep();
		MungeStepOutput mso = new MungeStepOutput("in",Boolean.class);
		mungeStep.connectInput(0, mso);
		super.setUp();
	}
	
	public void testDefault() throws Exception {
		step.open(logger);
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
		step.open(logger);
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

	@Override
	protected BooleanToStringMungeStep getTarget() {
		return step;
	}
}

