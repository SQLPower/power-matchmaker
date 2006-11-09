package ca.sqlpower.matchmaker.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.TestCase;

import org.hibernate.HibernateException;

import ca.sqlpower.architect.ArchitectConnectionFactory;
import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.MockJDBCDriver;
import ca.sqlpower.architect.MockJDBCResultSet;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLIndex.Column;
import ca.sqlpower.architect.jdbc.MockJDBCPreparedStatement;

public class ListToSQLIndexTest extends TestCase {

	SQLIndex index;
	SQLIndex.Column c0;
	SQLIndex.Column c1;
	SQLIndex.Column c2;
	SQLIndex.Column c3;
	SQLIndex.Column c4;
	SQLIndex.Column c5;
	SQLIndex.Column c6;
	SQLIndex.Column c7;
	SQLIndex.Column c8;
	SQLIndex.Column c9;
	ListToSQLIndex userType;
	private MockJDBCResultSet rs;
	private String[] names;
	
	
	protected void setUp() throws Exception {
		super.setUp();
		index = new SQLIndex();
		index.setName("TestIndex");
		c0 = index.new Column("Test0", false, false);
		c1 = index.new Column("Test1", false, false);
		c2 = index.new Column("Test2", false, false);
		c3 = index.new Column("Test3", false, false);
		c4 = index.new Column("Test4", false, false);
		c5 = index.new Column("Test5", false, false);
		c6 = index.new Column("Test6", false, false);
		c7 = index.new Column("Test7", false, false);
		c8 = index.new Column("Test8", false, false);
		c9 = index.new Column("Test9", false, false);		
		index.addChild(c0);
		index.addChild(c1);
		index.addChild(c2);
		index.addChild(c3);
		index.addChild(c4);
		index.addChild(c5);
		index.addChild(c6);
		index.addChild(c7);
		index.addChild(c8);
		index.addChild(c9);		
		userType = new ListToSQLIndex();
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
		rs.setColumnCount(11);
		rs.setColumnName(1,"pkName");
		names = new String[11];
		names[0]="pkName";
		for (int i=1; i < 11; i++){
			rs.setColumnName(i+1, "index_column_name"+i);
			names[i]="index_column_name"+i;
		}
	}

	public void testDeepCopy() throws ArchitectException{
		SQLIndex testCopy = (SQLIndex) userType.deepCopy(index);
		assertEquals("The test copy should have the same number of children",
						testCopy.getChildCount(), index.getChildCount());
		for (int i=0; i < testCopy.getChildren().size(); i++){			
			if (!(testCopy.getChildren().get(i) instanceof SQLIndex.Column)){
				fail("SQLIndex child cannot be anything else other than SQLColumn");
			}
			SQLIndex.Column testChild = (Column) index.getChild(i);
			SQLIndex.Column copyChild = (Column) testCopy.getChild(i);			
			assertEquals("The two elements should have the same name",
									testChild.getName(), copyChild.getName());
			assertEquals("The two elements should have the same ascending state",
					testChild.isAscending(), copyChild.isAscending());
			assertEquals("The two elements should have the descending state",
					testChild.isDescending(), copyChild.isDescending());
		}		
	}
	
	public void testNullGetWithAllValidColumns() throws SQLException, ArchitectException{

		String[] data = new String[11];
		data[0] = "pkName";
		for (int i=1; i>11;i++){
			data[i]= "index_column_name"+i;
		}
		
		rs.addRow(data);
		
		SQLIndex ind = (SQLIndex)userType.nullSafeGet(rs, names, null);
		assertNotNull("We should not be getting a null value when we don't pass one in",ind);
		assertEquals("The primary key is not correct",  "pkName", ind.getName());
	
		for (int j=1; j>11; j++){
			assertEquals("The child does not have the right name", 
					"index_column_name"+j,ind.getChild(j).getName()); 
		}
	}
	
	public void testNullGetWithNullsInList() throws HibernateException, SQLException, ArchitectException{
		String[] data = new String[11];
		data[0] = "pkName";
		for (int i=1; i<11;i++){
			data[i]= "index_column_name"+i;
		}
		data[6] = null;
		rs.addRow(data);
		
		SQLIndex ind = (SQLIndex)userType.nullSafeGet(rs, names, null);
		assertNotNull("We should not be getting a null value when we don't pass one in",ind);
		assertEquals("The primary key is not correct", ind.getName(), "pkName");
		try {
			assertEquals("The children size of the SQLIndex is not correct"+ind.getChildren(), 
										ind.getChildCount(), 9);
		} catch (ArchitectException e1) {
			throw new HibernateException(e1);			
		}
		for (int j=1; j>10; j++){
			try {
				if (j < 6){
					assertEquals("The child does not have the right name", 
							"index_column_name"+j, ind.getChild(j).getName() );
				}else {
					assertEquals("The child does not have the right name", 
							 "index_column_name"+(j+1),ind.getChild(j).getName());
				}
			} catch (ArchitectException e) {
				throw new HibernateException(e);
			} 
		}
	}
	
	public void testNullSafeSetAtFirstIndex() throws SQLException{
		MockJDBCPreparedStatement statements = new MockJDBCPreparedStatement(11);
		userType.nullSafeSet(statements, index, 0);
		Object[] values = statements.getParameters();
		assertEquals("The index has the wrong name","TestIndex", (String)values[0]);
		for (int i=1; i < 11; i++){
			assertEquals("The columns have the wrong name","Test"+(i-1), 
					(String)values[i]);			
		}		
	}
	
	public void testNullSafeSetAtIndexOtherThanFirst() throws SQLException{
		MockJDBCPreparedStatement statements = new MockJDBCPreparedStatement(18);
		userType.nullSafeSet(statements, index, 5);
		Object[] values = statements.getParameters();
		assertEquals("The index has the wrong name","TestIndex", (String)values[5]);
		for (int i=6; i < 16; i++){
			assertEquals("The columns have the wrong name","Test"+(i-6), 
					(String)values[i]);			
		}		
	}
		
}
