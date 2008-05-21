/*
 * Copyright (c) 2008, SQL Power Group Inc.
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


public class MatchMakerSettingsTest extends MatchMakerTestCase {

	String appUserName = "User Name";
	MatchMakerSettings mms;

	protected void setUp() throws Exception {
		super.setUp();
		mms = new MatchMakerSettings() {

			public MatchMakerSettings duplicate(MatchMakerObject parent, MatchMakerSession s) {
				return null;
			}};
		MatchMakerSession session = new TestingMatchMakerSession();
		((TestingMatchMakerSession)session).setAppUser(appUserName);
		mms.setSession(session);


	}

	@Override
	protected MatchMakerObject<MatchMakerSettings, MatchMakerObject> getTarget() {
		return mms;
	}

	@Override
	public void testDuplicate() throws Exception {
		// this class is not duplicated only its subclasses are.
	}
}
