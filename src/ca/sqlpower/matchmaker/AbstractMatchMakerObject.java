package ca.sqlpower.matchmaker;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ca.sqlpower.matchmaker.event.MatchMakerEventSupport;
import ca.sqlpower.matchmaker.event.MatchMakerListener;

public abstract class AbstractMatchMakerObject<C extends MatchMakerObject> implements MatchMakerObject<C> {

	private MatchMakerEventSupport<MatchMakerObject<C>,C> eventSupport =
		new MatchMakerEventSupport<MatchMakerObject<C>,C>(this);
	private List<C> children = new ArrayList<C>();
	private String lastUpdateAppUser;
	private String lastUpdateOsUser;
	private Date lastUpdateDate;

	
	public AbstractMatchMakerObject(String appUserName) {
		lastUpdateAppUser = appUserName;
	}

	public void addChild(C child) {
		children.add(child);
	}

	public void addMatchMakerListener(MatchMakerListener<MatchMakerObject<C>,C> l) {
		eventSupport.addMatchMakerListener(l);

	}
	
	protected MatchMakerEventSupport<MatchMakerObject<C>,C> getEventSupport() {
		return eventSupport;
	}

	public int getChildCount() {
		return children.size();
	}

	public List<C> getChildren() {
		return children;
	}

	public void removeChild(C child) {
		children.remove(child);
	}

	public void removeMatchMakerListener(MatchMakerListener<MatchMakerObject<C>,C> l) {
		eventSupport.removeMatchMakerListener(l);
	}

	public String getLastUpdateAppUser() {
		return lastUpdateAppUser;
	}
	public String getLastUpdateOSUser() {
		return lastUpdateOsUser;
	}
	public Date getLastUpdateDate() {
		return lastUpdateDate;
	}
	public void registerUpdate() {
		lastUpdateDate = new Date(System.currentTimeMillis());
		lastUpdateOsUser = System.getProperty("user.name");
		// TODO: update app user
	}
}
