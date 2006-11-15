package ca.sqlpower.matchmaker.swingui.action;

import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.commons.digester.Digester;
import org.apache.log4j.Logger;
import org.hibernate.Transaction;
import org.xml.sax.SAXException;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.SwingUserSettings;
import ca.sqlpower.matchmaker.hibernate.PlMatch;
import ca.sqlpower.matchmaker.hibernate.PlMatchCriterion;
import ca.sqlpower.matchmaker.hibernate.PlMatchGroup;
import ca.sqlpower.matchmaker.hibernate.PlMergeConsolidateCriteria;
import ca.sqlpower.matchmaker.hibernate.PlMergeCriteria;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.matchmaker.util.HibernateUtil;

public class PlMatchImportAction extends AbstractAction {

	private static final Logger logger = Logger.getLogger(PlMatchImportAction.class);
    private final MatchMakerSwingSession swingSession;

	private PlMatch match;
	private JFrame owningFrame;

	public PlMatchImportAction(MatchMakerSwingSession swingSession, JFrame owningFrame) {
		super("Import",
				ASUtils.createJLFIcon( "general/Import",
                "Import",
                ArchitectFrame.getMainInstance().getSprefs().getInt(SwingUserSettings.ICON_SIZE, 24)));
		putValue(SHORT_DESCRIPTION, "Import Match");
        this.swingSession = swingSession;
		this.owningFrame = owningFrame;
	}


	public void actionPerformed(ActionEvent e) {
		JFileChooser fc = new JFileChooser(
				swingSession.getLastImportExportAccessPath());
		fc.setFileFilter(ASUtils.XML_FILE_FILTER);
		fc.setDialogTitle("Import Match");

		File importFile = null;
		int fcChoice = fc.showOpenDialog(owningFrame);

		if (fcChoice == JFileChooser.APPROVE_OPTION) {
			importFile = fc.getSelectedFile();
			swingSession.setLastImportExportAccessPath(
					importFile.getAbsolutePath());

			BufferedInputStream in = null;
			try {
				in = new BufferedInputStream(new FileInputStream(importFile));
				load(in);
			} catch (FileNotFoundException e1) {
				ASUtils.showExceptionDialogNoReport(
						"File Not Found", e1 );
			} catch (IOException e1) {
				ASUtils.showExceptionDialogNoReport(
						"IO Error", e1 );
			} catch (ArchitectException e1) {
				ASUtils.showExceptionDialogNoReport(
						"Unexpected Error", e1 );
			}

			if ( match == null ) {
				JOptionPane.showConfirmDialog(null,
						"Unable to read match ID from XML",
						"XML File error",
						JOptionPane.ERROR_MESSAGE);
				return;
			} else {

			    System.out.println("id:" + match.getMatchId());
                System.out.println("desc:" + match.getMatchDesc());
                System.out.println("table:" + match.getMatchTable());

                System.out.println("group size:"
                        + match.getPlMatchGroups().size());
                List<PlMatchGroup> l = new ArrayList<PlMatchGroup>(match
                        .getPlMatchGroups());
                for (PlMatchGroup g : l) {
                    System.out.println("group id:" + g.getGroupId());
                    for (PlMatchCriterion c : g.getPlMatchCriterias()) {
                        System.out.println("         PlMatchCriterion:"
                                + c.getColumnName());
                    }
                }
                for (PlMergeCriteria c : match.getPlMergeCriteria()) {
                    System.out.println("merge crit=" + c.getTableName() + "."
                            + c.getIndexColumnName0());
                }
                for (PlMergeConsolidateCriteria c : match
                        .getPlMergeConsolidateCriterias()) {
                    System.out.println("merge con crit=" + c.getTableName()
                            + "." + c.getColumnName());
                }

                // XXX: check for duplication
				/*Match match2 = swingSession.getMatchByName(match.getMatchId());
				if ( match2 != null ) {
					int option = JOptionPane.showConfirmDialog(
							null,
		                    "Match ["+match.getMatchId()+"] Exists! Do you want to overwrite it?",
		                    "Match ["+match.getMatchId()+"] Exists!",
		                    JOptionPane.OK_CANCEL_OPTION );
					if ( option != JOptionPane.OK_OPTION ) {
						return;
					} else {
						HibernateUtil.primarySession().delete(match2);
					}
				}*/
			}

			Transaction tx = HibernateUtil.primarySession().beginTransaction();
            HibernateUtil.primarySession().save(match);
			HibernateUtil.primarySession().flush();
			tx.commit();
			HibernateUtil.primarySession().refresh(match);
			HibernateUtil.primarySession().flush();



		}
	}


