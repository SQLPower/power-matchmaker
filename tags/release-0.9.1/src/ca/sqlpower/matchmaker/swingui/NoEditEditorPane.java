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

import javax.swing.JComponent;
/**
 * Editor Pane for those panels that have nothing to edit.
 */
class NoEditEditorPane implements EditorPane {

	/**
	 * The non-editable display for this EditorPane.
	 */
	private JComponent panel;
	
	public NoEditEditorPane(JComponent panel) {
		this.panel =panel;
	}
	
	/**
	 * doSave() is supposed to return the succesfull-ness of a save operation.
	 * Since nothing changes, nothing needs to be saved, so we just say that
	 * saving worked.
	 */
	public boolean doSave() {
		return true;
	}

	/**
	 * Since nothing changes, no changes are discarded.
	 */
	public boolean discardChanges() {
		return false;
	}
	
	/**
	 * Always returns false because, since nothing is being edited, there are
	 * never changes, nevermind changes that haven't been saved.
	 */
	public boolean hasUnsavedChanges() {
		return false;
	}

	public JComponent getPanel() {
		return panel;
	}
	
	public void setPanel(JComponent panel) {
		this.panel = panel;
	}

	
}
