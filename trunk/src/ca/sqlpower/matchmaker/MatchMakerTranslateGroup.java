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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.i 
 */

package ca.sqlpower.matchmaker;




public class MatchMakerTranslateGroup
	extends AbstractMatchMakerObject<MatchMakerTranslateGroup, MatchMakerTranslateWord> {

	private Long oid;

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((getName() == null) ? 0 : getName().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if(!(obj instanceof MatchMakerTranslateGroup)) 
            return false;
		final MatchMakerTranslateGroup other = (MatchMakerTranslateGroup) obj;
		if (getName() == null) {
			if (other.getName() != null)
				return false;
		} else if (!getName().equals(other.getName()))
			return false;
		return true;
	}

	public MatchMakerTranslateGroup() {
	}
    
    /** 
     * Set the translate words to a monitonly increasing form in the order of the parent
     * and add any newly created children to this translation group
     * 
     * Because you can get update collisions if you change the 
     * sequence numbers to one that already exists in the database, we
     * have to use a non-overlapping set.  To avoid marchig off 
     * to infinity.  We start from zero if there is enough space
     * before the current lowest sequence number to fit all words.
     * 
     * 
     */
    public void syncChildrenSeqNo(){
    	long minSeqNo = Long.MAX_VALUE;
    	long maxSeqNo = 0;
    	for (MatchMakerTranslateWord w: getChildren()) {
    		if (w.getLocation() != null){
    			minSeqNo = Math.min(minSeqNo, w.getLocation());
    			maxSeqNo = Math.max(maxSeqNo, w.getLocation());
    		}
    	}
    	long startNumber;
    	if (minSeqNo > getChildCount()){
    		startNumber = 0;
    	} else {
    		startNumber = maxSeqNo+1;
    	}
        for (Long i = 0L; i < getChildCount(); i++){
            MatchMakerTranslateWord child = getChildren().get(i.intValue());
            child.setLocation(i+startNumber);
        }
    }

	@Override
	public String toString() {
		return getName();
	}

	public MatchMakerTranslateGroup duplicate(MatchMakerObject parent, MatchMakerSession s) {
		MatchMakerTranslateGroup g = new MatchMakerTranslateGroup();
		g.setSession(s);
		g.setParent(parent);
		g.setName(this.getName());
		for (MatchMakerTranslateWord w: getChildren()){
			g.addChild(w.duplicate(g,s));
		}
		return g;
	}
}