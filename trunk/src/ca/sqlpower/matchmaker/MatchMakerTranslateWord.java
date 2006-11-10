package ca.sqlpower.matchmaker;

public class MatchMakerTranslateWord<C extends MatchMakerTranslateWord> extends AbstractMatchMakerObject<C> {

	private Long oid;
	private String from;
	private String to;

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
	public void addChild(C child) {
		throw new IllegalStateException("MatchMakerTranslateWord does not allow child!");
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
}
