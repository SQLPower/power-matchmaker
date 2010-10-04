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

import ca.sqlpower.object.SPObject;

public class MatchMakerTranslateGroupTest extends MatchMakerTestCase<MatchMakerTranslateGroup> {

	public MatchMakerTranslateGroupTest(String name) {
		super(name);
	}

	final String appUserName = "test_user";
	MatchMakerTranslateGroup target;
	protected void setUp() throws Exception {
		super.setUp();
		target = new MatchMakerTranslateGroup();
		MatchMakerSession session = new TestingMatchMakerSession();
		((TestingMatchMakerSession)session).setAppUser(appUserName);
		TranslateGroupParent parent = new TranslateGroupParent();
		parent.addChild(target);
		parent.setSession(session);
		getRootObject().addChild(parent, 0);
	}

	@Override
	protected MatchMakerTranslateGroup getTarget() {
		return target;
	}

	@Override
	protected Class<? extends SPObject> getChildClassType() {
		return MatchMakerTranslateWord.class;
	}
}
