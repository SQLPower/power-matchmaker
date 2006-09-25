package ca.sqlpower.matchmaker.swingui.action;

import java.awt.event.ActionEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;

import javax.swing.AbstractAction;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.DateFormatAllowsNull;
import ca.sqlpower.architect.IOUtils;
import ca.sqlpower.architect.swingui.ASUtils;
import ca.sqlpower.architect.swingui.ArchitectFrame;
import ca.sqlpower.architect.swingui.SwingUserSettings;
import ca.sqlpower.matchmaker.hibernate.PlMatch;

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
		PrintWriter out;
		try {
			out = new PrintWriter(new FileWriter("abc.xml"));
			save(out,"UTF-8");
		} catch (IOException e1) {
			throw new RuntimeException("IO Error during save", e1);
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
            ioo.println(out, "<MATCH_ID>"+
            		ArchitectUtils.escapeXML(match.getMatchId())+
            		"</MATCH_ID>");
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
            		ArchitectUtils.escapeXML("N")+ // XXX: we don't know
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
            ioo.println(out, "<INDEX_COLUMN_NAME0>"+
            		ArchitectUtils.escapeXML(match.getIndexColumnName0())+
            		"</INDEX_COLUMN_NAME0>");
            ioo.println(out, "<INDEX_COLUMN_TYPE0>"+
            		ArchitectUtils.escapeXML(match.getIndexColumnType0())+
            		"</INDEX_COLUMN_TYPE0>");
            ioo.println(out, "<MATCH_SEND_EMAIL_IND>"+
            		ArchitectUtils.escapeXML("N")+ // XXX: we don't know
            		"</MATCH_SEND_EMAIL_IND>");
            ioo.println(out, "<TRUNCATE_CAND_DUP_IND>"+
            		ArchitectUtils.escapeXML("N")+ // XXX: we don't know
            		"</TRUNCATE_CAND_DUP_IND>");
            ioo.println(out, "</PL_MATCH>");

/*            saveDataSources(out);
            saveSourceDatabases(out);
            saveTargetDatabase(out);
            saveDDLGenerator(out);
            saveCompareDMSettings(out);
            savePlayPen(out);
            saveProfiles(out);

            */
            ioo.indent--;
            ioo.println(out, "</EXPORT>");
        } finally {
            if (out != null) out.close();
        }
    }

}
