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


public class TrimSpacesMungeStepTest extends MatchMakerTestCase<TrimSpacesMungeStep> {
	
	public TrimSpacesMungeStepTest(String name) {
		super(name);
	}

	TrimSpacesMungeStep step;
	
	private final Logger logger = Logger.getLogger("testLogger");
	
	@Override
	protected void setUp() throws Exception {
		step = new TrimSpacesMungeStep();
		super.setUp();
	}
	
	public void testEmptyString() throws Exception{
		MungeStepOutput<String> mso = new MungeStepOutput<String>("test", String.class);
		mso.setData("");
		step.connectInput(0, mso);
		step.open(logger);
		step.call();
		MungeStepOutput out = step.getOutputByName("trimSpacesOutput");
		assertEquals("", out.getData());
	}
	
	public void testNoSpaceString() throws Exception{
		MungeStepOutput<String> mso = new MungeStepOutput<String>("test", String.class);
		mso.setData("abcde");
		step.connectInput(0, mso);
		step.open(logger);
		step.call();
		MungeStepOutput out = step.getOutputByName("trimSpacesOutput");
		assertEquals("abcde", out.getData());
	}
	
	public void testLostsOfSurroundingWhiteSpaceString() throws Exception{
		MungeStepOutput<String> mso = new MungeStepOutput<String>("test", String.class);
		mso.setData("   abcde  \t");
		step.connectInput(0, mso);
		step.open(logger);
		step.call();
		MungeStepOutput out = step.getOutputByName("trimSpacesOutput");
		assertEquals("abcde", out.getData());
	}
	
	public void testLostsOfWhiteSpaceString() throws Exception{
		MungeStepOutput<String> mso = new MungeStepOutput<String>("test", String.class);
		mso.setData("   ab c\tde  \t");
		step.connectInput(0, mso);
		step.open(logger);
		step.call();
		MungeStepOutput out = step.getOutputByName("trimSpacesOutput");
		assertEquals("ab c\tde", out.getData());
	}

	@Override
	protected TrimSpacesMungeStep getTarget() {
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
