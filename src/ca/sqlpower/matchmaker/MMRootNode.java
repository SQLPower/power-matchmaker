/*
 * Copyright (c) 2010, SQL Power Group Inc.
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ca.sqlpower.enterprise.client.Group;
import ca.sqlpower.enterprise.client.User;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.ConstructorParameter;
import ca.sqlpower.object.annotation.ConstructorParameter.ParameterType;
import ca.sqlpower.object.annotation.NonProperty;

/**
 * A root node to be at the top of the tree of all AbstractMatchMaker objects.
 */
public class MMRootNode extends AbstractMatchMakerObject {
	/**
     * List of allowable child types
     */
    @SuppressWarnings("unchecked")
	public static final List<Class<? extends SPObject>> allowedChildTypes = 
		Collections.unmodifiableList(new ArrayList<Class<? extends SPObject>>(
				Arrays.asList(FolderParent.class,  TranslateGroupParent.class, 
						User.class, Group.class)));
    
    /**
     * This is the folder that holds all the current projects in the tree.
     */
    private final FolderParent currentFolderParent;
    
    /**
     * This is the folder that holds all of the backup projects in the tree.
     */
    private final FolderParent backupFolderParent;

    /**
     * This is the folder that holds all the translate groups in the tree.
     * The translate groups each hold a bunch of translations.
     */
    private final TranslateGroupParent tgp;
    
    /**
     * This is the list of users in the system workspace and is only used as such. This
     * list can be ignored when this is a normal MMRootNode.
     */
    private final List<User> users = new ArrayList<User>();
    
    /**
     * This is the list of groups in the system workspace and is only used as such. This
     * list can be ignored when this is a normal MMRootNode.
     */
    private final List<Group> groups = new ArrayList<Group>();
    
    /**
     * The session for this root node must be set immediately after creating it.
     */
    public MMRootNode() {
        setName("Root Node");
        currentFolderParent = new FolderParent();
        currentFolderParent.setName("Current Projects");
        currentFolderParent.setParent(this);
        backupFolderParent = new FolderParent();
        backupFolderParent.setName("Backup Projects");
        backupFolderParent.setParent(this);
        tgp = new TranslateGroupParent();
        tgp.setName("Translation Groups");
        tgp.setParent(this);
    }
    
    @Constructor
    public MMRootNode(@ConstructorParameter(parameterType=ParameterType.CHILD, propertyName="currentFolderParent") FolderParent currentFolderParent,
    		@ConstructorParameter(parameterType=ParameterType.CHILD, propertyName="backupFolderParent") FolderParent backupFolderParent, 
    		@ConstructorParameter(parameterType=ParameterType.CHILD, propertyName="tgp") TranslateGroupParent tgp) {
    	this.currentFolderParent = currentFolderParent;
    	this.currentFolderParent.setParent(this);
    	this.backupFolderParent = backupFolderParent;
    	this.backupFolderParent.setParent(this);
    	this.tgp = tgp;
    	this.tgp.setParent(this);
    }
    
    @Override
    protected void addChildImpl(SPObject child, int index) {
    	if (child instanceof User) {
    		addUser((User) child, index);
    	} else if (child instanceof Group) {
    		addGroup((Group) child, index);
    	} else {
    		super.addChildImpl(child, index);
    	}
    }
    
    private void addUser(User user, int index) {
    	users.add(index, user);
    	user.setParent(this);
    	fireChildAdded(User.class, user, index);
    }
    
    private void addGroup(Group group, int index) {
    	groups.add(index, group);
    	group.setParent(this);
    	fireChildAdded(Group.class, group, index);
    }
    
    @Override
    protected boolean removeChildImpl(SPObject child) {
    	if (child instanceof User) {
    		return removeUser((User) child);
    	} else if (child instanceof Group) {
    		return removeGroup((Group) child);
    	} else {
    		return super.removeChildImpl(child);
    	}
    }

	private boolean removeGroup(Group child) {
		boolean removed;
		int index = groups.indexOf(child);
		removed = groups.remove(child);
		if (removed) {
			fireChildRemoved(Group.class, child, index);
		}
		return removed;
	}

	private boolean removeUser(User child) {
		boolean removed;
		int index = users.indexOf(child);
		removed = users.remove(child);
		if (removed) {
			fireChildRemoved(User.class, child, index);
		}
		return removed;
	}

	@Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    public MMRootNode duplicate(MatchMakerObject parent) {
        throw new UnsupportedOperationException("MMTreeNodes cannot be duplicated");
    }

	@Override
	public List<SPObject> getChildren() {
		List<SPObject> children = new ArrayList<SPObject>();
		if (currentFolderParent != null) {
			children.add(currentFolderParent);
		}
		if (backupFolderParent != null) {
			children.add(backupFolderParent);
		}
		if (tgp != null) {
			children.add(tgp);
		}
		children.addAll(users);
		children.addAll(groups);
		return children;
	}

	@Override
	@NonProperty
	public List<Class<? extends SPObject>> getAllowedChildTypes() {
		return allowedChildTypes;
	}

	@NonProperty
	public TranslateGroupParent getTranslateGroupParent() {
		return tgp;
	}

	@NonProperty
	public FolderParent getCurrentFolderParent() {
		return currentFolderParent;
	}

	@NonProperty
	public FolderParent getBackupFolderParent() {
		return backupFolderParent;
	}
}
