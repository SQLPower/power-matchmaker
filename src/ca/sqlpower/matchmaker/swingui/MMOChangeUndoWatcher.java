/*
 * Copyright (c) 2007, SQL Power Group Inc.
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

package ca.sqlpower.matchmaker.swingui;

import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEdit;

import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerUtils;
import ca.sqlpower.matchmaker.event.MatchMakerEvent;
import ca.sqlpower.matchmaker.event.MatchMakerListener;
import ca.sqlpower.matchmaker.undo.UndoableEditClass;

/**
 * Utility class that registers every change to a subtree of MatchMakerObjects
 * by flipping its hasChanged flag to true. Mainly intended to support the
 * hasUnsavedChanges feature of EditorPane.
 */
public class MMOChangeUndoWatcher <T extends MatchMakerObject,C extends MatchMakerObject>
implements MatchMakerListener<T,C> {

    private boolean hasChanged = false;
    private UndoManager undo;
    private EditorPane pane;
    private MatchMakerSwingSession swingSession;
    
    
    /**
     * Creates a new MMO Change Watcher for the matchmaker object subtree rooted
     * at the given node.
     * 
     * @param mmo The root node of the subtree to monitor.
     */
    public MMOChangeUndoWatcher(MatchMakerObject<T,C> mmo, EditorPane pane, MatchMakerSwingSession session) {
    	swingSession = session;
    	this.pane = pane;
    	
        MatchMakerUtils.listenToHierarchy(this, mmo);
        undo = new UndoManager();
        swingSession.setUndo(undo);
    }

    /**
     * Listener implementation.
     */
    public void mmChildrenInserted(MatchMakerEvent<T,C> evt) {
        hasChanged = true;
        for (MatchMakerObject<T,C> child : evt.getChildren()) {
            MatchMakerUtils.listenToHierarchy(this, child);
        }
    }

    /**
     * Listener implementation.
     */
    public void mmChildrenRemoved(MatchMakerEvent<T,C> evt) {
        hasChanged = true;
        for (MatchMakerObject<T,C> child : evt.getChildren()) {
            MatchMakerUtils.unlistenToHierarchy(this, child);
        }
    }

    /**
     * Listener implementation.
     */
    public void mmPropertyChanged(MatchMakerEvent<T,C> evt) {
        hasChanged = true;
        if (!evt.isUndoEvent()) {
			System.out.println(evt.getSource());
			hasChanged = true;
			UndoableEdit ue = new UndoableEditClass(evt, null);
			undo.addEdit(ue);
			swingSession.refreshUndoAction();
		} else {
			if (!undo.canUndo()) {
				hasChanged = false;
			}
		}
        pane.refreshComponents();
    }

    /**
     * Listener implementation.
     */
    @SuppressWarnings("unchecked")
    public void mmStructureChanged(MatchMakerEvent<T,C> evt) {
        hasChanged = true;
        MatchMakerUtils.listenToHierarchy(this, evt.getSource());
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
    }
    
    public boolean undoAll() {
    	while (undo.canUndo()) {
			undo.undo();
		}
		return true;
    }

}
