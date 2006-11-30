package ca.sqlpower.matchmaker;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.DateFormatAllowsNull;
import ca.sqlpower.architect.IOUtils;
import ca.sqlpower.architect.SQLIndex;

public class MatchExportor {

	private final static Logger logger = Logger.getLogger(MatchExportor.class);
	private final DateFormat df = new DateFormatAllowsNull();

	public void save(Match match, PrintWriter out, String encoding) throws ArchitectException {

		IOUtils ioo = new IOUtils();
        ioo.indent = 0;

        try {
            ioo.println(out, "<?xml version='1.0' encoding='"+encoding+"'?>");
            ioo.println(out, "<EXPORT>");
            ioo.indent++;


            ioo.println(out, "<PL_MATCH>");
            ioo.indent++;
            ioo.println(out, "<MATCH_ID>"+
            		ArchitectUtils.escapeXML(match.getName())+
            		"</MATCH_ID>");
            ioo.println(out, "<MATCH_DESC>"+
            		ArchitectUtils.escapeXML(match.getMatchSettings().getDescription())+
            		"</MATCH_DESC>");
            ioo.println(out, "<TABLE_OWNER>"+
            		ArchitectUtils.escapeXML(match.getSourceTableSchema())+
            		"</TABLE_OWNER>");
            ioo.println(out, "<MATCH_TABLE>"+
            		ArchitectUtils.escapeXML(match.getSourceTableName())+
            		"</MATCH_TABLE>");
            ioo.println(out, "<PK_COLUMN>"+
            		ArchitectUtils.escapeXML(match.getSourceTableIndex().getName())+
            		"</PK_COLUMN>");
            ioo.println(out, "<RESULTS_TABLE>"+
            		ArchitectUtils.escapeXML(match.getResultTableName())+
            		"</RESULTS_TABLE>");
            ioo.println(out, "<CREATE_DATE><DATE>"+
            		ArchitectUtils.escapeXML(df.format(match.getMatchSettings().getCreateDate()))+
            		"</DATE></CREATE_DATE>");
            ioo.println(out, "<LAST_UPDATE_DATE><DATE>"+
            		ArchitectUtils.escapeXML(df.format(match.getMatchSettings().getLastUpdateDate()))+
            		"</DATE></LAST_UPDATE_DATE>");
            ioo.println(out, "<LAST_UPDATE_USER>"+
            		ArchitectUtils.escapeXML(match.getMatchSettings().getLastUpdateAppUser())+
            		"</LAST_UPDATE_USER>");
            ioo.println(out, "<MATCH_LAST_RUN_DATE><DATE>"+
            		ArchitectUtils.escapeXML(df.format(match.getMatchSettings().getLastRunDate()))+
            		"</DATE></MATCH_LAST_RUN_DATE>");
            ioo.println(out, "<MATCH_LOG_FILE_NAME>"+
            		ArchitectUtils.escapeXML(match.getMatchSettings().getLog().toString())+
            		"</MATCH_LOG_FILE_NAME>");
            ioo.println(out, "<MATCH_APPEND_TO_LOG_IND>"+
            		ArchitectUtils.escapeXML(match.getMatchSettings().getAppendToLog()?"Y":"N")+
            		"</MATCH_APPEND_TO_LOG_IND>");
            ioo.println(out, "<MATCH_PROCESS_CNT>"+
            		match.getMatchSettings().getProcessCount()+
            		"</MATCH_PROCESS_CNT>");
            ioo.println(out, "<MATCH_SHOW_PROGRESS_FREQ>"+
            		match.getMatchSettings().getShowProgressFreq()+
            		"</MATCH_SHOW_PROGRESS_FREQ>");
            ioo.println(out, "<RESULTS_TABLE_OWNER>"+
            		ArchitectUtils.escapeXML(match.getResultTableSchema())+
            		"</RESULTS_TABLE_OWNER>");
            ioo.println(out, "<MATCH_TYPE>"+
            		ArchitectUtils.escapeXML(match.getType().name())+
            		"</MATCH_TYPE>");
            ioo.println(out, "<LAST_UPDATE_OS_USER>"+
            		ArchitectUtils.escapeXML(match.getMatchSettings().getLastUpdateOSUser())+
            		"</LAST_UPDATE_OS_USER>");
            ioo.println(out, "<MATCH_SEND_EMAIL_IND>"+
            		ArchitectUtils.escapeXML(match.getMatchSettings().getSendEmail()?"Y":"N")+
            		"</MATCH_SEND_EMAIL_IND>");
            ioo.println(out, "<TRUNCATE_CAND_DUP_IND>"+
            		ArchitectUtils.escapeXML(match.getMatchSettings().getTruncateCandDupe()?"Y":"N")+
            		"</TRUNCATE_CAND_DUP_IND>");
            ioo.println(out, "<AUTO_MATCH_THRESHOLD>"+
            		match.getMatchSettings().getAutoMatchThreshold()+
            		"</AUTO_MATCH_THRESHOLD>");
            ioo.println(out, "<MERGE_LAST_RUN_DATE><DATE>"+
            		ArchitectUtils.escapeXML(df.format(match.getMergeSettings().getLastRunDate()))+
            		"</DATE></MERGE_LAST_RUN_DATE>");
            ioo.println(out, "<MATCH_DEBUG_MODE_IND>"+
            		ArchitectUtils.escapeXML(match.getMatchSettings().getDebug()?"Y":"N")+
            		"</MATCH_DEBUG_MODE_IND>");
            ioo.println(out, "<MERGE_LOG_FILE_NAME>"+
            		ArchitectUtils.escapeXML(match.getMergeSettings().getLog().toString())+
            		"</MERGE_LOG_FILE_NAME>");
            ioo.println(out, "<MERGE_APPEND_TO_LOG_IND>"+
            		ArchitectUtils.escapeXML(match.getMergeSettings().getAppendToLog()?"Y":"N")+
            		"</MERGE_APPEND_TO_LOG_IND>");
            ioo.println(out, "<MERGE_PROCESS_CNT>"+
            		match.getMergeSettings().getProcessCount()+
            		"</MERGE_PROCESS_CNT>");
            ioo.println(out, "<MERGE_SHOW_PROGRESS_FREQ>"+
            		match.getMergeSettings().getShowProgressFreq()+
            		"</MERGE_SHOW_PROGRESS_FREQ>");
            ioo.println(out, "<MERGE_AUGMENT_NULL_IND>"+
            		ArchitectUtils.escapeXML(match.getMergeSettings().getAugmentNull()?"Y":"N")+
            		"</MERGE_AUGMENT_NULL_IND>");

            List<SQLIndex.Column> indexColNames = match.getSourceTableIndex().getChildren();
            for (int i = 0, n = indexColNames.size(); i < n; i++) {
                ioo.println(out, "<INDEX_COLUMN_NAME"+i+">"+
                        ArchitectUtils.escapeXML(indexColNames.get(i).getName())+
                        "</INDEX_COLUMN_NAME"+i+">");
            }

            ioo.println(out, "<MERGE_SEND_EMAIL_IND>"+
            		ArchitectUtils.escapeXML(match.getMergeSettings().getSendEmail()?"Y":"N")+
            		"</MERGE_SEND_EMAIL_IND>");

            ioo.indent--;
            ioo.println(out, "</PL_MATCH>");

            saveMatchGroup(ioo, out, match);
            saveMergeCriteria(ioo, out, match);
            saveMergeCriteria(ioo, out, match);
            saveFolder(ioo, out, match);

            ioo.indent--;
            ioo.println(out, "</EXPORT>");
        } finally {
            if (out != null) out.close();
        }
	}

