package ca.sqlpower.matchmaker;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.xml.sax.SAXException;

import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;

/**
 * note:
 *   we don't load everything from the old version export like 'merge_script_file_name'
 *   we don't export the format and order like the old version export
 *
 *
 */
public class MatchMakerImportExportTest extends TestCase {

	private final String testData = "<?xml version='1.0' encoding='UTF-8'?>" +
			"<EXPORT>" +
			" <PL_MATCH>" +
			"  <MATCH_ID>MATCH_PT_COMPANY</MATCH_ID>" +
			"  <MATCH_DESC></MATCH_DESC>" +
			"  <TABLE_OWNER>DBO</TABLE_OWNER>" +
			"  <MATCH_TABLE>COMPANY</MATCH_TABLE>" +
			"  <PK_COLUMN>SYS_C0011398</PK_COLUMN>" +
			"  <RESULTS_TABLE>MATCH_PT_COMPANY_CAND_DUP</RESULTS_TABLE>" +
			"  <CREATE_DATE><DATE>2000-10-10 12:42:44</DATE></CREATE_DATE>" +
			"  <LAST_UPDATE_DATE><DATE>2004-08-18 02:58:52</DATE></LAST_UPDATE_DATE>" +
			"  <LAST_UPDATE_USER>PL</LAST_UPDATE_USER>" +
			"  <MATCH_LAST_RUN_DATE><DATE>2006-10-02 05:06:19</DATE></MATCH_LAST_RUN_DATE>" +
			"  <MATCH_LOG_FILE_NAME>P:\\test_data\\MATCH_MATCH_PT_COMPANY.log</MATCH_LOG_FILE_NAME>" +
			"  <MATCH_APPEND_TO_LOG_IND>N</MATCH_APPEND_TO_LOG_IND>" +
			"  <MATCH_PROCESS_CNT>0</MATCH_PROCESS_CNT>" +
			"  <MATCH_SHOW_PROGRESS_FREQ>5</MATCH_SHOW_PROGRESS_FREQ>" +
			"  <RESULTS_TABLE_OWNER>DEMO</RESULTS_TABLE_OWNER>" +
			"  <MATCH_TYPE>FIND DUPLICATES</MATCH_TYPE>" +
			"  <LAST_UPDATE_OS_USER>Administrator</LAST_UPDATE_OS_USER>" +
			"  <TEMP_SOURCE_TABLE_NAME>COMPANY00</TEMP_SOURCE_TABLE_NAME>" +
			"  <TEMP_CAND_DUP_TABLE_NAME>MATCH_PT_COMPANY_CAND_DUP01</TEMP_CAND_DUP_TABLE_NAME>" +
			"  <INDEX_COLUMN_NAME0>company_name</INDEX_COLUMN_NAME0>" +
			"  <INDEX_COLUMN_TYPE0></INDEX_COLUMN_TYPE0>" +
			"  <MATCH_SEND_EMAIL_IND>N</MATCH_SEND_EMAIL_IND>" +
			"  <TRUNCATE_CAND_DUP_IND>N</TRUNCATE_CAND_DUP_IND>" +
			"  <MERGE_SCRIPT_FILE_NAME>E:\\Power Loader Suite\\Matchmaker\\script\\MATCH_PT_COMPANY_merge_pkg.sql</MERGE_SCRIPT_FILE_NAME>" +
			"  <AUTO_MATCH_THRESHOLD>85</AUTO_MATCH_THRESHOLD>" +
			"  <MERGE_RUN_STATUS>SUCCESS</MERGE_RUN_STATUS>" +
			"  <MERGE_LAST_RUN_DATE><DATE>2006-01-19 12:17:57</DATE></MERGE_LAST_RUN_DATE>" +
			"  <MERGE_PACKAGE_NAME>MATCH_PT_COMPANY_MERGE_PKG</MERGE_PACKAGE_NAME>" +
			"  <MERGE_PROCEDURE_NAME>MERGE_ALL</MERGE_PROCEDURE_NAME>" +
			"  <MATCH_RUN_STATUS>SUCCESS</MATCH_RUN_STATUS>" +
			"  <MATCH_TABLE_PK_COLUMN_FORMAT>VARCHAR2(35)</MATCH_TABLE_PK_COLUMN_FORMAT>" +
			"  <MATCH_DEBUG_MODE_IND>Y</MATCH_DEBUG_MODE_IND>" +
			"  <MERGE_LOG_FILE_NAME>P:\\test_data\\MERGE_MATCH_PT_COMPANY.log</MERGE_LOG_FILE_NAME>" +
			"  <MERGE_APPEND_TO_LOG_IND>N</MERGE_APPEND_TO_LOG_IND>" +
			"  <MERGE_PROCESS_CNT>0</MERGE_PROCESS_CNT>" +
			"  <MERGE_SHOW_PROGRESS_FREQ>10</MERGE_SHOW_PROGRESS_FREQ>" +
			"  <MERGE_AUGMENT_NULL_IND>Y</MERGE_AUGMENT_NULL_IND>" +
			"  <TEMP_CAN_DUP_TABLE_NAME>MATCH_PT_COMPANY_CAND_DUP01</TEMP_CAN_DUP_TABLE_NAME>" +
			"  <INDEX_COLUMN_NAME0>company_name</INDEX_COLUMN_NAME0>" +
			"  <INDEX_COLUMN_TYPE0></INDEX_COLUMN_TYPE0>" +
			"  <INDEX_COLUMN_NAME1></INDEX_COLUMN_NAME1>" +
			"   <INDEX_COLUMN_TYPE1></INDEX_COLUMN_TYPE1>" +
			"  <INDEX_COLUMN_NAME2></INDEX_COLUMN_NAME2>" +
			"  <INDEX_COLUMN_TYPE2></INDEX_COLUMN_TYPE2>" +
			"  <INDEX_COLUMN_NAME3></INDEX_COLUMN_NAME3>" +
			"  <INDEX_COLUMN_TYPE3></INDEX_COLUMN_TYPE3>" +
			"  <INDEX_COLUMN_NAME4></INDEX_COLUMN_NAME4>" +
			"  <INDEX_COLUMN_TYPE4></INDEX_COLUMN_TYPE4>" +
			"  <INDEX_COLUMN_NAME5></INDEX_COLUMN_NAME5>" +
			"  <INDEX_COLUMN_TYPE5></INDEX_COLUMN_TYPE5>" +
			"  <INDEX_COLUMN_NAME6></INDEX_COLUMN_NAME6>" +
			"  <INDEX_COLUMN_TYPE6></INDEX_COLUMN_TYPE6>" +
			"  <INDEX_COLUMN_NAME7></INDEX_COLUMN_NAME7>" +
			"  <INDEX_COLUMN_TYPE7></INDEX_COLUMN_TYPE7>" +
			"  <INDEX_COLUMN_NAME8></INDEX_COLUMN_NAME8>" +
			"  <INDEX_COLUMN_TYPE8></INDEX_COLUMN_TYPE8>" +
			"  <INDEX_COLUMN_NAME9></INDEX_COLUMN_NAME9>" +
			"  <INDEX_COLUMN_TYPE9></INDEX_COLUMN_TYPE9>" +
			"  <MERGE_SEND_EMAIL_IND>N</MERGE_SEND_EMAIL_IND>" +
			" </PL_MATCH>" +
			" <PL_MATCH_GROUP>" +
			"  <MATCH_ID>MATCH_PT_COMPANY</MATCH_ID>" +
			"  <GROUP_ID>MATCH_90</GROUP_ID>" +
			"  <MATCH_PERCENT>90</MATCH_PERCENT>" +
			"  <LAST_UPDATE_DATE><DATE>2004-06-16 11:42:02</DATE></LAST_UPDATE_DATE>" +
			"  <LAST_UPDATE_USER>DEMO</LAST_UPDATE_USER>" +
			"  <ACTIVE_IND>Y</ACTIVE_IND>" +
			"  <LAST_UPDATE_OS_USER>Administrator</LAST_UPDATE_OS_USER>" +
			" </PL_MATCH_GROUP>" +
			" <PL_MATCH_GROUP>" +
			"  <MATCH_ID>MATCH_PT_COMPANY</MATCH_ID>" +
			"  <GROUP_ID>MATCH_50</GROUP_ID>" +
			"  <MATCH_PERCENT>50</MATCH_PERCENT>" +
			"  <LAST_UPDATE_DATE><DATE>2004-07-23 05:09:46</DATE></LAST_UPDATE_DATE>" +
			"  <LAST_UPDATE_USER>PL</LAST_UPDATE_USER>" +
			"  <ACTIVE_IND>N</ACTIVE_IND>" +
			"  <LAST_UPDATE_OS_USER>Administrator</LAST_UPDATE_OS_USER>" +
			" </PL_MATCH_GROUP>" +
			" <PL_MATCH_GROUP>" +
			"  <MATCH_ID>MATCH_PT_COMPANY</MATCH_ID>" +
			"  <GROUP_ID>GROUP85</GROUP_ID>" +
			"  <MATCH_PERCENT>85</MATCH_PERCENT>" +
			"  <LAST_UPDATE_DATE><DATE>2001-05-14 12:02:04</DATE></LAST_UPDATE_DATE>" +
			"  <LAST_UPDATE_USER>DEMO</LAST_UPDATE_USER>" +
			"  <ACTIVE_IND>Y</ACTIVE_IND>" +
			"  <LAST_UPDATE_OS_USER></LAST_UPDATE_OS_USER>" +
			" </PL_MATCH_GROUP>" +
			" <PL_MATCH_GROUP>" +
			"  <MATCH_ID>MATCH_PT_COMPANY</MATCH_ID>" +
			"  <GROUP_ID>GROUP100</GROUP_ID>" +
			"  <MATCH_PERCENT>100</MATCH_PERCENT>" +
			"  <LAST_UPDATE_DATE><DATE>2003-01-22 10:56:19</DATE></LAST_UPDATE_DATE>" +
			"  <LAST_UPDATE_USER>DEMO</LAST_UPDATE_USER>" +
			"  <ACTIVE_IND>Y</ACTIVE_IND>" +
			"  <LAST_UPDATE_OS_USER>Administrator</LAST_UPDATE_OS_USER>" +
			" </PL_MATCH_GROUP>" +
			" <PL_MATCH_CRITERIA>" +
			"  <MATCH_ID>MATCH_PT_COMPANY</MATCH_ID>" +
			"  <GROUP_ID>MATCH_90</GROUP_ID>" +
			"  <COLUMN_NAME>FAX_NO</COLUMN_NAME>" +
			"  <CASE_SENSITIVE_IND>N</CASE_SENSITIVE_IND>" +
			"  <SUPPRESS_CHAR>()-</SUPPRESS_CHAR>" +
			"  <SOUND_IND>N</SOUND_IND>" +
			"  <FIRST_N_CHAR>null</FIRST_N_CHAR>" +
			"  <LAST_UPDATE_DATE><DATE>2004-06-16 11:42:32</DATE></LAST_UPDATE_DATE>" +
			"  <LAST_UPDATE_USER>DEMO</LAST_UPDATE_USER>" +
			"  <MATCH_START>N</MATCH_START>" +
			"  <MATCH_END>N</MATCH_END>" +
			"  <LAST_UPDATE_OS_USER>Administrator</LAST_UPDATE_OS_USER>" +
			"  <ALLOW_NULL_IND>N</ALLOW_NULL_IND>" +
			"  <TRANSLATE_IND>N</TRANSLATE_IND>" +
			"  <PURGE_IND>N</PURGE_IND>" +
			"  <REMOVE_SPECIAL_CHARS>N</REMOVE_SPECIAL_CHARS>" +
			"  <REORDER_IND>N</REORDER_IND>" +
			"  <FIRST_N_CHAR_BY_WORD_IND>N</FIRST_N_CHAR_BY_WORD_IND>" +
			"  <REPLACE_WITH_SPACE_IND>N</REPLACE_WITH_SPACE_IND>" +
			"  <MATCH_FIRST_PLUS_ONE_IND>N</MATCH_FIRST_PLUS_ONE_IND>" +
			"  <TRANSLATE_GROUP_NAME></TRANSLATE_GROUP_NAME>" +
			"  <SEQ_NO>null</SEQ_NO>" +
			" </PL_MATCH_CRITERIA>" +
			" <PL_MATCH_CRITERIA>" +
			"  <MATCH_ID>MATCH_PT_COMPANY</MATCH_ID>" +
			"  <GROUP_ID>MATCH_90</GROUP_ID>" +
			"  <COLUMN_NAME>COMPANY_NAME</COLUMN_NAME>" +
			"  <CASE_SENSITIVE_IND>N</CASE_SENSITIVE_IND>" +
			"  <SUPPRESS_CHAR></SUPPRESS_CHAR>" +
			"  <SOUND_IND>N</SOUND_IND>" +
			"  <FIRST_N_CHAR>null</FIRST_N_CHAR>" +
			"  <LAST_UPDATE_DATE><DATE>2004-07-22 03:52:36</DATE></LAST_UPDATE_DATE>" +
			"  <LAST_UPDATE_USER>PL</LAST_UPDATE_USER>" +
			"  <MATCH_START>N</MATCH_START>" +
			"  <MATCH_END>N</MATCH_END>" +
			"  <LAST_UPDATE_OS_USER>Administrator</LAST_UPDATE_OS_USER>" +
			"  <ALLOW_NULL_IND>N</ALLOW_NULL_IND>" +
			"  <TRANSLATE_IND>N</TRANSLATE_IND>" +
			"  <PURGE_IND>N</PURGE_IND>" +
			"  <REMOVE_SPECIAL_CHARS>N</REMOVE_SPECIAL_CHARS>" +
			"  <REORDER_IND>N</REORDER_IND>" +
			"  <FIRST_N_CHAR_BY_WORD_IND>N</FIRST_N_CHAR_BY_WORD_IND>" +
			"  <REPLACE_WITH_SPACE_IND>N</REPLACE_WITH_SPACE_IND>" +
			"  <MATCH_FIRST_PLUS_ONE_IND>N</MATCH_FIRST_PLUS_ONE_IND>" +
			"  <TRANSLATE_GROUP_NAME>Address_standardize</TRANSLATE_GROUP_NAME>" +
			"  <SEQ_NO>null</SEQ_NO>" +
			" </PL_MATCH_CRITERIA>" +
			" <PL_MATCH_CRITERIA>" +
			"  <MATCH_ID>MATCH_PT_COMPANY</MATCH_ID>" +
			"  <GROUP_ID>MATCH_50</GROUP_ID>" +
			"  <COLUMN_NAME>COMPANY_NAME</COLUMN_NAME>" +
			"  <CASE_SENSITIVE_IND>Y</CASE_SENSITIVE_IND>" +
			"  <SUPPRESS_CHAR>&apos;,&amp;</SUPPRESS_CHAR>" +
			"  <SOUND_IND>Y</SOUND_IND>" +
			"  <FIRST_N_CHAR>null</FIRST_N_CHAR>" +
			"  <LAST_UPDATE_DATE><DATE>2004-06-16 11:43:15</DATE></LAST_UPDATE_DATE>" +
			"  <LAST_UPDATE_USER>DEMO</LAST_UPDATE_USER>" +
			"  <MATCH_START>N</MATCH_START>" +
			"  <MATCH_END>N</MATCH_END>" +
			"  <LAST_UPDATE_OS_USER>Administrator</LAST_UPDATE_OS_USER>" +
			"  <ALLOW_NULL_IND>N</ALLOW_NULL_IND>" +
			"  <TRANSLATE_IND>N</TRANSLATE_IND>" +
			"  <PURGE_IND>N</PURGE_IND>" +
			"  <REMOVE_SPECIAL_CHARS>N</REMOVE_SPECIAL_CHARS>" +
			"  <REORDER_IND>N</REORDER_IND>" +
			"  <FIRST_N_CHAR_BY_WORD_IND>N</FIRST_N_CHAR_BY_WORD_IND>" +
			"  <REPLACE_WITH_SPACE_IND>N</REPLACE_WITH_SPACE_IND>" +
			"  <MATCH_FIRST_PLUS_ONE_IND>N</MATCH_FIRST_PLUS_ONE_IND>" +
			"  <TRANSLATE_GROUP_NAME></TRANSLATE_GROUP_NAME>" +
			"  <SEQ_NO>null</SEQ_NO>" +
			" </PL_MATCH_CRITERIA>" +
			" <PL_MATCH_CRITERIA>" +
			"  <MATCH_ID>MATCH_PT_COMPANY</MATCH_ID>" +
			"  <GROUP_ID>GROUP85</GROUP_ID>" +
			"  <COLUMN_NAME>COMPANY_NAME</COLUMN_NAME>" +
			"  <CASE_SENSITIVE_IND>Y</CASE_SENSITIVE_IND>" +
			"  <SUPPRESS_CHAR>.&apos;</SUPPRESS_CHAR>" +
			"  <SOUND_IND>N</SOUND_IND>" +
			"  <FIRST_N_CHAR>15</FIRST_N_CHAR>" +
			"  <LAST_UPDATE_DATE><DATE>2004-07-22 03:51:50</DATE></LAST_UPDATE_DATE>" +
			"  <LAST_UPDATE_USER>PL</LAST_UPDATE_USER>" +
			"  <MATCH_START>N</MATCH_START>" +
			"  <MATCH_END>N</MATCH_END>" +
			"  <LAST_UPDATE_OS_USER>Administrator</LAST_UPDATE_OS_USER>" +
			"  <ALLOW_NULL_IND>N</ALLOW_NULL_IND>" +
			"  <TRANSLATE_IND>N</TRANSLATE_IND>" +
			"  <PURGE_IND>N</PURGE_IND>" +
			"  <REMOVE_SPECIAL_CHARS>N</REMOVE_SPECIAL_CHARS>" +
			"  <REORDER_IND>N</REORDER_IND>" +
			"  <FIRST_N_CHAR_BY_WORD_IND>N</FIRST_N_CHAR_BY_WORD_IND>" +
			"  <REPLACE_WITH_SPACE_IND>N</REPLACE_WITH_SPACE_IND>" +
			"  <MATCH_FIRST_PLUS_ONE_IND>N</MATCH_FIRST_PLUS_ONE_IND>" +
			"  <TRANSLATE_GROUP_NAME>Company_remove</TRANSLATE_GROUP_NAME>" +
			"  <SEQ_NO>null</SEQ_NO>" +
			" </PL_MATCH_CRITERIA>" +
			" <PL_MATCH_CRITERIA>" +
			"  <MATCH_ID>MATCH_PT_COMPANY</MATCH_ID>" +
			"  <GROUP_ID>GROUP100</GROUP_ID>" +
			"  <COLUMN_NAME>PRIMARY_PHONE_NO</COLUMN_NAME>" +
			"  <CASE_SENSITIVE_IND>N</CASE_SENSITIVE_IND>" +
			"  <SUPPRESS_CHAR>-()</SUPPRESS_CHAR>" +
			"  <SOUND_IND>N</SOUND_IND>" +
			"  <FIRST_N_CHAR>null</FIRST_N_CHAR>" +
			"  <LAST_UPDATE_DATE><DATE>2004-07-22 11:20:55</DATE></LAST_UPDATE_DATE>" +
			"  <LAST_UPDATE_USER>PL</LAST_UPDATE_USER>" +
			"  <MATCH_START>N</MATCH_START>" +
			"  <MATCH_END>N</MATCH_END>" +
			"  <LAST_UPDATE_OS_USER>Administrator</LAST_UPDATE_OS_USER>" +
			"  <ALLOW_NULL_IND>N</ALLOW_NULL_IND>" +
			"  <TRANSLATE_IND>N</TRANSLATE_IND>" +
			"  <PURGE_IND>N</PURGE_IND>" +
			"  <REMOVE_SPECIAL_CHARS>N</REMOVE_SPECIAL_CHARS>" +
			"  <REORDER_IND>N</REORDER_IND>" +
			"  <FIRST_N_CHAR_BY_WORD_IND>N</FIRST_N_CHAR_BY_WORD_IND>" +
			"  <REPLACE_WITH_SPACE_IND>N</REPLACE_WITH_SPACE_IND>" +
			"  <MATCH_FIRST_PLUS_ONE_IND>N</MATCH_FIRST_PLUS_ONE_IND>" +
			"  <TRANSLATE_GROUP_NAME></TRANSLATE_GROUP_NAME>" +
			"  <SEQ_NO>null</SEQ_NO>" +
			"" +
			" </PL_MATCH_CRITERIA>" +
			" <PL_MATCH_CRITERIA>" +
			"" +
			"  <MATCH_ID>MATCH_PT_COMPANY</MATCH_ID>" +
			"  <GROUP_ID>GROUP100</GROUP_ID>" +
			"  <COLUMN_NAME>COMPANY_NAME</COLUMN_NAME>" +
			"  <CASE_SENSITIVE_IND>N</CASE_SENSITIVE_IND>" +
			"  <SUPPRESS_CHAR></SUPPRESS_CHAR>" +
			"  <SOUND_IND>N</SOUND_IND>" +
			"  <FIRST_N_CHAR>null</FIRST_N_CHAR>" +
			"  <LAST_UPDATE_DATE><DATE>2004-07-22 11:46:09</DATE></LAST_UPDATE_DATE>" +
			"  <LAST_UPDATE_USER>PL</LAST_UPDATE_USER>" +
			"  <MATCH_START>N</MATCH_START>" +
			"  <MATCH_END>N</MATCH_END>" +
			"  <LAST_UPDATE_OS_USER>Administrator</LAST_UPDATE_OS_USER>" +
			"  <ALLOW_NULL_IND>N</ALLOW_NULL_IND>" +
			"  <TRANSLATE_IND>N</TRANSLATE_IND>" +
			"  <PURGE_IND>N</PURGE_IND>" +
			"  <REMOVE_SPECIAL_CHARS>N</REMOVE_SPECIAL_CHARS>" +
			"  <REORDER_IND>N</REORDER_IND>" +
			"  <FIRST_N_CHAR_BY_WORD_IND>N</FIRST_N_CHAR_BY_WORD_IND>" +
			"  <REPLACE_WITH_SPACE_IND>N</REPLACE_WITH_SPACE_IND>" +
			"  <MATCH_FIRST_PLUS_ONE_IND>N</MATCH_FIRST_PLUS_ONE_IND>" +
			"  <TRANSLATE_GROUP_NAME></TRANSLATE_GROUP_NAME>" +
			"  <SEQ_NO>null</SEQ_NO>" +
			" </PL_MATCH_CRITERIA>" +
			" <PL_FOLDER>" +
			"  <FOLDER_NAME>JOHNSON_TEST</FOLDER_NAME>" +
			"  <FOLDER_DESC>test first folder</FOLDER_DESC>" +
			" </PL_FOLDER>" +
			" <PL_FOLDER_DETAIL>" +
			"  <FOLDER_NAME>JOHNSON_TEST</FOLDER_NAME>" +
			"  <OBJECT_TYPE>MATCH</OBJECT_TYPE>" +
			"  <OBJECT_NAME>MATCH_PT_COMPANY</OBJECT_NAME>" +
			" </PL_FOLDER_DETAIL>" +
			"</EXPORT>" +
			"";

