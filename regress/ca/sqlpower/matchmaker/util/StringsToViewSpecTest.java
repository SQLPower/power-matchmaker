package ca.sqlpower.matchmaker.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.TestCase;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.util.MockJDBCDriver;
import ca.sqlpower.util.MockJDBCPreparedStatement;
import ca.sqlpower.util.MockJDBCResultSet;

public class StringsToViewSpecTest extends TestCase {

    /**
     * The object under test.
     */
    private StringsToViewSpec userType;
    
    /**
     * An array of example objects that the tests will attempt to
     * convert to strings.
     */
    private ViewSpec[] testViewSpecs;

    /**
     * A fake result set that simulates what Hibernate would pass to
     * the userType in real life.  It is not pre-populated with rows
     * of data by setUp, but the column names are set up.
     */
	private MockJDBCResultSet rs;
    
    /**
     * The column names of rs which hibernate would give to the user type.
     */
	private String[] names;
    
    /**
     * The data that tests can feed into the result set rs.
     */
    private String[][] data;
	
	protected void setUp() throws Exception {
		super.setUp();
			
		userType = new StringsToViewSpec();
		SPDataSource ds = new SPDataSource();
		String URL = "jdbc:mock:dbmd.catalogTerm=Catalog&dbmd.schemaTerm=Schema&catalogs=farm,yard,zoo&schemas.farm=cow,pig&schemas.yard=cat,robin&schemas.zoo=lion,giraffe&tables.farm.cow=moo&tables.farm.pig=oink&tables.yard.cat=meow&tables.yard.robin=tweet&tables.zoo.lion=roar&tables.zoo.giraffe=***,^%%";
		ds.getParentType().setJdbcDriver(MockJDBCDriver.class.getCanonicalName());
		ds.setName("a");
		ds.setPass("a");
		ds.setUrl(URL);
		ds.setDisplayName("a");
		ds.setUser("a");
		
		Connection con = ds.createConnection();
		Statement stmt = con.createStatement();
		rs = (MockJDBCResultSet) stmt.getResultSet();
		rs.setColumnCount(3);
		names = new String[3];
		rs.setColumnName(1,"select");
		names[0]="select";
		rs.setColumnName(2,"from");
		names[1]="from";
		rs.setColumnName(3,"where");
		names[2]="where";
		testViewSpecs = new ViewSpec[3];
		testViewSpecs[0] = new ViewSpec("Select *","from","where");
		testViewSpecs[1] = new ViewSpec("Select 1","from 1","where 1");
		testViewSpecs[2] = new ViewSpec("Select 2","from 2","where 2");
		
		data = new String[testViewSpecs.length+1][3];
		for (int i=0; i< testViewSpecs.length; i++) {
			data[i][0] = testViewSpecs[i].getSelect();
			data[i][1] = testViewSpecs[i].getFrom();
			data[i][2] = testViewSpecs[i].getWhere();     
		}
        // note, the data array has one extra entry which is left null (for testing null safety of nullSafeGet)
	}

	public void testDeepCopy() {
		for (ViewSpec query: testViewSpecs) {
			ViewSpec testCopy = (ViewSpec) userType.deepCopy(query);
			assertEquals("Invalid query",query,testCopy);
				
		}
	}
	
	public void testNullGet() throws SQLException {
        rs.addRow(new Object[] {null, null, null});
        rs.next();
        ViewSpec spec = (ViewSpec) userType.nullSafeGet(rs, names, null);
	    assertNull("Spec should have been null because all data was null", spec);
	}
	
    public void testNonNullGet() throws SQLException {
        rs.addRow(new Object[] {"select clause", "from clause", "where clause"});
        rs.next();
        ViewSpec spec = (ViewSpec) userType.nullSafeGet(rs, names, null);
        assertEquals("select clause", spec.getSelect());
        assertEquals("from clause", spec.getFrom());
        assertEquals("where clause", spec.getWhere());
    }
	
	public void testNullSafeSetAtFirstIndex() throws SQLException{
		MockJDBCPreparedStatement statements = new MockJDBCPreparedStatement(11);
		for	(int i = 0;i < data.length; i++){	
			if (i < testViewSpecs.length) {
				userType.nullSafeSet(statements, testViewSpecs[i], 1);
			} else {
				userType.nullSafeSet(statements, null, 1);
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
			if (i < testViewSpecs.length) {
				userType.nullSafeSet(statements, testViewSpecs[i], 6);
			} else {
				userType.nullSafeSet(statements, null, 6);
			}
			Object[] values = statements.getParameters();
			assertEquals("The select string is not correct for dataset "+i, data[i][0], (String)values[5]);
			assertEquals("The from string is not correct for dataset "+i, data[i][1], (String)values[6]);
			assertEquals("The where string is not correct for dataset "+i, data[i][2], (String)values[7]);
		}		
	}
		
}
