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

import ca.sqlpower.matchmaker.util.MatchMakerNewValueMaker;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sql.DataSourceCollection;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.testutil.NewValueMaker;


public class PlFolderTest extends MatchMakerTestCase<PlFolder> {

	private PlFolder plFolder;
	final String appUserName = "THE_USER";
	
	public PlFolderTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		plFolder = new PlFolder("Test Folder");
		MatchMakerSession session = new TestingMatchMakerSession();
		((TestingMatchMakerSession)session).setAppUser(appUserName);
		plFolder.setSession(session);
	}

	@Override
	protected PlFolder getTarget() {
		return plFolder;
	}

	@Override
	protected Class<? extends SPObject> getChildClassType() {
		return Project.class;
	}

	@Override
	public SPObject getSPObjectUnderTest() {
		return getTarget();
	}
	
	@Override
	public NewValueMaker createNewValueMaker(SPObject root, DataSourceCollection<SPDataSource> dsCollection) {
		return new MatchMakerNewValueMaker(root, dsCollection);
	}
}
