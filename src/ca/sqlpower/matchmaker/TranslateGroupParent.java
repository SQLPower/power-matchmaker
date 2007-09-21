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
 * A holder for translate groups
 */
public class TranslateGroupParent extends AbstractMatchMakerObject<TranslateGroupParent, MatchMakerTranslateGroup> {
    /**
     * 
     */
    private final MatchMakerSession session;

    public TranslateGroupParent(MatchMakerSession session) {
        this.session = session;
        
    }
	
    /**
	 * Fires the event for a structure change if children order has been changed.
	 */
	public void childrenOrderChanged() {
		getEventSupport().fireStructureChanged();
	}
	
    /**
     * Persists the translate group as well as adding it to the child list and 
     * firing the proper events 
     */
    public void addNewChild(MatchMakerTranslateGroup child) {
        this.session.getDAO(MatchMakerTranslateGroup.class).save(child);
        super.addChild(child);
    }

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
        this.session.getDAO(MatchMakerTranslateGroup.class).delete(child);
        super.removeChild(child);
    }

    /**
     * Checks if the given translate group exists in the folder hierarchy.
     * 
     * @param tg the translate group.
     * @return true if <tt>tg</tt> exists in the folder hierarchy; false if it doesn't.
     */
    public boolean isInUseInBusinessModel(MatchMakerTranslateGroup tg) {
        for (MatchMakerObject mmo : session.getCurrentFolderParent().getChildren()){
            if (checkMMOContainsTranslateGroup(mmo,tg)) {
            	return true;
            }
        }
        return false;
    }

    /**
     * Recursively searches for a match rule using the given translate group.
     *  
     * @param mmo the object and descendants we want to check.
     * @param tg the translate group to search for. Must not be null.
     * @return true if <tt>tg</tt> is used by <tt>mmo</tt> or one of its descendants; false otherwise.
     */
    private boolean checkMMOContainsTranslateGroup(MatchMakerObject mmo,MatchMakerTranslateGroup tg){
        if (mmo instanceof MatchRule){
            MatchRule rule = (MatchRule) mmo;
            if (tg.equals(rule.getTranslateGroup())) {
                return true;
            }
        }
        if (mmo instanceof Match) {
            Match matchChild = (Match) mmo;
            for (MatchRuleSet critGroup : matchChild.getMatchRuleSets()) {
                if (checkMMOContainsTranslateGroup(critGroup, tg)) return true;
            }
        } else {
            for (int i = 0; i< mmo.getChildCount(); i++){
                MatchMakerObject child = (MatchMakerObject) mmo.getChildren().get(i);
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

	public TranslateGroupParent duplicate(MatchMakerObject parent, MatchMakerSession session) {
		throw new IllegalAccessError("Translate group not duplicatable");
	}
}