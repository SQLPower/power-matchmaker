/*
 * Copyright (c) 2010, SQL Power Group Inc.
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

public class MD5CheckSumMungeStepTest extends AbstractMungeStepTest<MD5CheckSumMungeStep>{

	MD5CheckSumMungeStep step;
	
	public MD5CheckSumMungeStepTest(String name) {
		super(name);
	}
	
	@Override
	protected void setUp() throws Exception {
		step = new MD5CheckSumMungeStep();
		super.setUp();
		MungeProcess process = (MungeProcess) createNewValueMaker(
        		getRootObject(), null).makeNewValue(
        				MungeProcess.class, null, "parent process");
        process.addMungeStep(step, process.getMungeSteps().size());
	}

	@Override
	protected MD5CheckSumMungeStep getTarget() {
		return step;
	}
	
	@Override
	public void testCallAfterCommit() throws Exception {
		// do nothing
	}
	
	@Override
	public void testCallAfterRollback() throws Exception {
		// do nothing
	}
}
