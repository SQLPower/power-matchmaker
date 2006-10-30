package ca.sqlpower.matchmaker.swingui.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.DateFormatAllowsNull;
import ca.sqlpower.architect.IOUtils;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.SwingUserSettings;
import ca.sqlpower.architect.swingui.ASUtils.FileExtensionFilter;
import ca.sqlpower.matchmaker.hibernate.PlFolder;
import ca.sqlpower.matchmaker.hibernate.PlMatch;
import ca.sqlpower.matchmaker.hibernate.PlMatchCriterion;
import ca.sqlpower.matchmaker.hibernate.PlMatchGroup;
import ca.sqlpower.matchmaker.hibernate.PlMergeConsolidateCriteria;
import ca.sqlpower.matchmaker.hibernate.PlMergeCriteria;
import ca.sqlpower.matchmaker.swingui.MatchMakerFrame;

public class PlMatchExportAction extends AbstractAction {


	private PlMatch match;
	private DateFormat df = new DateFormatAllowsNull();

	public PlMatchExportAction(PlMatch match) {

		super("Export",
				ASUtils.createJLFIcon( "general/Export",
						"Export",
						ArchitectFrame.getMainInstance().getSprefs().getInt(SwingUserSettings.ICON_SIZE, 24)));
		putValue(SHORT_DESCRIPTION, "Export Match");

		this.match = match;
	}


	public void actionPerformed(ActionEvent e) {

		if ( match == null ) {
			match = ArchitectUtils.getTreeObject(
					MatchMakerFrame.getMainInstance().getTree(),
					PlMatch.class );
		}
		if ( match == null ) {
			return;
		}
		JFileChooser fc = new JFileChooser(
				MatchMakerFrame.getMainInstance().getLastImportExportAccessPath());
		fc.setFileFilter(ASUtils.XML_FILE_FILTER);
		fc.setDialogTitle("Export Match");
		fc.setSelectedFile(
				new File("export_match_"+match.getMatchId()+"."+
						((FileExtensionFilter) ASUtils.XML_FILE_FILTER).getFilterExtension(0)));
		fc.setApproveButtonText("Save");

		File export = null;

		while (true) {
			int fcChoice = fc.showOpenDialog(null);
			if (fcChoice == JFileChooser.APPROVE_OPTION) {
				export = fc.getSelectedFile();
				MatchMakerFrame.getMainInstance().setLastImportExportAccessPath(
						export.getAbsolutePath());

				if ( export.exists()) {
					int response = JOptionPane.showConfirmDialog(
							MatchMakerFrame.getMainInstance(),
							"The file\n\n"+export.getPath()+
							"\n\nalready exists. Do you want to overwrite it?",
							"File Exists", JOptionPane.YES_NO_OPTION);
					if (response == JOptionPane.YES_OPTION ) {
						break;
					}
				} else {
					break;
				}
			} else {
				return;
			}
		}

		if ( export != null ) {
        	PrintWriter out;
        	try {
        		out = new PrintWriter(export);
        		save(out,"UTF-8");
        	} catch (IOException e1) {
        		throw new RuntimeException("IO Error during save", e1);
        	}
        }

	}

