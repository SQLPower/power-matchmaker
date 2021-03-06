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

import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.munge.TranslateWordMungeStep;
import ca.sqlpower.object.ObjectDependentException;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.NonProperty;


/** 
 * A holder for translate groups
 */
public class TranslateGroupParent extends AbstractMatchMakerObject {
	
	/**
	 * Defines an absolute ordering of the child types of this class.
	 */
	/**
	 * This the list that tells us the allowable child types and their order in getting children.
	 */
	@SuppressWarnings("unchecked")
	public static final List<Class<? extends SPObject>> allowedChildTypes = 
		Collections.unmodifiableList(new ArrayList<Class<? extends SPObject>>(
				Arrays.asList(MatchMakerTranslateGroup.class)));
	
	List<MatchMakerTranslateGroup> matchMakerTranslateGroups = new ArrayList<MatchMakerTranslateGroup>();
	
    /**
     * Deletes the translate group as well as adding it to the child list and 
     * firing the proper events.  If the group is in use, it will not be deleted
     * and an IllegalStateException will be thrown.
     * <p>
     * To check if a group is in use before attempting to delete it, use
     * {@link #isInUseInBusinessModel(MatchMakerTranslateGroup)}.
     */
    public void removeAndDeleteChild(MatchMakerTranslateGroup child) {
        if(isInUseInBusinessModel(child)) {
        	throw new IllegalStateException("The translate group \""+child.getName()+"\" is in use and can't be deleted");
        }
        try {
        	super.removeChild(child);
        } catch (ObjectDependentException e) {
        	throw new RuntimeException(e);
        }
    }

    /**
     * Checks if the given translate group exists in the folder hierarchy.
     * 
     * @param tg the translate group.
     * @return true if <tt>tg</tt> exists in the folder hierarchy; false if it doesn't.
     */
    @NonProperty
    public boolean isInUseInBusinessModel(MatchMakerTranslateGroup tg) {
        for (SPObject spo : getSession().getCurrentFolderParent().getChildren()) {
        	MatchMakerObject mmo = (MatchMakerObject)spo;
            if (checkMMOContainsTranslateGroup(mmo,tg)) {
            	return true;
            }
        }
        return false;
    }

    /**
     * Recursively searches for a munge step that uses the given translate group.
     *  
     * @param mmo the object and descendants we want to check.
     * @param tg the translate group to search for. Must not be null.
     * @return true if <tt>tg</tt> is used by <tt>mmo</tt> or one of its descendants; false otherwise.
     */
    private boolean checkMMOContainsTranslateGroup(MatchMakerObject mmo,MatchMakerTranslateGroup tg){
        if (mmo instanceof TranslateWordMungeStep) {
            TranslateWordMungeStep step = (TranslateWordMungeStep) mmo;
            if (tg.equals(step.getTranslateGroup())) {
                return true;
            }
        } else if (mmo instanceof MungeStep || mmo instanceof TranslateGroupParent) {
        	return false;
        } else if (mmo instanceof Project) {
            Project projectChild = (Project) mmo;
            for (MungeProcess critGroup : projectChild.getMungeProcesses()) {
                if (checkMMOContainsTranslateGroup((MatchMakerObject)critGroup, tg)) return true;
            }
        } else {
            for (SPObject spo : mmo.getChildren()) {
            	MatchMakerObject child = (MatchMakerObject) spo;
                if (checkMMOContainsTranslateGroup(child, tg)) return true;
            }
        }
        
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        return this==obj;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

	public TranslateGroupParent duplicate(MatchMakerObject parent) {
		TranslateGroupParent g = new TranslateGroupParent();
		g.setParent(parent);
		g.setName(this.getName());
		g.setVisible(isVisible());
		int i = 0;
		for (MatchMakerTranslateGroup w: getChildren(MatchMakerTranslateGroup.class)){
			MatchMakerTranslateGroup duplicate = w.duplicate(g);
			g.addChild(duplicate, i);
			i++;
		}
		return g;
	}

	@NonProperty
	public List<? extends SPObject> getChildren() {
		return Collections.unmodifiableList(matchMakerTranslateGroups);
	}
	
	@NonProperty
	public List<MatchMakerTranslateGroup> getTranslateGroups() {
		return Collections.unmodifiableList(matchMakerTranslateGroups);
	}
	
	public void addChild(SPObject spo) {
		addChild(spo, matchMakerTranslateGroups.size());
	}
	
	@Override
	protected void addChildImpl(SPObject child, int index) {
		matchMakerTranslateGroups.add(index, (MatchMakerTranslateGroup)child);
		fireChildAdded(MatchMakerTranslateGroup.class, child, index);
	}
	
	@Override
	protected boolean removeChildImpl(SPObject child) {
		int index = matchMakerTranslateGroups.indexOf(child);
		boolean removed = matchMakerTranslateGroups.remove(child);
		fireChildRemoved(MatchMakerTranslateGroup.class, child, index);
		return removed;
	}

	@NonProperty
	public List<Class<? extends SPObject>> getAllowedChildTypes() {
		return allowedChildTypes;
	}
}