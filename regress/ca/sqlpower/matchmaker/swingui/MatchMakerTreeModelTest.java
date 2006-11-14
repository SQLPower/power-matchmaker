package ca.sqlpower.matchmaker.swingui;

import java.util.ArrayList;
import java.util.Arrays;

import junit.framework.TestCase;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.TestingAbstractMatchMakerObject;

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
		treeModel = new MatchMakerTreeModel(new ArrayList<PlFolder>());
		currentFoldersNode = (MatchMakerObject<MatchMakerObject, PlFolder>) treeModel.getChild(treeModel.getRoot(), 0);
		currentFoldersNode.addChild(folder);
	}

	public void testTreeNodeInsertEvent() {

		treeModel.addTreeModelListener(counter);
		folder.addChild(mmo);
		assertEquals("insert event count should be 1",
				1, counter.getChildrenInsertedCount());
		assertEquals("total event count should be 1",
				1, counter.getAllEventCounts());
		assertEquals("Last event source should be folder",
				folder,counter.getLastEvt().getSource());


	}

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
