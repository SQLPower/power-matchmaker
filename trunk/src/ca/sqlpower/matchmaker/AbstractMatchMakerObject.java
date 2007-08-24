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

package ca.sqlpower.matchmaker;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.event.MatchMakerEventSupport;
import ca.sqlpower.matchmaker.event.MatchMakerListener;

/**
 * The abstract class of MatchMakerObject, it has a listener listens to the change
 * of children, properties and structure, any thing changed in the object will
 * cause auditing information changes.
 *
 * @param <T> The type of this matchmaker object implementation
 * @param <C> The child type of this matchmaker object implementation
 */
public abstract class AbstractMatchMakerObject<T extends MatchMakerObject, C extends MatchMakerObject>
	implements MatchMakerObject<T, C> {

    private static final Logger logger = Logger.getLogger(AbstractMatchMakerObject.class);

	private MatchMakerObject parent;

	@SuppressWarnings("unchecked")
	private MatchMakerEventSupport<T,C> eventSupport =
		new MatchMakerEventSupport<T,C>((T) this);

	private List<C> children = new ArrayList<C>();
	private String lastUpdateAppUser;
	private String lastUpdateOsUser;
	private Date lastUpdateDate;
	private Date createDate;
	private MatchMakerSession matchMakerSession;
	private String name;


	public AbstractMatchMakerObject() {

	}

	/**
	 * anyone who going to overwrite this method should fire the childrenInserted
	 * event in the overwriten method
	 * @param child
	 */
	public void addChild(C child) {
        logger.debug("addChild: children collection is a "+children.getClass().getName());
        if(child== null) throw new NullPointerException("Cannot add a null child");
		children.add(child);
		child.setParent(this);
		List<C> insertedChildren = new ArrayList<C>();
		insertedChildren.add(child);
		eventSupport.fireChildrenInserted("children",new int[] {children.size()-1},insertedChildren);
	}

	/**
	 * adds child to position index, use for change position of children
	 * anyone who going to overwrite this method should fire the childrenInserted
	 * event in the overwriten method
	 * @param child
	 */
	public void addChild(int index, C child) {
        logger.debug("addChild: children collection is a "+children.getClass().getName());
        if(child== null) throw new NullPointerException("Cannot add a null child");
		children.add(index, child);
		child.setParent(this);
		List<C> insertedChildren = new ArrayList<C>();
		insertedChildren.add(child);
		eventSupport.fireChildrenInserted("children",new int[] {index},insertedChildren);
	}

	public int getChildCount() {
		return children.size();
	}

	public List<C> getChildren() {
		return children;
	}
    /**
     * Replaces the list of children with the passed in list.
     *
     * This is intentionaly package private because it is only supposed to be used in methods that
     * support the ORM.
     *
     * @param children
     */
    void setChildren(List<C> children){
        this.children = children;
        eventSupport.fireStructureChanged();
    }

	/**
     * Removes the given child and fires a childrenRemoved event.  If the
     * given child is not present in this object, calling this method has
     * no effect (no children are removed and no events are fired).
     * <p>
	 * Anyone who is going to override this method should fire the ChildrenRemoved
	 * event in the overridden method.
     *
	 * @param child
	 */
	public void removeChild(C child) {
		int childIndex = children.indexOf(child);
        if (childIndex == -1) return;
        int [] removedIndices = {childIndex};
		List<C> removedChildren = new ArrayList<C>();
		removedChildren.add(child);
		children.remove(child);
		eventSupport.fireChildrenRemoved("children",removedIndices,removedChildren);
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
	 * Register an update if the object is participating in a session
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

	public MatchMakerObject getParent() {
		return parent;
	}

	public void setParent(MatchMakerObject parent) {
		MatchMakerObject oldValue = this.parent;
		this.parent = parent;
		eventSupport.firePropertyChange("parent", oldValue, this.parent);
	}

	public void setSession(MatchMakerSession matchMakerSession) {
		this.matchMakerSession = matchMakerSession;
	}

	/**
	 * Returns this object's session, if it has one, otherwise defers
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
            return getParent().getSession();
        }
    }

	public Date getCreateDate() {
		return createDate;
	}

	public boolean allowsChildren() {
		return true;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		String oldValue = this.name;
		this.name = name;
		eventSupport.firePropertyChange("name", oldValue, this.name);
	}

	public abstract boolean equals(Object obj);

	public abstract int hashCode();


	/////// Event stuff ///////

	public void addMatchMakerListener(MatchMakerListener<T, C> l) {
		eventSupport.addMatchMakerListener(l);
	}

	public void removeMatchMakerListener(MatchMakerListener<T, C> l) {
		eventSupport.removeMatchMakerListener(l);
	}

	protected MatchMakerEventSupport<T, C> getEventSupport() {
		return eventSupport;
	}

}