package ca.sqlpower.matchmaker.event;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.TestingAbstractMatchMakerObject;

public class MatchMakerEventSupportTest extends TestCase {

	MatchMakerEventSupport<MatchMakerObject,MatchMakerObject> support;
	MatchMakerObject<TestingAbstractMatchMakerObject, MatchMakerObject> mmo;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mmo = new TestingAbstractMatchMakerObject() {};
		support= new MatchMakerEventSupport<MatchMakerObject, MatchMakerObject>(mmo);
	}

	public void testListenerRemovesSelf() {
		MatchMakerEventCounter<MatchMakerObject,MatchMakerObject> mmec1 = new MatchMakerEventCounter<MatchMakerObject,MatchMakerObject>();
		MatchMakerEventCounter<MatchMakerObject,MatchMakerObject> mmec2 = new MatchMakerEventCounter<MatchMakerObject,MatchMakerObject>();
		MatchMakerEventCounter<MatchMakerObject,MatchMakerObject> mmec3 = new MatchMakerEventCounter<MatchMakerObject,MatchMakerObject>();
		MatchMakerEventCounter<MatchMakerObject,MatchMakerObject> mmec4 = new MatchMakerEventCounter<MatchMakerObject,MatchMakerObject>();
		MatchMakerEventCounter<MatchMakerObject,MatchMakerObject> mmecRS = new MatchMakerEventCounter<MatchMakerObject,MatchMakerObject>() {

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
		MatchMakerEventCounter<MatchMakerObject,MatchMakerObject> mmec =
			new MatchMakerEventCounter<MatchMakerObject,MatchMakerObject>();
		support.addMatchMakerListener(mmec);
		support.fireStructureChanged();
		MatchMakerEvent<MatchMakerObject,MatchMakerObject> lastEvt =
			mmec.getLastEvt();
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
		MatchMakerEventCounter<MatchMakerObject,MatchMakerObject> mmec =
			new MatchMakerEventCounter<MatchMakerObject,MatchMakerObject>();
		support.addMatchMakerListener(mmec);
		int[] index = { 0 };
		List<MatchMakerObject> mmoChildren = new ArrayList<MatchMakerObject>();
		mmoChildren.add(new TestingAbstractMatchMakerObject() {
		});
		support.fireChildrenInserted("InChild", index, mmoChildren);
		MatchMakerEvent<MatchMakerObject,MatchMakerObject> lastEvt =
			mmec.getLastEvt();
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
		MatchMakerEventCounter<MatchMakerObject,MatchMakerObject> mmec =
			new MatchMakerEventCounter<MatchMakerObject,MatchMakerObject>();
		support.addMatchMakerListener(mmec);
		int[] index = { 1 };
		List<MatchMakerObject> mmoChildren = new ArrayList<MatchMakerObject>();
		mmoChildren.add(new TestingAbstractMatchMakerObject() {
		});
		support.fireChildrenRemoved("OutChild", index, mmoChildren);
		MatchMakerEvent<MatchMakerObject,MatchMakerObject> lastEvt =
			mmec.getLastEvt();
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
		MatchMakerEventCounter<MatchMakerObject,MatchMakerObject> mmec =
			new MatchMakerEventCounter<MatchMakerObject,MatchMakerObject>();
		support.addMatchMakerListener(mmec);
		support.firePropertyChange("ppt1", "a", "b");
		MatchMakerEvent<MatchMakerObject,MatchMakerObject> lastEvt =
			mmec.getLastEvt();
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

	public void testNoEventWhenNoPropertyChange() {
		MatchMakerEventCounter<MatchMakerObject,MatchMakerObject> mmec =
			new MatchMakerEventCounter<MatchMakerObject,MatchMakerObject>();
		support.addMatchMakerListener(mmec);
		support.firePropertyChange("cowmoo", "a", "a");
		assertEquals(0, mmec.getPropertyChangedCount());
	}

	public void testNoEventWhenNoPropertyChangeNullNull() {
		MatchMakerEventCounter<MatchMakerObject,MatchMakerObject> mmec =
			new MatchMakerEventCounter<MatchMakerObject,MatchMakerObject>();
		support.addMatchMakerListener(mmec);
		support.firePropertyChange("cowmoo", null, null);
		assertEquals(0, mmec.getPropertyChangedCount());
	}

	public void testEventWhenNoPropertyChangesNullToNotNull() {
		MatchMakerEventCounter<MatchMakerObject,MatchMakerObject> mmec =
			new MatchMakerEventCounter<MatchMakerObject,MatchMakerObject>();
		support.addMatchMakerListener(mmec);
		support.firePropertyChange("cowmoo", null, "moo!");
		assertEquals(1, mmec.getPropertyChangedCount());
	}

	public void testEventWhenNoPropertyChangesNotNullToNull() {
		MatchMakerEventCounter<MatchMakerObject,MatchMakerObject> mmec =
			new MatchMakerEventCounter<MatchMakerObject,MatchMakerObject>();
		support.addMatchMakerListener(mmec);
		support.firePropertyChange("cowmoo", "moo!", null);
		assertEquals(1, mmec.getPropertyChangedCount());
	}

	public void testNullPropertyNameDisallowedForPropertyChange() {
		try {
			support.firePropertyChange(null, "cow", "moo");
			fail("MatchMakerEventSupport allowed a null property name");
		} catch (NullPointerException e) {
			// yay
		}
	}

	public void testNullPropertyNameDisallowedForChildrenInserted() {
		try {
			List<MatchMakerObject> myList = new ArrayList<MatchMakerObject>();
			myList.add(new TestingAbstractMatchMakerObject() {});
			support.fireChildrenInserted(null, new int[] { 0 }, myList);
			fail("MatchMakerEventSupport allowed a null property name");
		} catch (NullPointerException e) {
			// yay
		}
	}

	public void testNullPropertyNameDisallowedForChildrenRemoved() {
		try {
			List<MatchMakerObject> myList = new ArrayList<MatchMakerObject>();
			myList.add(new TestingAbstractMatchMakerObject() {});
			support.fireChildrenRemoved(null, new int[] { 0 }, myList);
			fail("MatchMakerEventSupport allowed a null property name");
		} catch (NullPointerException e) {
			// yay
		}
	}

	public void testAddNullListenerDisallowed() {
		try {
			support.addMatchMakerListener(null);
			fail("MatchMakerEventSupport allowed a null listener");
		} catch (NullPointerException e) {
			// yee-haw
		}
	}

	public void testIndexArrayAndObjectListWithDifferentLengthsOnChildrenInsertedFails() {
		try {
			List<MatchMakerObject> myList = new ArrayList<MatchMakerObject>();
			myList.add(new TestingAbstractMatchMakerObject() {});
			support.fireChildrenInserted("test", new int[] { 0, 1 }, myList);
			fail("MatchMakerEventSupport allowed index and child lists of different sizes");
		} catch (IllegalArgumentException e) {
			// yay
		} catch (Throwable e) {
			fail("Wrong exception type was thrown: "+e.getClass().getName());
		}
	}

	public void testIndexArrayAndObjectListWithDifferentLengthsOnChildrenRemovedFails() {
		try {
			List<MatchMakerObject> myList = new ArrayList<MatchMakerObject>();
			myList.add(new TestingAbstractMatchMakerObject() {});
			support.fireChildrenRemoved("test", new int[] { 0, 1 }, myList);
			fail("MatchMakerEventSupport allowed index and child lists of different sizes");
		} catch (IllegalArgumentException e) {
			// yay
		} catch (Throwable e) {
			fail("Wrong exception type was thrown: "+e.getClass().getName());
		}
	}

}
