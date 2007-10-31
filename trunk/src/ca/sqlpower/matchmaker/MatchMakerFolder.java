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

/**
 * A container class desigend to hold match maker objects
 */
public class MatchMakerFolder<C extends MatchMakerObject>
	extends AbstractMatchMakerObject<MatchMakerFolder, C> {

    private String folderDesc;

	public MatchMakerFolder( ) {
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




}
