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

import java.math.BigDecimal;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

public class StringToNumberMungeStepTest extends TestCase {
	MungeStep step;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		step = new StringToNumberMungeStep();
		step.setParameter(StringToNumberMungeStep.CONTINUE_ON_MALFORMED_NUMBER, "False");
		step.open(Logger.getLogger(StringToNumberMungeStepTest.class));
	}
	
	public void test0() throws Exception{
		MungeStepOutput<String> mso = new MungeStepOutput<String>("test", String.class);
		mso.setData("0");
		step.connectInput(0, mso);
		step.call();
		MungeStepOutput out = step.getChildren(MungeStepOutput.class).get(0);
		assertEquals(new BigDecimal(0), (BigDecimal)out.getData());
	}
	
	public void test1() throws Exception{
		MungeStepOutput<String> mso = new MungeStepOutput<String>("test", String.class);
		mso.setData("1");
		step.connectInput(0, mso);
		step.call();
		MungeStepOutput out = step.getChildren(MungeStepOutput.class).get(0);
		assertEquals(new BigDecimal(1), (BigDecimal)out.getData());
	}
	
	public void testNeg1() throws Exception{
		MungeStepOutput<String> mso = new MungeStepOutput<String>("test", String.class);
		mso.setData("-1");
		step.connectInput(0, mso);
		step.call();
		MungeStepOutput out = step.getChildren(MungeStepOutput.class).get(0);
		assertEquals(new BigDecimal(-1), (BigDecimal)out.getData());
	}
	
	public void testNull() throws Exception {
		MungeStepOutput<String> mso = new MungeStepOutput<String>("test", String.class);
		mso.setData(null);
		step.connectInput(0, mso);
		step.call();
		MungeStepOutput out = step.getChildren(MungeStepOutput.class).get(0);
		assertNull(out.getData());
	}
	
	public void testExceptionThrown() throws Exception{
		MungeStepOutput<String> mso = new MungeStepOutput<String>("test", String.class);
		mso.setData("One Billion Dollars!!!!!");
		step.connectInput(0, mso);
		try {
			step.call();
			fail("One Billion Dollars!!!!! is not a number and this should have thrown an exception!");
		} catch (NumberFormatException e) {
			//yay
		}
	}
	
	public void testContinueOnError() throws Exception {
		step.setParameter(StringToNumberMungeStep.CONTINUE_ON_MALFORMED_NUMBER, "true");		
		MungeStepOutput<String> mso = new MungeStepOutput<String>("test", String.class);
		mso.setData("One Billion Dollars!!!!!");
		step.connectInput(0, mso);
		step.call();
		MungeStepOutput out = step.getChildren(MungeStepOutput.class).get(0);
		assertEquals(null, (BigDecimal)out.getData());
	}
	
	
}