	 /**
     * Do just the writing part of save, given a PrintWriter
     * @param out - the file to write to
     * @return True iff the save completed OK
     * @throws IOException
     * @throws ArchitectException
     */
    public void save(PrintWriter out, String encoding) throws IOException {

        IOUtils ioo = new IOUtils();
        ioo.indent = 0;

        try {
            ioo.println(out, "<?xml version=\"1.0\" encoding=\""+encoding+"\"?>");
            ioo.println(out, "<EXPORT>");
            ioo.indent++;


            ioo.println(out, "<PL_MATCH>");
            ioo.indent++;
            ioo.println(out, "<MATCH_ID>"+
            		ArchitectUtils.escapeXML(match.getMatchId())+
            		"</MATCH_ID>");
            ioo.println(out, "<MATCH_DESC>"+
            		ArchitectUtils.escapeXML(match.getMatchDesc())+
            		"</MATCH_DESC>");
            ioo.println(out, "<TABLE_OWNER>"+
            		ArchitectUtils.escapeXML(match.getTableOwner())+
            		"</TABLE_OWNER>");
            ioo.println(out, "<MATCH_TABLE>"+
            		ArchitectUtils.escapeXML(match.getMatchTable())+
            		"</MATCH_TABLE>");
            ioo.println(out, "<PK_COLUMN>"+
            		ArchitectUtils.escapeXML(match.getPkColumn())+
            		"</PK_COLUMN>");
            ioo.println(out, "<RESULTS_TABLE>"+
            		ArchitectUtils.escapeXML(match.getResultsTable())+
            		"</RESULTS_TABLE>");
            ioo.println(out, "<CREATE_DATE><DATE>"+
            		ArchitectUtils.escapeXML(df.format(match.getCreateDate()))+
            		"</DATE></CREATE_DATE>");
            ioo.println(out, "<LAST_UPDATE_DATE><DATE>"+
            		ArchitectUtils.escapeXML(df.format(match.getLastUpdateDate()))+
            		"</DATE></LAST_UPDATE_DATE>");
            ioo.println(out, "<LAST_UPDATE_USER>"+
            		ArchitectUtils.escapeXML(match.getLastUpdateUser())+
            		"</LAST_UPDATE_USER>");
            ioo.println(out, "<MATCH_LAST_RUN_DATE><DATE>"+
            		ArchitectUtils.escapeXML(df.format(match.getMatchLastRunDate()))+
            		"</DATE></MATCH_LAST_RUN_DATE>");
            ioo.println(out, "<MATCH_LOG_FILE_NAME>"+
            		ArchitectUtils.escapeXML(match.getMatchLogFileName())+
            		"</MATCH_LOG_FILE_NAME>");
            ioo.println(out, "<MATCH_APPEND_TO_LOG_IND>"+
            		ArchitectUtils.escapeXML(match.isMatchAppendToLogInd()?"Y":"N")+
            		"</MATCH_APPEND_TO_LOG_IND>");
            ioo.println(out, "<MATCH_PROCESS_CNT>"+
            		match.getMatchProcessCnt()+
            		"</MATCH_PROCESS_CNT>");
            ioo.println(out, "<MATCH_SHOW_PROGRESS_FREQ>"+
            		match.getMatchShowProgressFreq()+
            		"</MATCH_SHOW_PROGRESS_FREQ>");
            ioo.println(out, "<RESULTS_TABLE_OWNER>"+
            		ArchitectUtils.escapeXML(match.getResultsTableOwner())+
            		"</RESULTS_TABLE_OWNER>");
            ioo.println(out, "<MATCH_TYPE>"+
            		ArchitectUtils.escapeXML(match.getMatchType())+
            		"</MATCH_TYPE>");
            ioo.println(out, "<LAST_UPDATE_OS_USER>"+
            		ArchitectUtils.escapeXML(match.getLastUpdateOsUser())+
            		"</LAST_UPDATE_OS_USER>");
            ioo.println(out, "<TEMP_SOURCE_TABLE_NAME>"+
            		ArchitectUtils.escapeXML(match.getTempSourceTableName())+
            		"</TEMP_SOURCE_TABLE_NAME>");
            ioo.println(out, "<TEMP_CAND_DUP_TABLE_NAME>"+
            		ArchitectUtils.escapeXML(match.getTempCandDupTableName())+
            		"</TEMP_CAND_DUP_TABLE_NAME>");
            ioo.println(out, "<MATCH_SEND_EMAIL_IND>"+
            		ArchitectUtils.escapeXML(match.isMatchSendEmailInd()?"Y":"N")+
            		"</MATCH_SEND_EMAIL_IND>");
            ioo.println(out, "<TRUNCATE_CAND_DUP_IND>"+
            		ArchitectUtils.escapeXML(match.isTruncateCandDupInd()?"Y":"N")+
            		"</TRUNCATE_CAND_DUP_IND>");

            ioo.println(out, "<MERGE_SCRIPT_FILE_NAME>"+
            		ArchitectUtils.escapeXML(match.getMergeScriptFileName())+
            		"</MERGE_SCRIPT_FILE_NAME>");
            ioo.println(out, "<AUTO_MATCH_THRESHOLD>"+
            		match.getAutoMatchThreshold()+
            		"</AUTO_MATCH_THRESHOLD>");
            ioo.println(out, "<MERGE_RUN_STATUS>"+
            		ArchitectUtils.escapeXML(match.getMergeRunStatus())+
            		"</MERGE_RUN_STATUS>");
            ioo.println(out, "<MERGE_LAST_RUN_DATE><DATE>"+
            		ArchitectUtils.escapeXML(df.format(match.getMergeLastRunDate()))+
            		"</DATE></MERGE_LAST_RUN_DATE>");
            ioo.println(out, "<MERGE_PACKAGE_NAME>"+
            		ArchitectUtils.escapeXML(match.getMergePackageName())+
            		"</MERGE_PACKAGE_NAME>");

            ioo.println(out, "<MERGE_PROCEDURE_NAME>"+
            		ArchitectUtils.escapeXML(match.getMergeProcedureName())+
            		"</MERGE_PROCEDURE_NAME>");
            ioo.println(out, "<MATCH_RUN_STATUS>"+
            		ArchitectUtils.escapeXML(match.getMatchRunStatus())+
            		"</MATCH_RUN_STATUS>");
            ioo.println(out, "<MATCH_TABLE_PK_COLUMN_FORMAT>"+
            		ArchitectUtils.escapeXML(match.getMatchTablePkColumnFormat())+
            		"</MATCH_TABLE_PK_COLUMN_FORMAT>");
            ioo.println(out, "<MATCH_DEBUG_MODE_IND>"+
            		ArchitectUtils.escapeXML(match.isMatchDebugModeInd()?"Y":"N")+
            		"</MATCH_DEBUG_MODE_IND>");
            ioo.println(out, "<MERGE_LOG_FILE_NAME>"+
            		ArchitectUtils.escapeXML(match.getMergeLogFileName())+
            		"</MERGE_LOG_FILE_NAME>");
            ioo.println(out, "<MERGE_APPEND_TO_LOG_IND>"+
            		ArchitectUtils.escapeXML(match.isMergeAppendToLogInd()?"Y":"N")+
            		"</MERGE_APPEND_TO_LOG_IND>");
            ioo.println(out, "<MERGE_PROCESS_CNT>"+
            		match.getMergeProcessCnt()+
            		"</MERGE_PROCESS_CNT>");
            ioo.println(out, "<MERGE_SHOW_PROGRESS_FREQ>"+
            		match.getMergeShowProgressFreq()+
            		"</MERGE_SHOW_PROGRESS_FREQ>");
            ioo.println(out, "<MERGE_AUGMENT_NULL_IND>"+
            		ArchitectUtils.escapeXML(match.isMergeAugmentNullInd()?"Y":"N")+
            		"</MERGE_AUGMENT_NULL_IND>");
            ioo.println(out, "<TEMP_CAN_DUP_TABLE_NAME>"+
            		ArchitectUtils.escapeXML(match.getTempCandDupTableName())+
            		"</TEMP_CAN_DUP_TABLE_NAME>");

            List<String> indexColNames = match.getIndexColumnNames();
            List<String> indexColTypes = match.getIndexColumnTypes();
            if (indexColNames.size() != indexColTypes.size()) {
                throw new IllegalStateException(
                        "IndexColNames has "+indexColNames.size()+
                        " items, but indexColTypes has "+indexColTypes.size()+
                        " items!  They have to be the same!");
            }
            for (int i = 0, n = indexColNames.size(); i < n; i++) {
                ioo.println(out, "<INDEX_COLUMN_NAME"+i+">"+
                        ArchitectUtils.escapeXML(indexColNames.get(i))+
                        "</INDEX_COLUMN_NAME"+i+">");
                ioo.println(out, "<INDEX_COLUMN_TYPE"+i+">"+
                        ArchitectUtils.escapeXML(indexColTypes.get(i))+
                        "</INDEX_COLUMN_TYPE"+i+">");
            }

            ioo.println(out, "<MERGE_SEND_EMAIL_IND>"+
            		ArchitectUtils.escapeXML(match.isMergeSendEmailInd()?"Y":"N")+
            		"</MERGE_SEND_EMAIL_IND>");

            ioo.indent--;
            ioo.println(out, "</PL_MATCH>");

            saveMatchGroup(ioo,out);
            saveMergeCriteria(ioo,out);
            saveMergeConsolidateCriteria(ioo,out);
            saveFolder(ioo,out);



            ioo.indent--;
            ioo.println(out, "</EXPORT>");
        } finally {
            if (out != null) out.close();
        }
    }

