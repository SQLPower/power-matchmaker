package ca.sqlpower.matchmaker.event;

import ca.sqlpower.matchmaker.MatchMakerObject;
import junit.framework.TestCase;

public class MatchMakerEventSupportTest extends TestCase {

	MatchMakerEventSupport support;
	MatchMakerObject mmo;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mmo =new MatchMakerObject(){
			
		};
		support= new MatchMakerEventSupport(mmo);
	}
	
	public void testStructureChangedSource() {
		MatchMakerEventCounter mmec = new MatchMakerEventCounter();
		support.addMatchMakerListener(mmec);
		support.fireStructureChanged();
		MatchMakerEvent lastEvt = mmec.getLastEvt();
		assertNotNull("No event fired",lastEvt);
		assertTrue("Failed to get the proper source when structure change event thrown",lastEvt.getSource()== mmo);
		assertEquals("Fired the wrong number of events",1, mmec.getStructureChangedCount());
		assertEquals("Fired other events",1, mmec.getAllEventCounts());
		support.removeMatchMakerListener(mmec);
		support.fireStructureChanged();
		assertEquals("Fired extra events",1, mmec.getStructureChangedCount());
		assertEquals("Fired other events",1, mmec.getAllEventCounts());
	}
	public void testChildrenInsertedSource() {
		MatchMakerEventCounter mmec = new MatchMakerEventCounter();
		support.addMatchMakerListener(mmec);
		int[] index = {0};
		MatchMakerObject[] mmoChildren = {new MatchMakerObject(){}};
		support.fireChildrenInserted("InChild",index,mmoChildren);
		MatchMakerEvent lastEvt = mmec.getLastEvt();
		assertNotNull("No event fired",lastEvt);
		assertTrue("Failed to get the proper source when children inserted event thrown",lastEvt.getSource()== mmo);
		assertEquals("Fired the wrong number of events",1, mmec.getChildrenInsertedCount());
		assertEquals("Fired other events",1, mmec.getAllEventCounts());
		assertEquals("Wrong index",index,lastEvt.getChangeIndices());
		assertEquals("Wrong child", mmoChildren,lastEvt.getChildren());
		
		support.removeMatchMakerListener(mmec);
		support.fireChildrenInserted("InChild",index,mmoChildren);
		assertEquals("Fired extra events",1, mmec.getChildrenInsertedCount());
		assertEquals("Fired other events",1, mmec.getAllEventCounts());
		
	}
	public void testChildrenRemovedSource() {
		MatchMakerEventCounter mmec = new MatchMakerEventCounter();
		support.addMatchMakerListener(mmec);
		int[] index = {1};
		MatchMakerObject[] mmoChildren = {new MatchMakerObject(){}};
		support.fireChildrenRemoved("OutChild",index,mmoChildren);
		MatchMakerEvent lastEvt = mmec.getLastEvt();
		assertNotNull("No event fired",lastEvt);
		assertTrue("Failed to get the proper source when children removed event thrown",lastEvt.getSource()== mmo);
		assertEquals("Fired the wrong number of events",1, mmec.getChildrenRemovedCount());
		assertEquals("Fired other events",1, mmec.getAllEventCounts());
		assertEquals("Wrong property","OutChild",lastEvt.getPropertyName());
		assertEquals("Wrong index",index,lastEvt.getChangeIndices());
		assertEquals("Wrong child", mmoChildren,lastEvt.getChildren());
		support.removeMatchMakerListener(mmec);
		support.fireChildrenRemoved("InChild",index,mmoChildren);
		assertEquals("Fired extra events",1, mmec.getChildrenRemovedCount());
		assertEquals("Fired other events",1, mmec.getAllEventCounts());
	}
	public void testPropertyChangeSource() {
		MatchMakerEventCounter mmec = new MatchMakerEventCounter();
		support.addMatchMakerListener(mmec);
		support.firePropertyChange("ppt1","a","b");
		MatchMakerEvent lastEvt = mmec.getLastEvt();
		assertNotNull("No event fired",lastEvt);
		assertTrue("Failed to get the proper source when property change event thrown",lastEvt.getSource()== mmo);
		assertEquals("Fired the wrong number of events",1, mmec.getPropertyChangedCount());
		assertEquals("Fired other events",1, mmec.getAllEventCounts());
		assertEquals("Wrong property","ppt1",lastEvt.getPropertyName());
		assertEquals("Wrong old value", "a",lastEvt.getOldValue());
		assertEquals("Wrong new value", "b",lastEvt.getNewValue());
		support.removeMatchMakerListener(mmec);
		support.firePropertyChange("ppt1","a","b");
		assertEquals("Fired extra events",1, mmec.getPropertyChangedCount());
		assertEquals("Fired other events",1, mmec.getAllEventCounts());
		
	}



}
