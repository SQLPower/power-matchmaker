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

package ca.sqlpower.matchmaker;

import ca.sqlpower.object.SPObject;

public class MMRootNodeTest extends MatchMakerTestCase<MMRootNode> {
	
	MMRootNode rootNode;
	final String appUserName = "test_user";
	
	public MMRootNodeTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		MatchMakerSession session = new TestingMatchMakerSession();
		((TestingMatchMakerSession)session).setAppUser(appUserName);
		rootNode = new MMRootNode(session);
	}
	
	@Override
	protected Class<? extends SPObject> getChildClassType() {
		return null;
	}

	@Override
	protected MMRootNode getTarget() {
		return rootNode;
	}
	
	@Override
	public void testDuplicate() throws Exception {
		// this object does not duplicate
	}
}