    private void saveFolder(IOUtils ioo, PrintWriter out) {


    	if ( match.getFolder() == null ) {
    		return;
    	}

    	PlFolder folder = (PlFolder) match.getFolder();

		ioo.println(out, "<PL_FOLDER>");
		ioo.indent++;
		ioo.println(out, "<FOLDER_NAME>"+
        		ArchitectUtils.escapeXML(folder.getFolderName())+
        		"</FOLDER_NAME>");
		ioo.println(out, "<FOLDER_DESC>"+
        		ArchitectUtils.escapeXML(folder.getFolderDesc())+
        		"</FOLDER_DESC>");
		ioo.indent--;
        ioo.println(out, "</PL_FOLDER>");


        ioo.println(out, "<PL_FOLDER_DETAIL>");
		ioo.indent++;
		ioo.println(out, "<FOLDER_NAME>"+
        		ArchitectUtils.escapeXML(folder.getFolderName())+
        		"</FOLDER_NAME>");
		ioo.println(out, "<OBJECT_TYPE>"+
        		ArchitectUtils.escapeXML("MATCH")+
        		"</OBJECT_TYPE>");
		ioo.println(out, "<OBJECT_NAME>"+
        		ArchitectUtils.escapeXML(match.getMatchId())+
        		"</OBJECT_NAME>");
		ioo.indent--;
        ioo.println(out, "</PL_FOLDER_DETAIL>");

	}


