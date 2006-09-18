package ca.sqlpower.matchmaker;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

import ca.sqlpower.persistance.CatNap;


public class Match {
	// Key
    private String matchID;
    // non-key
    private String matchDescr;
    private String tableOwner;
    private String matchTable;
    private String pkColumn;
    private String filter;
    private String resultsTable;
    private Date createDate;
    private Date lastUpdateDate;
    private String lastUpdateUser;
    private String sequenceName;
    private String compileFlag;
    private String mergeScriptFileName;
    private int autoMatchThreshold;
    private Date mergeCompletionDate;
    private String mergeLastUser;
    private String mergeRunStats;
    private Date matchLastRunDate;
    private String matchLastRunUser;
    private int mergeTotalSteps;
    private int mergeStepsCompleted;
    private Date mergeLastRunDate;
    private String mergLastRunUser;
    private String matchPackageName;
    private String matchProcedureNameAll;
    private String matchProcedureNameOne;
    private String mergePackageName;
    private String mergeProcedureName;
    private String matchRunStatus;
    private String matchRunTablePkColumnFormat;
    private int mergeRowsInserted;
    private String mergeDesc;
    private String matchLogFileName;
    private boolean matchAppendToLogInd;
    private int matchProcessCnt;
    private int matchShowProgressFreqw;
    private boolean matchDebugModeInd;
    private String matchRollbackSegmentName;
    private boolean augmentNull;
    private String matchScriptFileName;
    private int matchTotalSteps;
    private int matchStepsCompleted;
    private int matchRowsInserted;
    private String batchFileName;
    private String selectClause;
    private String fromClause;
    private String whereClause;
    private String filterCriteria;
    private String resultsTableOwner;
    private boolean matchBreakInd;
    private String matchType;
    private String lastUpdateOSUser;
    private String matchStepDesc;
    private String mergeStepDesc;
    private boolean mergeTablesBackupInd;
    private String matchStatus;
    private int lastBackupNo;
    private boolean checkedOutInd;
    private Date checkedOutDate;
    private String checkedOutUser;
    private String checkedOutOSUser;
    private String tempSourceTableName;
    private String tempCanDupTableName;
    private String tempCandDupTableName;
    private String indexColumName0;
    private String indexColumName1;
    private String indexColumName2;
    private String indexColumName3;
    private String indexColumName4;
    private String indexColumName5;
    private String indexColumName6;
    private String indexColumName7;
    private String indexColumName8;
    private String indexColumName9;
    private String fromClauseDB;
    private String indexColumnType0;
    private String indexColumnType1;
    private String indexColumnType2;
    private String indexColumnType3;
    private String indexColumnType4;
    private String indexColumnType5;
    private String indexColumnType6;
    private String indexColumnType7;
    private String indexColumnType8;
    private String indexColumnType9;
    private boolean matchSendEmailInd;
    private boolean mergeSendEmailInd;
    private boolean truncateCandDup;
    private String xRefOwner;
    private String xRefTableName;
    private boolean autoMatchActiveInd;
    List <MatchXrefMap> matchXRefMap;
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

    public String getIndexColumName0() {
		return indexColumName0;
	}

	public void setIndexColumName0(String indexColumName0) {
		this.indexColumName0 = indexColumName0;
	}

	public String getIndexColumName1() {
		return indexColumName1;
	}

	public void setIndexColumName1(String indexColumName1) {
		this.indexColumName1 = indexColumName1;
	}

	public String getIndexColumName2() {
		return indexColumName2;
	}

	public void setIndexColumName2(String indexColumName2) {
		this.indexColumName2 = indexColumName2;
	}

	public String getIndexColumName3() {
		return indexColumName3;
	}

	public void setIndexColumName3(String indexColumName3) {
		this.indexColumName3 = indexColumName3;
	}

	public String getIndexColumName4() {
		return indexColumName4;
	}

	public void setIndexColumName4(String indexColumName4) {
		this.indexColumName4 = indexColumName4;
	}

	public String getIndexColumName5() {
		return indexColumName5;
	}

	public void setIndexColumName5(String indexColumName5) {
		this.indexColumName5 = indexColumName5;
	}

	public String getIndexColumName6() {
		return indexColumName6;
	}

	public void setIndexColumName6(String indexColumName6) {
		this.indexColumName6 = indexColumName6;
	}

	public String getIndexColumName7() {
		return indexColumName7;
	}

	public void setIndexColumName7(String indexColumName7) {
		this.indexColumName7 = indexColumName7;
	}

	public String getIndexColumName8() {
		return indexColumName8;
	}

	public void setIndexColumName8(String indexColumName8) {
		this.indexColumName8 = indexColumName8;
	}

	public String getIndexColumName9() {
		return indexColumName9;
	}

	public void setIndexColumName9(String indexColumName9) {
		this.indexColumName9 = indexColumName9;
	}

	public String getIndexColumnType0() {
		return indexColumnType0;
	}