	private void saveFolder(IOUtils ioo, PrintWriter out, Match match) {
		if ( match.getParent() == null ) {
    		return;
    	}

    	PlFolder folder = (PlFolder) match.getParent();

		ioo.println(out, "<PL_FOLDER>");
		ioo.indent++;
		ioo.println(out, "<FOLDER_NAME>"+
        		ArchitectUtils.escapeXML(folder.getName())+
        		"</FOLDER_NAME>");
		ioo.println(out, "<FOLDER_DESC>"+
        		ArchitectUtils.escapeXML(folder.getFolderDesc())+
        		"</FOLDER_DESC>");
		ioo.indent--;
        ioo.println(out, "</PL_FOLDER>");


        ioo.println(out, "<PL_FOLDER_DETAIL>");
		ioo.indent++;
		ioo.println(out, "<FOLDER_NAME>"+
        		ArchitectUtils.escapeXML(folder.getName())+
        		"</FOLDER_NAME>");
		ioo.println(out, "<OBJECT_TYPE>"+
        		ArchitectUtils.escapeXML("MATCH")+
        		"</OBJECT_TYPE>");
		ioo.println(out, "<OBJECT_NAME>"+
        		ArchitectUtils.escapeXML(match.getName())+
        		"</OBJECT_NAME>");
		ioo.indent--;
        ioo.println(out, "</PL_FOLDER_DETAIL>");
	}

