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

package ca.sqlpower.matchmaker.undo;

import java.beans.PropertyChangeEvent;

import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.util.TransactionEvent;

/**
 * Utility class that registers every change to a subtree of MatchMakerObjects
 * by flipping its hasChanged flag to true. Mainly intended to support the
 * hasUnsavedChanges feature of DataEntryPanel.
 */
public class MMOChangeUndoWatcher implements SPListener {

	private static final Logger logger = Logger.getLogger(MMOChangeUndoWatcher.class);
	
    private boolean hasChanged;
    private UndoManager undo;
    private AbstractUndoableEditorPane pane;
    private MatchMakerSwingSession swingSession;
    private MatchMakerObject mmo;
    
    /**
     *  A "start compound edit" event would create a new instance of compound edit
     *  and subsequent edits would be part of the compound edit until a 
     *  "end compound edit" event is received.
     */
    private CompoundEdit ce;
    
    /**
     *  undoCount works like magic in architect, each "start compound edit" event
     *  would increment it and each "end compound edit" event would decrement
     *  it. This allows multilevel compound edits.
     */
    private static int undoCount;
    
    /**
     * Creates a new MMO Change Watcher for the matchmaker object subtree rooted
     * at the given node.
     * 
     * @param mmo The root node of the subtree to monitor.
     */
    public MMOChangeUndoWatcher(MatchMakerObject mmo, AbstractUndoableEditorPane pane, MatchMakerSwingSession session) {
    	this.swingSession = session;
    	this.mmo = mmo;
    	this.pane = pane;
    	
    	logger.debug("Initializing undo watcher: " + this);
        SQLPowerUtils.listenToHierarchy(mmo, this);
        undo = new UndoManager();
        swingSession.setUndo(undo);
        undoCount = 0;
        ce = null;
        hasChanged = false;
    }
    
    private void addEdit(UndoableEdit undoEdit) {
        if (logger.isDebugEnabled()) {
            logger.debug("Adding new edit: " + undoEdit);
        }

        // if we are not in a compound edit
        if (undoCount == 0) {
            undo.addEdit(undoEdit);
        } else {
            ce.addEdit(undoEdit);
        }
    }
    
    /**
     * Begins a compound edit. Compound edits can be nested, so every call
     * to this method has to be balanced with a call to
     * {@link #compoundGroupEnd()}.
     * 
     * fires a state changed event when a new compound edit is created
     */
    private void compoundGroupStart() {
        undoCount++;
        if (undoCount == 1) {
            ce = new CompoundEdit();
        }
        if (logger.isDebugEnabled()) {
            logger.debug("compoundGroupStart: edit stack =" + undoCount);
        }
    }

    /**
     * Ends a compound edit. Compound edits can be nested, so every call to
     * this method has to be preceeded by a call to
     * {@link #compoundGroupStart()}.
     * 
     * @throws IllegalStateException
     *             if there wasn't already a compound edit in progress.
     */
    private void compoundGroupEnd() {
        if (undoCount <= 0) {
            throw new IllegalStateException("No compound edit in progress");
        }
        undoCount--;
        if (undoCount == 0)
            returnToEditState();
        if (logger.isDebugEnabled()) {
            logger.debug("compoundGroupEnd: edit stack =" + undoCount + " ce=" + ce);
        }
    }

    /**
     * Return to a single edit state from a compound edit state
     */
    private void returnToEditState() {
        if (undoCount != 0) {
            throw new IllegalStateException("The compound edit stack (" + undoCount + ") should be 0");
        }
        if (ce != null) {
            ce.end();
            if (ce.canUndo()) {
                if (logger.isDebugEnabled())
                    logger.debug("Adding compound edit " + ce + " to undo manager");
                undo.addEdit(ce);
            } else {
                if (logger.isDebugEnabled())
                    logger.debug("Compound edit " + ce + " is not undoable so we are not adding it");
            }
            ce = null;
        }
        logger.debug("Returning to regular state");
    }

