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

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.event.MatchMakerEventSupport;
import ca.sqlpower.object.AbstractSPObject;
import ca.sqlpower.object.ObjectDependentException;
import ca.sqlpower.object.SPObject;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * The abstract class of MatchMakerObject, it has a listener listens to the change
 * of children, properties and structure, any thing changed in the object will
 * cause auditing information changes.
 *
 * @param <T> The type of this matchmaker object implementation
 * @param <C> The child type of this matchmaker object implementation
 */
public abstract class AbstractMatchMakerObject extends AbstractSPObject implements MatchMakerObject 
	 {

    private static final Logger logger = Logger.getLogger(AbstractMatchMakerObject.class);

    @SuppressWarnings("unchecked")
	private MatchMakerEventSupport eventSupport =
		new MatchMakerEventSupport(this);
    
    private String lastUpdateAppUser;
	private String lastUpdateOsUser;
	private Date lastUpdateDate;
	@SuppressWarnings(value={"UWF_UNWRITTEN_FIELD"}, justification="Used reflectively by Hibernate")
	private Date createDate;
	private MatchMakerSession matchMakerSession;

	private boolean visible;

	public AbstractMatchMakerObject() {
		visible = true;
	}
	
	public String getLastUpdateAppUser() {
		return lastUpdateAppUser;
	}
	public String getLastUpdateOSUser() {
		return lastUpdateOsUser;
	}
	public Date getLastUpdateDate() {
		return lastUpdateDate;
	}
    
	/**
	 * Responds to an update by updating the audit information (last update user and date), but
     * only if the object is participating in an ORM session.  The reasons for only doing this
     * in the context of an ORM session are:
     * <ol>
     *  <li>The application username is a property of the ORM session
     *  <li>Updating these properties during unit testing complicates some of the automatic tests
     * </ol>
	 */
	public void registerUpdate() {
		// FIXME Change this to be handled by the daos
		logger.debug("We have registered a change");
		if (matchMakerSession != null){
			lastUpdateDate = new Date();
			lastUpdateOsUser = System.getProperty("user.name");
			lastUpdateAppUser = matchMakerSession.getAppUser();
		}
	}

	public void setSession(MatchMakerSession matchMakerSession) {
		this.matchMakerSession = matchMakerSession;
	}

	/**
	 * Returns this object's session, if it has one. Otherwise, defers
	 * to the parent's getSession() method.
	 */
    public MatchMakerSession getSession() {
        if (getParent() == this) {
            // this check prevents infinite recursion in case of funniness
            throw new IllegalStateException("Something tells me this class belongs to the royal family");
        }
        if (matchMakerSession != null || getParent() == null) {
        	if (logger.isDebugEnabled()) {
        		logger.debug(getClass().getName()+"@"+System.identityHashCode(this)+
        				": Returning session "+matchMakerSession+
        				" (my parent is "+getParent()+")");
        	}
            return matchMakerSession;
        } else {
        	if (logger.isDebugEnabled()) {
        		logger.debug(getClass().getName()+"@"+System.identityHashCode(this)+
        				": looking up the tree");
        	}
        	SPObject parent = getParent();
        	if (!(parent instanceof MatchMakerObject)) {
        		throw new ClassCastException("There shouldn't be any SPObjects that aren't MatchMakerObjects in Matchmaker sessions!");
        	}
            return ((MatchMakerObject)parent).getSession();
        }
    }

	public Date getCreateDate() {
		return createDate;
	}
	
	public MatchMakerObject getParent() {
		return (MatchMakerObject) super.getParent();
	}


	/////// Event stuff ///////

	public void setVisible(boolean visible) {
		boolean old = this.visible;
		this.visible = visible;
		firePropertyChange("visible", old, visible);
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	/////// Undo Stuff ///////
	// fires the event as a undo event if this is true;
	private boolean isUndoing;

	public boolean isUndoing() {
		return isUndoing;
	}

	public void setUndoing(boolean isUndoing) {
		this.isUndoing = isUndoing;
	}

    @Override
    public String toString() {
    	return super.toString() + ", " + getName() + ":" + getUUID();
    }

	@Override
	public void removeDependency(SPObject spo) {
		//TODO: Might add this in at somepoint. Not sure, this just removes a necessary override.
	}

	@Override
	protected boolean removeChildImpl(SPObject child) {
		return false;
	}

	@Override
	public  List<? extends SPObject> getDependencies() {
		//TODO: Might add this in at somepoint. Not sure, this just removes a necessary override.
		return null;
	}
    
    /**
	 * Starts a compound edit so that the whole compound edit can
	 * be undo'ed at the same time. Note that one must call endCompoundEdit after or the
	 * undo listeners will not work properly. Use the following code snippet to ensure 
	 * that endCompoundEdit() is called. <p>
	 * <code> try { 
	 * startCompoundEdit();
	 * ... mutate objects ...
	 * } finally {
	 * endCompoundEdit();
	 * }
	 * </code>
     * @see AbstractMatchMakerObject#endCompoundEdit()
	 */
	public void startCompoundEdit() {
		firePropertyChange("UNDOSTATE", false, true);
	}
	
	/**
	 * Ends a compound edit.
	 * @see AbstractMatchMakerObject#startCompoundEdit()
	 */
	public void endCompoundEdit() {
		firePropertyChange("UNDOSTATE", true, false);
	}
	
	/**
     * Swaps the elements at the specified positions.
     * (If the specified positions are equal, invoking this method leaves
     * the list unchanged.)
     *
     * @param i the index of one element to be swapped.
     * @param j the index of the other element to be swapped.
     */
	public void swapChildren(int i, int j) {
		final List<MatchMakerObject> l = (List<MatchMakerObject>)getChildren();
		try {
			begin("Swapping Children");
			int less;
			int more;
			if (i < j) {
				less = i;
				more = j;
			} else {
				less = j;
				more = i;
			}
			MatchMakerObject child1 = l.get(less);
			MatchMakerObject child2 = l.get(more);

			try {
				removeChild(child1);
				removeChild(child2);
			} catch (ObjectDependentException e) {
				throw new RuntimeException();
			}

			addChild(child2, less);
			addChild(child1, more);
			commit();
		} catch(RuntimeException e) {
			rollback(e.getMessage());
			throw e;
		}
	}
	
	public void moveChild(int from, int to) {
		if (to == from) return;
		final List<MatchMakerObject> l = (List<MatchMakerObject>)getChildren();
		MatchMakerObject child = l.get(from);
		try {
			begin("Moving Child");
			try {
				removeChild(l.get(from));
			} catch (ObjectDependentException e) {
				throw new RuntimeException();
			}
			addChild(child, to);
			commit();
		} catch(RuntimeException e) {
			rollback(e.getMessage());
			throw e;
		}
	}
}