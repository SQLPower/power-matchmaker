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

import java.awt.Color;

/**
 * A set of match rules, the child type is MatchRule
 * matchPercent can be NULL, and it's NULL when the object just create(default constructor)
 *
 * @param <C>
 */
public class MatchRuleSet
	extends AbstractMatchMakerObject<MatchRuleSet, MatchRule> {
	
	/**
	 * This is the name given to a rule set made by the Match Maker
	 * to identify any new {@link PotentialMatchRecord} that are also created by
	 * the Match Maker when previously unconnected nodes are defined to be
	 * related.
	 */
	public static final String SYNTHETIC_MATCHES = "Synthetic_matches";

	private Long oid;
	private String desc;
    
    /**
     * The user's confidence that this group of match rule produces
     * actual matches.  Will usually be NULL or in the range 0-100, but
     * this range is not enforced.
     */
	private Short matchPercent;
    
    /**
     * A SQL Where fragment that restricts this set of rules to a subset
     * of the whole match table.
     */
	private String filter;
    
    /**
     * Marks whether or not the match engine should process this match group when
     * running a match process.  True means it will be processed.
     */
	private boolean active = true;
    
    /**
     * The colour associated with this group of rule by the user.  Useful
     * in the GUI.
     */
	private Color colour;
    
	/**
     * Constructor that sets up a default match group.
	 */
	public MatchRuleSet() {
	}

    public Long getOid() {
        return oid;
    }

    public void setOid(Long oid) {
        this.oid = oid;
    }
    
    /**
     * Gets the grandparent of this object in the MatchMaker object tree.  If the parent
     * (a folder) is null, returns null.
     */
    public Match getParentMatch() {
        MatchMakerObject parentFolder = getParent();
        if (parentFolder == null) {
            return null;
        } else {
            return (Match) parentFolder.getParent();
        }
    }

    /**
     * Sets the parent of this object to be the rule set folder of the given match object
     *
     * this will fire a <b>parent</b> changed event not a parent match event
     */
    public void setParentMatch(Match grandparent) {
        if (grandparent == null) {
            setParent(null);
        } else {
            setParent(grandparent.getMatchRuleSetFolder());
        }
    }

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		String oldDesc = this.desc;
		this.desc = desc;
		getEventSupport().firePropertyChange("desc", oldDesc, desc);
	}

	public Short getMatchPercent() {
		return matchPercent;
	}

	public void setMatchPercent(Short matchPercent) {
        Short oldValue = this.matchPercent;
		this.matchPercent = matchPercent;
		getEventSupport().firePropertyChange("matchPercent", oldValue, matchPercent);
	}

	public boolean getActive() {
		return active;
	}

	public void setActive(boolean active) {
		boolean oldValue = this.active;
		this.active = active;
		getEventSupport().firePropertyChange("active", oldValue, active);
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		String oldValue = this.filter;
		this.filter = filter;
		getEventSupport().firePropertyChange("filter", oldValue, filter);
	}

    public Color getColour() {
        return colour;
    }
    
    public void setColour(Color groupColor) {
        Color oldValue = this.colour;
        this.colour = groupColor;
        getEventSupport().firePropertyChange("colour", oldValue, groupColor);
    }
    
	@Override
	public int hashCode() {
        int result = ((getName() == null) ? 0 : getName().hashCode());
        return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof MatchRuleSet))
			return false;
		final MatchRuleSet other = (MatchRuleSet) obj;
		if (getName() == null) {
			if (other.getName() != null)
				return false;
		} else if (!getName().equals(other.getName()))
			return false;
		return true;
	}

	public String createNewUniqueName() {

        StringBuffer name = new StringBuffer("new rule");
        int i = 1;
        while ( getRuleByName(name.toString()) != null ) {
        	name = new StringBuffer("new rule").append(i++);
        }
        return name.toString();
    }

	private MatchRule getRuleByName(String name) {
		for ( MatchRule c : getChildren() ) {
			if ( c.getName().equals(name))
				return c;
        }
		return null;
	}

	/**
	 * duplicate all the properties of the MatchRule group 
	 * and it's children, except oid and parent
     * 
	 * @return new matchmaker group object with the same properties
	 * and children
	 */
	public MatchRuleSet duplicate(MatchMakerObject parent, MatchMakerSession s){
		MatchRuleSet group = new MatchRuleSet();
		group.setActive(getActive());
		group.setDesc(getDesc()==null?null:new String(getDesc()));
		group.setFilter(getFilter()==null?null:new String(getFilter()));
        group.setColour(getColour() == null ? null : new Color(getColour().getRGB()));
		group.setMatchPercent(getMatchPercent()==null?null:new Short(getMatchPercent()));
		group.setName(getName()==null?null:new String(getName()));
		group.setSession(s);
		
		for ( MatchRule rule : getChildren()) {
			MatchRule newRule = rule.duplicate(group,s);
			group.addChild(newRule);
		}
		return group;
	}
}
