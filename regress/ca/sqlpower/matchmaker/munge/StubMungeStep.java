/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of DQguru
 *
 * DQguru is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DQguru is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General @Override public License for more details.
 *
 * You should have received a copy of the GNU General @Override public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker.munge;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerEngine.EngineMode;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.object.CleanupExceptions;
import ca.sqlpower.object.ObjectDependentException;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.util.RunnableDispatcher;
import ca.sqlpower.util.WorkspaceContainer;
import ca.sqlpower.validation.ValidateResult;

/**
 * A non-functional implementation of MungeStep for the benefit of test cases.
 */
public class StubMungeStep implements MungeStep {

    private static final Logger logger = Logger.getLogger(StubMungeStep.class);
    
    @Override public int addInput(InputDescriptor desc) {
        logger.debug("Stub call: StubMungeStep.addInput()");
        return 0;
    }

    @Override public Boolean call() throws Exception {
        logger.debug("Stub call: StubMungeStep.call()");
        return null;
    }

    @Override public boolean canAddInput() {
        logger.debug("Stub call: StubMungeStep.canAddInput()");
        return false;
    }

    @Override public void connectInput(int index, MungeStepOutput o) {
        logger.debug("Stub call: StubMungeStep.connectInput()");
    }

    @Override public void disconnectInput(int index) {
        logger.debug("Stub call: StubMungeStep.disconnectInput()");
    }

    @Override public InputDescriptor getInputDescriptor(int inputNumber) {
        logger.debug("Stub call: StubMungeStep.getInputDescriptor()");
        return null;
    }
    
    @Override public int getInputCount() {
        logger.debug("Stub call: StubMungeStep.getInputCount()");
        return 0;
    }

    @Override public List<MungeStepOutput> getMSOInputs() {
        logger.debug("Stub call: StubMungeStep.getInputs()");
        return null;
    }

    @Override public String getParameter(String name) {
        logger.debug("Stub call: StubMungeStep.getParameter()");
        return null;
    }

    @Override public Collection<String> getParameterNames() {
        logger.debug("Stub call: StubMungeStep.getParameterNames()");
        return null;
    }

    @Override public MungeProcess getParent() {
        logger.debug("Stub call: StubMungeStep.getParent()");
        return null;
    }

    @Override public void open(EngineMode mode, Logger logger) throws Exception {
    	logger.debug("Stub call: StubMungeStep.open()");
    }
    
    @Override public void open(Logger logger) throws Exception {
        logger.debug("Stub call: StubMungeStep.open()");
    }

    @Override public void setParameter(String name, String newValue) {
        logger.debug("Stub call: StubMungeStep.setParameter()");
    }
    

	@Override public void setParameter(String name, boolean newValue) {
		logger.debug("Stub call: StubMungeStep.setParameter()");
		
	}

	@Override public void setParameter(String name, int newValue) {
		logger.debug("Stub call: StubMungeStep.setParameter()");
	}

    @Override public boolean allowsChildren() {
        logger.debug("Stub call: StubMungeStep.allowsChildren()");
        return false;
    }

    @Override public MungeStep duplicate(MatchMakerObject parent, MatchMakerSession session) {
        logger.debug("Stub call: StubMungeStep.duplicate()");
        return null;
    }

    @Override public List<MungeStepOutput> getChildren() {
        logger.debug("Stub call: StubMungeStep.getChildren()");
        return null;
    }

    @Override public String getName() {
        logger.debug("Stub call: StubMungeStep.getName()");
        return null;
    }

    @Override public MatchMakerSession getSession() {
        logger.debug("Stub call: StubMungeStep.getSession()");
        return null;
    }

    @Override public void setName(String string) {
        logger.debug("Stub call: StubMungeStep.setName()");
    }

    @Override public void setSession(MatchMakerSession matchMakerSession) {
        logger.debug("Stub call: StubMungeStep.setSession()");
    }

    @Override public Date getCreateDate() {
        logger.debug("Stub call: StubMungeStep.getCreateDate()");
        return null;
    }

    @Override public String getLastUpdateAppUser() {
        logger.debug("Stub call: StubMungeStep.getLastUpdateAppUser()");
        return null;
    }

    @Override public Date getLastUpdateDate() {
        logger.debug("Stub call: StubMungeStep.getLastUpdateDate()");
        return null;
    }

