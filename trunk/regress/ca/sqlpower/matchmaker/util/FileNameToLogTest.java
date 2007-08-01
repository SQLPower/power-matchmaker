package ca.sqlpower.matchmaker.util;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.TestCase;
import ca.sqlpower.matchmaker.Match.MatchMode;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.testutil.MockJDBCDriver;
import ca.sqlpower.testutil.MockJDBCPreparedStatement;
import ca.sqlpower.testutil.MockJDBCResultSet;


public class FileNameToLogTest extends TestCase {

	File[] allLogs;

	FileNameToLog userType;
	private MockJDBCResultSet rs;
	private String[] names;
	String[] data;

	protected void setUp() throws Exception {
		super.setUp();

		userType = new FileNameToLog();
		MockJDBCDriver driver = new MockJDBCDriver();
		SPDataSource ds = new SPDataSource(new PlDotIni());
		String URL = "jdbc:mock:dbmd.catalogTerm=Catalog&dbmd.schemaTerm=Schema&catalogs=farm,yard,zoo&schemas.farm=cow,pig&schemas.yard=cat,robin&schemas.zoo=lion,giraffe&tables.farm.cow=moo&tables.farm.pig=oink&tables.yard.cat=meow&tables.yard.robin=tweet&tables.zoo.lion=roar&tables.zoo.giraffe=***,^%%";
		ds.getParentType().setJdbcDriver(MockJDBCDriver.class.getCanonicalName());
		ds.setName("a");
		ds.setPass("a");
		ds.setUrl(URL);
		ds.setDisplayName("a");
		ds.setUser("a");

		Connection con = ds.createConnection();
		Statement statements = con.createStatement();
		rs = (MockJDBCResultSet) statements.getResultSet();
		rs.setColumnCount(1);
		rs.setColumnName(1,"file_name");
		names = new String[1];
		names[0]="file_name";
		data = new String[MatchMode.values().length+1];
		allLogs = new File[3];

		for (int i = 0; i<3;i++) {
			data[i] = "File"+i;
			allLogs[i] = new File(data[i]);
		}
	}

	public void testDeepCopy() {

		File testCopy = (File) userType.deepCopy(allLogs[0]);
		assertEquals("Invalid log",allLogs[0],testCopy);
		testCopy = (File) userType.deepCopy(allLogs[1]);
		assertEquals("Invalid log",allLogs[1],testCopy);
		testCopy = (File) userType.deepCopy(allLogs[2]);
		assertEquals("Invalid log",allLogs[2],testCopy);

	}

	public void testNullGet() throws SQLException {
		for	(int i = 0;i < data.length; i++){
			Object[] row = {data[i]};
			rs.addRow(row);
			rs.next();
			File get = (File)userType.nullSafeGet(rs, names, null);
			if (i < allLogs.length) {
				assertEquals("The log type is not correct", allLogs[i], get);
			} else {
				assertEquals("The result is not correct",null, get);
			}

		}
	}


	public void testNullSafeSetAtFirstIndex() throws SQLException{
		MockJDBCPreparedStatement statements = new MockJDBCPreparedStatement(11);
		for	(int i = 0;i < data.length; i++){
			if (i < allLogs.length) {
				userType.nullSafeSet(statements, allLogs[i], 1);
			} else {
				userType.nullSafeSet(statements, null, 1);
			}
			Object[] values = statements.getParameters();
			assertEquals("The Log is not correct", data[i], (String)values[0]);
		}
	}

	public void testNullSafeSetAtIndexOtherThanFirst() throws SQLException{
	MockJDBCPreparedStatement statements = new MockJDBCPreparedStatement(11);
		for	(int i = 0;i < data.length; i++){
			if (i < allLogs.length) {
				userType.nullSafeSet(statements, allLogs[i], 6);
			} else {
				userType.nullSafeSet(statements, null, 6);
			}
			Object[] values = statements.getParameters();
			assertEquals("The match string is not correct", data[i], (String)values[5]);
		}
	}

}
