package ca.sqlpower.matchmaker.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.TestCase;
import ca.sqlpower.architect.ArchitectConnectionFactory;
import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.MockJDBCDriver;
import ca.sqlpower.architect.MockJDBCResultSet;
import ca.sqlpower.architect.jdbc.MockJDBCPreparedStatement;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.util.log.Level;
import ca.sqlpower.matchmaker.util.log.Log;
import ca.sqlpower.matchmaker.util.log.LogFactory;

public class FileNameToLogTest extends TestCase {

	Log[] allLogs;

	FileNameToLog userType;
	private MockJDBCResultSet rs;
	private String[] names;
	String[] data;
	
	protected void setUp() throws Exception {
		super.setUp();
			
		userType = new FileNameToLog();
		MockJDBCDriver driver = new MockJDBCDriver();
		ArchitectDataSource ds = new ArchitectDataSource();
		String URL = "jdbc:mock:dbmd.catalogTerm=Catalog&dbmd.schemaTerm=Schema&catalogs=farm,yard,zoo&schemas.farm=cow,pig&schemas.yard=cat,robin&schemas.zoo=lion,giraffe&tables.farm.cow=moo&tables.farm.pig=oink&tables.yard.cat=meow&tables.yard.robin=tweet&tables.zoo.lion=roar&tables.zoo.giraffe=***,^%%";
		ds.setDriverClass(MockJDBCDriver.class.getCanonicalName());
		ds.setName("a");
		ds.setPass("a");
		ds.setUrl(URL);
		ds.setDisplayName("a");
		ds.setUser("a");
		ArchitectConnectionFactory factory = new ArchitectConnectionFactory(ds);
		
		Connection con = factory.createConnection();
		Statement statements = con.createStatement();
		rs = (MockJDBCResultSet) statements.getResultSet();
		rs.setColumnCount(1);
		rs.setColumnName(1,"file_name");
		names = new String[1];
		names[0]="file_name";
		data = new String[Match.MatchType.values().length+1];
		allLogs = new Log[3];
		for (int i = 0; i<3;i++) {
			data[i] = "File"+i;
			allLogs[i] = LogFactory.getLogger(Level.DEBUG, data[i]);
		}
	}

	public void testDeepCopy() throws ArchitectException{
		for (Log log: allLogs) {
			Log testCopy = (Log) userType.deepCopy(log);
			assertEquals("Invalid log",log.getConstraint(),testCopy.getConstraint());
				
		}
	}
	
	public void testNullGet() throws SQLException, ArchitectException{
		for	(int i = 0;i < data.length; i++){	
			Object[] row = {data[i]};
			rs.addRow(row);
			rs.next();
			Log get = (Log)userType.nullSafeGet(rs, names, null);
			if (i < allLogs.length) {
				assertEquals("The log type is not correct", allLogs[i].getConstraint(), get.getConstraint());
			} else {
				assertEquals("The result is not correct",null, get);
			}
				
		}
	}
	
	
	public void testNullSafeSetAtFirstIndex() throws SQLException{
		MockJDBCPreparedStatement statements = new MockJDBCPreparedStatement(11);
		for	(int i = 0;i < data.length; i++){	
			if (i < allLogs.length) {
				userType.nullSafeSet(statements, allLogs[i], 0);
			} else {
				userType.nullSafeSet(statements, null, 0);
			}
			Object[] values = statements.getParameters();
			assertEquals("The Log is not correct", data[i], (String)values[0]);
		}
	}
	
	public void testNullSafeSetAtIndexOtherThanFirst() throws SQLException{
	MockJDBCPreparedStatement statements = new MockJDBCPreparedStatement(11);
		for	(int i = 0;i < data.length; i++){	
			if (i < allLogs.length) {
				userType.nullSafeSet(statements, allLogs[i], 5);
			} else {
				userType.nullSafeSet(statements, null, 5);
			}
			Object[] values = statements.getParameters();
			assertEquals("The match string is not correct", data[i], (String)values[5]);
		}		
	}
		
}
