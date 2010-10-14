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

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerTestCase;
import ca.sqlpower.matchmaker.TestingMatchMakerSession;
import ca.sqlpower.object.SPObject;

public class NumberToStringMungeStepTest extends MatchMakerTestCase<NumberToStringMungeStep> {

	NumberToStringMungeStep step;
	
	public NumberToStringMungeStepTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		step = new NumberToStringMungeStep();
		step.setSession(new TestingMatchMakerSession());
		step.open(Logger.getLogger(NumberToStringMungeStepTest.class));
		super.setUp();
		MungeProcess process = (MungeProcess) createNewValueMaker(
        		getRootObject(), null).makeNewValue(
        				MungeProcess.class, null, "parent process");
        process.addMungeStep(step, process.getMungeSteps().size());
	}
	
	public void testNull() throws Exception{
		MungeStepOutput<BigDecimal> mso = new MungeStepOutput<BigDecimal>("test", BigDecimal.class);
		mso.setData(null);
		step.connectInput(0, mso);
		step.call();
		MungeStepOutput out = step.getChildren(MungeStepOutput.class).get(0);
		assertNull(out.getData());
	}
	
	public void test0() throws Exception{
		MungeStepOutput<BigDecimal> mso = new MungeStepOutput<BigDecimal>("test", BigDecimal.class);
		mso.setData(new BigDecimal(0));
		step.connectInput(0, mso);
		step.call();
		MungeStepOutput out = step.getChildren(MungeStepOutput.class).get(0);
		assertEquals("0", out.getData());
	}
	
	public void test1() throws Exception{
		MungeStepOutput<BigDecimal> mso = new MungeStepOutput<BigDecimal>("test", BigDecimal.class);
		mso.setData(new BigDecimal(1));
		step.connectInput(0, mso);
		step.call();
		MungeStepOutput out = step.getChildren(MungeStepOutput.class).get(0);
		assertEquals("1", out.getData());
	}
	
	public void testNeg1() throws Exception{
		MungeStepOutput<BigDecimal> mso = new MungeStepOutput<BigDecimal>("test", BigDecimal.class);
		mso.setData(new BigDecimal(-1));
		step.connectInput(0, mso);
		step.call();
		MungeStepOutput out = step.getChildren(MungeStepOutput.class).get(0);
		assertEquals("-1", out.getData());
	}
	
	public void testNeg1234567890() throws Exception{
		MungeStepOutput<BigDecimal> mso = new MungeStepOutput<BigDecimal>("test", BigDecimal.class);
		mso.setData(new BigDecimal(-1234567890));
		step.connectInput(0, mso);
		step.call();
		MungeStepOutput out = step.getChildren(MungeStepOutput.class).get(0);
		assertEquals("-1234567890", out.getData());
	}

	@Override
	protected NumberToStringMungeStep getTarget() {
		return step;
	}

	@Override
	protected Class<? extends SPObject> getChildClassType() {
		return MungeStepOutput.class;
	}
	
	@Override
	public void testAllowedChildTypesField() throws Exception {
		// Do nothing
	}
	
	@Override
	public void testDuplicate() throws Exception {
		// Do nothing
	}
}
