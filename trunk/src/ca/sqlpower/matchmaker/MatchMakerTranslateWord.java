package ca.sqlpower.matchmaker;

public class MatchMakerTranslateWord
	extends AbstractMatchMakerObject<MatchMakerTranslateWord,MatchMakerObject> {

	private Long oid;
	private String from ="";
	private String to="";
    private Long location;

	public MatchMakerTranslateWord() {
	}

	/**
	 * Return the from value.  
	 * If the from value is null return "" if it dosn't 
	 * Done this way to stop update storm in hibernate
	 */
	public String getFrom() {
		if (from == null) return "";
		return from;
	}

    /**
     * Some databases will behave badly if you have nulls nested in subselects
     * so we change null to "" otherwise this is a normal setter.
     */
	public void setFrom(String from) {
		String oldValue = this.from;
		this.from = from;
		getEventSupport().firePropertyChange("from", oldValue, this.from);
	}

	/**
	 * Return the to value.  
	 * If the from value is null return "" if it dosn't 
	 * Done this way to stop update storm in hibernate
	 */
	public String getTo() {
		if (to == null) return "";
		return to;
	}

    /**
     * Some databases will behave badly if you have nulls nested in subselects
     * so we change null to "" otherwise this is a normal setter.
     */
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
        int result = 0;
        result = PRIME * result + ((getFrom() == null) ? 0 : getFrom().hashCode());
        result = PRIME * result + ((getParent() == null) ? 0 : getParent().hashCode());
        result = PRIME * result + ((getTo() == null) ? 0 : getTo().hashCode());
        return result;
    }

	@Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof MatchMakerTranslateWord))
            return false;
        final MatchMakerTranslateWord other = (MatchMakerTranslateWord) obj;
        if (getFrom() == null) {
            if (other.getFrom() != null)
                return false;
        } else if (!getFrom().equals(other.getFrom()))
            return false;
        if (getParent() == null) {
            if (other.getParent() != null)
                return false;
        } else if (!getParent().equals(other.getParent()))
            return false;
        if (getTo() == null) {
            if (other.getTo() != null)
                return false;
        } else if (!getTo().equals(other.getTo()))
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
    @Override
    public String toString() {
    	StringBuffer buf = new StringBuffer();
    	buf.append("OID: ").append(oid);
    	buf.append(" Parent: ").append(this.getParent());
    	buf.append(" From:").append(getFrom());
    	buf.append("->To:").append(getTo());
    	buf.append(". Priority: ").append(location);
    	return buf.toString();
    }
    
    public MatchMakerTranslateWord duplicate(MatchMakerObject parent, MatchMakerSession session) {
    	MatchMakerTranslateWord w = new MatchMakerTranslateWord();
    	w.setName(getName());
    	w.setFrom(getFrom());
    	w.setTo(getTo());
    	w.setLocation(getLocation());
    	w.setParent(parent);
    	w.setSession(session);
    	return w;
    }
}