	private void saveMergeCriteria(IOUtils ioo, PrintWriter out, Match match) {
		// TODO Auto-generated method stub
		logger.debug("Stub call: MatchExportor.saveMergeCriteria()");

	}

	private void saveMatchGroup(IOUtils ioo, PrintWriter out, Match match) throws ArchitectException {
		for ( MatchMakerCriteriaGroup group : match.getMatchCriteriaGroups()) {
			ioo.println(out, "<PL_MATCH_GROUP>");
			ioo.indent++;

			ioo.println(out, "<MATCH_ID>"+
					ArchitectUtils.escapeXML(match.getName())+
			"</MATCH_ID>");
			ioo.println(out, "<GROUP_ID>"+
					ArchitectUtils.escapeXML(group.getName())+
			"</GROUP_ID>");
			ioo.println(out, "<MATCH_PERCENT>"+
					group.getMatchPercent()+
			"</MATCH_PERCENT>");
			ioo.println(out, "<LAST_UPDATE_DATE><DATE>"+
					ArchitectUtils.escapeXML(df.format(group.getLastUpdateDate()))+
			"</DATE></LAST_UPDATE_DATE>");
			ioo.println(out, "<LAST_UPDATE_USER>"+
					ArchitectUtils.escapeXML(group.getLastUpdateAppUser())+
			"</LAST_UPDATE_USER>");
			ioo.println(out, "<ACTIVE_IND>"+
					ArchitectUtils.escapeXML(group.getActive()?"Y":"N")+
			"</ACTIVE_IND>");
			ioo.println(out, "<LAST_UPDATE_OS_USER>"+
					ArchitectUtils.escapeXML(group.getLastUpdateOSUser())+
			"</LAST_UPDATE_OS_USER>");
			ioo.indent--;
			ioo.println(out, "</PL_MATCH_GROUP>");
		}

		for ( MatchMakerCriteriaGroup group : match.getMatchCriteriaGroups()) {
			saveMatchCriteria(ioo,out,match,group);
		}
	}

	private void saveMatchCriteria(IOUtils ioo,
				PrintWriter out, Match match, MatchMakerCriteriaGroup group) throws ArchitectException {

		List <MatchMakerCriteria> criterias = group.getChildren();
		for ( MatchMakerCriteria c : criterias ) {
			ioo.println(out, "<PL_MATCH_CRITERIA>");
    		ioo.indent++;
    		ioo.println(out, "<MATCH_ID>"+
            		ArchitectUtils.escapeXML(match.getName())+
            		"</MATCH_ID>");
    		ioo.println(out, "<GROUP_ID>"+
            		ArchitectUtils.escapeXML(group.getName())+
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
            		ArchitectUtils.escapeXML(c.getLastUpdateAppUser())+
            		"</LAST_UPDATE_USER>");
    		ioo.println(out, "<MATCH_START>"+
    				ArchitectUtils.escapeXML(c.isMatchStart()?"Y":"N")+
            		"</MATCH_START>");
    		ioo.println(out, "<MATCH_END>"+
    				ArchitectUtils.escapeXML(c.isMatchEnd()?"Y":"N")+
            		"</MATCH_END>");
    		ioo.println(out, "<LAST_UPDATE_OS_USER>"+
    				ArchitectUtils.escapeXML(c.getLastUpdateOSUser())+
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
    				ArchitectUtils.escapeXML(c.getTranslateGroup().getName())+
            		"</TRANSLATE_GROUP_NAME>");
    		ioo.println(out, "<SEQ_NO>"+
    				c.getSeqNo()+
            		"</SEQ_NO>");

    		ioo.indent--;
            ioo.println(out, "</PL_MATCH_CRITERIA>");
		}
	}

}
