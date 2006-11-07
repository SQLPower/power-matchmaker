package ca.sqlpower.matchmaker;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ca.sqlpower.matchmaker.event.MatchMakerEvent;
import ca.sqlpower.matchmaker.event.MatchMakerEventSupport;
import ca.sqlpower.matchmaker.event.MatchMakerListener;

/**
 * The abstract class of MatchMakerObject, it has a listener listens to the change
 * of children, properties and structure, any thing changed in the object will
 * cause auditing information changes.
 *
 * @param <C>
 */
public abstract class AbstractMatchMakerObject<C extends MatchMakerObject>
	implements MatchMakerObject<C> {

	private MatchMakerEventSupport<MatchMakerObject,C> eventSupport =
		new MatchMakerEventSupport<MatchMakerObject,C>(this);
	private List<C> children = new ArrayList<C>();
	private String lastUpdateAppUser;
	private String lastUpdateOsUser;
	private Date lastUpdateDate;
	private String appUserName;


	public AbstractMatchMakerObject(String appUserName) {
		this.appUserName = appUserName;
		eventSupport.addMatchMakerListener(new MatchMakerListener<MatchMakerObject,C>(){

			public void mmPropertyChanged(MatchMakerEvent<MatchMakerObject, C> evt) {
				registerUpdate();
			}

			public void mmChildrenInserted(MatchMakerEvent<MatchMakerObject, C> evt) {
				registerUpdate();
			}

			public void mmChildrenRemoved(MatchMakerEvent<MatchMakerObject, C> evt) {
				registerUpdate();
			}

			public void mmStructureChanged(MatchMakerEvent<MatchMakerObject, C> evt) {
				registerUpdate();
			}});
	}

	/**
	 * anyone who going to overwrite this method should fire the childrenInserted
	 * event in the overwriten method
	 * @param child
	 */
	public void addChild(C child) {
		children.add(child);
		int [] insertedIndices = {children.size()};
		List<C> insertedChildren = new ArrayList<C>();
		insertedChildren.add(child);
		eventSupport.fireChildrenInserted("children",insertedIndices,insertedChildren);
	}

	public void addMatchMakerListener(MatchMakerListener<MatchMakerObject ,C> l) {
		eventSupport.addMatchMakerListener(l);

	}


	protected MatchMakerEventSupport<MatchMakerObject,C> getEventSupport() {
		return eventSupport;
	}

	public int getChildCount() {
		return children.size();
	}

	public List<C> getChildren() {
		return children;
	}

	/**
	 * anyone who going to overwrite this method should fire the ChildrenRemoved
	 * event in the overwriten method
	 * @param child
	 */
	public void removeChild(C child) {
		int [] removedIndices = {children.indexOf(child)};
		List<C> removedChildren = new ArrayList<C>();
		removedChildren.add(child);
		children.remove(child);
		eventSupport.fireChildrenRemoved("children",removedIndices,removedChildren);
	}

	public void removeMatchMakerListener(MatchMakerListener<MatchMakerObject ,C> l) {
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
		lastUpdateAppUser = appUserName;
	}
}
