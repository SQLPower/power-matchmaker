package ca.sqlpower.matchmaker.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.TestCase;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.testutil.MockJDBCDriver;
import ca.sqlpower.testutil.MockJDBCPreparedStatement;
import ca.sqlpower.testutil.MockJDBCResultSet;

public class TypeStringToMatchTypeEnumTest extends TestCase {

	Match.MatchMode[] allTypes = Match.MatchMode.values();

	TypeStringToMatchTypeEnum userType;
	private MockJDBCResultSet rs;
	private String[] names;
	String[] data;
	
	protected void setUp() throws Exception {
		super.setUp();
			
		userType = new TypeStringToMatchTypeEnum();
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
		rs.setColumnName(1,"match_type");
		names = new String[1];
		names[0]="match_type";
		data = new String[Match.MatchMode.values().length+1];
		int i = 0;
		for (Match.MatchMode type: allTypes) {
			data[i] = type.toString();
			i++;     
		}
	}

	public void testDeepCopy() {
		for (Match.MatchMode type: allTypes) {
			Match.MatchMode testCopy = (Match.MatchMode) userType.deepCopy(type);
			assertEquals("Invalid match type",type,testCopy);
				
		}
	}
	
	public void testNullGet() throws SQLException {
		for	(int i = 0;i < data.length; i++){	
			Object[] row = {data[i]};
			rs.addRow(row);
			rs.next();
			Match.MatchMode get = (Match.MatchMode)userType.nullSafeGet(rs, names, null);
			if (i < allTypes.length) {
				assertEquals("The match type is not correct", allTypes[i], get);
			} else {
				assertEquals("The result is not correct",null, get);
			}
				
		}
	}
	
	
	public void testNullSafeSetAtFirstIndex() throws SQLException{
		MockJDBCPreparedStatement statements = new MockJDBCPreparedStatement(11);
		for	(int i = 0;i < data.length; i++){	
			if (i < allTypes.length) {
				userType.nullSafeSet(statements, allTypes[i], 1);
			} else {
				userType.nullSafeSet(statements, null, 1);
			}
			Object[] values = statements.getParameters();
			assertEquals("The match string is not correct", data[i], (String)values[0]);
		}
	}
	
	public void testNullSafeSetAtIndexOtherThanFirst() throws SQLException{
	MockJDBCPreparedStatement statements = new MockJDBCPreparedStatement(11);
		for	(int i = 0;i < data.length; i++){	
			if (i < allTypes.length) {
				userType.nullSafeSet(statements, allTypes[i], 6);
			} else {
				userType.nullSafeSet(statements, null, 6);
			}
			Object[] values = statements.getParameters();
			assertEquals("The match string is not correct", data[i], (String)values[5]);
		}		
	}
		
}
