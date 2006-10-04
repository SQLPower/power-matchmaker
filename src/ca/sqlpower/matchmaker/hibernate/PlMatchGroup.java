package ca.sqlpower.matchmaker.hibernate;
// Generated Sep 18, 2006 4:34:38 PM by Hibernate Tools 3.2.0.beta7


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.event.ChangeEvent;

import org.apache.commons.beanutils.BeanUtils;

/**
 * PlMatchGroup generated by hbm2java, but maintained by hand
 */
public class PlMatchGroup extends DefaultHibernateObject implements java.io.Serializable {

    // Fields

     private PlMatchGroupId id;
     private PlMatch plMatch;
     private String description;
     private Short matchPercent;
     private Date lastUpdateDate;
     private String lastUpdateUser;
     private String filterCriteria;
     /**
      * true if the match should be deactivated
      * false if it should be active
      */
     private boolean activeInd;
     private String lastUpdateOsUser;
     private Set<PlMatchCriteria> plMatchCriterias = new TreeSet<PlMatchCriteria>();

     // Constructors

    /** default constructor */
    public PlMatchGroup() {
    	matchPercent = new Short("0");
    }

	/** minimal constructor */
    public PlMatchGroup(PlMatchGroupId id, PlMatch plMatch) {
    	this();
        this.id = id;
        this.plMatch = plMatch;
    }

    /** partial constructor */
    public PlMatchGroup(PlMatchGroupId id, PlMatch plMatch, PlMatchGroup model) {
    	this();
        this.id = id;
        this.plMatch = plMatch;
        plMatch.getPlMatchGroups().add(model);
    }

    /**
     * Deep copy of this object
     * @return A deep copy of the PlXXX object
     */
    public PlMatchGroup copyOf() {
    	try {
    		PlMatchGroup copy = (PlMatchGroup) BeanUtils.cloneBean(this);

        copy.id = id.copyOf();
        copy.plMatch = plMatch.copyOf();
    	return copy;
    	} catch (Exception e) {
    		throw new RuntimeException("Could not copy", e);
    	}
    }

    /** full constructor */
    public PlMatchGroup(PlMatchGroupId id, PlMatch plMatch, String description, Short matchPercent, Date lastUpdateDate, String lastUpdateUser, String filterCriteria, boolean activeInd, String lastUpdateOsUser, Set<PlMatchCriteria> plMatchCriterias) {
       this.id = id;
       this.plMatch = plMatch;
       this.description = description;
       this.matchPercent = matchPercent;
       this.lastUpdateDate = lastUpdateDate;
       this.lastUpdateUser = lastUpdateUser;
       this.filterCriteria = filterCriteria;
       this.activeInd = activeInd;
       this.lastUpdateOsUser = lastUpdateOsUser;
       this.plMatchCriterias = plMatchCriterias;
    }


    public PlMatchGroup(PlMatchGroup orig) {
    	// Copy all the "simple" properties first
    	try {
			BeanUtils.copyProperties(orig, this);
		} catch (Exception e) {
			throw new RuntimeException("PlMatch Copy Constructor caught " + e, e);
		}

		// Unfortunately the above has compromised the Set properties, so we
		// empty them, and re-populate using copy constructors for each object
		plMatchCriterias.clear();
		for (PlMatchCriteria p : orig.plMatchCriterias) {
			plMatchCriterias.add(p.copyOf());
		}
	}

	@Override
    public int getChildCount() {
    	return plMatchCriterias.size();
    }

    @Override
    public List<DefaultHibernateObject> getChildren() {
    	List<DefaultHibernateObject> children = new ArrayList<DefaultHibernateObject>();
    	for (PlMatchCriteria group : plMatchCriterias){
    		children.add(group);
    	}
    	Collections.sort(children);
    	return children;
    }
    // Property accessors
    public PlMatchGroupId getId() {
        return this.id;
    }

    public void setId(PlMatchGroupId id) {
    	if(id != this.id){
    		this.id = id;
    		fireChangeEvent(new ChangeEvent(this));
    	}
    }
    public PlMatch getPlMatch() {
        return this.plMatch;
    }

    public void setPlMatch(PlMatch plMatch) {
    	if(plMatch!=this.plMatch){
    		this.plMatch = plMatch;
    		fireChangeEvent(new ChangeEvent(this));
    	}
    }

    public String getDescription() {
        return this.description;
    }


    public Short getMatchPercent() {
        return this.matchPercent;
    }


    public Date getLastUpdateDate() {
        return this.lastUpdateDate;
    }

    public String getLastUpdateUser() {
        return this.lastUpdateUser;
    }


    public String getFilterCriteria() {
        return this.filterCriteria;
    }


    public boolean isActiveInd() {
        return this.activeInd;
    }


    public String getLastUpdateOsUser() {
        return this.lastUpdateOsUser;
    }

