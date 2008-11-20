/*
 * Copyright (c) 2008, SQL Power Group Inc.
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

package ca.sqlpower.matchmaker.munge;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.event.MatchMakerEventCounter;

public class AbstractMungeStepTest extends TestCase {

	TestingAbstractMungeStep mungeStep;
	
	protected void setUp() throws Exception {
		super.setUp();
		mungeStep = new TestingAbstractMungeStep();
	}

	/**
	 * Test to ensure that add input adds the MungeStepOuput, and 
	 * that it fires a property changed event
	 */
	public void testAddInput() {
		InputDescriptor desc = new InputDescriptor("test", Object.class);
		MatchMakerEventCounter<MungeStep, MungeStepOutput> mml =
			new MatchMakerEventCounter<MungeStep, MungeStepOutput>();
		mungeStep.addMatchMakerListener(mml);
		mungeStep.addInput(desc);
		assertEquals("Did not get any events",1,mml.getPropertyChangedCount());
		mungeStep.removeMatchMakerListener(mml);
		mungeStep.addInput(desc);
		assertEquals("Got extra events",1,mml.getAllEventCounts());
	}

	/**
	 * Test to ensure that remove input removes the MungeStepOutput, and 
	 * that it fires a property changed event
	 */
	public void testRemoveInput() {
		InputDescriptor desc = new InputDescriptor("test", Object.class);
		mungeStep.addInput(desc);
		MatchMakerEventCounter<MungeStep, MungeStepOutput> mml =
			new MatchMakerEventCounter<MungeStep, MungeStepOutput>();
		mungeStep.addMatchMakerListener(mml);
		mungeStep.removeInput(0);
		assertEquals("Did not get any events",1,mml.getPropertyChangedCount());
	}

	/**
	 * Test to ensure setParameter properly sets a munge step parameter, and 
	 * that it fires a property changed event
	 */
	public void testSetParameter() {
		MatchMakerEventCounter<MungeStep, MungeStepOutput> mml =
			new MatchMakerEventCounter<MungeStep, MungeStepOutput>();
		mungeStep.addMatchMakerListener(mml);
		mungeStep.setParameter("test", "test");
		assertEquals("Did not get any events",1,mml.getPropertyChangedCount());
		assertEquals("Parameter cannot be found", "test", mungeStep.getParameter("test"));
	}
	
	public void testConnectInput() {
		MatchMakerEventCounter<MungeStep, MungeStepOutput> mml =
			new MatchMakerEventCounter<MungeStep, MungeStepOutput>();
		mungeStep.addMatchMakerListener(mml);
		InputDescriptor desc = new InputDescriptor("test", String.class);
		mungeStep.addInput(desc);
		assertEquals("Did not get event for adding input",1,mml.getPropertyChangedCount());
		MungeStepOutput o = new MungeStepOutput<String>("test", String.class);
		int oldInputCount = mungeStep.getMSOInputs().size();
		mungeStep.connectInput(0, o);
		assertEquals(oldInputCount + 1, mungeStep.getMSOInputs().size());
		assertEquals("Did not get event for connecting input",3,mml.getPropertyChangedCount());
		assertTrue("Munge step output did not get connected", mungeStep.getMSOInputs().contains(o));
	}
	
	public void testDisconnectInput() {
		MatchMakerEventCounter<MungeStep, MungeStepOutput> mml =
			new MatchMakerEventCounter<MungeStep, MungeStepOutput>();
		mungeStep.addMatchMakerListener(mml);
		InputDescriptor desc = new InputDescriptor("test", String.class);
		mungeStep.addInput(desc);
		assertEquals("Did not get event for adding input",1,mml.getPropertyChangedCount());
		MungeStepOutput o = new MungeStepOutput<String>("test", String.class);
		mungeStep.connectInput(0, o);
		assertEquals("Did not get event for connecting input",3,mml.getPropertyChangedCount());
		mungeStep.disconnectInput(0);
		assertEquals("Did not get any event for disconnecting input",4,mml.getPropertyChangedCount());
		assertFalse("Munge step output did not get disconnected", mungeStep.getMSOInputs().contains(o));
	}
    
    public void testRollbackBeforeOpen() throws Exception {
        try {
            mungeStep.rollback();
            fail("Rollback should fail when step not open");
        } catch (IllegalStateException ex) {
            // expected
        }
    }

    public void testOpenWithNullLogger() throws Exception {
        try {
            mungeStep.open(null);
            fail("Successfully opened step with null logger");
        } catch (NullPointerException ex) {
            // expected
        }
    }
    
    public void testRollback() throws Exception {
        mungeStep.open(Logger.getLogger(getClass()));
        mungeStep.rollback();
        assertTrue(mungeStep.isRolledBack());
    }
    
    public void testCommitBeforeOpen() throws Exception {
        try {
            mungeStep.commit();
            fail("Commit should fail when step not open");
        } catch (IllegalStateException ex) {
            // expected
        }
    }

    public void testCommit() throws Exception {
        mungeStep.open(Logger.getLogger(getClass()));
        mungeStep.commit();
        assertTrue(mungeStep.isCommitted());
    }
    
    public void testReopenAfterCommit() throws Exception {
        mungeStep.open(Logger.getLogger(getClass()));
        mungeStep.commit();
        mungeStep.close();
        assertTrue(mungeStep.isCommitted());
        assertFalse(mungeStep.isRolledBack());
        mungeStep.open(Logger.getLogger(getClass()));
        assertFalse(mungeStep.isCommitted());
        assertFalse(mungeStep.isRolledBack());
    }
    
    public void testReopenImmediate() throws Exception {
        mungeStep.open(Logger.getLogger(getClass()));
        try {
            mungeStep.open(Logger.getLogger(getClass()));
            fail("Step should not reopen without closing first");
        } catch (IllegalStateException ex) {
            // expected
        }
    }
    
    public void testCloseBeforeCommitOrRollback() throws Exception {
        mungeStep.open(Logger.getLogger(getClass()));
        try {
            mungeStep.close();
            fail("Step should not close without commit or rollback");
        } catch (IllegalStateException ex) {
            // expected
        }
    }

    public void testCloseBeforeOpen() throws Exception {
        try {
            mungeStep.close();
            fail("Step should not close before opening");
        } catch (IllegalStateException ex) {
            // expected
        }
    }

    public void testCallAfterRollback() throws Exception {
        mungeStep.open(Logger.getLogger(getClass()));
        mungeStep.call();
        mungeStep.rollback();
        assertTrue(mungeStep.isRolledBack());
        try {
            mungeStep.call();
            fail("call after rollback should be illegal");
        } catch (IllegalStateException ex) {
            // expected
        }
    }
    
    public void testCallAfterCommit() throws Exception {
        mungeStep.open(Logger.getLogger(getClass()));
        mungeStep.call();
        mungeStep.commit();
        try {
            mungeStep.call();
            fail("call after commit should be illegal");
        } catch (IllegalStateException ex) {
            // expected
        }
    }
    
    public void testHasConnectedInputs() {
    	mungeStep.addInput(new InputDescriptor("A", Object.class));
    	mungeStep.addInput(new InputDescriptor("B", Object.class));
    	mungeStep.addInput(new InputDescriptor("C", Object.class));
    	
    	assertFalse(mungeStep.hasConnectedInputs());
    	
    	mungeStep.connectInput(0, new MungeStepOutput<Object>("A", Object.class));
    	assertTrue(mungeStep.hasConnectedInputs());
    	
    	mungeStep.disconnectInput(0);
    	assertFalse(mungeStep.hasConnectedInputs());
    	
    	mungeStep.connectInput(1, new MungeStepOutput<Object>("A", Object.class));
    	mungeStep.connectInput(2, new MungeStepOutput<Object>("B", Object.class));
    	assertTrue(mungeStep.hasConnectedInputs());
    	
    	mungeStep.disconnectInput(1);
    	assertTrue(mungeStep.hasConnectedInputs());
    	
    	mungeStep.disconnectInput(2);
    	assertFalse(mungeStep.hasConnectedInputs());
    }
}
