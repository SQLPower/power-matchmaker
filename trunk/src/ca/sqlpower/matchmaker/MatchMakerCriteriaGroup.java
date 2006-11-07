package ca.sqlpower.matchmaker;


public class MatchMakerCriteriaGroup extends AbstractMatchMakerObject<MatchmakerCriteria> {

	private String name;
	private String desc;
	private long matchPercent;
	private String filter;
	private boolean active;
	
	public MatchMakerCriteriaGroup(String appUserName) {
		super(appUserName);
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		String oldDesc = this.desc;
		this.desc = desc;
		getEventSupport().firePropertyChange("desc", oldDesc, desc);
	}

	public long getMatchPercent() {
		return matchPercent;
	}

	public void setMatchPercent(long matchPercent) {
		long oldValue = this.matchPercent;
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

}
