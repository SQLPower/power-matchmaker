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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.i 
 */

package ca.sqlpower.matchmaker.swingui;

import javax.swing.JComponent;

/**
 * The interface (that should be) implmented by all the Editors
 * that are to appear in the "right side" editor pane area.
 *
 */
public interface EditorPane {
    
	/** 
     * True if this Pane has any changes; will usually delegate
	 * to the Panel's Validator's hasValidated() method.
	 * @return
	 */
	public boolean hasUnsavedChanges();

	/** 
     * Performs the editor save.
     * 
     * <p><b>IMPORTANT NOTE:</b> Make sure this method does not blindly return true
     * just so that it has a valid return type, it is essiental that it
     * returns if the object is saved properly or not.  This is required
     * since if the save does fail, the swing session needs to know to restore
     * the interface back and reselect the lastTreePath in the JTree.  You have
     * officially been warned...
     * </p>
     * @return the success of the saving process (do not fake it!)
	 */                
	public boolean doSave();

	/** 
     * Retrieves the Editor's visual component.
     */
	public JComponent getPanel();
}