	public void setIndexColumnType0(String indexColumnType0) {
		this.indexColumnType0 = indexColumnType0;
	}

	public String getIndexColumnType1() {
		return indexColumnType1;
	}

	public void setIndexColumnType1(String indexColumnType1) {
		this.indexColumnType1 = indexColumnType1;
	}

	public String getIndexColumnType2() {
		return indexColumnType2;
	}

	public void setIndexColumnType2(String indexColumnType2) {
		this.indexColumnType2 = indexColumnType2;
	}

	public String getIndexColumnType3() {
		return indexColumnType3;
	}

	public void setIndexColumnType3(String indexColumnType3) {
		this.indexColumnType3 = indexColumnType3;
	}

	public String getIndexColumnType4() {
		return indexColumnType4;
	}

	public void setIndexColumnType4(String indexColumnType4) {
		this.indexColumnType4 = indexColumnType4;
	}

	public String getIndexColumnType5() {
		return indexColumnType5;
	}

	public void setIndexColumnType5(String indexColumnType5) {
		this.indexColumnType5 = indexColumnType5;
	}

	public String getIndexColumnType6() {
		return indexColumnType6;
	}

	public void setIndexColumnType6(String indexColumnType6) {
		this.indexColumnType6 = indexColumnType6;
	}

	public String getIndexColumnType7() {
		return indexColumnType7;
	}

	public void setIndexColumnType7(String indexColumnType7) {
		this.indexColumnType7 = indexColumnType7;
	}

	public String getIndexColumnType8() {
		return indexColumnType8;
	}

	public void setIndexColumnType8(String indexColumnType8) {
		this.indexColumnType8 = indexColumnType8;
	}

	public String getIndexColumnType9() {
		return indexColumnType9;
	}

	public void setIndexColumnType9(String indexColumnType9) {
		this.indexColumnType9 = indexColumnType9;
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

   
    public String getMatchScriptFileName() {
        return matchScriptFileName;
    }

    public void setMatchScriptFileName(String matchScriptFileName) {
        this.matchScriptFileName = matchScriptFileName;
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

    public List<MatchXrefMap> getMatchXRefMap() {
        return matchXRefMap;
    }

    public void setMatchXRefMap(List<MatchXrefMap> matchXRefMap) {
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


    public String getMergeStepDesc() {
        return mergeStepDesc;
    }

    public void setMergeStepDesc(String mergeStepDesc) {
        this.mergeStepDesc = mergeStepDesc;
    }

    public boolean isAutoMatchActiveInd() {
		return autoMatchActiveInd;
	}

	public void setAutoMatchActiveInd(boolean autoMatchActiveInd) {
		this.autoMatchActiveInd = autoMatchActiveInd;
	}

	public boolean isCheckedOutInd() {
		return checkedOutInd;
	}

	public void setCheckedOutInd(boolean checkedOutInd) {
		this.checkedOutInd = checkedOutInd;
	}

	public boolean isMatchAppendToLogInd() {
		return matchAppendToLogInd;
	}

	public void setMatchAppendToLogInd(boolean matchAppendToLogInd) {
		this.matchAppendToLogInd = matchAppendToLogInd;
	}

	public boolean isMatchBreakInd() {
		return matchBreakInd;
	}

	public void setMatchBreakInd(boolean matchBreakInd) {
		this.matchBreakInd = matchBreakInd;
	}

	public boolean isMatchDebugModeInd() {
		return matchDebugModeInd;
	}

	public void setMatchDebugModeInd(boolean matchDebugModeInd) {
		this.matchDebugModeInd = matchDebugModeInd;
	}

	public int getMatchProcessCnt() {
		return matchProcessCnt;
	}

	public void setMatchProcessCnt(int matchProcessCnt) {
		this.matchProcessCnt = matchProcessCnt;
	}

	public String getMatchRunTablePkColumnFormat() {
		return matchRunTablePkColumnFormat;
	}

	public void setMatchRunTablePkColumnFormat(String matchRunTablePkColumnFormat) {
		this.matchRunTablePkColumnFormat = matchRunTablePkColumnFormat;
	}

	public boolean isMatchSendEmailInd() {
		return matchSendEmailInd;
	}

	public void setMatchSendEmailInd(boolean matchSendEmailInd) {
		this.matchSendEmailInd = matchSendEmailInd;
	}

	public boolean isMergeSendEmailInd() {
		return mergeSendEmailInd;
	}

	public void setMergeSendEmailInd(boolean mergeSendEmailInd) {
		this.mergeSendEmailInd = mergeSendEmailInd;
	}

	public int getMergeStepsCompleted() {
		return mergeStepsCompleted;
	}

	public void setMergeStepsCompleted(int mergeStepsCompleted) {
		this.mergeStepsCompleted = mergeStepsCompleted;
	}

	public boolean isMergeTablesBackupInd() {
		return mergeTablesBackupInd;
	}

	public void setMergeTablesBackupInd(boolean mergeTablesBackupInd) {
		this.mergeTablesBackupInd = mergeTablesBackupInd;
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
