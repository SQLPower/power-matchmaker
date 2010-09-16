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





public class MatchMakerTranslateGroup
	extends AbstractMatchMakerObject 
	implements Comparable <MatchMakerTranslateGroup> {

    /**
     * Object identifier. Required for the persistence layer.
     */
    @SuppressWarnings("unused")
	private Long oid;
    
    /**
     * Returns this translate group's peristent Object Identifier.  This get method
     * should probably be declared in the AbstractMatchMakerObject, but I just need it
     * here right now.
     */
    public Long getOid() {
        return oid;
    }

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
     * Constructor that is only useful for unit tests which require a translate
     * group with a non-null OID.  If you use this in the real app, it will probably
     * break the ORM layer.
     * 
     * @param initialOid The initial OID to assign to the new instance.
     */
    MatchMakerTranslateGroup(Long initialOid) {
        this.oid = initialOid;
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
		g.setVisible(isVisible());
		for (MatchMakerTranslateWord w: getChildren()){
			g.addChild(w.duplicate(g,s));
		}
		return g;
	}

	public int compareTo(MatchMakerTranslateGroup o) {
		return getName().compareTo(((MatchMakerTranslateGroup) o).getName());
	}

}