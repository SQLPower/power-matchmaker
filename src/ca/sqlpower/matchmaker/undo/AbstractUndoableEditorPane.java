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

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.swingui.CleanupModel;
import ca.sqlpower.matchmaker.swingui.MatchMakerEditorPane;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;

public abstract class AbstractUndoableEditorPane<T extends MatchMakerObject> implements MatchMakerEditorPane, CleanupModel {
	
	private static final Logger logger = Logger.getLogger(AbstractUndoableEditorPane.class);
	
	protected final MatchMakerSwingSession swingSession;
	protected final T mmo;
	/**
	 * The panel that holds this editor's GUI.
	 */
	protected JPanel panel;

	
	/**
	 * keeps track of whether the table has unsaved changes and all the 
	 * edits made.
	 */ 
	protected MMOChangeUndoWatcher undo;
	
	public AbstractUndoableEditorPane(MatchMakerSwingSession swingSession, T mmo) {
		this.swingSession = swingSession;
		this.mmo = mmo;
		if (mmo == null) {
			throw new NullPointerException("You can't edit a null match maker object");
		}
	}
	
	/**
	 * Saves the mmo
	 */
	public boolean applyChanges() {
		logger.debug("saving object: " + mmo);
		undo.setHasChanged(false);
		return true;
	}
	
	/**
	 * Discards the changes by reverting to the original state
	 */
	public void discardChanges() {
		undo.undoAll();
	}

	/**
	 * Returns true if there are changes that have not been saved.
	 */
	public boolean hasUnsavedChanges() {
		return undo.getHasChanged();
	}

	public JComponent getPanel() {
		return panel;
	}

	public void setPanel(JPanel p) {
		panel = p;
	}

	public void cleanup() {
		undo.cleanup();
	}
	
	public void initUndo() {
		undo = new MMOChangeUndoWatcher(mmo,this,swingSession);
	}

	public abstract void undoEventFired(PropertyChangeEvent evt);
	
	public MatchMakerObject getCurrentEditingMMO() {
		return mmo;
	}
}