	public void saveMatchGroup(IOUtils ioo, PrintWriter out ) throws IOException {

    	List <PlMatchGroup> groups = new ArrayList<PlMatchGroup>(match.getPlMatchGroups());

    	for ( PlMatchGroup g : groups ) {
    		ioo.println(out, "<PL_MATCH_GROUP>");
    		ioo.indent++;

    		ioo.println(out, "<MATCH_ID>"+
            		ArchitectUtils.escapeXML(g.getPlMatch().getMatchId())+
            		"</MATCH_ID>");
    		ioo.println(out, "<GROUP_ID>"+
            		ArchitectUtils.escapeXML(g.getGroupId())+
            		"</GROUP_ID>");
    		ioo.println(out, "<MATCH_PERCENT>"+
            		g.getMatchPercent()+
            		"</MATCH_PERCENT>");
    		ioo.println(out, "<LAST_UPDATE_DATE><DATE>"+
            		ArchitectUtils.escapeXML(df.format(g.getLastUpdateDate()))+
            		"</DATE></LAST_UPDATE_DATE>");
    		ioo.println(out, "<LAST_UPDATE_USER>"+
            		ArchitectUtils.escapeXML(g.getLastUpdateUser())+
            		"</LAST_UPDATE_USER>");
    		ioo.println(out, "<ACTIVE_IND>"+
            		ArchitectUtils.escapeXML(g.isActiveInd()?"Y":"N")+
            		"</ACTIVE_IND>");
    		ioo.println(out, "<LAST_UPDATE_OS_USER>"+
            		ArchitectUtils.escapeXML(g.getLastUpdateOsUser())+
            		"</LAST_UPDATE_OS_USER>");
    		ioo.indent--;
            ioo.println(out, "</PL_MATCH_GROUP>");
    	}

    	for ( PlMatchGroup g : groups ) {
    		saveMatchCriteria(ioo,out,g);
    	}
    }


