package ca.sqlpower.matchmaker.swingui;

import java.util.Arrays;

import javax.swing.event.TreeModelEvent;

import junit.framework.TestCase;
import ca.sqlpower.matchmaker.FolderParent;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.TestingAbstractMatchMakerObject;
import ca.sqlpower.matchmaker.TestingMatchMakerSession;

public class MatchMakerTreeModelTest extends TestCase {

	MatchMakerTreeEventCounter counter;
	MatchMakerTreeModel treeModel;
	private MatchMakerObject<MatchMakerObject, PlFolder> currentFoldersNode;
	private final PlFolder<TestingAbstractMatchMakerObject> folder =
		new PlFolder<TestingAbstractMatchMakerObject>("Test Folder");
	private final TestingAbstractMatchMakerObject mmo =
		new TestingAbstractMatchMakerObject();

	protected void setUp() throws Exception {
		super.setUp();
		counter = new MatchMakerTreeEventCounter();
		FolderParent current = new FolderParent(new TestingMatchMakerSession());
		FolderParent backup = new FolderParent(new TestingMatchMakerSession());
		treeModel = new MatchMakerTreeModel(current,backup);
		currentFoldersNode = (MatchMakerObject<MatchMakerObject, PlFolder>) treeModel.getChild(treeModel.getRoot(), 0);
		currentFoldersNode.addChild(folder);
	}

	/**
	 * test insert children event
	 *
	 */
	public void testTreeNodeInsertEvent() {

		treeModel.addTreeModelListener(counter);
		folder.addChild(mmo);
		assertEquals("insert event count should be 1",
				1, counter.getChildrenInsertedCount());
		assertEquals("total event count should be 1",
				1, counter.getAllEventCounts());
		assertEquals("Last event source should be folder",
				folder,counter.getLastEvt().getSource());
		TreeModelEvent evt = counter.getLastEvt();
		assertEquals(1, evt.getChildIndices().length);
		assertEquals(1, evt.getChildren().length);
		assertEquals(folder.getChildren().indexOf(mmo), evt.getChildIndices()[0]);
		assertSame(mmo, evt.getChildren()[0]);
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
		mmo.addChild(new Match());

		assertEquals("insert event count should be 1",
				1, counter.getChildrenInsertedCount());
		assertEquals("total event count should be 1",
				1, counter.getAllEventCounts());
		assertEquals("Last event source should be folder",
				mmo,counter.getLastEvt().getSource());
	}

	/**
	 * test remove children event
	 *
	 */
	public void testTreeNodeRemoveEvent() {

		folder.addChild(mmo);
		treeModel.addTreeModelListener(counter);

		mmo.getEventSupport().fireChildrenRemoved("property name",
				new int[]{0}, Arrays.asList(new MatchMakerObject[] {mmo}));
		assertEquals("remove event count should be 1",
				1, counter.getChildrenRemovedCount());
		assertEquals("total event count should be 1",
				1, counter.getAllEventCounts());
		assertEquals("Last event source should be mmo",
				mmo,counter.getLastEvt().getSource());

		TreeModelEvent evt = counter.getLastEvt();
		assertEquals(1, evt.getChildIndices().length);
		assertEquals(1, evt.getChildren().length);
		assertEquals(0, evt.getChildIndices()[0]);
		assertSame(mmo, evt.getChildren()[0]);
	}

	/**
	 * remove children on tree should also remove the listener on the children
	 * so this is for that.
	 */
	public void testTreeNodeRemoveChildEvent() {

		folder.addChild(mmo);
		folder.removeChild(mmo);
		treeModel.addTreeModelListener(counter);

		mmo.getEventSupport().fireChildrenRemoved("property name",
				new int[]{0}, Arrays.asList(new MatchMakerObject[] {mmo}));
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
		mmo.getEventSupport().firePropertyChange("property name",
				"old value","new value");
		assertEquals("property change event count should be 1",
				1, counter.getPropertyChangedCount());
		assertEquals("total event count should be 1",
				1, counter.getAllEventCounts());
		assertEquals("Last event source should be mmo",
				mmo,counter.getLastEvt().getSource());
	}

	public void testTreeNodeStructChangeEvent() {
		final PlFolder<TestingAbstractMatchMakerObject> folder =
			new PlFolder<TestingAbstractMatchMakerObject>("Test Folder");
		currentFoldersNode.addChild(folder);

		final TestingAbstractMatchMakerObject mmo =
			new TestingAbstractMatchMakerObject();
		folder.addChild(mmo);

		treeModel.addTreeModelListener(counter);
		mmo.getEventSupport().fireStructureChanged();

		assertEquals("structure change event count should be 1",
				1, counter.getStructureChangedCount());
		assertEquals("total event count should be 1",
				1, counter.getAllEventCounts());
		assertEquals("Last event source should be mmo",
				mmo,counter.getLastEvt().getSource());
	}
}
