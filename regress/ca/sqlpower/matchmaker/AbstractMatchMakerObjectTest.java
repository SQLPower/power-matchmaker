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

import junit.framework.TestCase;
import ca.sqlpower.object.CountingSPListener;

public class AbstractMatchMakerObjectTest extends TestCase {

	TestingAbstractMatchMakerObject test;
	final String appUserName = "user1";
	MatchMakerSession session = new TestingMatchMakerSession();

	protected void setUp() throws Exception {
		super.setUp();
		test = new TestingAbstractMatchMakerObject(){};
		((TestingMatchMakerSession)session).setAppUser(appUserName);
		test.setSession(session);
	}

	public void testChildren() {
		MatchMakerObject mmo1 = new TestingAbstractMatchMakerObject(){};
		MatchMakerObject mmo2 = new TestingAbstractMatchMakerObject(){};
		assertEquals("Started out with the wrong number of children",0,test.getChildren().size());
		test.addChild(mmo1);
		assertEquals("faild to add the correct number of children",1,test.getChildren().size());
		test.addChild(mmo2);
		assertEquals("faild to add the correct number of children",2,test.getChildren().size());
		assertEquals("Incorrect child in position 0",mmo1,test.getChildren().get(0));
		assertEquals("Incorrect child in position 1",mmo2,test.getChildren().get(1));
	}

	public void testMatchMakerEventListener() {
		CountingSPListener mml = new CountingSPListener();
		test.addSPListener(mml);
		test.addChild(new TestingAbstractMatchMakerObject(){});
		assertEquals("Did not get any events",1,mml.getChildAddedCount());
		test.removeSPListener(mml);
		test.addChild(new TestingAbstractMatchMakerObject(){});
		assertEquals("Got extra events",1,mml.getChildAddedCount());

	}

	public void testParentSetCorrectly() {
		TestingAbstractMatchMakerObject mmo1 = new TestingAbstractMatchMakerObject(){};
		TestingAbstractMatchMakerObject mmo2 = new TestingAbstractMatchMakerObject(){};
		mmo2.addChild(mmo1);
		assertEquals("mmo2 is not the parent of mmo1",mmo2,mmo1.getParent());
	}
}    
