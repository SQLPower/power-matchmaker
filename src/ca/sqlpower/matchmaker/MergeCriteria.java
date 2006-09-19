package ca.sqlpower.matchmaker;


import java.sql.Date;

public class MergeCriteria {

	//keys
    private String matchID;
    private String owner;
    private String tableName;
    private String indexColumnName0;
    
    //non-keys
    private String columnName;
    private Date lastUpdateDate;
    private String lastUpdateUser;
    private boolean deleteDupInd;
    private long seqNo;
    private String lastUpdateOsUser;
    private String primaryKeyIndex;
    private String indexColumnName1;
    private String indexColumnName2;
    private String indexColumnName3;
    private String indexColumnName4;
    private String indexColumnName5;
    private String indexColumnName6;
    private String indexColumnName7;
    private String indexColumnName8;
    private String indexColumnName9;
    
    

    public String getIndexColumnName0() {
		return indexColumnName0;
	}

	public void setIndexColumnName0(String indexColumnName0) {
		this.indexColumnName0 = indexColumnName0;
	}

	public String getIndexColumnName1() {
		return indexColumnName1;
	}

	public void setIndexColumnName1(String indexColumnName1) {
		this.indexColumnName1 = indexColumnName1;
	}

	public String getIndexColumnName2() {
		return indexColumnName2;
	}

	public void setIndexColumnName2(String indexColumnName2) {
		this.indexColumnName2 = indexColumnName2;
	}

	public String getIndexColumnName3() {
		return indexColumnName3;
	}

	public void setIndexColumnName3(String indexColumnName3) {
		this.indexColumnName3 = indexColumnName3;
	}

	public String getIndexColumnName4() {
		return indexColumnName4;
	}

	public void setIndexColumnName4(String indexColumnName4) {
		this.indexColumnName4 = indexColumnName4;
	}

	public String getIndexColumnName5() {
		return indexColumnName5;
	}

	public void setIndexColumnName5(String indexColumnName5) {
		this.indexColumnName5 = indexColumnName5;
	}

	public String getIndexColumnName6() {
		return indexColumnName6;
	}

	public void setIndexColumnName6(String indexColumnName6) {
		this.indexColumnName6 = indexColumnName6;
	}

	public String getIndexColumnName7() {
		return indexColumnName7;
	}

	public void setIndexColumnName7(String indexColumnName7) {
		this.indexColumnName7 = indexColumnName7;
	}

	public String getIndexColumnName8() {
		return indexColumnName8;
	}

	public void setIndexColumnName8(String indexColumnName8) {
		this.indexColumnName8 = indexColumnName8;
	}

	public String getIndexColumnName9() {
		return indexColumnName9;
	}

	public void setIndexColumnName9(String indexColumnName9) {
		this.indexColumnName9 = indexColumnName9;
	}

	public void mergeCriteria(){

    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }



    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public boolean isDeleteDupInd() {
		return deleteDupInd;
	}

	public void setDeleteDupInd(boolean deleteDupInd) {
		this.deleteDupInd = deleteDupInd;
	}

	public String getLastUpdateOsUser() {
		return lastUpdateOsUser;
	}

	public void setLastUpdateOsUser(String lastUpdateOsUser) {
		this.lastUpdateOsUser = lastUpdateOsUser;
	}

	public void setSeqNo(long seqNo) {
		this.seqNo = seqNo;
	}

	public String getLastUpdateUser() {
        return lastUpdateUser;
    }

    public void setLastUpdateUser(String lastUpdateUser) {
        this.lastUpdateUser = lastUpdateUser;
    }

    public String getMatchID() {
        return matchID;
    }

    public void setMatchID(String matchID) {
        this.matchID = matchID;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getPrimaryKeyIndex() {
        return primaryKeyIndex;
    }

    public void setPrimaryKeyIndex(String primaryKeyIndex) {
        this.primaryKeyIndex = primaryKeyIndex;
    }

    public long getSeqNo() {
        return seqNo;
    }

    public void setSeqNo(int seqNo) {
        this.seqNo = seqNo;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((indexColumnName0 == null) ? 0 : indexColumnName0.hashCode());
		result = PRIME * result + ((matchID == null) ? 0 : matchID.hashCode());
		result = PRIME * result + ((owner == null) ? 0 : owner.hashCode());
		result = PRIME * result + ((tableName == null) ? 0 : tableName.hashCode());
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
		final MergeCriteria other = (MergeCriteria) obj;
		if (indexColumnName0 == null) {
			if (other.indexColumnName0 != null)
				return false;
		} else if (!indexColumnName0.equals(other.indexColumnName0))
			return false;
		if (matchID == null) {
			if (other.matchID != null)
				return false;
		} else if (!matchID.equals(other.matchID))
			return false;
		if (owner == null) {
			if (other.owner != null)
				return false;
		} else if (!owner.equals(other.owner))
			return false;
		if (tableName == null) {
			if (other.tableName != null)
				return false;
		} else if (!tableName.equals(other.tableName))
			return false;
		return true;
	}

}
