package ca.sqlpower.matchmaker;

/**
 * group of matchmaker criteria, the child type is MatchmakerCriteria
 * matchPercent can be NULL, and it's NULL when the object just create(default constructor)
 *
 * @param <C>
 */
public class MatchMakerCriteriaGroup
	extends AbstractMatchMakerObject<MatchMakerCriteriaGroup, MatchMakerCriteria> {

	private Long oid;
	private String desc;
	private Short matchPercent;		// NULL or something from 0-100, but not guaranteed
	private String filter;			// SQL filter for process match criteria group
	private Boolean active;			// enable or disable a group for the engine

	// default constructor
	public MatchMakerCriteriaGroup( ) {
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
     * Sets the parent of this object to be the matach criteria group folder of the given match object
     * 
     * this will fire a <b>parent</b> changed event not a parent match event
     */
    public void setParentMatch(Match grandparent) {
        if (grandparent == null) {
            setParent(null);
        } else {
            setParent(grandparent.getMatchCriteriaGroupFolder());
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

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		Boolean oldValue = this.active;
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

	@Override
	public int hashCode() {
        int result = ((getName() == null) ? 0 : getName().hashCode());
        return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof MatchMakerCriteriaGroup))
			return false;
		final MatchMakerCriteriaGroup other = (MatchMakerCriteriaGroup) obj;
		if (getName() == null) {
			if (other.getName() != null)
				return false;
		} else if (!getName().equals(other.getName()))
			return false;
		return true;
	}
	
	public String createNewUniqueName() {

        StringBuffer name = new StringBuffer("new criteria");
        int i = 1;
        while ( getCriteriaByName(name.toString()) != null ) {
        	name = new StringBuffer("new criteria").append(i++);
        }
        return name.toString();
    }
	
	private MatchMakerCriteria getCriteriaByName(String name) {
		for ( MatchMakerCriteria c : getChildren() ) {
			if ( c.getName().equals(name))
				return c;
        }
		return null;
	}
}
