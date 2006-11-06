package ca.sqlpower.matchmaker.event;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import ca.sqlpower.matchmaker.AbstractMatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerObject;

public class MatchMakerEventSupportTest extends TestCase {

	MatchMakerEventSupport<MatchMakerObject<MatchMakerObject>, MatchMakerObject> support;

	MatchMakerObject<MatchMakerObject> mmo;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mmo = new AbstractMatchMakerObject<MatchMakerObject>("a") {

		};
		support = new MatchMakerEventSupport<MatchMakerObject<MatchMakerObject>, MatchMakerObject>(mmo);
	}

	public void testListenerRemovesSelf() {
		MatchMakerEventCounter mmec1 = new MatchMakerEventCounter();
		MatchMakerEventCounter mmec2 = new MatchMakerEventCounter();
		MatchMakerEventCounter mmec3 = new MatchMakerEventCounter();
		MatchMakerEventCounter mmec4 = new MatchMakerEventCounter();
		MatchMakerEventCounter mmecRS = new MatchMakerEventCounter() {

			@Override
			public void mmStructureChanged(MatchMakerEvent evt) {
				super.mmStructureChanged(evt);
				support.removeMatchMakerListener(this);
			}

		};

		support.addMatchMakerListener(mmec1);
		support.addMatchMakerListener(mmec2);
		support.addMatchMakerListener(mmecRS);
		support.addMatchMakerListener(mmec3);
		support.addMatchMakerListener(mmec4);

		support.fireStructureChanged();
		assertEquals("Got One event", 1, mmec1.getStructureChangedCount());
		assertEquals("Got One event", 1, mmec2.getStructureChangedCount());
		assertEquals("Got One event", 1, mmec3.getStructureChangedCount());
		assertEquals("Got One event", 1, mmec4.getStructureChangedCount());
		assertEquals("Got One event", 1, mmecRS.getStructureChangedCount());

		support.fireStructureChanged();
		assertEquals("Got two events", 2, mmec1.getStructureChangedCount());
		assertEquals("Got two events", 2, mmec2.getStructureChangedCount());
		assertEquals("Got two events", 2, mmec3.getStructureChangedCount());
		assertEquals("Got two events", 2, mmec4.getStructureChangedCount());
		assertEquals("Got One event", 1, mmecRS.getStructureChangedCount());

	}

	public void testStructureChangedSource() {
		MatchMakerEventCounter mmec = new MatchMakerEventCounter();
		support.addMatchMakerListener(mmec);
		support.fireStructureChanged();
		MatchMakerEvent lastEvt = mmec.getLastEvt();
		assertNotNull("No event fired", lastEvt);
		assertTrue("Failed to get the proper source when structure change event thrown", lastEvt.getSource() == mmo);
		assertEquals("Fired the wrong number of events", 1, mmec.getStructureChangedCount());
		assertEquals("Fired other events", 1, mmec.getAllEventCounts());
		support.removeMatchMakerListener(mmec);
		support.fireStructureChanged();
		assertEquals("Fired extra events", 1, mmec.getStructureChangedCount());
		assertEquals("Fired other events", 1, mmec.getAllEventCounts());
	}

	public void testChildrenInsertedSource() {
		MatchMakerEventCounter mmec = new MatchMakerEventCounter();
		support.addMatchMakerListener(mmec);
		int[] index = { 0 };
		List<MatchMakerObject> mmoChildren = new ArrayList<MatchMakerObject>();
		mmoChildren.add(new AbstractMatchMakerObject<MatchMakerObject>("a") {
		});
		support.fireChildrenInserted("InChild", index, mmoChildren);
		MatchMakerEvent lastEvt = mmec.getLastEvt();
		assertNotNull("No event fired", lastEvt);
		assertTrue("Failed to get the proper source when children inserted event thrown", lastEvt.getSource() == mmo);
		assertEquals("Fired the wrong number of events", 1, mmec.getChildrenInsertedCount());
		assertEquals("Fired other events", 1, mmec.getAllEventCounts());
		assertEquals("Wrong index", index, lastEvt.getChangeIndices());
		assertEquals("Wrong child", mmoChildren, lastEvt.getChildren());

		support.removeMatchMakerListener(mmec);
		support.fireChildrenInserted("InChild", index, mmoChildren);
		assertEquals("Fired extra events", 1, mmec.getChildrenInsertedCount());
		assertEquals("Fired other events", 1, mmec.getAllEventCounts());

	}

	public void testChildrenRemovedSource() {
		MatchMakerEventCounter mmec = new MatchMakerEventCounter();
		support.addMatchMakerListener(mmec);
		int[] index = { 1 };
		List<MatchMakerObject> mmoChildren = new ArrayList<MatchMakerObject>();
		mmoChildren.add(new AbstractMatchMakerObject<MatchMakerObject>("a") {
		});
		support.fireChildrenRemoved("OutChild", index, mmoChildren);
		MatchMakerEvent lastEvt = mmec.getLastEvt();
		assertNotNull("No event fired", lastEvt);
		assertTrue("Failed to get the proper source when children removed event thrown", lastEvt.getSource() == mmo);
		assertEquals("Fired the wrong number of events", 1, mmec.getChildrenRemovedCount());
		assertEquals("Fired other events", 1, mmec.getAllEventCounts());
		assertEquals("Wrong property", "OutChild", lastEvt.getPropertyName());
		assertEquals("Wrong index", index, lastEvt.getChangeIndices());
		assertEquals("Wrong child", mmoChildren, lastEvt.getChildren());
		support.removeMatchMakerListener(mmec);
		support.fireChildrenRemoved("InChild", index, mmoChildren);
		assertEquals("Fired extra events", 1, mmec.getChildrenRemovedCount());
		assertEquals("Fired other events", 1, mmec.getAllEventCounts());
	}

	public void testPropertyChangeSource() {
		MatchMakerEventCounter mmec = new MatchMakerEventCounter();
		support.addMatchMakerListener(mmec);
		support.firePropertyChange("ppt1", "a", "b");
		MatchMakerEvent lastEvt = mmec.getLastEvt();
		assertNotNull("No event fired", lastEvt);
		assertTrue("Failed to get the proper source when property change event thrown", lastEvt.getSource() == mmo);
		assertEquals("Fired the wrong number of events", 1, mmec.getPropertyChangedCount());
		assertEquals("Fired other events", 1, mmec.getAllEventCounts());
		assertEquals("Wrong property", "ppt1", lastEvt.getPropertyName());
		assertEquals("Wrong old value", "a", lastEvt.getOldValue());
		assertEquals("Wrong new value", "b", lastEvt.getNewValue());
		support.removeMatchMakerListener(mmec);
		support.firePropertyChange("ppt1", "a", "b");
		assertEquals("Fired extra events", 1, mmec.getPropertyChangedCount());
		assertEquals("Fired other events", 1, mmec.getAllEventCounts());
	}

}
