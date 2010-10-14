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


package ca.sqlpower.matchmaker;

import ca.sqlpower.matchmaker.event.MatchMakerEventCounter;
import ca.sqlpower.matchmaker.munge.BooleanConstantMungeStep;
import ca.sqlpower.matchmaker.munge.DeDupeResultStep;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.munge.MungeResultStep;
import ca.sqlpower.matchmaker.munge.SQLInputStep;
import ca.sqlpower.object.SPObject;


public class MungeProcessTest extends MatchMakerTestCase<MungeProcess> {

	MungeProcess target;
	final String appUserName = "test user";

    public MungeProcessTest(String name	) {
        super(name);
        propertiesToIgnoreForEventGeneration.add("parentProject");
        propertiesThatDifferOnSetAndGet.add("parent");
    }
	protected void setUp() throws Exception {
		super.setUp();
		target = new MungeProcess();
		MungeResultStep resultStep = new DeDupeResultStep();
		target.addChild(resultStep);
		MatchMakerSession session = new TestingMatchMakerSession();
		((TestingMatchMakerSession)session).setAppUser(appUserName);
		target.setSession(session);
		Project parent = (Project) createNewValueMaker(
				getRootObject(), null).makeNewValue(
						Project.class, null, "Parent project");
		parent.addMungeProcess(target, 0);
	}

	@Override
	protected MungeProcess getTarget() {
		return target;
	}


    public void testSetParentProject(){
        Project project = new Project();
        MatchMakerEventCounter listener = new MatchMakerEventCounter();
        MungeProcess process = new MungeProcess();

        process.addSPListener(listener);
        process.setParent(project);
        assertEquals("Incorrect number of events fired",1,listener.getAllEventCounts());
        assertEquals("Wrong property fired in the event","parent",listener.getLastPropertyChangeEvent().getPropertyName());
    }
	@Override
	protected Class<? extends SPObject> getChildClassType() {
		return SQLInputStep.class;
	}

	/**
	 * Tests that input steps can only be added at the start of the munge
	 * process list. Any additions later in the list will throw an exception as
	 * they would be considered a step in process.
	 */
	public void testInsertSQLInputStepAtStart() throws Exception {
		MungeProcess process = new MungeProcess();
		SQLInputStep input1 = new SQLInputStep();
		try {
			process.addMungeStep(input1, 0);
		} catch (Exception e) {
			fail("The process failed to allow an input step to be correctly added at the start");
		}
		
		BooleanConstantMungeStep step = new BooleanConstantMungeStep();
		process.addMungeStep(step, 1);
		
		SQLInputStep input2 = new SQLInputStep();
		try {
			process.addMungeStep(input2, 1);
		} catch (Exception e) {
			fail("The process failed to allow an input step to be correctly added before other steps.");
		}
		
		SQLInputStep input3 = new SQLInputStep();
		try {
			process.addMungeStep(input3, 3);
			fail("We should not be allowed to add a munge step after the third position.");
		} catch (Exception e) {
			//we pass at this point because an exception was thrown
		}
	}

	/**
	 * Tests that steps can only be added after the input steps if the step is
	 * not itself an input step. This ensures the list ordering is as we expect.
	 */
	public void testOtherStepsAfterInputSteps() throws Exception {
		MungeProcess process = new MungeProcess();
		SQLInputStep input1 = new SQLInputStep();
		process.addMungeStep(input1, 0);
		
		BooleanConstantMungeStep step = new BooleanConstantMungeStep();
		try {
			process.addMungeStep(step, 1);
		} catch (Exception e) {
			fail("We should be able to add munge steps after the input steps.");
		}

		BooleanConstantMungeStep step2 = new BooleanConstantMungeStep();
		try {
			process.addMungeStep(step2, 0);
			fail("We should not be able to add munge steps before the input steps.");
		} catch (Exception e) {
			//we pass because we get an exception for an invalid position
		}
		
		BooleanConstantMungeStep step3 = new BooleanConstantMungeStep();
		try {
			process.addMungeStep(step3, 3);
			fail("We should not be able to add munge steps after the result step.");
		} catch (Exception e) {
			//we pass because we get an exception for an invalid position
		}
	}
}
