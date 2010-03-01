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

package ca.sqlpower.matchmaker;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * A container class designed to hold match maker objects when a parent
 * has to have children of multiple types.  Since this isn't allowed by
 * the general MatchMakerObject contract, the parent can have a folder
 * child for each type of child it needs, and each folder will hold the
 * children of that type.
 */
public class MatchMakerFolder<C extends MatchMakerObject>
	extends AbstractMatchMakerObject<MatchMakerFolder, C> {

	private static final Logger logger = Logger.getLogger(MatchMakerFolder.class);
	
    private String folderDesc;
    
    /**
     * The class of child objects held by this folder.
     */
    private final Class<C> childClass;
    
	public MatchMakerFolder(Class<C> childClass) {
		this.childClass = childClass;
	}

	public String getFolderDesc() {
		return folderDesc;
	}
	
	public void setFolderDesc(String folderDesc) {
		String oldValue = this.folderDesc;
		this.folderDesc = folderDesc;
		getEventSupport().firePropertyChange("folderDesc", oldValue, folderDesc);
	}

	@Override
	public int hashCode() {
		return (getName() == null) ? 0 : getName().hashCode();
	}

	/**
	 * Compares to another folder by folder name only.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if ( !(obj instanceof MatchMakerFolder) ) {
			return false;
		}
		final MatchMakerFolder other = (MatchMakerFolder) obj;
		return (getName() == null) ?
				other.getName() == null :
				getName().equals(other.getName());
	}

	@Override
	public String toString() {
		return "Folder \""+getName()+"\"";
	}
	
	/**
	 * This method should not be used.  The project object is expected to perform
	 * the duplication of its folders.
	 */
	public MatchMakerFolder duplicate(MatchMakerObject parent, MatchMakerSession s) {
		throw new RuntimeException("The match maker folder should never be duplicated.  It should be managed by the Match object");
	}
	
	/**
	 * Inserts the given child object at the given position of this
	 * MatchMakerObject's child list, then fires an event indicating the
	 * insertion took place. It includes a flag which it adds
	 * to the childInserted event to signal to event listeners as to
	 * whether or not this addChild action is part of a compound edit
	 * <p>

	 * @param index
	 *            The position to insert the child at. 0 inserts the child at
	 *            the beginning of the list. The given index must be at least 0
	 *            but not more than the current child count of this object.
	 * @param child
	 *            The child object to add. Must not be null.
	 * @param isCompound
	 *            True if this addChild call is part of a compound event. False
	 *            if it is not.
	 */
	public final void addChild(int index, C child, boolean isCompound) {
        addImpl(index, child, isCompound);
	}
	
	protected void addImpl(int index, C child, boolean isCompound) {
		logger.debug("addChild: children collection is a "+getChildren().getClass().getName());
        if(child== null) throw new NullPointerException("Cannot add a null child");
		getChildren().add(index, child);
		child.setParent(this);
		List<C> insertedChildren = new ArrayList<C>();
		insertedChildren.add(child);
		getEventSupport().fireChildrenInserted("children", new int[] {index}, insertedChildren, isCompound);
	}
	
	@Override
	public void moveChild(int from, int to) {
		if (to == from) return;
		final List<C> l = getChildren();
		C child = l.get(from);
		try {
			startCompoundEdit();
			removeChild(l.get(from));
			addChild(to, child, true);
		} finally {
			endCompoundEdit();
		}
	}
}
