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

import junit.framework.TestCase;

public class TrimSpacesMungeStepTest extends TestCase {
	MungeStep step;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		step = new TrimSpacesMungeStep();
		step.open(Logger.getLogger(TrimSpacesMungeStepTest.class));
	}
	
	public void testEmptyString() throws Exception{
		MungeStepOutput<String> mso = new MungeStepOutput<String>("test", String.class);
		mso.setData("");
		step.connectInput(0, mso);
		step.call();
		MungeStepOutput out = step.getOutputByName("trimSpacesOutput");
		assertEquals("", out.getData());
	}
	
	public void testNoSpaceString() throws Exception{
		MungeStepOutput<String> mso = new MungeStepOutput<String>("test", String.class);
		mso.setData("abcde");
		step.connectInput(0, mso);
		step.call();
		MungeStepOutput out = step.getOutputByName("trimSpacesOutput");
		assertEquals("abcde", out.getData());
	}
	
	public void testLostsOfSurroundingWhiteSpaceString() throws Exception{
		MungeStepOutput<String> mso = new MungeStepOutput<String>("test", String.class);
		mso.setData("   abcde  \t");
		step.connectInput(0, mso);
		step.call();
		MungeStepOutput out = step.getOutputByName("trimSpacesOutput");
		assertEquals("abcde", out.getData());
	}
	
	public void testLostsOfWhiteSpaceString() throws Exception{
		MungeStepOutput<String> mso = new MungeStepOutput<String>("test", String.class);
		mso.setData("   ab c\tde  \t");
		step.connectInput(0, mso);
		step.call();
		MungeStepOutput out = step.getOutputByName("trimSpacesOutput");
		assertEquals("ab c\tde", out.getData());
	}
	
}
