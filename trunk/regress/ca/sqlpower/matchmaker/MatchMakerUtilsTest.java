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

import ca.sqlpower.matchmaker.event.MatchMakerEvent;
import ca.sqlpower.matchmaker.event.MatchMakerListener;
import junit.framework.TestCase;

public class MatchMakerUtilsTest extends TestCase {

	public void testListenToHierarchy() {
		TestingAbstractMatchMakerObject root = new TestingAbstractMatchMakerObject();
		TestingAbstractMatchMakerObject c1 = new TestingAbstractMatchMakerObject();
		TestingAbstractMatchMakerObject c2 = new TestingAbstractMatchMakerObject();
		TestingAbstractMatchMakerObject c1c1 = new TestingAbstractMatchMakerObject();
		TestingAbstractMatchMakerObject c1c2 = new TestingAbstractMatchMakerObject();

		root.addChild(c1);
		root.addChild(c2);
		c1.addChild(c1c1);
		c1.addChild(c1c2);

		MatchMakerListener myListener = new MatchMakerListener<TestingAbstractMatchMakerObject,TestingAbstractMatchMakerObject>() {
			public void mmPropertyChanged(MatchMakerEvent<TestingAbstractMatchMakerObject, TestingAbstractMatchMakerObject> evt) { }
			public void mmChildrenInserted(MatchMakerEvent<TestingAbstractMatchMakerObject, TestingAbstractMatchMakerObject> evt) { }
			public void mmChildrenRemoved(MatchMakerEvent<TestingAbstractMatchMakerObject, TestingAbstractMatchMakerObject> evt) { }
			public void mmStructureChanged(MatchMakerEvent<TestingAbstractMatchMakerObject, TestingAbstractMatchMakerObject> evt) { }
		};
		MatchMakerUtils.listenToHierarchy(myListener, root);

		assertTrue(root.hasListener(myListener));
		assertTrue(c1.hasListener(myListener));
		assertTrue(c2.hasListener(myListener));
		assertTrue(c1c1.hasListener(myListener));
		assertTrue(c1c2.hasListener(myListener));
	}

	public void testUnlistenToHierarchy() {
		TestingAbstractMatchMakerObject root = new TestingAbstractMatchMakerObject();
		TestingAbstractMatchMakerObject c1 = new TestingAbstractMatchMakerObject();
		TestingAbstractMatchMakerObject c2 = new TestingAbstractMatchMakerObject();
		TestingAbstractMatchMakerObject c1c1 = new TestingAbstractMatchMakerObject();
		TestingAbstractMatchMakerObject c1c2 = new TestingAbstractMatchMakerObject();

		root.addChild(c1);
		root.addChild(c2);
		c1.addChild(c1c1);
		c1.addChild(c1c2);

		MatchMakerListener myListener = new MatchMakerListener<TestingAbstractMatchMakerObject,TestingAbstractMatchMakerObject>() {
			public void mmPropertyChanged(MatchMakerEvent<TestingAbstractMatchMakerObject, TestingAbstractMatchMakerObject> evt) { }
			public void mmChildrenInserted(MatchMakerEvent<TestingAbstractMatchMakerObject, TestingAbstractMatchMakerObject> evt) { }
			public void mmChildrenRemoved(MatchMakerEvent<TestingAbstractMatchMakerObject, TestingAbstractMatchMakerObject> evt) { }
			public void mmStructureChanged(MatchMakerEvent<TestingAbstractMatchMakerObject, TestingAbstractMatchMakerObject> evt) { }
		};
		MatchMakerUtils.listenToHierarchy(myListener, root);
		MatchMakerUtils.unlistenToHierarchy(myListener, root);

		assertFalse(root.hasListener(myListener));
		assertFalse(c1.hasListener(myListener));
		assertFalse(c2.hasListener(myListener));
		assertFalse(c1c1.hasListener(myListener));
		assertFalse(c1c2.hasListener(myListener));
	}

}