	private void saveMatchCriteria(IOUtils ioo, PrintWriter out, PlMatchGroup g) {

		List <PlMatchCriterion> criterias = new ArrayList<PlMatchCriterion>(g.getPlMatchCriterias());
		for ( PlMatchCriterion c : criterias ) {
			ioo.println(out, "<PL_MATCH_CRITERIA>");
    		ioo.indent++;
    		ioo.println(out, "<MATCH_ID>"+
            		ArchitectUtils.escapeXML(g.getPlMatch().getMatchId())+
            		"</MATCH_ID>");
    		ioo.println(out, "<GROUP_ID>"+
            		ArchitectUtils.escapeXML(g.getGroupId())+
            		"</GROUP_ID>");
    		ioo.println(out, "<COLUMN_NAME>"+
            		ArchitectUtils.escapeXML(c.getColumnName())+
            		"</COLUMN_NAME>");
    		ioo.println(out, "<CASE_SENSITIVE_IND>"+
            		ArchitectUtils.escapeXML(c.isCaseSensitiveInd()?"Y":"N")+
            		"</CASE_SENSITIVE_IND>");
    		ioo.println(out, "<SUPPRESS_CHAR>"+
            		ArchitectUtils.escapeXML(c.getSuppressChar())+
            		"</SUPPRESS_CHAR>");
    		ioo.println(out, "<SOUND_IND>"+
            		ArchitectUtils.escapeXML(c.isSoundInd()?"Y":"N")+
            		"</SOUND_IND>");
    		ioo.println(out, "<FIRST_N_CHAR>"+
            		c.getFirstNChar()+
            		"</FIRST_N_CHAR>");
    		ioo.println(out, "<LAST_UPDATE_DATE><DATE>"+
            		ArchitectUtils.escapeXML(df.format(c.getLastUpdateDate()))+
            		"</DATE></LAST_UPDATE_DATE>");
    		ioo.println(out, "<LAST_UPDATE_USER>"+
            		ArchitectUtils.escapeXML(c.getLastUpdateUser())+
            		"</LAST_UPDATE_USER>");
    		ioo.println(out, "<MATCH_START>"+
    				ArchitectUtils.escapeXML(c.isMatchStart()?"Y":"N")+
            		"</MATCH_START>");
    		ioo.println(out, "<MATCH_END>"+
    				ArchitectUtils.escapeXML(c.isMatchEnd()?"Y":"N")+
            		"</MATCH_END>");
    		ioo.println(out, "<LAST_UPDATE_OS_USER>"+
    				ArchitectUtils.escapeXML(c.getLastUpdateOsUser())+
            		"</LAST_UPDATE_OS_USER>");
    		ioo.println(out, "<ALLOW_NULL_IND>"+
    				ArchitectUtils.escapeXML(c.isAllowNullInd()?"Y":"N")+
            		"</ALLOW_NULL_IND>");
    		ioo.println(out, "<TRANSLATE_IND>"+
    				ArchitectUtils.escapeXML("N")+		// XXX: we don't know
            		"</TRANSLATE_IND>");
    		ioo.println(out, "<PURGE_IND>"+
    				ArchitectUtils.escapeXML("N")+		// XXX: we don't know
            		"</PURGE_IND>");
    		ioo.println(out, "<REMOVE_SPECIAL_CHARS>"+
    				ArchitectUtils.escapeXML(c.isRemoveSpecialChars()?"Y":"N")+
            		"</REMOVE_SPECIAL_CHARS>");
    		ioo.println(out, "<REORDER_IND>"+
    				ArchitectUtils.escapeXML(c.isReorderInd()?"Y":"N")+
            		"</REORDER_IND>");
    		ioo.println(out, "<FIRST_N_CHAR_BY_WORD_IND>"+
    				ArchitectUtils.escapeXML(c.isFirstNCharByWordInd()?"Y":"N")+
            		"</FIRST_N_CHAR_BY_WORD_IND>");
    		ioo.println(out, "<REPLACE_WITH_SPACE_IND>"+
    				ArchitectUtils.escapeXML(c.isReplaceWithSpaceInd()?"Y":"N")+
            		"</REPLACE_WITH_SPACE_IND>");
    		ioo.println(out, "<MATCH_FIRST_PLUS_ONE_IND>"+
    				ArchitectUtils.escapeXML(c.isMatchFirstPlusOneInd()?"Y":"N")+
            		"</MATCH_FIRST_PLUS_ONE_IND>");
    		ioo.println(out, "<TRANSLATE_GROUP_NAME>"+
    				ArchitectUtils.escapeXML(c.getTranslateGroup().getTranslateGroupName())+
            		"</TRANSLATE_GROUP_NAME>");
    		ioo.println(out, "<SEQ_NO>"+
    				c.getSeqNo()+
            		"</SEQ_NO>");

    		ioo.indent--;
            ioo.println(out, "</PL_MATCH_CRITERIA>");
		}
	}

