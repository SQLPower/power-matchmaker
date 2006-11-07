package ca.sqlpower.matchmaker;

public class MatchMakerTranslateWord<C extends MatchMakerTranslateWord> extends AbstractMatchMakerObject<C> {

	private String from;
	private String to;

	public MatchMakerTranslateWord(String appUserName) {
		super(appUserName);
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
}
