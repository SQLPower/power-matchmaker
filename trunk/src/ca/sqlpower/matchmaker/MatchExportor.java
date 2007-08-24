/*
 * Copyright (c) 2007, SQL Power Group Inc.
 *
 * This file is part of Power*MatchMaker.
 *
 * Power*MatchMaker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*MatchMaker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.DateFormatAllowsNull;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.xml.XMLHelper;

public class MatchExportor {

	private final static Logger logger = Logger.getLogger(MatchExportor.class);
	private final DateFormat df = new DateFormatAllowsNull();

	public void save(Match match, PrintWriter out, String encoding) throws ArchitectException {

		XMLHelper xmlHelper = new XMLHelper();
        xmlHelper.indent = 0;

        try {
            xmlHelper.println(out, "<?xml version='1.0' encoding='"+encoding+"'?>");
            xmlHelper.println(out, "<EXPORT>");
            xmlHelper.indent++;


            xmlHelper.println(out, "<PL_MATCH>");
            xmlHelper.indent++;
            xmlHelper.println(out, "<MATCH_ID>"+
            		SQLPowerUtils.escapeXML(match.getName())+
            		"</MATCH_ID>");
            xmlHelper.println(out, "<MATCH_DESC>"+
            		SQLPowerUtils.escapeXML(match.getMatchSettings().getDescription())+
            		"</MATCH_DESC>");
            xmlHelper.println(out, "<TABLE_CATALOG>"+
            		SQLPowerUtils.escapeXML(match.getSourceTableCatalog())+
            		"</TABLE_CATALOG>");
            xmlHelper.println(out, "<TABLE_OWNER>"+
            		SQLPowerUtils.escapeXML(match.getSourceTableSchema())+
            		"</TABLE_OWNER>");
            xmlHelper.println(out, "<MATCH_TABLE>"+
            		SQLPowerUtils.escapeXML(match.getSourceTableName())+
            		"</MATCH_TABLE>");
            if ( match.getSourceTableIndex() != null ) {
            	xmlHelper.println(out, "<PK_COLUMN>"+
            			SQLPowerUtils.escapeXML(match.getSourceTableIndex().getName())+
            			"</PK_COLUMN>");
            }
            xmlHelper.println(out, "<RESULTS_TABLE_CATALOG>"+
            		SQLPowerUtils.escapeXML(match.getResultTableCatalog())+
            		"</RESULTS_TABLE_CATALOG>");
            xmlHelper.println(out, "<RESULTS_TABLE_OWNER>"+
            		SQLPowerUtils.escapeXML(match.getResultTableSchema())+
            		"</RESULTS_TABLE_OWNER>");
            xmlHelper.println(out, "<RESULTS_TABLE>"+
            		SQLPowerUtils.escapeXML(match.getResultTableName())+
            		"</RESULTS_TABLE>");
            xmlHelper.println(out, "<CREATE_DATE><DATE>"+
            		SQLPowerUtils.escapeXML(df.format(match.getMatchSettings().getCreateDate()))+
            		"</DATE></CREATE_DATE>");
            xmlHelper.println(out, "<LAST_UPDATE_DATE><DATE>"+
            		SQLPowerUtils.escapeXML(df.format(match.getMatchSettings().getLastUpdateDate()))+
            		"</DATE></LAST_UPDATE_DATE>");
            xmlHelper.println(out, "<LAST_UPDATE_USER>"+
            		SQLPowerUtils.escapeXML(match.getMatchSettings().getLastUpdateAppUser())+
            		"</LAST_UPDATE_USER>");
            xmlHelper.println(out, "<MATCH_LAST_RUN_DATE><DATE>"+
            		SQLPowerUtils.escapeXML(df.format(match.getMatchSettings().getLastRunDate()))+
            		"</DATE></MATCH_LAST_RUN_DATE>");
            if ( match.getMatchSettings().getLog() != null ) {
	            xmlHelper.println(out, "<MATCH_LOG_FILE_NAME>"+
	            		SQLPowerUtils.escapeXML(match.getMatchSettings().getLog().toString())+
	            		"</MATCH_LOG_FILE_NAME>");
            }
           	xmlHelper.println(out, "<MATCH_APPEND_TO_LOG_IND>"+
           			SQLPowerUtils.escapeXML(match.getMatchSettings().getAppendToLog()?"Y":"N")+
            		"</MATCH_APPEND_TO_LOG_IND>");

            xmlHelper.println(out, "<MATCH_PROCESS_CNT>"+
            		match.getMatchSettings().getProcessCount()+
            		"</MATCH_PROCESS_CNT>");
            xmlHelper.println(out, "<MATCH_SHOW_PROGRESS_FREQ>"+
            		match.getMatchSettings().getShowProgressFreq()+
            		"</MATCH_SHOW_PROGRESS_FREQ>");
            xmlHelper.println(out, "<MATCH_TYPE>"+
            		SQLPowerUtils.escapeXML(match.getType().name())+
            		"</MATCH_TYPE>");
            xmlHelper.println(out, "<LAST_UPDATE_OS_USER>"+
            		SQLPowerUtils.escapeXML(match.getMatchSettings().getLastUpdateOSUser())+
            		"</LAST_UPDATE_OS_USER>");
            xmlHelper.println(out, "<MATCH_SEND_EMAIL_IND>"+
            		SQLPowerUtils.escapeXML(match.getMatchSettings().getSendEmail()?"Y":"N")+
            		"</MATCH_SEND_EMAIL_IND>");
            xmlHelper.println(out, "<TRUNCATE_CAND_DUP_IND>"+
            		SQLPowerUtils.escapeXML(match.getMatchSettings().getTruncateCandDupe()?"Y":"N")+
            		"</TRUNCATE_CAND_DUP_IND>");
            xmlHelper.println(out, "<AUTO_MATCH_THRESHOLD>"+
            		match.getMatchSettings().getAutoMatchThreshold()+
            		"</AUTO_MATCH_THRESHOLD>");
            xmlHelper.println(out, "<MERGE_LAST_RUN_DATE><DATE>"+
            		SQLPowerUtils.escapeXML(df.format(match.getMergeSettings().getLastRunDate()))+
            		"</DATE></MERGE_LAST_RUN_DATE>");
            xmlHelper.println(out, "<MATCH_DEBUG_MODE_IND>"+
            		SQLPowerUtils.escapeXML(match.getMatchSettings().getDebug()?"Y":"N")+
            		"</MATCH_DEBUG_MODE_IND>");
            if (match.getMergeSettings().getLog() != null) {
            	xmlHelper.println(out, "<MERGE_LOG_FILE_NAME>"+
            			SQLPowerUtils.escapeXML(match.getMergeSettings().getLog().toString())+
            	"</MERGE_LOG_FILE_NAME>");
            }
            xmlHelper.println(out, "<MERGE_APPEND_TO_LOG_IND>"+
            		SQLPowerUtils.escapeXML(match.getMergeSettings().getAppendToLog()?"Y":"N")+
            		"</MERGE_APPEND_TO_LOG_IND>");
            xmlHelper.println(out, "<MERGE_PROCESS_CNT>"+
            		match.getMergeSettings().getProcessCount()+
            		"</MERGE_PROCESS_CNT>");
            xmlHelper.println(out, "<MERGE_SHOW_PROGRESS_FREQ>"+
            		match.getMergeSettings().getShowProgressFreq()+
            		"</MERGE_SHOW_PROGRESS_FREQ>");
            xmlHelper.println(out, "<MERGE_AUGMENT_NULL_IND>"+
            		SQLPowerUtils.escapeXML(match.getMergeSettings().getAugmentNull()?"Y":"N")+
            		"</MERGE_AUGMENT_NULL_IND>");

            if ( match.getSourceTableIndex()!= null ) {
            	List<SQLIndex.Column> indexColNames = match.getSourceTableIndex().getChildren();
            	for (int i = 0, n = indexColNames.size(); i < n; i++) {
            		xmlHelper.println(out, "<INDEX_COLUMN_NAME"+i+">"+
            				SQLPowerUtils.escapeXML(indexColNames.get(i).getName())+
            				"</INDEX_COLUMN_NAME"+i+">");
            	}
            }

            xmlHelper.println(out, "<MERGE_SEND_EMAIL_IND>"+
            		SQLPowerUtils.escapeXML(match.getMergeSettings().getSendEmail()?"Y":"N")+
            		"</MERGE_SEND_EMAIL_IND>");

            xmlHelper.indent--;
            xmlHelper.println(out, "</PL_MATCH>");

            saveMatchGroup(xmlHelper, out, match);
            saveMergeCriteria(xmlHelper, out, match);
            saveMergeCriteria(xmlHelper, out, match);
            saveFolder(xmlHelper, out, match);

            xmlHelper.indent--;
            xmlHelper.println(out, "</EXPORT>");
        } finally {
            if (out != null) out.close();
        }
	}

	private void saveFolder(XMLHelper xmlHelper, PrintWriter out, Match match) {
		if ( match.getParent() == null ) {
    		return;
    	}

    	PlFolder folder = (PlFolder) match.getParent();

		xmlHelper.println(out, "<PL_FOLDER>");
		xmlHelper.indent++;
		xmlHelper.println(out, "<FOLDER_NAME>"+
        		SQLPowerUtils.escapeXML(folder.getName())+
        		"</FOLDER_NAME>");
		xmlHelper.println(out, "<FOLDER_DESC>"+
        		SQLPowerUtils.escapeXML(folder.getFolderDesc())+
        		"</FOLDER_DESC>");
		xmlHelper.indent--;
        xmlHelper.println(out, "</PL_FOLDER>");


        xmlHelper.println(out, "<PL_FOLDER_DETAIL>");
		xmlHelper.indent++;
		xmlHelper.println(out, "<FOLDER_NAME>"+
        		SQLPowerUtils.escapeXML(folder.getName())+
        		"</FOLDER_NAME>");
		xmlHelper.println(out, "<OBJECT_TYPE>"+
        		SQLPowerUtils.escapeXML("MATCH")+
        		"</OBJECT_TYPE>");
		xmlHelper.println(out, "<OBJECT_NAME>"+
        		SQLPowerUtils.escapeXML(match.getName())+
        		"</OBJECT_NAME>");
		xmlHelper.indent--;
        xmlHelper.println(out, "</PL_FOLDER_DETAIL>");
	}

	private void saveMergeCriteria(XMLHelper ioo, PrintWriter out, Match match) {
		// TODO Auto-generated method stub
		logger.debug("Stub call: MatchExportor.saveMergeCriteria()");

	}

	private void saveMatchGroup(XMLHelper xmlHelper, PrintWriter out, Match match) {
		for ( MatchMakerCriteriaGroup group : match.getMatchCriteriaGroups()) {
			xmlHelper.println(out, "<PL_MATCH_GROUP>");
			xmlHelper.indent++;

			xmlHelper.println(out, "<MATCH_ID>"+
					SQLPowerUtils.escapeXML(match.getName())+
			"</MATCH_ID>");
			xmlHelper.println(out, "<GROUP_ID>"+
					SQLPowerUtils.escapeXML(group.getName())+
			"</GROUP_ID>");
			xmlHelper.println(out, "<MATCH_PERCENT>"+
					group.getMatchPercent()+
			"</MATCH_PERCENT>");
			xmlHelper.println(out, "<LAST_UPDATE_DATE><DATE>"+
					SQLPowerUtils.escapeXML(df.format(group.getLastUpdateDate()))+
			"</DATE></LAST_UPDATE_DATE>");
			xmlHelper.println(out, "<LAST_UPDATE_USER>"+
					SQLPowerUtils.escapeXML(group.getLastUpdateAppUser())+
			"</LAST_UPDATE_USER>");
			xmlHelper.println(out, "<ACTIVE_IND>"+
					SQLPowerUtils.escapeXML(group.getActive()?"Y":"N")+
			"</ACTIVE_IND>");
			xmlHelper.println(out, "<LAST_UPDATE_OS_USER>"+
					SQLPowerUtils.escapeXML(group.getLastUpdateOSUser())+
			"</LAST_UPDATE_OS_USER>");
			xmlHelper.indent--;
			xmlHelper.println(out, "</PL_MATCH_GROUP>");
		}

		for ( MatchMakerCriteriaGroup group : match.getMatchCriteriaGroups()) {
			saveMatchCriteria(xmlHelper,out,match,group);
		}
	}

	private void saveMatchCriteria(XMLHelper xmlHelper,
				PrintWriter out, Match match, MatchMakerCriteriaGroup group) {

		List <MatchMakerCriteria> criterias = group.getChildren();
		for ( MatchMakerCriteria c : criterias ) {
			xmlHelper.println(out, "<PL_MATCH_CRITERIA>");
    		xmlHelper.indent++;
    		xmlHelper.println(out, "<MATCH_ID>"+
            		SQLPowerUtils.escapeXML(match.getName())+
            		"</MATCH_ID>");
    		xmlHelper.println(out, "<GROUP_ID>"+
            		SQLPowerUtils.escapeXML(group.getName())+
            		"</GROUP_ID>");
    		xmlHelper.println(out, "<COLUMN_NAME>"+
            		SQLPowerUtils.escapeXML(c.getColumnName())+
            		"</COLUMN_NAME>");
    		xmlHelper.println(out, "<CASE_SENSITIVE_IND>"+
            		SQLPowerUtils.escapeXML(c.isCaseSensitiveInd()?"Y":"N")+
            		"</CASE_SENSITIVE_IND>");
    		xmlHelper.println(out, "<SUPPRESS_CHAR>"+
            		SQLPowerUtils.escapeXML(c.getSuppressChar())+
            		"</SUPPRESS_CHAR>");
    		xmlHelper.println(out, "<SOUND_IND>"+
            		SQLPowerUtils.escapeXML(c.isSoundInd()?"Y":"N")+
            		"</SOUND_IND>");
    		xmlHelper.println(out, "<FIRST_N_CHAR>"+
            		c.getFirstNChar()+
            		"</FIRST_N_CHAR>");
    		xmlHelper.println(out, "<LAST_UPDATE_DATE><DATE>"+
            		SQLPowerUtils.escapeXML(df.format(c.getLastUpdateDate()))+
            		"</DATE></LAST_UPDATE_DATE>");
    		xmlHelper.println(out, "<LAST_UPDATE_USER>"+
            		SQLPowerUtils.escapeXML(c.getLastUpdateAppUser())+
            		"</LAST_UPDATE_USER>");
    		xmlHelper.println(out, "<MATCH_START>"+
    				SQLPowerUtils.escapeXML(c.isMatchStart()?"Y":"N")+
            		"</MATCH_START>");
    		xmlHelper.println(out, "<MATCH_END>"+
    				SQLPowerUtils.escapeXML(c.isMatchEnd()?"Y":"N")+
            		"</MATCH_END>");
    		xmlHelper.println(out, "<LAST_UPDATE_OS_USER>"+
    				SQLPowerUtils.escapeXML(c.getLastUpdateOSUser())+
            		"</LAST_UPDATE_OS_USER>");
    		xmlHelper.println(out, "<ALLOW_NULL_IND>"+
    				SQLPowerUtils.escapeXML(c.isAllowNullInd()?"Y":"N")+
            		"</ALLOW_NULL_IND>");
    		xmlHelper.println(out, "<TRANSLATE_IND>"+
    				SQLPowerUtils.escapeXML("N")+		// XXX: we don't know
            		"</TRANSLATE_IND>");
    		xmlHelper.println(out, "<PURGE_IND>"+
    				SQLPowerUtils.escapeXML("N")+		// XXX: we don't know
            		"</PURGE_IND>");
    		xmlHelper.println(out, "<REMOVE_SPECIAL_CHARS>"+
    				SQLPowerUtils.escapeXML(c.isRemoveSpecialChars()?"Y":"N")+
            		"</REMOVE_SPECIAL_CHARS>");
    		xmlHelper.println(out, "<REORDER_IND>"+
    				SQLPowerUtils.escapeXML(c.isReorderInd()?"Y":"N")+
            		"</REORDER_IND>");
    		xmlHelper.println(out, "<FIRST_N_CHAR_BY_WORD_IND>"+
    				SQLPowerUtils.escapeXML(c.isFirstNCharByWordInd()?"Y":"N")+
            		"</FIRST_N_CHAR_BY_WORD_IND>");
    		xmlHelper.println(out, "<REPLACE_WITH_SPACE_IND>"+
    				SQLPowerUtils.escapeXML(c.isReplaceWithSpaceInd()?"Y":"N")+
            		"</REPLACE_WITH_SPACE_IND>");
    		xmlHelper.println(out, "<MATCH_FIRST_PLUS_ONE_IND>"+
    				SQLPowerUtils.escapeXML(c.isMatchFirstPlusOneInd()?"Y":"N")+
            		"</MATCH_FIRST_PLUS_ONE_IND>");
    		if ( c.getTranslateGroup()!= null ) {
    			xmlHelper.println(out, "<GROUP_NAME>"+
    					SQLPowerUtils.escapeXML(c.getTranslateGroup().getName())+
    			"</GROUP_NAME>");
    		}
    		xmlHelper.println(out, "<SEQ_NO>"+
    				c.getSeqNo()+
            		"</SEQ_NO>");

    		xmlHelper.indent--;
            xmlHelper.println(out, "</PL_MATCH_CRITERIA>");
		}
	}

}
