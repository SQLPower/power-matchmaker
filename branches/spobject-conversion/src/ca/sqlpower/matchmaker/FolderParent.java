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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ca.sqlpower.object.ObjectDependentException;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.NonProperty;


/** 
 * A holder for folders.
 * 
 * A class that fires events and keeps the database in 
 * sync with the folder parent. 
 */
public class FolderParent extends AbstractMatchMakerObject {
	
	/**
	 * This the list that tells us the allowable child types and their order in getting children.
	 */
	@SuppressWarnings("unchecked")
	public static final List<Class<? extends SPObject>> allowedChildTypes = 
		Collections.unmodifiableList(new ArrayList<Class<? extends SPObject>>(
				Arrays.asList(PlFolder.class)));
    
    List<PlFolder> plFolders = new ArrayList<PlFolder>();

    public void addChild(SPObject spo) {
    	addChild(spo, plFolders.size());
    }
    
    @Override
    protected void addChildImpl(SPObject spo, int index) {
    	plFolders.add(index, (PlFolder)spo);
    	spo.setParent(this);
    	fireChildAdded(PlFolder.class, spo, index);
    }
    
    @Override
    protected boolean removeChildImpl(SPObject spo) {
    	int index = plFolders.indexOf(spo);
    	boolean removed = plFolders.remove(spo);
    	fireChildRemoved(PlFolder.class, spo, index);
    	return removed;
    }
    
    /**
     * Persists the translate group as well as adding it to the child list and 
     * firing the proper events 
     */
    public void addNewChild(PlFolder child) {
        this.getSession().getDAO(PlFolder.class).save(child);
        addChild(child);
    }

    /**
     * Removes from the parent list and deletes the object from the database
     */
    public void deleteAndRemoveChild(PlFolder child) {
    	this.getSession().getDAO(PlFolder.class).delete(child);
		try {
			removeChild(child);
		} catch (ObjectDependentException e) {
			throw new RuntimeException();
		}
	}
    
    @Override
    public boolean equals(Object obj) {
        return this==obj;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

	public FolderParent duplicate(MatchMakerObject parent) {
		throw new IllegalAccessError("Folder parent not duplicatable");
	}

	@Override
	@NonProperty
	public List<SPObject> getChildren() {
		List<SPObject> children = new ArrayList<SPObject>();
		children.addAll(plFolders);
		return Collections.unmodifiableList(children);
	}

	@Override
	@NonProperty
	public List<Class<? extends SPObject>> getAllowedChildTypes() {
		return allowedChildTypes;
	}

	@NonProperty
	public List<PlFolder> getPlFolders() {
		return Collections.unmodifiableList(plFolders);
	}
}