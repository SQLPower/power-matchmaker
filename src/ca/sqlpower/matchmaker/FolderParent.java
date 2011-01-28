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

import java.io.File;


/** 
 * A holder for folders.
 * 
 * A class that fires events and keeps the database in 
 * sync with the folder parent. 
 */
public class FolderParent extends AbstractMatchMakerObject<FolderParent, PlFolder> {
    /**
     * 
     */
    private final MatchMakerSession session;

    public FolderParent(MatchMakerSession session) {
        this.session = session;
    }
    
    /**
     * Persists the translate group as well as adding it to the child list and 
     * firing the proper events 
     */
    public void addNewChild(PlFolder child) {
        this.session.getDAO(PlFolder.class).save(child);
        super.addChild(child);
    }

    /**
     * Removes from the parent list and deletes the object from the database
     */
    public void deleteAndRemoveChild(PlFolder child) {
    	this.session.getDAO(PlFolder.class).delete(child);
		super.removeChild(child);
		MatchMakerUtils.deleteDirectory(new File(MatchMakerUtils.makeGraphDBFolderLocation(child.getName())));
	}
    
    
    

    @Override
    public boolean equals(Object obj) {
        return this==obj;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

	public FolderParent duplicate(MatchMakerObject parent, MatchMakerSession session) {
		throw new IllegalAccessError("Folder parent not duplicatable");
	}
}