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


package ca.sqlpower.matchmaker.swingui;

import javax.swing.event.TreeModelEvent;

import junit.framework.TestCase;
import ca.sqlpower.matchmaker.MMRootNode;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.TestingMatchMakerSession;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.object.ObjectDependentException;
import ca.sqlpower.swingui.FolderNode;

public class MatchMakerTreeModelTest extends TestCase {

	MatchMakerTreeEventCounter counter;
	MatchMakerTreeModel treeModel;
	private MatchMakerObject currentFoldersNode;
	private final PlFolder folder = new PlFolder("Test Folder");
	private final Project mmo = new Project();

	protected void setUp() throws Exception {
		super.setUp();
		counter = new MatchMakerTreeEventCounter();
		MatchMakerSession session = new TestingMatchMakerSession();
		MMRootNode rootNode = new MMRootNode(session);
		treeModel = new MatchMakerTreeModel(rootNode, session);
		currentFoldersNode = (MatchMakerObject) treeModel.getChild(treeModel.getRoot(), 0);
		currentFoldersNode.addChild(folder, 0);
	}

	/**
	 * test insert children event
	 *
	 */
	public void testTreeNodeInsertEvent() {

		treeModel.addTreeModelListener(counter);
		folder.addChild(mmo);
		assertEquals("insert event count should be 2 because we are adding the project and FolderNodes",
				2, counter.getChildrenInsertedCount());
		assertEquals("total event count should be 2", 2, counter.getAllEventCounts());
		assertEquals("Last event source should be folder",  mmo, counter.getLastEvt().getSource());
		TreeModelEvent evt = counter.getLastEvt();
		assertEquals(2, evt.getChildIndices().length);
		assertEquals(2, evt.getChildren().length);
		assertEquals(folder.getChildren().indexOf(mmo), evt.getChildIndices()[0]);
		assertSame(mmo, ((FolderNode)evt.getChildren()[0]).getParent());
	}

	/**
	 * all tree node on the tree should listen to the tree event,
	 * this test is for the new children just added to the tree,
	 * they should listen to the tree too.
	 * mmo is the new child, and it should have the tree listener automaticly
	 */
	public void testTreeNodeInsertToChildEvent() {

		folder.addChild(mmo);
		treeModel.addTreeModelListener(counter);
		mmo.addChild(new MungeProcess());

		assertEquals("insert event count should be 1",
				1, counter.getChildrenInsertedCount());
		assertEquals("total event count should be 1",
				1, counter.getAllEventCounts());
		assertEquals("Parent of last event source should be project",
				mmo,((FolderNode)counter.getLastEvt().getSource()).getParent());
	}

	/**
	 * test remove children event
	 *
	 */
	public void testTreeNodeRemoveEvent() {
		
		mmo.setName("Test Project");
		
		folder.addChild(mmo);
		treeModel.addTreeModelListener(counter);

		try {
			folder.removeChild(mmo);
		} catch (ObjectDependentException e) {
			fail("You should not get an error here");
		}
		
		assertEquals("remove event count should be 1",
				1, counter.getChildrenRemovedCount());
		assertEquals("total event count should be 1",
				1, counter.getAllEventCounts());
		
		
		assertEquals("Last event source should be folder",
				folder,counter.getLastEvt().getSource());

		TreeModelEvent evt = counter.getLastEvt();
		assertEquals(1, evt.getChildIndices().length);
		assertEquals(1, evt.getChildren().length);
		assertEquals(0, evt.getChildIndices()[0]);
		assertSame(mmo, (evt.getChildren()[0]));
	}

	/**
	 * remove children on tree should also remove the listener on the children
	 * so this is for that.
	 */
	public void testTreeNodeRemoveChildEvent() {

		folder.addChild(mmo);
		try {
			folder.removeChild(mmo);
		}catch (ObjectDependentException e) {
			throw new RuntimeException(e);
		}
		treeModel.addTreeModelListener(counter);

		mmo.setName("newName");
		assertEquals("remove event count should be 0",
				0, counter.getChildrenRemovedCount());
		assertEquals("total event count should be 0",
				0, counter.getAllEventCounts());
		assertEquals("Last event source should be mmo",
				null,counter.getLastEvt());
	}

	public void testTreeNodeChangeEvent() {

		folder.addChild(mmo);
		treeModel.addTreeModelListener(counter);
		mmo.setName("newName");
		assertEquals("property change event count should be 1",
				1, counter.getPropertyChangedCount());
		assertEquals("total event count should be 1",
				1, counter.getAllEventCounts());
		assertEquals("Last event source should be mmo",
				mmo,counter.getLastEvt().getSource());
	}
}
