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

import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.Mutator;
import ca.sqlpower.object.annotation.NonProperty;

/**
 * A container class desigend to hold match maker objects (for now),
 * we need to make it generic to hold other sqlPower database objects like Power Loader
 * jobs and transactions. Then the child type will be ca.sqlpower.sql.DatabaseObject and
 * this class can be relocated to the <tt>ca.sqlpower.sql</tt> package.
 *
 * <p>All setter methods in this class fire the appropriate events.
 */
public class PlFolder extends AbstractMatchMakerObject {

	/**
	 * This the list that tells us the allowable child types and their order in getting children.
	 */
	@SuppressWarnings("unchecked")
	public static final List<Class<? extends SPObject>> allowedChildTypes = 
		Collections.unmodifiableList(new ArrayList<Class<? extends SPObject>>(
				Arrays.asList(Project.class)));
	
	private final List<Project> projects = new ArrayList<Project>();
	
	/**
	 * The object id
	 */
	private Long oid;
	
	/**
	 * This folder's description.
	 */
    private String folderDesc;

    /**
     * XXX I don't know what this property is for.
     */
    private String folderStatus;

    /**
     * The last number assigned to a backup of this folder. This will be incremented by
     * 1 every time a new backup is made.
     *
     * <p>XXX we'd like to do away with this property and just determine the correct next
     * backup number by searching the database.
     */
    private Long lastBackupNo = 0L;

    /**
     * Creates a new folder with a null name.  You'll have to call setName()
     * before expecting the folder to do much useful stuff.
     */
    @Constructor
	public PlFolder() {
		this(null);
	}

	/**
     * Creates a new folder with the specified name.
	 */
	public PlFolder(String name){
		setName(name);
	}

	@Accessor
	public String getFolderDesc() {
		return folderDesc;
	}

	@Mutator
	public void setFolderDesc(String folderDesc) {
		String oldValue = this.folderDesc;
		this.folderDesc = folderDesc;
		firePropertyChange("folderDesc", oldValue, folderDesc);
	}

	@Accessor
	public String getFolderStatus() {
		return folderStatus;
	}

	@Mutator
	public void setFolderStatus(String folderStatus) {
		String oldValue = this.folderStatus;
		this.folderStatus = folderStatus;
		firePropertyChange("folderStatus", oldValue, folderStatus);
	}

	@Accessor
	public Long getLastBackupNo() {
		return lastBackupNo;
	}

	@Mutator
	public void setLastBackupNo(Long lastBackupNo) {
		long oldValue = this.lastBackupNo;
		this.lastBackupNo = lastBackupNo;
		firePropertyChange("lastBackupNo", oldValue, lastBackupNo);
	}


	public int hashCode() {
		if (getName() == null){ 
			return 0;
		} else {
			return getName().hashCode();
		}
	}


	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final PlFolder other = (PlFolder) obj;
		if (getName() == null) {
			if (other.getName() != null)
				return false;
		} else if (!getName().equals(other.getName()))
			return false;
		return true;
	}

	public PlFolder duplicate(MatchMakerObject parent) {
		PlFolder f = new PlFolder();
		f.setName(getName());
		f.setFolderDesc(getFolderDesc());
		f.setLastBackupNo(getLastBackupNo());
		f.setFolderStatus(getFolderStatus());
		f.setParent(parent);
		f.setVisible(isVisible());
		for (SPObject spo: getChildren()) {
			Project pr = (Project)spo;
			f.addChild(pr.duplicate(parent));
		}
		return f;
	}

	@NonProperty
	public Long getOid() {
		return oid;
	}

	@Override
	@NonProperty
	public List<? extends SPObject> getChildren() {
		return Collections.unmodifiableList(projects);
	}
	
	public void addChild(SPObject spo) {
		addChild(spo, projects.size());
	}
	
	@Override
	protected void addChildImpl(SPObject spo, int index) {
		projects.add(index,(Project)spo);
		fireChildAdded(Project.class, spo, index);
	}
	
	@Override
	protected boolean removeChildImpl(SPObject spo) {
		int index = projects.indexOf(spo);
		boolean removed = projects.remove(spo);
		fireChildRemoved(Project.class, spo, index);
		return removed;
	}

	@Override
	@NonProperty
	public List<Class<? extends SPObject>> getAllowedChildTypes() {
		return allowedChildTypes;
	}
	
	@NonProperty
	public List<Project> getProjects() {
		return Collections.unmodifiableList(projects);
	}
}
