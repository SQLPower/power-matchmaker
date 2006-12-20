package ca.sqlpower.matchmaker;

import junit.framework.TestCase;
import ca.sqlpower.matchmaker.event.MatchMakerEventCounter;

public class AbstractMatchMakerObjectTest extends TestCase {

	MatchMakerObject<TestingAbstractMatchMakerObject,MatchMakerObject> test;
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
		assertEquals("Started out with the wrong number of children",0,test.getChildCount());
		test.addChild(mmo1);
		assertEquals("faild to add the correct number of children",1,test.getChildCount());
		test.addChild(mmo2);
		assertEquals("faild to add the correct number of children",2,test.getChildCount());
		assertEquals("Incorrect child in position 0",mmo1,test.getChildren().get(0));
		assertEquals("Incorrect child in position 1",mmo2,test.getChildren().get(1));
	}

	public void testMatchMakerEventListener() {
		MatchMakerEventCounter<TestingAbstractMatchMakerObject, MatchMakerObject> mml =
			new MatchMakerEventCounter<TestingAbstractMatchMakerObject, MatchMakerObject>();
		test.addMatchMakerListener(mml);
		test.addChild(new TestingAbstractMatchMakerObject(){});
		assertEquals("Did not get any events",1,mml.getAllEventCounts());
		test.removeMatchMakerListener(mml);
		test.addChild(new TestingAbstractMatchMakerObject(){});
		assertEquals("Got extra events",1,mml.getAllEventCounts());

	}

	public void testParentSetCorrectly() {
		TestingAbstractMatchMakerObject mmo1 = new TestingAbstractMatchMakerObject(){};
		TestingAbstractMatchMakerObject mmo2 = new TestingAbstractMatchMakerObject(){};
		mmo2.addChild(mmo1);
		assertEquals("mmo2 is not the parent of mmo1",mmo2,mmo1.getParent());
	}
    
    public void testRemoveChildDoesntFireWhenChildNotPresent() {
        MatchMakerEventCounter<TestingAbstractMatchMakerObject, MatchMakerObject> mml =
            new MatchMakerEventCounter<TestingAbstractMatchMakerObject, MatchMakerObject>();
        test.addMatchMakerListener(mml);
        test.removeChild(new StubMatchMakerObject("not a child of test"));
        assertEquals(0, mml.getAllEventCounts());
    }
}    
