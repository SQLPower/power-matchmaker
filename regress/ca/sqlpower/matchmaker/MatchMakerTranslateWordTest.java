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

public class MatchMakerTranslateWordTest extends MatchMakerTestCase<MatchMakerTranslateWord> {

	public MatchMakerTranslateWordTest(String name) {
		super(name);
	}

	final String appUserName = "test_user";
	MatchMakerTranslateGroup parent;
	MatchMakerTranslateWord target;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		target = new MatchMakerTranslateWord();
		MatchMakerSession session = new TestingMatchMakerSession();
		((TestingMatchMakerSession)session).setAppUser(appUserName);
		parent = new MatchMakerTranslateGroup();
		target.setSession(session);
		parent.addChild(target);
		getRootObject().addChild(parent, 0);
		
		// Ignoring because the getName() has been changed to enable
		// naming the node on the tree.
		propertiesThatDifferOnSetAndGet.add("name");
	}

	@Override
	protected MatchMakerTranslateWord getTarget() {
		return target;
	}
	private void checkNull() {
		assertNull("The default last_update_user in match object should be null",
				target.getLastUpdateAppUser());
	}

	private void checkAppUserName() {
		assertEquals("The last_update_user should be [" +
				appUserName +"], because user1 has changed this match object",
				appUserName, target.getLastUpdateAppUser());
	}

    public void testAssertDoesNotAllowChildren(){
        assertFalse(target.allowsChildren());
    }

	public void testAddChild() {
		try {
			target.addChild(new TestingAbstractMatchMakerObject());
			fail("Translate word does not allow child!");
		} catch ( IllegalArgumentException e ) {
			// what we excepted
		}
	}

	@Override
	protected Class<? extends SPObject> getChildClassType() {
		return null;
	}

}
