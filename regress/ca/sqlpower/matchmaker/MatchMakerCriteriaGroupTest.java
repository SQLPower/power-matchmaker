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


package ca.sqlpower.matchmaker;

import ca.sqlpower.matchmaker.event.MatchMakerEventCounter;



public class MatchMakerCriteriaGroupTest extends MatchMakerTestCase<MatchMakerCriteriaGroup> {

	MatchMakerCriteriaGroup target;
	final String appUserName = "test user";

    public MatchMakerCriteriaGroupTest() {
        super();
        propertiesToIgnoreForEventGeneration.add("parentMatch");
        propertiesThatDifferOnSetAndGet.add("parent");
    }
	protected void setUp() throws Exception {
		super.setUp();
		target = new MatchMakerCriteriaGroup();
		MatchMakerSession session = new TestingMatchMakerSession();
		((TestingMatchMakerSession)session).setAppUser(appUserName);
		target.setSession(session);
	}

	@Override
	protected MatchMakerCriteriaGroup getTarget() {
		return target;
	}


    public void testSetParentMatch(){
        Match match = new Match();
        MatchMakerEventCounter listener = new MatchMakerEventCounter();
        MatchMakerCriteriaGroup group = new MatchMakerCriteriaGroup();

        group.addMatchMakerListener(listener);
        group.setParentMatch(match);
        assertEquals("Incorrect number of events fired",1,listener.getAllEventCounts());
        assertEquals("Wrong property fired in the event","parent",listener.getLastEvt().getPropertyName());
    }

}