    public Set<PlMatchCriteria> getPlMatchCriterias() {
        return Collections.unmodifiableSet(this.plMatchCriterias);
    }
    
    
    
    public boolean addPlMatchCriteria(PlMatchCriteria pmc) {
    	boolean b = plMatchCriterias.add(pmc);
    	pmc.addAllHierachialChangeListener(getHierachialChangeListeners());
    	fireChangeEvent(new ChangeEvent(this));
    	return b;
    }
    
    public boolean removePlMatchCriteria(PlMatchCriteria pmc) {
    	boolean b = plMatchCriterias.remove(pmc);
    	pmc.removeAllHierachialChangeListener(getHierachialChangeListeners());
    	fireChangeEvent(new ChangeEvent(this));
    	return b;
    }
    
    public void clearPlMatchCriteria(){
    	for(PlMatchCriteria pmc:getPlMatchCriterias()){
    		plMatchCriterias.remove(pmc);
        	pmc.removeAllHierachialChangeListener(getHierachialChangeListeners());
    	}
    	fireChangeEvent(new ChangeEvent(this));
 
    }


    public void setActiveInd(boolean activeInd) {
	if (this.activeInd!= activeInd){
		this.activeInd = activeInd;
		fireChangeEvent(new ChangeEvent(this));
	}}


	public void setDescription(String description) {
	if (this.description!= description){
		this.description = description;
		fireChangeEvent(new ChangeEvent(this));
	}}


	public void setFilterCriteria(String filterCriteria) {
	if (this.filterCriteria!= filterCriteria){
		this.filterCriteria = filterCriteria;
		fireChangeEvent(new ChangeEvent(this));
	}}


	public void setLastUpdateDate(Date lastUpdateDate) {
	if (this.lastUpdateDate!= lastUpdateDate){
		this.lastUpdateDate = lastUpdateDate;
		fireChangeEvent(new ChangeEvent(this));
	}}


	public void setLastUpdateOsUser(String lastUpdateOsUser) {
	if (this.lastUpdateOsUser!= lastUpdateOsUser){
		this.lastUpdateOsUser = lastUpdateOsUser;
		fireChangeEvent(new ChangeEvent(this));
	}}


	public void setLastUpdateUser(String lastUpdateUser) {
	if (this.lastUpdateUser!= lastUpdateUser){
		this.lastUpdateUser = lastUpdateUser;
		fireChangeEvent(new ChangeEvent(this));
	}}


	public void setMatchPercent(Short matchPercent) {
	if (this.matchPercent != matchPercent){
		this.matchPercent = matchPercent;
		fireChangeEvent(new ChangeEvent(this));
	}
	}


	private void setPlMatchCriterias(Set<PlMatchCriteria> plMatchCriterias) {
		if (this.plMatchCriterias!= plMatchCriterias){
			this.plMatchCriterias = plMatchCriterias;
			fireChangeEvent(new ChangeEvent(this));
		}
	}


	@Override
    public String toString() {
    	return id.getGroupId() +" ("+matchPercent+"%)";
    }

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PlMatchGroup other = (PlMatchGroup) obj;
		return id.equals(other.getId());
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}


	public int compareTo(Object o) {

		PlMatchGroup other = (PlMatchGroup) o;
		if (other.getMatchPercent().compareTo(getMatchPercent()) != 0){
			return other.getMatchPercent().compareTo(getMatchPercent());
		} else if (filterCriteria.compareTo(other.getFilterCriteria()) !=0) {
			return filterCriteria.compareTo(other.getFilterCriteria());
		} else if (id.getMatchId().compareTo(other.getId().getMatchId())!=0 ) {
			return id.getMatchId().compareTo(other.getId().getMatchId());
		} else {
			return id.getGroupId().compareTo(other.getId().getGroupId());
		}
	}


	/* for xml parser, overwrite all method that don't take String parameter,
	 * also create id when set matchId or groupId
	 *
	 */
	public void setMatchId(String id) {
		if ( this.id == null ) {
			this.id = new PlMatchGroupId();
		}
		this.id.setMatchId(id);
	}
	public void setGroupId(String id) {
		if ( this.id == null ) {
			this.id = new PlMatchGroupId();
		}
		this.id.setGroupId(id);
	}
	public void setMatchPercent(String val) {
		if ( val != null && val.length()>0 && !val.equalsIgnoreCase("null")) {
			setMatchPercent(Short.valueOf(val));
		}
	}
	public void setLastUpdateDate(String val) throws ParseException {
		if ( val != null && val.length()>0 && !val.equalsIgnoreCase("null")) {
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			setLastUpdateDate(df.parse(val));
		}
	}
	public void setActiveInd(String val) {
		if ( val != null && val.length()>0 && !val.equalsIgnoreCase("null")) {
			setActiveInd(val.charAt(0) == 'y' || val.charAt(0) == 'Y');
		}
	}


}
