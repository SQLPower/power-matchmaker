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

public class FolderParentTest extends MatchMakerTestCase<FolderParent> {
	
	MMRootNode rootNode;
	FolderParent folderParent;
	final String appUserName = "test_user";
	
	public FolderParentTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		MatchMakerSession session = new TestingMatchMakerSession();
		((TestingMatchMakerSession)session).setAppUser(appUserName);
		rootNode = new MMRootNode(session);
		folderParent = rootNode.getCurrentFolderParent();
		getRootObject().addChild(rootNode, 0);
		rootNode.setSession(new TestingMatchMakerSession());
	}
	
	@Override
	public void testDuplicate() throws Exception {
		// FolderParent does not duplicate
	}
	
	@Override
	protected Class<? extends SPObject> getChildClassType() {
		return PlFolder.class;
	}

	@Override
	protected FolderParent getTarget() {
		return folderParent;
	}
	
	@Override
	public void testPersisterCreatesNewObjects() throws Exception {
		//stubbing out this test as folders are final children of the only
		//object (MMRootNode) that allows folders as children.
	}
}
