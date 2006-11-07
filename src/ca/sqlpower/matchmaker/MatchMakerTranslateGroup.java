package ca.sqlpower.matchmaker;


public class MatchMakerTranslateGroup<C extends MatchMakerTranslateWord> extends AbstractMatchMakerObject<C> {

	private String name;

	public MatchMakerTranslateGroup(String appUserName) {
		super(appUserName);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		String oldValue = this.name;
		this.name = name;
		getEventSupport().firePropertyChange("name", oldValue, this.name);
	}


}
