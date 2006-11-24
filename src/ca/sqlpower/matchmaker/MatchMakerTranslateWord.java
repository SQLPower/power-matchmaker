package ca.sqlpower.matchmaker;

public class MatchMakerTranslateWord
	extends AbstractMatchMakerObject<MatchMakerTranslateWord,MatchMakerObject> {

	private Long oid;
	private String from ="";
	private String to="";
    private Long location;

	public MatchMakerTranslateWord() {
	}

	public String getFrom() {
		return from;
	}

    /**
     * Some databases will behave badly if you have nulls nested in subselects
     * so we change null to "" otherwise this is a normal setter.
     */
	public void setFrom(String from) {
        if (from == null){
            from = "";
        }
		String oldValue = this.from;
		this.from = from;
		getEventSupport().firePropertyChange("from", oldValue, this.from);
	}

	public String getTo() {
		return to;
	}

    /**
     * Some databases will behave badly if you have nulls nested in subselects
     * so we change null to "" otherwise this is a normal setter.
     */
	public void setTo(String to) {
        if (to == null){
            to = "";
        }
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
        int result = 0;
        result = PRIME * result + ((from == null) ? 0 : from.hashCode());
        result = PRIME * result + ((getParent() == null) ? 0 : getParent().hashCode());
        result = PRIME * result + ((to == null) ? 0 : to.hashCode());
        return result;
    }

	@Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof MatchMakerTranslateWord))
            return false;
        final MatchMakerTranslateWord other = (MatchMakerTranslateWord) obj;
        if (from == null) {
            if (other.from != null)
                return false;
        } else if (!from.equals(other.from))
            return false;
        if (getParent() == null) {
            if (other.getParent() != null)
                return false;
        } else if (!getParent().equals(other.getParent()))
            return false;
        if (to == null) {
            if (other.to != null)
                return false;
        } else if (!to.equals(other.to))
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
