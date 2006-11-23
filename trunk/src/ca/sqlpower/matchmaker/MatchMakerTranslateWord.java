package ca.sqlpower.matchmaker;

public class MatchMakerTranslateWord
	extends AbstractMatchMakerObject<MatchMakerTranslateWord,MatchMakerObject> {

	private Long oid;
	private String from;
	private String to;
    private Long location;

	public MatchMakerTranslateWord() {
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		String oldValue = this.from;
		this.from = from;
		getEventSupport().firePropertyChange("from", oldValue, this.from);
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		String oldValue = this.to;
		this.to = to;
		getEventSupport().firePropertyChange("to", oldValue, this.to);
	}

	@Override
	public void addChild(MatchMakerObject child) {
		throw new IllegalStateException("MatchMakerTranslateWord does not allow child!");
	}
    @Override
    public boolean allowsChildren() {
        return false;
    }

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((oid == null) ? 0 : oid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final MatchMakerTranslateWord other = (MatchMakerTranslateWord) obj;
		if (oid == null) {
			if (other.oid != null)
				return false;
		} else if (!oid.equals(other.oid))
			return false;
		return true;
	}

    public Long getLocation() {
        return location;
    }

    public void setLocation(Long location) {
        final Long oldValue = this.location;
        this.location = location;
        getEventSupport().firePropertyChange("location", oldValue, this.location);
    }
}
