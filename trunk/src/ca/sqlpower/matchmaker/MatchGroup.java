package ca.sqlpower.matchmaker;

import java.sql.Date;
import java.util.List;

import ca.sqlpower.persistance.CatNap;


public class MatchGroup {
    
    private String matchID;
    private String groupID;
    private String description;
    private int matchPercent;
    private Date lastUpdateDate;
    private String lastUserName;
    private String filterCriteria;
    private boolean activeInd;
    private String lastUpdateOSUser;
    private List <MatchCriteria> matchCriteria;
    
    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((groupID == null) ? 0 : groupID.hashCode());
        result = PRIME * result + ((matchID == null) ? 0 : matchID.hashCode());
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
        final MatchGroup other = (MatchGroup) obj;
        if (groupID == null) {
            if (other.groupID != null)
                return false;
        } else if (!groupID.equals(other.groupID))
            return false;
        if (matchID == null) {
            if (other.matchID != null)
                return false;
        } else if (!matchID.equals(other.matchID))
            return false;
        return true;
    }

    public void matchGroup(){
        
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFilterCriteria() {
        return filterCriteria;
    }

    public void setFilterCriteria(String filterCriteria) {
        this.filterCriteria = filterCriteria;
    }

    public String getGroupID() {
        return groupID;
    }

    public void setGroupID(String groupID) {
        this.groupID = groupID;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public String getLastUpdateOSUser() {
        return lastUpdateOSUser;
    }

    public void setLastUpdateOSUser(String lastUpdateOSUser) {
        this.lastUpdateOSUser = lastUpdateOSUser;
    }

    public String getLastUserName() {
        return lastUserName;
    }

    public void setLastUserName(String lastUserName) {
        this.lastUserName = lastUserName;
    }

    public String getMatchID() {
        return matchID;
    }

    public void setMatchID(String matchID) {
        this.matchID = matchID;
    }

    public int getMatchPercent() {
        return matchPercent;
    }

    public void setMatchPercent(int matchPercent) {
        this.matchPercent = matchPercent;
    }

	public boolean isActiveInd() {
		return activeInd;
	}

	public void setActiveInd(boolean activeInd) {
		this.activeInd = activeInd;
	}

	public List<MatchCriteria> getMatchCriteria() {
		return matchCriteria;
	}

	public void setMatchCriteria(List<MatchCriteria> matchCriteria) {
		this.matchCriteria = matchCriteria;
	}
    
    
}