	private MatchExportor exportor;
	private MatchImportor importor;

	private static final String ENCODING="UTF-8";
	private boolean deleteOnExit = true;

	private Match match;

	private TestingMatchMakerSession session;

	protected void setUp() throws Exception {
		super.setUp();
		exportor = new MatchExportor();
		importor = new MatchImportor();

		match = new Match();
        session = new TestingMatchMakerSession();
		session.setDatabase(new SQLDatabase());
        match.setSession(session);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testLoadFile() throws Exception {
		// StringReader r = new StringReader(testData);
		ByteArrayInputStream r = new ByteArrayInputStream(testData.getBytes());


		assertNotNull("match is not created.",match);
		assertEquals("match should have 0 group!",0,match.getMatchCriteriaGroups().size());
		importor.load(match,r);

		assertEquals("match name mismatch!","MATCH_PT_COMPANY",match.getName());
		assertEquals("match should have 4 groups!",4,match.getMatchCriteriaGroups().size());
		assertEquals("match folder name is not right!","JOHNSON_TEST",match.getParent().getName());

		int count = 0;
		for ( MatchMakerCriteriaGroup g : match.getMatchCriteriaGroups()) {
			count += g.getChildCount();
		}
		assertEquals("we should have total 6 criteria in the match",count,6);
	}

	public void testSaveFile() throws IOException {
		final String name = "test match should have a funny name with #$%~!@@#$%^&*() charactors";
		final String folderName = "folder name )(*&^%$#@!~\";'.,<>";
		match.setName(name);

		for ( int i=0; i<10; i++ ) {
			MatchMakerCriteriaGroup group = new MatchMakerCriteriaGroup();
			group.setName("group"+i);
			match.addMatchCriteriaGroup(group);

			for ( int n=0; n<10; n++) {
				final MatchMakerCriteria matchMakerCriteria = new MatchMakerCriteria();
				matchMakerCriteria.setName("criteria_"+i+"_"+n);
				group.addChild(matchMakerCriteria);
			}
		}
		match.setParent(new PlFolder(folderName));

		File tmp = File.createTempFile("test", ".xml");
		if (deleteOnExit) {
			tmp.deleteOnExit();
		}
		PrintWriter out = new PrintWriter(tmp,ENCODING);
		assertNotNull(out);
		assertTrue("File size not zero", (tmp.length() == 0) );
		exportor.save(match,out,ENCODING);

		assertTrue("File not exists", tmp.exists());
		assertTrue("File size zero", (tmp.length() > 0) );
	}
	public void testSaveAndLoadFile() throws IOException, ParserConfigurationException, SAXException {
		final String name = "test match should have a funny name with #$%~!@@#$%^&*() charactors";
		final String folderName = "folder name )(*&^%$#@!~\";'.,<>";
		match.setName(name);

		for ( int i=0; i<10; i++ ) {
			MatchMakerCriteriaGroup group = new MatchMakerCriteriaGroup();
			group.setName("group"+i);
			match.addMatchCriteriaGroup(group);

			for ( int n=0; n<10; n++) {
				final MatchMakerCriteria matchMakerCriteria = new MatchMakerCriteria();
				matchMakerCriteria.setName("criteria_"+i+"_"+n);
				group.addChild(matchMakerCriteria);
			}
		}
		match.setParent(new PlFolder(folderName));

		File tmp = File.createTempFile("test", ".xml");
		if (deleteOnExit) {
			tmp.deleteOnExit();
		}
		PrintWriter out = new PrintWriter(tmp,ENCODING);
		assertNotNull(out);
		exportor.save(match,out,ENCODING);

		Match match2 = new Match();
		match2.setSession(session);
		importor.load(match2,new FileInputStream(tmp));
		assertEquals("the name is not right",match2.getName(),name);
		assertTrue("reloaded match should equals to old match",match.equals(match2));
		assertTrue("reloaded match parent should equals to old one",
				match.getParent().equals(match2.getParent()));
		int groupCount = match2.getChildCount();
		assertEquals("child count equals",groupCount,match.getChildCount());
		for ( int i=0; i<groupCount; i++ ) {
			MatchMakerCriteriaGroup g = match2.getMatchCriteriaGroups().get(i);
			MatchMakerCriteriaGroup g2 = match.getMatchCriteriaGroups().get(i);
			assertTrue("groups should be the same", g.equals(g2));

			for ( int j=0; j<g.getChildCount(); j++) {
				MatchMakerCriteria c = g.getChildren().get(j);
				MatchMakerCriteria c2 = g2.getChildren().get(j);
				assertTrue("criteria should be the same.", c.equals(c2));
			}
		}

		File tmp2 = File.createTempFile("test2", ".xml");
		if (deleteOnExit) {
			tmp2.deleteOnExit();
		}

		exportor.save(match2,new PrintWriter(tmp2,ENCODING),ENCODING);
		assertEquals(tmp.length(), tmp2.length());
	}

	public void testSaveSourceTable() throws IOException, ParserConfigurationException, SAXException {
		SPDataSource ds = new SPDataSource();
	      ds.getParentType().setJdbcDriver("ca.sqlpower.util.MockJDBCDriver");
	      ds.setUrl("jdbc:mock:dbmd.catalogTerm=Catalog&dbmd.schemaTerm=Schema&catalogs=farm&schemas.farm=cow&tables.farm.cow=moo");
	      ds.setUser("n/a");
	      ds.setPass("n/a");
	      final SQLDatabase db = new SQLDatabase(ds);
	      session.setDatabase(db);
	      SQLTable sourceTable = db.getTableByName("farm", "cow", "moo");
	      match.setSourceTable(sourceTable);

	      File tmp = File.createTempFile("test", ".xml");
	      if (deleteOnExit) {
	    	  tmp.deleteOnExit();
	      }
	      PrintWriter out = new PrintWriter(tmp,ENCODING);
	      assertNotNull(out);
	      exportor.save(match,out,ENCODING);

	      Match match2 = new Match();
	      match2.setSession(session);

	      importor.load(match2,new FileInputStream(tmp));
	      SQLTable table2 = match2.getSourceTable();
	      assertTrue("table is not equals",sourceTable.equals(table2));
	}

	public void testSaveResultTable() throws IOException, ParserConfigurationException, SAXException {
		SPDataSource ds = new SPDataSource();
	      ds.getParentType().setJdbcDriver("ca.sqlpower.util.MockJDBCDriver");
	      ds.setUrl("jdbc:mock:dbmd.catalogTerm=Catalog&dbmd.schemaTerm=Schema&catalogs=farm&schemas.farm=cow&tables.farm.cow=moo");
	      ds.setUser("n/a");
	      ds.setPass("n/a");
	      final SQLDatabase db = new SQLDatabase(ds);
	      session.setDatabase(db);
	      SQLTable table1 = db.getTableByName("farm", "cow", "moo");
	      match.setResultTable(table1);

	      File tmp = File.createTempFile("test", ".xml");
	      if (deleteOnExit) {
	    	  tmp.deleteOnExit();
	      }
	      PrintWriter out = new PrintWriter(tmp,ENCODING);
	      assertNotNull(out);
	      exportor.save(match,out,ENCODING);

	      Match match2 = new Match();
	      match2.setSession(session);

	      importor.load(match2,new FileInputStream(tmp));
	      SQLTable table2 = match2.getResultTable();
	      assertTrue("table is not equals",table1.equals(table2));
	}

}