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

public class StringsToViewSpecTest extends TestCase {

	ViewSpec[] testQuery;

	StringsToViewSpec userType;
	private MockJDBCResultSet rs;
	private String[] names;
	String[][] data;
	
	protected void setUp() throws Exception {
		super.setUp();
			
		userType = new StringsToViewSpec();
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
		rs.setColumnCount(3);
		names = new String[3];
		rs.setColumnName(1,"select");
		names[0]="select";
		rs.setColumnName(2,"from");
		names[1]="from";
		rs.setColumnName(3,"where");
		names[2]="where";
		data = new String[Match.MatchType.values().length+1][3];
		testQuery = new ViewSpec[3];
		testQuery[0] = new ViewSpec("Select *","from","where");
		testQuery[1] = new ViewSpec("Select 1","from 1","where 1");
		testQuery[2] = new ViewSpec("Select 2","from 2","where 2");
		
		for (int i=0; i< testQuery.length; i++) {
			data[i][0] = testQuery[i].getSelect();
			data[i][1] = testQuery[i].getFrom();
			data[i][2] = testQuery[i].getWhere();     
		}
	}

	public void testDeepCopy() throws ArchitectException{
		for (ViewSpec query: testQuery) {
			ViewSpec testCopy = (ViewSpec) userType.deepCopy(query);
			assertEquals("Invalid query",query,testCopy);
				
		}
	}
	
	public void testNullGet() throws SQLException, ArchitectException{
		for	(int i = 0;i < data.length; i++){	
			Object[] row = data[i];
			rs.addRow(row);
			rs.next();
			ViewSpec get = (ViewSpec)userType.nullSafeGet(rs, names, null);
			if (i < testQuery.length) {
				assertEquals("The query is not correct", testQuery[i], get);
			} else {
				assertEquals("The result is not correct",null, get);
			}
				
		}
	}
	
	
	public void testNullSafeSetAtFirstIndex() throws SQLException{
		MockJDBCPreparedStatement statements = new MockJDBCPreparedStatement(11);
		for	(int i = 0;i < data.length; i++){	
			if (i < testQuery.length) {
				userType.nullSafeSet(statements, testQuery[i], 0);
			} else {
				userType.nullSafeSet(statements, null, 0);
			}
			Object[] values = statements.getParameters();
			assertEquals("The select string is not correct", data[i][0], (String)values[0]);
			assertEquals("The from string is not correct", data[i][1], (String)values[1]);
			assertEquals("The where string is not correct", data[i][2], (String)values[2]);
		}
	}
	
	public void testNullSafeSetAtIndexOtherThanFirst() throws SQLException{
	MockJDBCPreparedStatement statements = new MockJDBCPreparedStatement(11);
		for	(int i = 0;i < data.length; i++){	
			if (i < testQuery.length) {
				userType.nullSafeSet(statements, testQuery[i], 5);
			} else {
				userType.nullSafeSet(statements, null, 5);
			}
			Object[] values = statements.getParameters();
			assertEquals("The select string is not correct for dataset "+i, data[i][0], (String)values[5]);
			assertEquals("The from string is not correct for dataset "+i, data[i][1], (String)values[6]);
			assertEquals("The where string is not correct for dataset "+i, data[i][2], (String)values[7]);
		}		
	}
		
}
