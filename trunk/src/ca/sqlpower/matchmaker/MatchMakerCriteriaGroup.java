package ca.sqlpower.matchmaker;

/**
 * group of matchmaker criteria, the child type is MatchmakerCriteria
 * matchPercent can be NULL, and it's NULL when the object just create(default constructor)
 *
 * @param <C>
 */
public class MatchMakerCriteriaGroup<C extends MatchmakerCriteria> extends AbstractMatchMakerObject<C> {

	private Long oid;
	private String name;
	private String desc;
	private Long matchPercent;		// NULL or something from 0-100, but not guaranteed
	private String filter;			// SQL filter for process match criteria group
	private boolean active;			// enable or disable a group for the engine

	// default constructor
	public MatchMakerCriteriaGroup( ) {
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		String oldDesc = this.desc;
		this.desc = desc;
		getEventSupport().firePropertyChange("desc", oldDesc, desc);
	}

	public Long getMatchPercent() {
		return matchPercent;
	}

	public void setMatchPercent(Long matchPercent) {
		Long oldValue = this.matchPercent;
		this.matchPercent = matchPercent;
		getEventSupport().firePropertyChange("matchPercent", oldValue, matchPercent);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		String oldValue = this.name;
		this.name = name;
		getEventSupport().firePropertyChange("name", oldValue, name);
	}

	public boolean isActive() {
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

	@Override
	public int hashCode() {
		return oid.hashCode();
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final MatchmakerCriteria other = (MatchmakerCriteria) obj;
		if (oid == null) {
			if (other.oid != null)
				return false;
		} else if (!oid.equals(other.oid))
			return false;
		return true;
	}
}