	@Override
	public void childAdded(SPChildEvent e) {
    	hasChanged = true;
    	SQLPowerUtils.listenToHierarchy(e.getChild(), this);
    	
    	logger.debug("Child: " + e.getChild() + " is inserted into: " + e.getSource().toString());
    	if (e.getSource().isMagicEnabled()) {
    		UndoableEdit ue = new MMOChildrenInsertUndoableEdit(e);
    		addEdit(ue);
    	}
    	if (!undo.canUndo()) {
			hasChanged = false;
		}
    	swingSession.refreshUndoAction();
	}

	@Override
	public void childRemoved(SPChildEvent e) {
        hasChanged = true;
        SQLPowerUtils.unlistenToHierarchy(e.getChild(), this);
    	
    	logger.debug("Child: " + e.getChild() + " is removed from: " + e.getSource().toString());
    	if (e.getSource().isMagicEnabled()) {
    		UndoableEdit ue = new MMOChildrenRemoveUndoableEdit(e);
    		addEdit(ue);
    	}
    	if (!undo.canUndo()) {
			hasChanged = false;
		}
    	swingSession.refreshUndoAction();
		
	}

	@Override
	public void transactionStarted(TransactionEvent e) {
		compoundGroupStart();	
	}

	@Override
	public void transactionEnded(TransactionEvent e) {
		compoundGroupEnd();
    	swingSession.refreshUndoAction();
	}

	@Override
	public void transactionRollback(TransactionEvent e) {
		//Do nothing
	}

	@Override
	public void propertyChanged(PropertyChangeEvent evt) {
        hasChanged = true;
        logger.debug("Watcher: " + this + ", Property: " + evt.getPropertyName() + " from " + evt.getSource().toString() + " has changed.");
        logger.debug("old value: " + evt.getOldValue() + ", new value: " + evt.getNewValue());
        if (((SPObject)evt.getSource()).isMagicEnabled()) {
			UndoableEdit ue = new MMOPropertyChangeUndoableEdit(evt);
			
        	if ("UNDOSTATE".equals(evt.getPropertyName())) {
            	boolean undoing = (Boolean) evt.getNewValue();
            	if (undoing) {
            		logger.debug("Starting new compound edit with undoCount: " + undoCount);
            		undoCount++;
            		if (undoCount == 1) {
            			ce = new CompoundEdit();
            		}
            	} else {
            		logger.debug("Ending compound edit with undoCount: " + undoCount);
            		undoCount--;
            		if (undoCount == 0) {
            			ce.end();
            			undo.addEdit(ce);
                		ce = null;
            		}
            	}
            } else if (undoCount > 0) {
        		ce.addEdit(ue);
        	} else {
        		undo.addEdit(ue);
        	}
		} else {
			if (!undo.canUndo()) {
				hasChanged = false;
			}
			pane.undoEventFired(evt);
		}
        swingSession.refreshUndoAction();
	}

    /**
     * Returns the last value given to {@link #setHasChanged(boolean)}, unless this object
     * has received any MatchMakerObject events since then, in which case the return value
     * will be <tt>true</tt>.
     * 
     * @return True if there have been any MatchMakerObject events since the last call to
     * setHasChanged; otherwise, the most recent value set in setHasChanged.
     */
    public boolean getHasChanged() {
        return hasChanged;
    }

    /**
     * Sets the hasChanged flag to the given value. Normally, you would call
     * this method to set the status back to false just after you save the
     * subtree being monitored by this watcher.
     * 
     * @param hasChanged The new hasChanged status.
     */
    public void setHasChanged(boolean hasChanged) {
        this.hasChanged = hasChanged;
        if (!hasChanged) {
        	undo.discardAllEdits();
        	swingSession.refreshUndoAction();
        }
    }
    
    /**
     * Undo everything!
     */
    public void undoAll() {
    	while (undo.canUndo()) {
			undo.undo();
		}
    	setHasChanged(false);
    }
    
    /**
     * Make the undoWatcher stop listening to the matchmaker objects and
     * clears the undo stack
     */
    public void cleanup() {
    	if (undo != null) {
    		logger.debug("Cleaning up undo watcher: " + this);
    		undo.die();
			SQLPowerUtils.unlistenToHierarchy(mmo, this);
			mmo = null;
			pane = null;
			swingSession.refreshUndoAction();
    	}
    }
}
