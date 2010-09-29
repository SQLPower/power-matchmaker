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

import ca.sqlpower.object.SPObject;
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
				Arrays.asList(FolderParent.class,  TranslateGroupParent.class)));
    
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
    
    
    public MMRootNode(MatchMakerSession session) {
        setName("Root Node");
        currentFolderParent = new FolderParent(session);
        currentFolderParent.setName("Current Projects");
        currentFolderParent.setParent(this);
        backupFolderParent = new FolderParent(session);
        backupFolderParent.setName("Backup Projects");
        backupFolderParent.setParent(this);
        tgp = new TranslateGroupParent(session);
        tgp.setName("Translation Groups");
    }
    
    @NonProperty
	public boolean isRoot() {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    public MMRootNode duplicate(MatchMakerObject parent, MatchMakerSession session) {
        throw new UnsupportedOperationException("MMTreeNodes cannot be duplicated");
    }

	@Override
	public List<? extends SPObject> getChildren() {
		return Collections.unmodifiableList(Arrays.asList(currentFolderParent, backupFolderParent, tgp));
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
