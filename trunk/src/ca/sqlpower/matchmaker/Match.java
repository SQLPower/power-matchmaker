package ca.sqlpower.matchmaker;

import java.sql.Date;
import java.util.List;


public class Match {
    String matchID;
    String matchDescr;
    String tableOwner;
    String matchTable;
    String pkColumn;
    String filter;
    String resultsTable;
    Date createDate;
    Date lastUpdateDate;
    String lastUpdateUser;
    String sequenceName;
    String compileFlag;
    String mergeScriptFileName;
    int autoMatchThreshold;
    Date mergeCompletionDate;
    String mergeLastUser;
    String mergeRunStats;
    Date matchLastRunDate;
    String matchLastRunUser;
    int mergeTotalSteps;
    int mergeStepsCopmleted;
    Date mergeLastRunDate;
    String mergLastRunUser;
    String matchPackageName;
    String matchProcedureNameAll;
    String matchProcedureNameOne;
    String mergePackageName;
    String mergeProcedureName;
    String matchRunStatus;
    String matchRunTablePKColumnFormat;
    int mergeRowsInserted;
    String mergeDesc;
    String matchLogFileName;
    boolean matchAppendToLog;
    int matchProcessCNT;
    int matchShowProgressFreqw;
    boolean matchDebugMode;
    String matchRollbackSegmentName;
    boolean augmentNull;
    String matchScriptFileName;
    int matchTotalSteps;
    int matchStepsCompleted;
    int matchRowsInserted;
    String batchFileName;
    String selectClause;
    String fromClause;
    String whereClause;
    String filterCriteria;
    String resultsTableOwner;
    boolean matchBreak;
    String matchType;
    String lastUpdateOSUser;
    String matchStepDesc;
    String mergeStepDesc;
    boolean mergeTablesBackup;
    String matchStatus;
    int lastBackupNo;
    boolean checkedOut;
    Date checkedOutDate;
    String checkedOutUser;
    String checkedOutOSUser;
    String tempSourceTableName;
    String tempCanDupTableName;
    String tempCandDupTableName;
    List<String> indexColumName;
    String fromClauseDB;
    List<String> indexColumnType;
    boolean matchSendEmail;
    boolean mergeSendEmail;
    boolean truncateCandDup;
    String xRefOwner;
    String xRefTableName;
    boolean autoMatchActive;
    List <MatchXRefMap> matchXRefMap;
    List <MatchGroup> matchGroup;
    List <MergeConsolidateCriteria> mergeConsolidateCriteria;
    List <MergeCriteria> mergeCriteria;

    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
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
        final Match other = (Match) obj;
        if (matchID == null) {
            if (other.matchID != null)
                return false;
        } else if (!matchID.equals(other.matchID))
            return false;
        return true;
    }

    public boolean isAugmentNull() {
        return augmentNull;
    }

    public void setAugmentNull(boolean augmentNull) {
        this.augmentNull = augmentNull;
    }

    public boolean isAutoMatchActive() {
        return autoMatchActive;
    }

    public void setAutoMatchActive(boolean autoMatchActive) {
        this.autoMatchActive = autoMatchActive;
    }

    public int getAutoMatchThreshold() {
        return autoMatchThreshold;
    }

    public void setAutoMatchThreshold(int autoMatchThreshold) {
        this.autoMatchThreshold = autoMatchThreshold;
    }

    public String getBatchFileName() {
        return batchFileName;
    }

    public void setBatchFileName(String batchFileName) {
        this.batchFileName = batchFileName;
    }

    public boolean isCheckedOut() {
        return checkedOut;
    }

    public void setCheckedOut(boolean checkedOut) {
        this.checkedOut = checkedOut;
    }

    public Date getCheckedOutDate() {
        return checkedOutDate;
    }

    public void setCheckedOutDate(Date checkedOutDate) {
        this.checkedOutDate = checkedOutDate;
    }

    public String getCheckedOutOSUser() {
        return checkedOutOSUser;
    }

    public void setCheckedOutOSUser(String checkedOutOSUser) {
        this.checkedOutOSUser = checkedOutOSUser;
    }

    public String getCheckedOutUser() {
        return checkedOutUser;
    }

    public void setCheckedOutUser(String checkedOutUser) {
        this.checkedOutUser = checkedOutUser;
    }

    public String getCompileFlag() {
        return compileFlag;
    }

    public void setCompileFlag(String compileFlag) {
        this.compileFlag = compileFlag;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getFilterCriteria() {
        return filterCriteria;
    }

    public void setFilterCriteria(String filterCriteria) {
        this.filterCriteria = filterCriteria;
    }

    public String getFromClause() {
        return fromClause;
    }

    public void setFromClause(String fromClause) {
        this.fromClause = fromClause;
    }

    public String getFromClauseDB() {
        return fromClauseDB;
    }

    public void setFromClauseDB(String fromClauseDB) {
        this.fromClauseDB = fromClauseDB;
    }

    public List<String> getIndexColumName() {
        return indexColumName;
    }

    public void setIndexColumName(List<String> indexColumName) {
        this.indexColumName = indexColumName;
    }

    public List<String> getIndexColumnType() {
        return indexColumnType;
    }

    public void setIndexColumnType(List<String> indexColumnType) {
        this.indexColumnType = indexColumnType;
    }

    public int getLastBackupNo() {
        return lastBackupNo;
    }

    public void setLastBackupNo(int lastBackupNo) {
        this.lastBackupNo = lastBackupNo;
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

    public boolean isMatchAppendToLog() {
        return matchAppendToLog;
    }

    public void setMatchAppendToLog(boolean matchAppendToLog) {
        this.matchAppendToLog = matchAppendToLog;
    }

    public boolean isMatchBreak() {
        return matchBreak;
    }

    public void setMatchBreak(boolean matchBreak) {
        this.matchBreak = matchBreak;
    }

    public boolean isMatchDebugMode() {
        return matchDebugMode;
    }

    public void setMatchDebugMode(boolean matchDebugMode) {
        this.matchDebugMode = matchDebugMode;
    }

    public String getMatchDescr() {
        return matchDescr;
    }

    public void setMatchDescr(String matchDescr) {
        this.matchDescr = matchDescr;
    }

    public List<MatchGroup> getMatchGroup() {
        return matchGroup;
    }

    public void setMatchGroup(List<MatchGroup> matchGroup) {
        this.matchGroup = matchGroup;
    }

    public String getMatchID() {
        return matchID;
    }

    public void setMatchID(String matchID) {
        this.matchID = matchID;
    }

    public Date getMatchLastRunDate() {
        return matchLastRunDate;
    }

    public void setMatchLastRunDate(Date matchLastRunDate) {
        this.matchLastRunDate = matchLastRunDate;
    }

    public String getMatchLastRunUser() {
        return matchLastRunUser;
    }

    public void setMatchLastRunUser(String matchLastRunUser) {
        this.matchLastRunUser = matchLastRunUser;
    }

    public String getMatchLogFileName() {
        return matchLogFileName;
    }

    public void setMatchLogFileName(String matchLogFileName) {
        this.matchLogFileName = matchLogFileName;
    }

    public String getMatchPackageName() {
        return matchPackageName;
    }

    public void setMatchPackageName(String matchPackageName) {
        this.matchPackageName = matchPackageName;
    }

    public String getMatchProcedureNameAll() {
        return matchProcedureNameAll;
    }

    public void setMatchProcedureNameAll(String matchProcedureNameAll) {
        this.matchProcedureNameAll = matchProcedureNameAll;
    }

    public String getMatchProcedureNameOne() {
        return matchProcedureNameOne;
    }

    public void setMatchProcedureNameOne(String matchProcedureNameOne) {
        this.matchProcedureNameOne = matchProcedureNameOne;
    }

    public int getMatchProcessCNT() {
        return matchProcessCNT;
    }

    public void setMatchProcessCNT(int matchProcessCNT) {
        this.matchProcessCNT = matchProcessCNT;
    }

    public String getMatchRollbackSegmentName() {
        return matchRollbackSegmentName;
    }

    public void setMatchRollbackSegmentName(String matchRollbackSegmentName) {
        this.matchRollbackSegmentName = matchRollbackSegmentName;
    }

    public int getMatchRowsInserted() {
        return matchRowsInserted;
    }

    public void setMatchRowsInserted(int matchRowsInserted) {
        this.matchRowsInserted = matchRowsInserted;
    }

    public String getMatchRunStatus() {
        return matchRunStatus;
    }

    public void setMatchRunStatus(String matchRunStatus) {
        this.matchRunStatus = matchRunStatus;
    }

    public String getMatchRunTablePKColumnFormat() {
        return matchRunTablePKColumnFormat;
    }

    public void setMatchRunTablePKColumnFormat(String matchRunTablePKColumnFormat) {
        this.matchRunTablePKColumnFormat = matchRunTablePKColumnFormat;
    }

    public String getMatchScriptFileName() {
        return matchScriptFileName;
    }

    public void setMatchScriptFileName(String matchScriptFileName) {
        this.matchScriptFileName = matchScriptFileName;
    }

    public boolean isMatchSendEmail() {
        return matchSendEmail;
    }

    public void setMatchSendEmail(boolean matchSendEmail) {
        this.matchSendEmail = matchSendEmail;
    }

    public int getMatchShowProgressFreqw() {
        return matchShowProgressFreqw;
    }

    public void setMatchShowProgressFreqw(int matchShowProgressFreqw) {
        this.matchShowProgressFreqw = matchShowProgressFreqw;
    }

    public String getMatchStatus() {
        return matchStatus;
    }

    public void setMatchStatus(String matchStatus) {
        this.matchStatus = matchStatus;
    }

    public String getMatchStepDesc() {
        return matchStepDesc;
    }

    public void setMatchStepDesc(String matchStepDesc) {
        this.matchStepDesc = matchStepDesc;
    }

    public int getMatchStepsCompleted() {
        return matchStepsCompleted;
    }

    public void setMatchStepsCompleted(int matchStepsCompleted) {
        this.matchStepsCompleted = matchStepsCompleted;
    }

    public String getMatchTable() {
        return matchTable;
    }

    public void setMatchTable(String matchTable) {
        this.matchTable = matchTable;
    }

    public int getMatchTotalSteps() {
        return matchTotalSteps;
    }

    public void setMatchTotalSteps(int matchTotalSteps) {
        this.matchTotalSteps = matchTotalSteps;
    }

    public String getMatchType() {
        return matchType;
    }

    public void setMatchType(String matchType) {
        this.matchType = matchType;
    }

    public List<MatchXRefMap> getMatchXRefMap() {
        return matchXRefMap;
    }

    public void setMatchXRefMap(List<MatchXRefMap> matchXRefMap) {
        this.matchXRefMap = matchXRefMap;
    }

    public Date getMergeCompletionDate() {
        return mergeCompletionDate;
    }

    public void setMergeCompletionDate(Date mergeCompletionDate) {
        this.mergeCompletionDate = mergeCompletionDate;
    }

    public List<MergeConsolidateCriteria> getMergeConsolidateCriteria() {
        return mergeConsolidateCriteria;
    }

    public void setMergeConsolidateCriteria(
            List<MergeConsolidateCriteria> mergeConsolidateCriteria) {
        this.mergeConsolidateCriteria = mergeConsolidateCriteria;
    }

    public List<MergeCriteria> getMergeCriteria() {
        return mergeCriteria;
    }

    public void setMergeCriteria(List<MergeCriteria> mergeCriteria) {
        this.mergeCriteria = mergeCriteria;
    }

    public String getMergeDesc() {
        return mergeDesc;
    }

    public void setMergeDesc(String mergeDesc) {
        this.mergeDesc = mergeDesc;
    }

    public Date getMergeLastRunDate() {
        return mergeLastRunDate;
    }

    public void setMergeLastRunDate(Date mergeLastRunDate) {
        this.mergeLastRunDate = mergeLastRunDate;
    }

    public String getMergeLastUser() {
        return mergeLastUser;
    }

    public void setMergeLastUser(String mergeLastUser) {
        this.mergeLastUser = mergeLastUser;
    }

    public String getMergePackageName() {
        return mergePackageName;
    }

    public void setMergePackageName(String mergePackageName) {
        this.mergePackageName = mergePackageName;
    }

    public String getMergeProcedureName() {
        return mergeProcedureName;
    }

    public void setMergeProcedureName(String mergeProcedureName) {
        this.mergeProcedureName = mergeProcedureName;
    }

    public int getMergeRowsInserted() {
        return mergeRowsInserted;
    }

    public void setMergeRowsInserted(int mergeRowsInserted) {
        this.mergeRowsInserted = mergeRowsInserted;
    }

    public String getMergeRunStats() {
        return mergeRunStats;
    }

    public void setMergeRunStats(String mergeRunStats) {
        this.mergeRunStats = mergeRunStats;
    }

    public String getMergeScriptFileName() {
        return mergeScriptFileName;
    }

    public void setMergeScriptFileName(String mergeScriptFileName) {
        this.mergeScriptFileName = mergeScriptFileName;
    }

    public boolean isMergeSendEmail() {
        return mergeSendEmail;
    }

    public void setMergeSendEmail(boolean mergeSendEmail) {
        this.mergeSendEmail = mergeSendEmail;
    }

    public String getMergeStepDesc() {
        return mergeStepDesc;
    }

    public void setMergeStepDesc(String mergeStepDesc) {
        this.mergeStepDesc = mergeStepDesc;
    }

    public int getMergeStepsCopmleted() {
        return mergeStepsCopmleted;
    }

    public void setMergeStepsCopmleted(int mergeStepsCopmleted) {
        this.mergeStepsCopmleted = mergeStepsCopmleted;
    }

    public boolean isMergeTablesBackup() {
        return mergeTablesBackup;
    }

    public void setMergeTablesBackup(boolean mergeTablesBackup) {
        this.mergeTablesBackup = mergeTablesBackup;
    }

    public int getMergeTotalSteps() {
        return mergeTotalSteps;
    }

    public void setMergeTotalSteps(int mergeTotalSteps) {
        this.mergeTotalSteps = mergeTotalSteps;
    }

    public String getMergLastRunUser() {
        return mergLastRunUser;
    }

    public void setMergLastRunUser(String mergLastRunUser) {
        this.mergLastRunUser = mergLastRunUser;
    }

    public String getPkColumn() {
        return pkColumn;
    }

    public void setPkColumn(String pkColumn) {
        this.pkColumn = pkColumn;
    }

    public String getResultsTable() {
        return resultsTable;
    }

    public void setResultsTable(String resultsTable) {
        this.resultsTable = resultsTable;
    }

    public String getResultsTableOwner() {
        return resultsTableOwner;
    }

    public void setResultsTableOwner(String resultsTableOwner) {
        this.resultsTableOwner = resultsTableOwner;
    }

    public String getSelectClause() {
        return selectClause;
    }

    public void setSelectClause(String selectClause) {
        this.selectClause = selectClause;
    }

    public String getSequenceName() {
        return sequenceName;
    }

    public void setSequenceName(String sequenceName) {
        this.sequenceName = sequenceName;
    }

    public String getTableOwner() {
        return tableOwner;
    }

    public void setTableOwner(String tableOwner) {
        this.tableOwner = tableOwner;
    }

    public String getTempCandDupTableName() {
        return tempCandDupTableName;
    }

    public void setTempCandDupTableName(String tempCandDupTableName) {
        this.tempCandDupTableName = tempCandDupTableName;
    }

    public String getTempCanDupTableName() {
        return tempCanDupTableName;
    }

    public void setTempCanDupTableName(String tempCanDupTableName) {
        this.tempCanDupTableName = tempCanDupTableName;
    }

    public String getTempSourceTableName() {
        return tempSourceTableName;
    }

    public void setTempSourceTableName(String tempSourceTableName) {
        this.tempSourceTableName = tempSourceTableName;
    }

    public boolean isTruncateCandDup() {
        return truncateCandDup;
    }

    public void setTruncateCandDup(boolean truncateCandDup) {
        this.truncateCandDup = truncateCandDup;
    }

    public String getWhereClause() {
        return whereClause;
    }

    public void setWhereClause(String whereClause) {
        this.whereClause = whereClause;
    }

    public String getXRefOwner() {
        return xRefOwner;
    }

    public void setXRefOwner(String refOwner) {
        xRefOwner = refOwner;
    }

    public String getXRefTableName() {
        return xRefTableName;
    }

    public void setXRefTableName(String refTableName) {
        xRefTableName = refTableName;
    }

    public void Match(){

    }



}
