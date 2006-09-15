package ca.sqlpower.matchmaker;


import java.lang.reflect.Array;
import java.sql.Date;
import java.util.List;

public class MergeCriteria {

    private String matchID;
    private String owner;
    private String tableName;
    private String indexColumnName;
    private String columnName;
    private Date lastUpdateDate;
    private String lastUpdateUser;
    private boolean deleteDup;
    private long seqNo;
    private String lastUpdateOSUser;
    private String primaryKeyIndex;
    private List<String> indexColumnNames;

    public void mergeCriteria(){

    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public boolean isDeleteDup() {
        return deleteDup;
    }

    public void setDeleteDup(boolean deleteDup) {
        this.deleteDup = deleteDup;
    }

    public String getIndexColumnName() {
        return indexColumnName;
    }

    public void setIndexColumnName(String indexColumnName) {
        this.indexColumnName = indexColumnName;
    }

    public List<String> getIndexColumnNames() {
        return indexColumnNames;
    }

    public void setIndexColumnNames(List<String> indexColumnNames) {
        this.indexColumnNames = indexColumnNames;
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


}