	private void saveMergeCriteria(IOUtils ioo, PrintWriter out) {

		for ( PlMergeCriteria c : match.getPlMergeCriteria() ) {

    		ioo.println(out, "<PL_MERGE_CRITERIA>");
    		ioo.indent++;
    		ioo.println(out, "<MATCH_ID>"+
            		ArchitectUtils.escapeXML(c.getPlMatch().getMatchId())+
            		"</MATCH_ID>");
    		ioo.println(out, "<TABLE_CATALOG>"+
    				ArchitectUtils.escapeXML(c.getCatalog())+
    				"</TABLE_CATALOG>");
    		ioo.println(out, "<OWNER>"+
    				ArchitectUtils.escapeXML(c.getOwner())+
    				"</OWNER>");
    		ioo.println(out, "<TABLE_NAME>"+
            		ArchitectUtils.escapeXML(c.getTableName())+
            		"</TABLE_NAME>");
    		ioo.println(out, "<INDEX_COLUMN_NAME0>"+
    				ArchitectUtils.escapeXML(c.getIndexColumnName0())+
    				"</INDEX_COLUMN_NAME0>");
    		ioo.println(out, "<COLUMN_NAME>"+
    				ArchitectUtils.escapeXML(c.getColumnName())+
    				"</COLUMN_NAME>");

    		ioo.println(out, "<DELETE_DUP_IND>"+
            		ArchitectUtils.escapeXML(c.isDeleteDupInd()?"Y":"N")+
            		"</DELETE_DUP_IND>");
    		ioo.println(out, "<SEQ_NO>"+
            		c.getSeqNo()+
            		"</SEQ_NO>");
    		ioo.println(out, "<LAST_UPDATE_DATE>"+
    				ArchitectUtils.escapeXML(df.format(c.getLastUpdateDate()))+
            		"</LAST_UPDATE_DATE>");
    		ioo.println(out, "<LAST_UPDATE_OS_USER>"+
    				ArchitectUtils.escapeXML(c.getLastUpdateOsUser())+
            		"</LAST_UPDATE_OS_USER>");
    		ioo.println(out, "<LAST_UPDATE_USER>"+
    				ArchitectUtils.escapeXML(c.getLastUpdateUser())+
            		"</LAST_UPDATE_USER>");

    		ioo.println(out, "<PRIMARY_KEY_INDEX>"+
            		ArchitectUtils.escapeXML(c.getPrimaryKeyIndex())+
            		"</PRIMARY_KEY_INDEX>");
    		ioo.println(out, "<INDEX_COLUMN_NAME1>"+
            		ArchitectUtils.escapeXML(c.getIndexColumnName1())+
            		"</INDEX_COLUMN_NAME1>");
    		ioo.println(out, "<INDEX_COLUMN_NAME2>"+
            		ArchitectUtils.escapeXML(c.getIndexColumnName2())+
            		"</INDEX_COLUMN_NAME2>");
    		ioo.println(out, "<INDEX_COLUMN_NAME3>"+
            		ArchitectUtils.escapeXML(c.getIndexColumnName3())+
            		"</INDEX_COLUMN_NAME3>");
    		ioo.println(out, "<INDEX_COLUMN_NAME4>"+
            		ArchitectUtils.escapeXML(c.getIndexColumnName4())+
            		"</INDEX_COLUMN_NAME4>");
    		ioo.println(out, "<INDEX_COLUMN_NAME5>"+
            		ArchitectUtils.escapeXML(c.getIndexColumnName5())+
            		"</INDEX_COLUMN_NAME5>");
    		ioo.println(out, "<INDEX_COLUMN_NAME6>"+
            		ArchitectUtils.escapeXML(c.getIndexColumnName6())+
            		"</INDEX_COLUMN_NAME6>");
    		ioo.println(out, "<INDEX_COLUMN_NAME7>"+
            		ArchitectUtils.escapeXML(c.getIndexColumnName7())+
            		"</INDEX_COLUMN_NAME7>");
    		ioo.println(out, "<INDEX_COLUMN_NAME8>"+
            		ArchitectUtils.escapeXML(c.getIndexColumnName8())+
            		"</INDEX_COLUMN_NAME8>");
    		ioo.println(out, "<INDEX_COLUMN_NAME9>"+
            		ArchitectUtils.escapeXML(c.getIndexColumnName9())+
            		"</INDEX_COLUMN_NAME9>");
    		ioo.indent--;
    		ioo.println(out, "</PL_MERGE_CRITERIA>");
		}
	}

