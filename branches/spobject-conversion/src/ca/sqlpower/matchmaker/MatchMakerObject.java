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

package ca.sqlpower.matchmaker;

import ca.sqlpower.matchmaker.event.MatchMakerListener;

/**
 * The interface for all of the match maker specific business objects
 *
 * @param T The type of this implementation of MatchMakerObject
 * @param C The type of children this implementation of MatchMakerObject contains
 */
public interface MatchMakerObject extends Auditable {

	/**
	 * Registers the given listener as a recipient of future MatchMakerEvents
     * that this object generates.  Does not register the listener for any such
     * events that this object's ancestors or descendants generate.  For that,
     * see {@link MatchMakerUtils#listenToHierarchy(MatchMakerListener, MatchMakerObject)}.
	 */
	void addMatchMakerListener(MatchMakerListener l);

	/**
	 * De-registers the given listener as a recipient of future MatchMakerEvents
     * that this object generates.  If the given listener was not in this object's
     * listener list, calling this method has non effect.
	 */
	void removeMatchMakerListener(MatchMakerListener l);

	/**
	 * copy the match maker object 
	 */
	MatchMakerObject duplicate(MatchMakerObject parent, MatchMakerSession session);
	
    /**
     * Returns the current session that this object is associated with.
     */
    MatchMakerSession getSession();
    
	/**
	 * Associates the given session with this object.  All ensuing database and
     * DAO access will be done via this session.
	 */
	void setSession(MatchMakerSession matchMakerSession);
	
	/**
	 * Sets if the object will be shown in the tree.
	 */
	void setVisible(boolean v);

	/**
	 * Returns if this item is visible in the tree.
	 */
	boolean isVisible();
	
	
	/**
	 * Returns true if the object is undoing.
	 */
	boolean isUndoing();
	
	/**
	 * Sets if the object is undoing.
	 */
	void setUndoing(boolean isUndoing);
	
	/**
	 * Starts a compound edit so that the whole compound edit can
	 * be undo'ed at the same time. Note that one must call endCompoundEdit after or the
	 * undo listeners will not work properly. <p>
	 * see {@link AbstractMatchMakerObject#endCompoundEdit()} 
	 */
	void startCompoundEdit();
	
	/**
	 * Ends a compound edit, for more information, 
	 * see {@link AbstractMatchMakerObject#startCompoundEdit()} 
	 */
	void endCompoundEdit();
}
