/*
 * Copyright (c) 2008, SQL Power Group Inc.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.event.MatchMakerListener;

/**
 * This is a swing specific container that holds projects for a tree mode.
 * This is mostly a stub implementation of the MatchMakerObject.
 * <p>
 * This class is not to be used anywhere else other than the tree model as
 * it does not fire events or manage its children's parent pointers. This
 * container is only needed for the tree model to contain the projects as
 * the projects still need to be children of the default PLFolder for Hibernate
 * but we want to place the projects in different containers in the tree.
 * <p>
 * Do not use this class unless you are in the tree model and need a container
 * for projects that don't remove it from the parent default PlFolder.
 * <p>
 * It would probably be better for the DAO to handle this illusion of converting
 * the projects being in one folder to spreading them across multiple folders
 * in the tree. If the DAO did this the view and model would stay the same. 
 * However, Hibernate is harder to fool into doing this trick than a JTree
 * which is why we are doing it here.
 */
class DisconnectedTreeModelSpecificContainer<C extends MatchMakerObject> 
		implements MatchMakerObject<DisconnectedTreeModelSpecificContainer, C> {

    private static final Logger logger = Logger.getLogger(DisconnectedTreeModelSpecificContainer.class);
    
	/**
	 * The list of children this container holds.
	 */
	private List<C> childList;
	
	/**
	 * The name of this container.
	 */
	private String name;
	
	/**
	 * The parent that contains this container. The parent should involve
	 * the tree.
	 */
	private MatchMakerObject parent;
	
	/**
	 * The session that contains the tree this container belongs to.
	 */
	private MatchMakerSession session;
	
	/**
	 * This should not be made anywhere other than in the tree model in the
	 * swing UI. For more information see the class level comment.
	 */
	public DisconnectedTreeModelSpecificContainer(String name, MatchMakerSession session) {
		childList = new ArrayList<C>();
		this.name = name;
		this.session = session;
	}
	
	/**
	 * A private copy constructor used for the duplicate method.
	 */
	private DisconnectedTreeModelSpecificContainer(DisconnectedTreeModelSpecificContainer<C> container) {
		for (C child : container.getChildren()) {
			this.childList.add(child);
		}
		this.name = container.getName();
		this.parent = container.getParent();
		this.session = (MatchMakerSwingSession) container.getSession();
	}
	
	/**
	 * Adds a child object to this container but does not modify the child's parent.
	 */
	public void addChild(C child) {
		childList.add(child);
		logger.error("added child " + child + ". Child count is now " + childList.size(), new Exception());
	}

	/**
	 * Do not call this as this container is disconnected.
	 */
	public void addMatchMakerListener(MatchMakerListener l) {
		throw new IllegalStateException("There should be nothing listening to this folder!");
	}
	
	/**
	 * Do not call this as this container is disconnected.
	 */
	public void removeMatchMakerListener(MatchMakerListener l) {
		throw new IllegalStateException("There should be nothing listening to this folder!");
	}

	public boolean allowsChildren() {
		return true;
	}

	public DisconnectedTreeModelSpecificContainer<C> duplicate(MatchMakerObject parent,
			MatchMakerSession session) {
		return new DisconnectedTreeModelSpecificContainer<C>(this);
	}

	public void endCompoundEdit() {
		//no-op
	}

	public int getChildCount() {
		return childList.size();
	}

	public List<C> getChildren() {
		return Collections.unmodifiableList(childList);
	}

	public String getName() {
		return name;
	}

	public MatchMakerObject getParent() {
		return parent;
	}

	public MatchMakerSession getSession() {
		return session;
	}

	public boolean hierarchyContains(MatchMakerObject mmo) {
		if (childList.contains(mmo)) {
			return true;
		}
		for (C child : childList) {
			if (child.hierarchyContains(mmo)) {
				return true;
			}
		}
		return false;
	}

	public boolean isUndoing() {
		//no-op
		return false;
	}

	public boolean isVisible() {
		return true;
	}

	public void removeChild(C child) {
		childList.remove(child);
	}

	public void setName(String string) {
		name = string;
	}

	public void setParent(MatchMakerObject parent) {
		this.parent = parent;
	}

	public void setSession(MatchMakerSession matchMakerSession) {
		session = matchMakerSession;
	}

	public void setUndoing(boolean isUndoing) {
		//no-op
	}

	public void setVisible(boolean v) {
		//no-op
	}

	public void startCompoundEdit() {
		//no-op
	}

	public Date getCreateDate() {
		//no-op
		return null;
	}

	public String getLastUpdateAppUser() {
		//no-op
		return null;
	}

	public Date getLastUpdateDate() {
		//no-op
		return null;
	}

	public String getLastUpdateOSUser() {
		//no-op
		return null;
	}

	public void registerUpdate() {
		//no-op
	}

}
