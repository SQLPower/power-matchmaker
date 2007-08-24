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

package ca.sqlpower.matchmaker;

import java.util.List;

import ca.sqlpower.matchmaker.event.MatchMakerListener;

/**
 * The interface for all of the match maker specific business objects
 *
 * @param T The type of this implementation of MatchMakerObject
 * @param C The type of children this implementation of MatchMakerObject contains
 */
public interface MatchMakerObject<T extends MatchMakerObject, C extends MatchMakerObject> extends Auditable {

	/**
	 * Registers the given listener as a recipient of future MatchMakerEvents
     * that this object generates.  Does not register the listener for any such
     * events that this object's ancestors or descendants generate.  For that,
     * see {@link MatchMakerUtils#listenToHierarchy(MatchMakerListener, MatchMakerObject)}.
	 */
	void addMatchMakerListener(MatchMakerListener<T, C> l);

	/**
	 * De-registers the given listener as a recipient of future MatchMakerEvents
     * that this object generates.  If the given listener was not in this object's
     * listener list, calling this method has non effect.
	 */
	void removeMatchMakerListener(MatchMakerListener<T, C> l);

	/**
	 * Returns the parent of this object.
	 */
	MatchMakerObject getParent();

    /**
     * Returns the user-visible name of this object.
     */
    String getName();

    /**
     * Set the user visible name of this object
     */
    void setName(String string);
    
	/**
	 * Sets the parent (ie. the object that holds this one as a child)
	 */
	 void setParent(MatchMakerObject parent);

	 /**
      * Tells whether or not this MatchMaker object can have children.
      * 
	  * @return true if this MatchMakerObject allows children, false otherwise.
	  */
	 public boolean allowsChildren();

	/**
	 * Returns the object's primary children
	 */
	List<C> getChildren();

	/**
	 * Returns the number of children on this MatchMaker Object.
	 */
	int getChildCount();

	/**
	 * Add a new child to this object
	 */
	void addChild(C child);

    /**
     * Removes the given child and fires a childrenRemoved event.  If the
     * given child is not present in this object, calling this method has
     * no effect (no children are removed and no events are fired).
	 */
	void removeChild(C child);

	/**
	 * copy the match maker object 
	 */
	T duplicate(MatchMakerObject parent, MatchMakerSession session);
	
    /**
     * Returns the current session that this object is associated with.
     */
    public MatchMakerSession getSession();
    
	/**
	 * Associates the given session with this object.  All ensuing database and
     * DAO access will be done via this session.
	 */
	public void setSession(MatchMakerSession matchMakerSession);

}