    @Override public String getLastUpdateOSUser() {
        logger.debug("Stub call: StubMungeStep.getLastUpdateOSUser()");
        return null;
    }

    @Override public void registerUpdate() {
        logger.debug("Stub call: StubMungeStep.registerUpdate()");
    }

	@Override public MungeStepOutput getOutputByName(String name) {
		logger.debug("Stub call: StubMungeStep.getOutputByName()");
		return null;
	}

	@Override public boolean isVisible() {
		return true;
	}

	@Override public void setVisible(boolean v) {
	}

    @Override public boolean isUndoing() {
		return false;
	}

	@Override public void setUndoing(boolean isUndoing) {
	}

	@Override public boolean isInputStep() {
		return false;
	}

    @Override public void commit() {
        logger.debug("Stub call: StubMungeStep.commit()");
    }

    @Override public boolean isCommitted() {
        logger.debug("Stub call: StubMungeStep.isCommitted()");
        return false;
    }

    @Override public boolean isRolledBack() {
        logger.debug("Stub call: StubMungeStep.isRolledBack()");
        return false;
    }

    @Override public boolean isOpen() {
        logger.debug("Stub call: StubMungeStep.isOpen()");
        return false;
    }

	@Override public int disconnectInput(MungeStepOutput mso) {
		logger.debug("Stub call: StubMungeStep.disconnectInput()");
		return 0;
	}

	@Override public void removeUnusedInput() {
		logger.debug("Stub call: StubMungeStep.removeUnusedInput()");
	}

	@Override public void addInput(InputDescriptor desc, int index) {
		logger.debug("Stub call: StubMungeStep.addInput()");
	}

	@Override public void setPosition(int x, int y) {
		logger.debug("Stub call: StubMungeStep.setPosition()");
		
	}

	@Override public void endCompoundEdit() {
		logger.debug("Stub call: StubMungeStep.endCompoundEdit()");
	}

	@Override public void startCompoundEdit() {
		logger.debug("Stub call: StubMungeStep.startCompoundEdit()");
	}
	
	@Override public boolean hasConnectedInputs() {
		return false;
	}
	
	@Override public List<ValidateResult> checkPreconditions() {
		logger.debug("Stub call: StubMungeStep.checkPreconditions()");
		return null;
	}
	
	@Override public void refresh(Logger logger) throws Exception {
		logger.debug("Stub call: StubMungeStep.refresh()");
	}

	@Override
	public void moveChild(int from, int to) {
		
	}

	@Override
	public void addSPListener(SPListener l) {
		
	}

	@Override
	public void removeSPListener(SPListener l) {
		
	}

	@Override
	public void setParent(SPObject parent) {
		
	}

	@Override
	public int childPositionOffset(Class<? extends SPObject> childType) {
		return 0;
	}

	@Override
	public boolean removeChild(SPObject child) throws ObjectDependentException,
			IllegalArgumentException {
		return false;
	}

	@Override
	public void addChild(SPObject child, int index) {
		
	}

	@Override
	public String getUUID() {
		return null;
	}

	@Override
	public void setUUID(String uuid) {
		
	}

	@Override
	public void generateNewUUID() {
		
	}

	@Override
	public void removeDependency(SPObject dependency) {
		
	}

	@Override
	public List<? extends SPObject> getDependencies() {
		return null;
	}

	@Override
	public CleanupExceptions cleanup() {
		return null;
	}

	@Override
	public void begin(String message) {
	}

	@Override
	public void commit(String message) {
	}

	@Override
	public WorkspaceContainer getWorkspaceContainer() {
		return null;
	}

	@Override
	public RunnableDispatcher getRunnableDispatcher() {
		return null;
	}

	@Override
	public void rollback(String message) {
		
	}

	@Override
	public <T extends SPObject> List<T> getChildren(Class<T> type) {
		return null;
	}

	@Override
	public List<Class<? extends SPObject>> getAllowedChildTypes() {
		return null;
	}

	@Override
	public boolean allowsChildType(Class<? extends SPObject> type) {
		return false;
	}

	@Override
	public void setMagicEnabled(boolean enable) {
		
	}

	@Override
	public boolean isMagicEnabled() {
		return false;
	}

	@Override
	public boolean removeInput(int index) {
		return false;
	}

	@Override
	public void mungeClose() throws Exception {
	}

	@Override
	public void mungeRollback() throws Exception {
	}

	@Override
	public void mungeCommit() throws Exception {
	}
}
