package ca.sqlpower.matchmaker;

import junit.framework.TestCase;
import ca.sqlpower.matchmaker.event.MatchMakerEventCounter;

public class AbstractMatchMakerObjectTest extends TestCase {

	MatchMakerObject<MatchMakerObject> test;
	protected void setUp() throws Exception {
		super.setUp();
		test = new AbstractMatchMakerObject<MatchMakerObject>(){};
	}
	
	public void testChildren(){
		MatchMakerObject mmo1 = new AbstractMatchMakerObject<MatchMakerObject>(){};
		MatchMakerObject mmo2 = new AbstractMatchMakerObject<MatchMakerObject>(){};
		assertEquals("Started out with the wrong number of children",0,test.getChildCount());
		test.addChild(mmo1);
		assertEquals("faild to add the correct number of children",1,test.getChildCount());
		test.addChild(mmo2);
		assertEquals("faild to add the correct number of children",2,test.getChildCount());
		assertEquals("Incorrect child in position 0",mmo1,test.getChildren().get(0));
		assertEquals("Incorrect child in position 1",mmo2,test.getChildren().get(1));
	}
	
	public void testMatchMakerEventListener(){
		MatchMakerEventCounter mml = new MatchMakerEventCounter();
		test.addMatchMakerListener(mml);
		test.addChild(new AbstractMatchMakerObject<MatchMakerObject>(){});
		assertEquals("Did not get any events",1,mml.getAllEventCounts());
		test.removeMatchMakerListener(mml);
		test.addChild(new AbstractMatchMakerObject<MatchMakerObject>(){});
		assertEquals("Got extra events",1,mml.getAllEventCounts());
		
	}
	
	

}
