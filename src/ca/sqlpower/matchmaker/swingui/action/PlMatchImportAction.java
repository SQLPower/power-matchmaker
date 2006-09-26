package ca.sqlpower.matchmaker.swingui.action;

import java.awt.event.ActionEvent;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import org.apache.commons.digester.Digester;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.SwingUserSettings;
import ca.sqlpower.matchmaker.hibernate.PlMatch;
import ca.sqlpower.matchmaker.hibernate.PlMatchGroup;
import ca.sqlpower.matchmaker.swingui.MatchMakerFrame;

public class PlMatchImportAction extends AbstractAction {


	private static final Logger logger = Logger.getLogger(PlMatchImportAction.class);
	private PlMatch match;

	public PlMatchImportAction() {

		super("Import",
				ASUtils.createJLFIcon( "general/Import",
                "Import",
                ArchitectFrame.getMainInstance().getSprefs().getInt(SwingUserSettings.ICON_SIZE, 24)));
		putValue(SHORT_DESCRIPTION, "Import Match");


	}




	public void actionPerformed(ActionEvent e) {
		JFileChooser fc = new JFileChooser(
				MatchMakerFrame.getMainInstance().getLastExportAccessPath());
		fc.setFileFilter(ASUtils.XML_FILE_FILTER);
		fc.setDialogTitle("Import Match");

		File importFile = null;
		int fcChoice = fc.showOpenDialog(null);

		if (fcChoice == JFileChooser.APPROVE_OPTION) {
			importFile = fc.getSelectedFile();
			MatchMakerFrame.getMainInstance().setLastExportAccessPath(
					importFile.getAbsolutePath());

			BufferedInputStream in = null;
			try {
				in = new BufferedInputStream(new FileInputStream(importFile));
				load(in);
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (ArchitectException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
if ( match == null ) {
	System.out.println("match is null ");
	return;
}
System.out.println("id:"+match.getMatchId());
System.out.println("desc:"+match.getMatchDesc());
System.out.println("table:"+match.getMatchTable());
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
        d.addCallMethod("EXPORT/PL_MATCH/TABLE_OWNER", "setTableOwner", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MATCH_TABLE", "setMatchTable", 0);
        d.addCallMethod("EXPORT/PL_MATCH/PK_COLUMN", "setPkColumn", 0);
        d.addCallMethod("EXPORT/PL_MATCH/DESC", "setDesc", 0);
        d.addCallMethod("EXPORT/PL_MATCH/RESULTS_TABLE", "setResultsTable", 0);
//        d.addCallMethod("EXPORT/PL_MATCH/CREATE_DATE/DATE", "setCreateDate", 0);
//        d.addCallMethod("EXPORT/PL_MATCH/LAST_UPDATE_DATE/DATE", "setLastUpdateDate", 0);
        d.addCallMethod("EXPORT/PL_MATCH/LAST_UPDATE_USER", "setLastUpdateUser", 0);
        d.addCallMethod("EXPORT/PL_MATCH/COMPILE_FLAG", "setCompileFlag", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MERGE_SCRIPT_FILE_NAME", "setMergeScriptFileName", 0);
        d.addCallMethod("EXPORT/PL_MATCH/AUTO_MATCH_THRESHOLD", "setAutoMatchThreshold", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MERGE_RUN_STATUS", "setMergeRunStatus", 0);
//        d.addCallMethod("EXPORT/PL_MATCH/MATCH_LAST_RUN_DATE/DATE", "setMatchLastRunDate", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MATCH_LAST_RUN_USER", "setMatchLastRunUser", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MERGE_TOTAL_STEPS", "setMergeTotalSteps", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MERGE_STEPS_COMPLETED", "setMergeStepsCompleted", 0);
  //      d.addCallMethod("EXPORT/PL_MATCH/MERGE_LAST_RUN_DATE/DATE", "setMergeLastRunDate", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MERGE_LAST_RUN_USER", "setMergeLastRunUser", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MATCH_PACKAGE_NAME", "setMatchPackageName", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MATCH_PROCEDURE_NAME_ALL", "setMatchProcedureNameAll", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MATCH_PROCEDURE_NAME_ONE", "setMatchProcedureNameOne", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MERGE_PACKAGE_NAME", "setMergePackageName", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MERGE_PROCEDURE_NAME", "setMergeProcedureName", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MATCH_RUN_STATUS", "setMatchRunStatus", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MATCH_TABLE_PK_COLUMN_FORMAT", "setMatchTablePkColumnFormat", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MERGE_ROWS_INSERTED", "setMergeRowsInserted", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MATCH_LOG_FILE_NAME", "setMatchLogFileName", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MATCH_APPEND_TO_LOG_IND", "setMatchAppendToLogInd", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MATCH_PROCESS_CNT", "setMatchProcessCnt", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MATCH_SHOW_PROGRESS_FREQ", "setMatchShowProgressFreq", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MATCH_DEBUG_MODE_IND", "setMatchDebugModeInd", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MERGE_LOG_FILE_NAME", "setMergeLogFileName", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MERGE_APPEND_TO_LOG_IND", "setMergeAppendToLogInd", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MERGE_PROCESS_CNT", "setMergeProcessCnt", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MERGE_SHOW_PROGRESS_FREQ", "setMergeShowProgressFreq", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MERGE_DEBUG_MODE_IND", "setMergeDebugModeInd", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MERGE_AUGMENT_NULL_IND", "setMergeAugmentNullInd", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MATCH_SCRIPT_FILE_NAME", "setMatchScriptFileName", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MATCH_TOTAL_STEPS", "setMatchTotalSteps", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MATCH_STEPS_COMPLETED", "setMatchStepsCompleted", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MATCH_ROWS_INSERTED", "setMatchRowsInserted", 0);
        d.addCallMethod("EXPORT/PL_MATCH/RESULTS_TABLE_OWNER", "setResultsTable", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MATCH_TYPE", "setMatchType", 0);
        d.addCallMethod("EXPORT/PL_MATCH/LAST_UPDATE_OS_USER", "setLastUpdateOsUser", 0);
        d.addCallMethod("EXPORT/PL_MATCH/TEMP_SOURCE_TABLE_NAME", "setTempSourceTableName", 0);
        d.addCallMethod("EXPORT/PL_MATCH/TEMP_CAN_DUP_TABLE_NAME", "setTempCandDupTableName", 0);
        d.addCallMethod("EXPORT/PL_MATCH/TEMP_CAND_DUP_TABLE_NAME", "setTempCandDupTableName", 0);
        d.addCallMethod("EXPORT/PL_MATCH/INDEX_COLUMN_NAME0", "setIndexColumnName0", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MATCH_SEND_EMAIL_IND", "setMatchSendEmailInd", 0);
        d.addCallMethod("EXPORT/PL_MATCH/MERGE_SEND_EMAIL_IND", "setMergeSendEmailInd", 0);
        d.addCallMethod("EXPORT/PL_MATCH/TRUNCATE_CAND_DUP_IND", "setTruncateCandDupInd", 0);


        d.addObjectCreate("EXPORT/PL_MATCH/PL_MATCH_GROUP", PlMatchGroup.class);
        d.addSetProperties("EXPORT/PL_MATCH/PL_MATCH_GROUP");
        d.addSetNext("EXPORT/PL_MATCH/PL_MATCH_GROUP", "addMatchGroup");



        return d;
    }
}
