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

import java.util.Collections;
import java.util.List;

import ca.sqlpower.object.SPObject;


public class MatchMakerSettingsTest extends MatchMakerTestCase {

	public MatchMakerSettingsTest(String name) {
		super(name);
	}

	String appUserName = "User Name";
	MatchMakerSettings mms;

	protected void setUp() throws Exception {
		super.setUp();
		mms = new MatchMakerSettings() {

			public MatchMakerSettings duplicate(MatchMakerObject parent) {
				return null;
			}

			@Override
			public List<? extends SPObject> getChildren() {
				return Collections.emptyList();
			}

			@Override
			public List<Class<? extends SPObject>> getAllowedChildTypes() {
				return Collections.emptyList();
			}};
		MatchMakerSession session = new TestingMatchMakerSession();
		((TestingMatchMakerSession)session).setAppUser(appUserName);
		mms.setSession(session);

		getRootObject().addChild(mms,0);
	}

	@Override
	protected MatchMakerObject getTarget() {
		return mms;
	}

	@Override
	public void testDuplicate() throws Exception {
		// this class is not duplicated only its subclasses are.
	}

	@Override
	protected Class<? extends SPObject> getChildClassType() {
		return null;
	}

	@Override
	public SPObject getSPObjectUnderTest() {
		return getTarget();
	}
}
