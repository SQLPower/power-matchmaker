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

package ca.sqlpower.matchmaker.munge;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerTestCase;
import ca.sqlpower.matchmaker.event.MatchMakerEventCounter;
import ca.sqlpower.object.SPObject;

public abstract class AbstractMungeStepTest<T extends MatchMakerObject> extends MatchMakerTestCase<T> {

	
	MungeStep mungeStep;
	
	public AbstractMungeStepTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		mungeStep = (MungeStep)getTarget();
	}

	/**
	 * Test to ensure that add input adds the MungeStepOuput, and 
	 * that it fires a property changed event
	 */
	public void testAddInput() {
		if (mungeStep.canAddInput()) {
			InputDescriptor desc = new InputDescriptor("test", Object.class);
			MatchMakerEventCounter mml = new MatchMakerEventCounter();
			mungeStep.addSPListener(mml);
			mungeStep.addInput(desc);
			assertEquals("Did not get any events",1,mml.getChildAddedCount());
			mungeStep.removeSPListener(mml);
			mungeStep.addInput(desc);
			assertEquals("Got extra events",1,mml.getAllEventCounts());
		}
	}

	/**
	 * Test to ensure that remove input removes the MungeStepOutput, and 
	 * that it fires a property changed event
	 */
	public void testRemoveInput() {
		if(mungeStep.canAddInput()) {
			InputDescriptor desc = new InputDescriptor("test", Object.class);
			mungeStep.addInput(desc);
			MatchMakerEventCounter mml = new MatchMakerEventCounter();
			mungeStep.addSPListener(mml);
			mungeStep.removeInput(0);
			assertEquals("Did not get any events",1,mml.getChildRemovedCount());
		}
	}

	/**
	 * Test to ensure setParameter properly sets a munge step parameter, and 
	 * that it fires a property changed event
	 */
	public void testSetParameter() {
		MatchMakerEventCounter mml = new MatchMakerEventCounter();
		mungeStep.addSPListener(mml);
		mungeStep.setParameter("test", "test");
		assertEquals("Did not get any events",1,mml.getPropertyChangedCount());
		assertEquals("Parameter cannot be found", "test", mungeStep.getParameter("test"));
	}
	
	public void testConnectInput() {
		if (mungeStep.canAddInput()) {
			MatchMakerEventCounter mml = new MatchMakerEventCounter();
			mungeStep.addSPListener(mml);
			InputDescriptor desc = new InputDescriptor("test", String.class);
			mungeStep.addInput(desc);
			mungeStep.getMungeStepInputs().get(0).addSPListener(mml);
			assertEquals("Did not get event for adding input",1,mml.getChildAddedCount());
			MungeStepOutput o = new MungeStepOutput<String>("test", String.class);
			int oldInputCount = mungeStep.getMSOInputs().size();
			mungeStep.connectInput(0, o);
			assertEquals(oldInputCount + 1, mungeStep.getMSOInputs().size());
			assertEquals("Did not get event for connecting input",1,mml.getPropertyChangedCount());
			assertTrue("Munge step output did not get connected", mungeStep.getMSOInputs().contains(o));
		}
	}
	
	public void testDisconnectInput() {
		if(mungeStep.canAddInput()) {
			MatchMakerEventCounter mml = new MatchMakerEventCounter();
			mungeStep.addSPListener(mml);
			InputDescriptor desc = new InputDescriptor("test", String.class);
			mungeStep.addInput(desc);
			mungeStep.getMungeStepInputs().get(0).addSPListener(mml);
			assertEquals("Did not get event for adding input",1,mml.getChildAddedCount());
			MungeStepOutput o = new MungeStepOutput<String>("test", String.class);
			mungeStep.connectInput(0, o);
			assertEquals("Did not get event for connecting input",1,mml.getPropertyChangedCount());
			mungeStep.disconnectInput(0);
			assertEquals("Did not get any event for disconnecting input",2,mml.getPropertyChangedCount());
			assertFalse("Munge step output did not get disconnected", mungeStep.getMSOInputs().contains(o));
		}
	}
    
    public void testRollbackBeforeOpen() throws Exception {
        try {
            mungeStep.mungeRollback();
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
        mungeStep.mungeRollback();
        assertTrue(mungeStep.isRolledBack());
    }
    
    public void testCommitBeforeOpen() throws Exception {
        try {
            mungeStep.mungeCommit();
            fail("Commit should fail when step not open");
        } catch (IllegalStateException ex) {
            // expected
        }
    }

    public void testCommit() throws Exception {
        mungeStep.open(Logger.getLogger(getClass()));
        mungeStep.mungeCommit();
        assertTrue(mungeStep.isCommitted());
    }
    
    public void testReopenAfterCommit() throws Exception {
        mungeStep.open(Logger.getLogger(getClass()));
        mungeStep.mungeCommit();
        mungeStep.mungeClose();
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
            mungeStep.mungeClose();
            fail("Step should not close without commit or rollback");
        } catch (IllegalStateException ex) {
            // expected
        }
    }

    public void testCloseBeforeOpen() throws Exception {
        try {
            mungeStep.mungeClose();
            fail("Step should not close before opening");
        } catch (IllegalStateException ex) {
            // expected
        }
    }

    public void testCallAfterRollback() throws Exception {
        mungeStep.open(Logger.getLogger(getClass()));
        mungeStep.call();
        mungeStep.mungeRollback();
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
        mungeStep.mungeCommit();
        try {
            mungeStep.call();
            fail("call after commit should be illegal");
        } catch (IllegalStateException ex) {
            // expected
        }
    }
    
    public void testHasConnectedInputs() {
		if(mungeStep.canAddInput()) {
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

	@Override
	protected Class<? extends SPObject> getChildClassType() {
		return MungeStepOutput.class;
	}
	
	/**
	 * Since all the munge steps have this field in the super class, AbstractMungeStep,
	 * we can do the test here on only the AbstractMungeStep class.
	 */
	@Override
	public void testAllowedChildTypesField() throws Exception {
		Class<? extends SPObject> classUnderTest = AbstractMungeStep.class;
		Field childOrderField;
		try {
	        childOrderField = classUnderTest.getDeclaredField("allowedChildTypes");
		} catch (NoSuchFieldException ex) {
		    fail("Persistent " + classUnderTest + " must have a static final field called allowedChildTypes");
		    throw new AssertionError(); // NOTREACHED
		}
	    
	    assertEquals("The allowedChildTypes field must be final",
	            true, Modifier.isFinal(childOrderField.getModifiers()));
	
	    assertEquals("The allowedChildTypes field must be static",
	            true, Modifier.isStatic(childOrderField.getModifiers()));
	
	    // Note: in the future, we will change this to require that the field is private
	    assertEquals("The allowedChildTypes field must be public",
	            true, Modifier.isPublic(childOrderField.getModifiers()));
	    
		List<Class<? extends SPObject>> allowedChildTypes =
		    (List<Class<? extends SPObject>>) childOrderField.get(null);
		if (getSPObjectUnderTest().allowsChildren()) {
			assertFalse(allowedChildTypes.isEmpty());
		} else {
			assertTrue(allowedChildTypes.isEmpty());
		}
	}
}