	private void saveMergeConsolidateCriteria(IOUtils ioo, PrintWriter out) {

		for ( PlMergeConsolidateCriteria c : match.getPlMergeConsolidateCriterias() ) {

    		ioo.println(out, "<PL_MERGE_CONSOLIDATE_CRITERIA>");
    		ioo.indent++;
    		ioo.println(out, "<MATCH_ID>"+
            		ArchitectUtils.escapeXML(c.getPlMergeCriteria().getPlMatch().getMatchId())+
            		"</MATCH_ID>");
    		ioo.println(out, "<TABLE_CATALOG>"+
    				ArchitectUtils.escapeXML(c.getCatalog())+
    				"</TABLE_CATALOG>");
    		ioo.println(out, "<OWNER>"+
    				ArchitectUtils.escapeXML(c.getOwner())+
    				"</OWNER>");
    		ioo.println(out, "<TABLE_NAME>"+
            		ArchitectUtils.escapeXML(c.getTableName())+
            		"</TABLE_NAME>");
    		ioo.println(out, "<COLUMN_NAME>"+
            		ArchitectUtils.escapeXML(c.getColumnName())+
            		"</COLUMN_NAME>");

    		ioo.println(out, "<COLUMN_FORMAT>"+
    				ArchitectUtils.escapeXML(c.getColumnFormat())+
    				"</COLUMN_FORMAT>");
    		ioo.println(out, "<ACTION_TYPE>"+
    				ArchitectUtils.escapeXML(c.getActionType())+
    				"</ACTION_TYPE>");
    		ioo.println(out, "<LAST_UPDATE_USER>"+
    				ArchitectUtils.escapeXML(c.getLastUpdateUser())+
    				"</LAST_UPDATE_USER>");
    		ioo.println(out, "<LAST_UPDATE_DATE>"+
    				ArchitectUtils.escapeXML(df.format(c.getLastUpdateDate()))+
            		"</LAST_UPDATE_DATE>");
    		ioo.println(out, "<LAST_UPDATE_OS_USER>"+
    				ArchitectUtils.escapeXML(c.getLastUpdateOsUser())+
            		"</LAST_UPDATE_OS_USER>");

    		ioo.println(out, "<CAN_UPDATE_ACTION_IND>"+
            		ArchitectUtils.escapeXML(c.isCanUpdateActionInd()?"Y":"N")+
            		"</CAN_UPDATE_ACTION_IND>");
    		ioo.println(out, "<COLUMN_LENGTH>"+
            		c.getColumnLength()+
            		"</COLUMN_LENGTH>");
    		ioo.indent--;
    		ioo.println(out, "</PL_MERGE_CONSOLIDATE_CRITERIA>");
		}
	}

}