    public void load(InputStream in) throws IOException, ArchitectException {

        // use digester to read from file
        try {
            setupDigester().parse(in);
        } catch (SAXException ex) {
            logger.error("SAX Exception in config file parse!", ex);
            throw new ArchitectException("Syntax error in Project file", ex);
        } catch (IOException ex) {
            logger.error("IO Exception in config file parse!", ex);
            throw new ArchitectException("I/O Error", ex);
        } catch (Exception ex) {
            logger.error("General Exception in config file parse!", ex);
            throw new ArchitectException("Unexpected Exception", ex);
        }

    }


    private Digester setupDigester() {
        Digester d = new Digester();
        d.setValidating(false);
        match = new PlMatch();
        d.push(match);

        d.addSetProperties("EXPORT/PL_MATCH");
        d.addCallMethod("EXPORT/PL_MATCH/MATCH_ID", "setMatchId", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MATCH_DESC", "setMatchDesc", 0);
        d.addCallMethod("EXPORT/PL_MATCH/TABLE_OWNER", "setTableOwner", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MATCH_TABLE", "setMatchTable", 0);
        d.addCallMethod("EXPORT/PL_MATCH/PK_COLUMN", "setPkColumn", 0);
        d.addCallMethod("EXPORT/PL_MATCH/FILTER", "setFilter", 0);
        d.addCallMethod("EXPORT/PL_MATCH/RESULTS_TABLE", "setResultsTable", 0);
        d.addCallMethod("EXPORT/PL_MATCH/CREATE_DATE/DATE", "setCreateDate", 0);
        d.addCallMethod("EXPORT/PL_MATCH/LAST_UPDATE_DATE/DATE", "setLastUpdateDate", 0);
        d.addCallMethod("EXPORT/PL_MATCH/LAST_UPDATE_USER", "setLastUpdateUser", 0);

        d.addCallMethod("EXPORT/PL_MATCH/SEQUENCE_NAME", "setSequenceName", 0);
        d.addCallMethod("EXPORT/PL_MATCH/COMPILE_FLAG", "setCompileFlag", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MERGE_SCRIPT_FILE_NAME", "setMergeScriptFileName", 0);
        d.addCallMethod("EXPORT/PL_MATCH/AUTO_MATCH_THRESHOLD", "setAutoMatchThreshold", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MERGE_COMPLETION_DATE/DATE", "setMergeCompletionDate", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MERGE_LAST_USER", "setMergeLastUser", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MERGE_RUN_STATUS", "setMergeRunStatus", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MATCH_LAST_RUN_DATE/DATE", "setMatchLastRunDate", 0);

        d.addCallMethod("EXPORT/PL_MATCH/MATCH_LAST_RUN_USER", "setMatchLastRunUser", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MERGE_TOTAL_STEPS", "setMergeTotalSteps", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MERGE_STEPS_COMPLETED", "setMergeStepsCompleted", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MERGE_LAST_RUN_DATE/DATE", "setMergeLastRunDate", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MERGE_LAST_RUN_USER", "setMergeLastRunUser", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MATCH_PACKAGE_NAME", "setMatchPackageName", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MATCH_PROCEDURE_NAME_ALL", "setMatchProcedureNameAll", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MATCH_PROCEDURE_NAME_ONE", "setMatchProcedureNameOne", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MERGE_PACKAGE_NAME", "setMergePackageName", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MERGE_PROCEDURE_NAME", "setMergeProcedureName", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MATCH_RUN_STATUS", "setMatchRunStatus", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MATCH_TABLE_PK_COLUMN_FORMAT", "setMatchTablePkColumnFormat", 0);


        d.addCallMethod("EXPORT/PL_MATCH/MERGE_ROWS_INSERTED", "setMergeRowsInserted", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MERGE_DESC", "setMergeDesc", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MATCH_LOG_FILE_NAME", "setMatchLogFileName", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MATCH_APPEND_TO_LOG_IND", "setMatchAppendToLogInd", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MATCH_PROCESS_CNT", "setMatchProcessCnt", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MATCH_SHOW_PROGRESS_FREQ", "setMatchShowProgressFreq", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MATCH_DEBUG_MODE_IND", "setMatchDebugModeInd", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MATCH_ROLLBACK_SEGMENT_NAME", "setMatchRollbackSegmentName", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MERGE_LOG_FILE_NAME", "setMergeLogFileName", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MERGE_APPEND_TO_LOG_IND", "setMergeAppendToLogInd", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MERGE_PROCESS_CNT", "setMergeProcessCnt", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MERGE_SHOW_PROGRESS_FREQ", "setMergeShowProgressFreq", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MERGE_DEBUG_MODE_IND", "setMergeDebugModeInd", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MERGE_ROLLBACK_SEGMENT_NAME", "setMergeRollbackSegmentName", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MERGE_AUGMENT_NULL_IND", "setMergeAugmentNullInd", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MATCH_SCRIPT_FILE_NAME", "setMatchScriptFileName", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MATCH_TOTAL_STEPS", "setMatchTotalSteps", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MATCH_STEPS_COMPLETED", "setMatchStepsCompleted", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MATCH_ROWS_INSERTED", "setMatchRowsInserted", 0);
        d.addCallMethod("EXPORT/PL_MATCH/BATCH_FILE_NAME", "setBatchFileName", 0);
        d.addCallMethod("EXPORT/PL_MATCH/SELECT_CLAUSE", "setSelectClause", 0);
        d.addCallMethod("EXPORT/PL_MATCH/FROM_CLAUSE", "setFromClause", 0);
        d.addCallMethod("EXPORT/PL_MATCH/WHERE_CLAUSE", "setWhereClause", 0);
        d.addCallMethod("EXPORT/PL_MATCH/FILTER_CRITERIA", "setFilterCriteria", 0);
        d.addCallMethod("EXPORT/PL_MATCH/RESULTS_TABLE_OWNER", "setResultsTableOwner", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MATCH_BREAK_IND", "setMatchBreakInd", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MATCH_TYPE", "setMatchType", 0);
        d.addCallMethod("EXPORT/PL_MATCH/LAST_UPDATE_OS_USER", "setLastUpdateOsUser", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MATCH_STEP_DESC", "setMatchStepDesc", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MERGE_STEP_DESC", "setMergeStepDesc", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MERGE_TABLES_BACKUP_IND", "setMergeTablesBackupInd", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MATCH_STATUS", "setMatchStatus", 0);
        d.addCallMethod("EXPORT/PL_MATCH/LAST_BACKUP_NO", "setLastBackupNo", 0);
        d.addCallMethod("EXPORT/PL_MATCH/CHECKED_OUT_IND", "setCheckedOutInd", 0);
        d.addCallMethod("EXPORT/PL_MATCH/CHECKED_OUT_DATE/DATE", "setCheckedOutDate", 0);
        d.addCallMethod("EXPORT/PL_MATCH/CHECKED_OUT_USER", "setCheckedOutUser", 0);
        d.addCallMethod("EXPORT/PL_MATCH/CHECKED_OUT_OS_USER", "setCheckedOutOsUser", 0);
        d.addCallMethod("EXPORT/PL_MATCH/TEMP_SOURCE_TABLE_NAME", "setTempSourceTableName", 0);
        d.addCallMethod("EXPORT/PL_MATCH/TEMP_CAN_DUP_TABLE_NAME", "setTempCandDupTableName", 0);
        d.addCallMethod("EXPORT/PL_MATCH/TEMP_CAND_DUP_TABLE_NAME", "setTempCandDupTableName", 0);
        d.addCallMethod("EXPORT/PL_MATCH/INDEX_COLUMN_NAME0", "setIndexColumnName0", 0);
        d.addCallMethod("EXPORT/PL_MATCH/INDEX_COLUMN_NAME1", "setIndexColumnName1", 0);
        d.addCallMethod("EXPORT/PL_MATCH/INDEX_COLUMN_NAME2", "setIndexColumnName2", 0);
        d.addCallMethod("EXPORT/PL_MATCH/INDEX_COLUMN_NAME3", "setIndexColumnName3", 0);
        d.addCallMethod("EXPORT/PL_MATCH/INDEX_COLUMN_NAME4", "setIndexColumnName4", 0);
        d.addCallMethod("EXPORT/PL_MATCH/INDEX_COLUMN_NAME5", "setIndexColumnName5", 0);
        d.addCallMethod("EXPORT/PL_MATCH/INDEX_COLUMN_NAME6", "setIndexColumnName6", 0);
        d.addCallMethod("EXPORT/PL_MATCH/INDEX_COLUMN_NAME7", "setIndexColumnName7", 0);
        d.addCallMethod("EXPORT/PL_MATCH/INDEX_COLUMN_NAME8", "setIndexColumnName8", 0);
        d.addCallMethod("EXPORT/PL_MATCH/INDEX_COLUMN_NAME9", "setIndexColumnName9", 0);
        d.addCallMethod("EXPORT/PL_MATCH/FROM_CLAUSE_DB", "setFromClauseDb", 0);
        d.addCallMethod("EXPORT/PL_MATCH/INDEX_COLUMN_TYPE0", "setIndexColumnType0", 0);
        d.addCallMethod("EXPORT/PL_MATCH/INDEX_COLUMN_TYPE1", "setIndexColumnType1", 0);
        d.addCallMethod("EXPORT/PL_MATCH/INDEX_COLUMN_TYPE2", "setIndexColumnType2", 0);
        d.addCallMethod("EXPORT/PL_MATCH/INDEX_COLUMN_TYPE3", "setIndexColumnType3", 0);
        d.addCallMethod("EXPORT/PL_MATCH/INDEX_COLUMN_TYPE4", "setIndexColumnType4", 0);
        d.addCallMethod("EXPORT/PL_MATCH/INDEX_COLUMN_TYPE5", "setIndexColumnType5", 0);
        d.addCallMethod("EXPORT/PL_MATCH/INDEX_COLUMN_TYPE6", "setIndexColumnType6", 0);
        d.addCallMethod("EXPORT/PL_MATCH/INDEX_COLUMN_TYPE7", "setIndexColumnType7", 0);
        d.addCallMethod("EXPORT/PL_MATCH/INDEX_COLUMN_TYPE8", "setIndexColumnType8", 0);
        d.addCallMethod("EXPORT/PL_MATCH/INDEX_COLUMN_TYPE9", "setIndexColumnType9", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MATCH_SEND_EMAIL_IND", "setMatchSendEmailInd", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MERGE_SEND_EMAIL_IND", "setMergeSendEmailInd", 0);
        d.addCallMethod("EXPORT/PL_MATCH/TRUNCATE_CAND_DUP_IND", "setTruncateCandDupInd", 0);
        d.addCallMethod("EXPORT/PL_MATCH/XREF_OWNER", "setXrefOwner", 0);
        d.addCallMethod("EXPORT/PL_MATCH/XREF_TABLE_NAME", "setXrefTableName", 0);
        d.addCallMethod("EXPORT/PL_MATCH/AUTO_MATCH_ACTIVE_IND", "setAutoMatchActiveInd", 0);
        d.addCallMethod("EXPORT/PL_MATCH/TABLE_CATALOG", "setTableCatalog", 0);
        d.addCallMethod("EXPORT/PL_MATCH/RESULTS_TABLE_CATALOG", "setResultsTableCatalog", 0);
        d.addCallMethod("EXPORT/PL_MATCH/XREF_CATALOG", "setXrefCatalog", 0);



        d.addObjectCreate("EXPORT/PL_MATCH_GROUP", PlMatchGroup.class);
        d.addSetProperties("EXPORT/PL_MATCH_GROUP");
        d.addSetNext("EXPORT/PL_MATCH_GROUP", "addMatchGroup");

        d.addCallMethod("EXPORT/PL_MATCH_GROUP/MATCH_ID", "setMatchId", 0);
        d.addCallMethod("EXPORT/PL_MATCH_GROUP/GROUP_ID", "setGroupId", 0);
        d.addCallMethod("EXPORT/PL_MATCH_GROUP/DESCRIPTION", "setDescription", 0);
        d.addCallMethod("EXPORT/PL_MATCH_GROUP/MATCH_PERCENT", "setMatchPercent", 0);
        d.addCallMethod("EXPORT/PL_MATCH_GROUP/LAST_UPDATE_DATE/DATE", "setLastUpdateDate", 0);
        d.addCallMethod("EXPORT/PL_MATCH_GROUP/LAST_UPDATE_USER", "setLastUpdateUser", 0);
        d.addCallMethod("EXPORT/PL_MATCH_GROUP/FILTER_CRITERIA", "setFilterCriteria", 0);
        d.addCallMethod("EXPORT/PL_MATCH_GROUP/ACTIVE_IND", "setActiveInd", 0);
        d.addCallMethod("EXPORT/PL_MATCH_GROUP/LAST_UPDATE_OS_USER", "setLastUpdateOsUser", 0);



        d.addObjectCreate("EXPORT/PL_MATCH_CRITERIA", PlMatchCriterion.class);
        d.addSetProperties("EXPORT/PL_MATCH_CRITERIA");
        d.addSetNext("EXPORT/PL_MATCH_CRITERIA", "addMatchCriteria");

        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/MATCH_ID", "setMatchId", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/GROUP_ID", "setGroupId", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/COLUMN_NAME", "setColumnName", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/CASE_SENSITIVE_IND", "setCaseSensitiveInd", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/SUPPRESS_CHAR", "setSuppressChar", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/SOUND_IND", "setSoundInd", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/FIRST_N_CHAR", "setFirstNChar", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/LAST_UPDATE_DATE/DATE", "setLastUpdateDate", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/LAST_UPDATE_USER", "setLastUpdateUser", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/SEQ_NO", "setSeqNo", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/MATCH_START", "setMatchStart", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/MATCH_END", "setMatchEnd", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/VARIANCE_AMT", "setVarianceAmt", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/VARIANCE_TYPE", "setVarianceType", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/LAST_UPDATE_OS_USER", "setLastUpdateOsUser", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/ALLOW_NULL_IND", "setAllowNullInd", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/TRANSLATE_GROUP_NAME", "setTranslateGroupName", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/REMOVE_SPECIAL_CHARS", "setRemoveSpecialChars", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/REORDER_IND", "setReorderInd", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/FIRST_N_CHAR_BY_WORD_IND", "setFirstNCharByWordInd", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/REPLACE_WITH_SPACE", "setReplaceWithSpace", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/REPLACE_WITH_SPACE_IND", "setReplaceWithSpaceInd", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/FIRST_N_CHAR_BY_WORD", "setFirstNCharByWord", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/MIN_WORDS_IN_COMMON", "setMinWordsInCommon", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/MATCH_FIRST_PLUS_ONE_IND", "setMatchFirstPlusOneInd", 0);
        d.addCallMethod("EXPORT/PL_MATCH_CRITERIA/WORDS_IN_COMMON_NUM_WORDS", "setWordsInCommonNumWords", 0);

        d.addObjectCreate("EXPORT/PL_MERGE_CRITERIA", PlMergeCriteria.class);
        d.addSetProperties("EXPORT/PL_MERGE_CRITERIA");
        d.addSetNext("EXPORT/PL_MERGE_CRITERIA", "addMergeCriteria");

        d.addCallMethod("EXPORT/PL_MERGE_CRITERIA/MATCH_ID", "setMatchId", 0);
        d.addCallMethod("EXPORT/PL_MERGE_CRITERIA/TABLE_NAME", "setTableName", 0);
        d.addCallMethod("EXPORT/PL_MERGE_CRITERIA/COLUMN_NAME", "setColumnName", 0);
        d.addCallMethod("EXPORT/PL_MERGE_CRITERIA/LAST_UPDATE_DATE/DATE", "setLastUpdateDate", 0);
        d.addCallMethod("EXPORT/PL_MERGE_CRITERIA/LAST_UPDATE_USER", "setLastUpdateUser", 0);
        d.addCallMethod("EXPORT/PL_MERGE_CRITERIA/DELETE_DUP_IND", "setDeleteDupInd", 0);
        d.addCallMethod("EXPORT/PL_MERGE_CRITERIA/SEQ_NO", "setSeqNo", 0);
        d.addCallMethod("EXPORT/PL_MERGE_CRITERIA/OWNER", "setTableOwner", 0);
        d.addCallMethod("EXPORT/PL_MERGE_CRITERIA/LAST_UPDATE_OS_USER", "setLastUpdateOsUser", 0);
        d.addCallMethod("EXPORT/PL_MERGE_CRITERIA/PRIMARY_KEY_INDEX", "setPrimaryKeyIndex", 0);
        d.addCallMethod("EXPORT/PL_MERGE_CRITERIA/INDEX_COLUMN_NAME0", "setIndexColumnName0", 0);
        d.addCallMethod("EXPORT/PL_MERGE_CRITERIA/INDEX_COLUMN_NAME1", "setIndexColumnName1", 0);
        d.addCallMethod("EXPORT/PL_MERGE_CRITERIA/INDEX_COLUMN_NAME2", "setIndexColumnName2", 0);
        d.addCallMethod("EXPORT/PL_MERGE_CRITERIA/INDEX_COLUMN_NAME3", "setIndexColumnName3", 0);
        d.addCallMethod("EXPORT/PL_MERGE_CRITERIA/INDEX_COLUMN_NAME4", "setIndexColumnName4", 0);
        d.addCallMethod("EXPORT/PL_MERGE_CRITERIA/INDEX_COLUMN_NAME5", "setIndexColumnName5", 0);
        d.addCallMethod("EXPORT/PL_MERGE_CRITERIA/INDEX_COLUMN_NAME6", "setIndexColumnName6", 0);
        d.addCallMethod("EXPORT/PL_MERGE_CRITERIA/INDEX_COLUMN_NAME7", "setIndexColumnName7", 0);
        d.addCallMethod("EXPORT/PL_MERGE_CRITERIA/INDEX_COLUMN_NAME8", "setIndexColumnName8", 0);
        d.addCallMethod("EXPORT/PL_MERGE_CRITERIA/INDEX_COLUMN_NAME9", "setIndexColumnName9", 0);
        d.addCallMethod("EXPORT/PL_MERGE_CRITERIA/TABLE_CATALOG", "setTableCatalog", 0);


        d.addObjectCreate("EXPORT/PL_MERGE_CONSOLIDATE_CRITERIA", PlMergeConsolidateCriteria.class);
        d.addSetProperties("EXPORT/PL_MERGE_CONSOLIDATE_CRITERIA");
        d.addSetNext("EXPORT/PL_MERGE_CONSOLIDATE_CRITERIA", "addMergeConsolidateCriteria");

        d.addCallMethod("EXPORT/PL_MERGE_CONSOLIDATE_CRITERIA/MATCH_ID", "setMatchId", 0);
        d.addCallMethod("EXPORT/PL_MERGE_CONSOLIDATE_CRITERIA/OWNER", "setTableOwner", 0);
        d.addCallMethod("EXPORT/PL_MERGE_CONSOLIDATE_CRITERIA/TABLE_NAME", "setTableName", 0);
        d.addCallMethod("EXPORT/PL_MERGE_CONSOLIDATE_CRITERIA/COLUMN_NAME", "setColumnName", 0);
        d.addCallMethod("EXPORT/PL_MERGE_CONSOLIDATE_CRITERIA/COLUMN_FORMAT", "setColumnFormat", 0);
        d.addCallMethod("EXPORT/PL_MERGE_CONSOLIDATE_CRITERIA/ACTION_TYPE", "setActionType", 0);
        d.addCallMethod("EXPORT/PL_MERGE_CONSOLIDATE_CRITERIA/LAST_UPDATE_DATE/DATE", "setLastUpdateDate", 0);
        d.addCallMethod("EXPORT/PL_MERGE_CONSOLIDATE_CRITERIA/LAST_UPDATE_USER", "setLastUpdateUser", 0);
        d.addCallMethod("EXPORT/PL_MERGE_CONSOLIDATE_CRITERIA/CAN_UPDATE_ACTION_IND", "setCanUpdateActionInd", 0);
        d.addCallMethod("EXPORT/PL_MERGE_CONSOLIDATE_CRITERIA/COLUMN_LENGTH", "setColumnLength", 0);
        d.addCallMethod("EXPORT/PL_MERGE_CONSOLIDATE_CRITERIA/LAST_UPDATE_OS_USER", "setLastUpdateOsUser", 0);
        d.addCallMethod("EXPORT/PL_MERGE_CONSOLIDATE_CRITERIA/TABLE_CATALOG", "setTableCatalog", 0);
        return d;
    }



}