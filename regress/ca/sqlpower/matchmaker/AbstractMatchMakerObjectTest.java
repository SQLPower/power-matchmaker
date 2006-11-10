package ca.sqlpower.matchmaker;

import junit.framework.TestCase;
import ca.sqlpower.matchmaker.event.MatchMakerEventCounter;

public class AbstractMatchMakerObjectTest extends TestCase {

	MatchMakerObject<MatchMakerObject> test;
	final String appUserName = "user1";

	protected void setUp() throws Exception {
		super.setUp();
		test = new TestingAbstractMatchMakerObject(appUserName){};
	}

	public void testChildren() {
		MatchMakerObject mmo1 = new TestingAbstractMatchMakerObject("a"){};
		MatchMakerObject mmo2 = new TestingAbstractMatchMakerObject("a"){};
		assertEquals("Started out with the wrong number of children",0,test.getChildCount());
		test.addChild(mmo1);
		assertEquals("faild to add the correct number of children",1,test.getChildCount());
		test.addChild(mmo2);
		assertEquals("faild to add the correct number of children",2,test.getChildCount());
		assertEquals("Incorrect child in position 0",mmo1,test.getChildren().get(0));
		assertEquals("Incorrect child in position 1",mmo2,test.getChildren().get(1));
	}

	public void testMatchMakerEventListener() {
		MatchMakerEventCounter mml = new MatchMakerEventCounter();
		test.addMatchMakerListener(mml);
		test.addChild(new TestingAbstractMatchMakerObject("a"){});
		assertEquals("Did not get any events",1,mml.getAllEventCounts());
		test.removeMatchMakerListener(mml);
		test.addChild(new TestingAbstractMatchMakerObject("a"){});
		assertEquals("Got extra events",1,mml.getAllEventCounts());

	}


	public void testAuditingInfoAddChild() {
		MatchMakerObject mmo1 = new TestingAbstractMatchMakerObject("user2"){};
		assertNull("The default last_update_user in match object should be null",
				test.getLastUpdateAppUser());
		assertNull("The default last_update_user in match object should be null",
				mmo1.getLastUpdateAppUser());
		test.addChild(mmo1);
		assertEquals("The last_update_user should be [" +
				appUserName +"], because user1 has changed this match object",
				appUserName, test.getLastUpdateAppUser());
		assertNull("The default last_update_user in match object should be null," +
				" because we have never change it",
				mmo1.getLastUpdateAppUser());
	}

	public void testAuditingInfoRemoveChild() {
		MatchMakerObject mmo1 = new TestingAbstractMatchMakerObject("user2"){};
		assertNull("The default last_update_user in match object should be null",
				test.getLastUpdateAppUser());
		assertNull("The default last_update_user in match object should be null",
				mmo1.getLastUpdateAppUser());
		test.removeChild(mmo1);
		assertEquals("The last_update_user should be [" +
				appUserName +"], because user1 has changed this match object",
				appUserName, test.getLastUpdateAppUser());
		assertNull("The default last_update_user in match object should be null," +
				" because we have never change it",
				mmo1.getLastUpdateAppUser());
	}

	public void testParentSetCorrectly() {
		MatchMakerObject mmo1 = new TestingAbstractMatchMakerObject("user1"){};
		MatchMakerObject mmo2 = new TestingAbstractMatchMakerObject("user2"){};
		mmo2.addChild(mmo1);
		assertEquals("mmo2 is not the parent of mmo1",mmo2,mmo1.getParent());
	}
}
