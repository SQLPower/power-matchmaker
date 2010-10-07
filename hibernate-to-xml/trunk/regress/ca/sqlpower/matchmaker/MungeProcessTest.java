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
import ca.sqlpower.matchmaker.munge.DeDupeResultStep;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.munge.MungeResultStep;


public class MungeProcessTest extends MatchMakerTestCase<MungeProcess> {

	MungeProcess target;
	final String appUserName = "test user";

    public MungeProcessTest() {
        super();
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
	}

	@Override
	protected MungeProcess getTarget() {
		return target;
	}


    public void testSetParentProject(){
        Project project = new Project();
        MatchMakerEventCounter listener = new MatchMakerEventCounter();
        MungeProcess process = new MungeProcess();

        process.addMatchMakerListener(listener);
        process.setParentProject(project);
        assertEquals("Incorrect number of events fired",1,listener.getAllEventCounts());
        assertEquals("Wrong property fired in the event","parent",listener.getLastEvt().getPropertyName());
    }

}
