package ca.sqlpower.matchmaker;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.event.MatchMakerEvent;
import ca.sqlpower.matchmaker.event.MatchMakerEventSupport;
import ca.sqlpower.matchmaker.event.MatchMakerListener;

/**
 * The abstract class of MatchMakerObject, it has a listener listens to the change
 * of children, properties and structure, any thing changed in the object will
 * cause auditing information changes.
 *
 * @param <T> The type of this matchmaker object implementation
 * @param <C> The child type of this matchmaker object implementation
 */
public abstract class AbstractMatchMakerObject<T extends MatchMakerObject, C extends MatchMakerObject>
	implements MatchMakerObject<T, C> {

    private static final Logger logger = Logger.getLogger(AbstractMatchMakerObject.class);
    
	private MatchMakerObject parent;

	@SuppressWarnings("unchecked")
	private MatchMakerEventSupport<T,C> eventSupport =
		new MatchMakerEventSupport<T,C>((T) this);

	private List<C> children = new ArrayList<C>();
	private String lastUpdateAppUser;
	private String lastUpdateOsUser;
	private Date lastUpdateDate;
	private Date createDate;
	private MatchMakerSession matchMakerSession;
	private String name;


	public AbstractMatchMakerObject() {
		eventSupport.addMatchMakerListener(new MatchMakerListener<T,C>(){

			public void mmPropertyChanged(MatchMakerEvent<T, C> evt) {
				registerUpdate();
			}

			public void mmChildrenInserted(MatchMakerEvent<T, C> evt) {
				registerUpdate();
			}

			public void mmChildrenRemoved(MatchMakerEvent<T, C> evt) {
				registerUpdate();
			}

			public void mmStructureChanged(MatchMakerEvent<T, C> evt) {
				registerUpdate();
			}});
	}

	/**
	 * anyone who going to overwrite this method should fire the childrenInserted
	 * event in the overwriten method
	 * @param child
	 */
	public void addChild(C child) {
        logger.debug("addChild: children collection is a "+children.getClass().getName());
		children.add(child);
		child.setParent(this);
		List<C> insertedChildren = new ArrayList<C>();
		insertedChildren.add(child);
		eventSupport.fireChildrenInserted("children",new int[] {children.size()-1},insertedChildren);
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

	public String getLastUpdateAppUser() {
		return lastUpdateAppUser;
	}
	public String getLastUpdateOSUser() {
		return lastUpdateOsUser;
	}
	public Date getLastUpdateDate() {
		return lastUpdateDate;
	}
	/**
	 * Register an update if the object is participating in a session
	 */
	public void registerUpdate() {
		if (matchMakerSession != null){
			lastUpdateDate = new Date();
			lastUpdateOsUser = System.getProperty("user.name");
			lastUpdateAppUser = matchMakerSession.getAppUser();
		}
	}

	public MatchMakerObject getParent() {
		return parent;
	}

	public void setParent(MatchMakerObject parent) {
		MatchMakerObject oldValue = this.parent;
		this.parent = parent;
		eventSupport.firePropertyChange("parent", oldValue, this.parent);
	}

	public void setSession(MatchMakerSession matchMakerSession) {
		this.matchMakerSession = matchMakerSession;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public boolean allowsChildren() {
		return true;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		String oldValue = this.name;
		this.name = name;
		eventSupport.firePropertyChange("name", oldValue, this.name);
	}

	public abstract boolean equals(Object obj);

	public abstract int hashCode();


	/////// Event stuff ///////

	public void addMatchMakerListener(MatchMakerListener<T, C> l) {
		eventSupport.addMatchMakerListener(l);
	}

	public void removeMatchMakerListener(MatchMakerListener<T, C> l) {
		eventSupport.removeMatchMakerListener(l);
	}

	protected MatchMakerEventSupport<T, C> getEventSupport() {
		return eventSupport;
	}

}