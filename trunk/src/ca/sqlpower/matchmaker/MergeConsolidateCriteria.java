package ca.sqlpower.matchmaker;

import java.sql.Date;

import ca.sqlpower.persistance.CatNap;


public class MergeConsolidateCriteria extends CatNap{

    private String mathId;
    private String owner;
    private String tableName;
    private String columnName;
    private String columnFormat;
    private String actionType;
    private Date lastUpdateDate;
    private String listUpdateUser;
    private boolean canUpdateActionInd;
    private long columnLength;
    private String laastUpdateOsUSer;

    public void mergeConslidateCriteria(){

    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public boolean isCanUpdateActionInd() {
		return canUpdateActionInd;
	}

	public void setCanUpdateActionInd(boolean canUpdateActionInd) {
		this.canUpdateActionInd = canUpdateActionInd;
	}

	public void setColumnLength(long columnLength) {
		this.columnLength = columnLength;
	}

	public String getColumnFormat() {
        return columnFormat;
    }

    public void setColumnFormat(String columnFormat) {
        this.columnFormat = columnFormat;
    }

    public long getColumnLength() {
        return columnLength;
    }

    public void setColumnLength(int columnLength) {
        this.columnLength = columnLength;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getLaastUpdateOsUSer() {
        return laastUpdateOsUSer;
    }

    public void setLaastUpdateOsUSer(String laastUpdateOsUSer) {
        this.laastUpdateOsUSer = laastUpdateOsUSer;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public String getListUpdateUser() {
        return listUpdateUser;
    }

    public void setListUpdateUser(String listUpdateUser) {
        this.listUpdateUser = listUpdateUser;
    }

    public String getMathId() {
        return mathId;
    }

    public void setMathId(String mathId) {
        this.mathId = mathId;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

}
