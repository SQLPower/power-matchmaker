package ca.sqlpower.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import junit.framework.TestCase;

import org.hibernate.HibernateException;

import ca.sqlpower.architect.ArchitectConnectionFactory;
import ca.sqlpower.architect.ArchitectDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.MockJDBCDriver;
import ca.sqlpower.architect.MockJDBCResultSet;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.jdbc.MockJDBCPreparedStatement;
import ca.sqlpower.matchmaker.util.NamesToSQLTable;

public class NamesToSQLTableTest extends TestCase {

	NamesToSQLTable userType;

	protected void setUp() throws Exception {
		super.setUp();
		userType = new NamesToSQLTable();

	}

	public void testDeepCopy() throws ArchitectException {
		SQLCatalog c = new SQLCatalog();
		c.setName("catalog");
		SQLSchema s = new SQLSchema(c,"schema",true);
		SQLTable t = new SQLTable(s,"table","","TABLE",true);
		Object copy = userType.deepCopy(t);
		assertTrue("the copy is not an instance of SQLTable ",copy instanceof SQLTable);
		SQLTable copyTable = (SQLTable)copy;
		assertTrue("The catalog is not the same",c==copyTable.getCatalog());
		assertTrue("The schema is not the same",s==copyTable.getSchema());
		assertTrue("The table is the same",t!=copyTable);
		
		assertEquals("The catalog has a different value",t.getCatalogName(),copyTable.getCatalogName());
		assertEquals("The Schema has a different value",t.getSchemaName(),copyTable.getSchemaName());
		assertEquals("The table has a different value",t.getName(),copyTable.getName());
	}
	
	public void testDeepCopyNoSchema() throws ArchitectException {
		SQLCatalog c = new SQLCatalog();
		c.setName("catalog");
		SQLTable t = new SQLTable(c,"table","","TABLE",true);
		Object copy = userType.deepCopy(t);
		assertTrue("the copy is not an instance of SQLTable ",copy instanceof SQLTable);
		SQLTable copyTable = (SQLTable)copy;
		assertTrue("The catalog is the same",c==copyTable.getCatalog());
		assertNull("The schema is null",copyTable.getSchema());
		assertTrue("The table is the same",t!=copyTable);
		
		assertEquals("The catalog has a different value",t.getCatalogName(),copyTable.getCatalogName());
		assertEquals("The Schema has a different value",t.getSchemaName(),copyTable.getSchemaName());
		assertEquals("The table has a different value",t.getName(),copyTable.getName());
	}

	public void testDeepCopyNoSchemaOrCatalog() throws ArchitectException {
		
		SQLTable t = new SQLTable();
		t.setName("table");
		Object copy = userType.deepCopy(t);
		assertTrue("the copy is not an instance of SQLTable ",copy instanceof SQLTable);
		SQLTable copyTable = (SQLTable)copy;
		assertNull("The catalog is not null",copyTable.getCatalog());
		assertNull("The schema is not null",copyTable.getSchema());
		assertTrue("The table is the same",t!=copyTable);
		
		assertEquals("The catalog has a different value",t.getCatalogName(),copyTable.getCatalogName());
		assertEquals("The Schema has a different value",t.getSchemaName(),copyTable.getSchemaName());
		assertEquals("The table has a different value",t.getName(),copyTable.getName());
	}
	
	public void testIsMutable() {
		assertTrue("SQL Table should be mutable", userType.isMutable());
	}

	public void testNullSafeGet() throws HibernateException, SQLException {
		// Creates a mock JDBC Connection with a simple setup that has
		// a catalog name "PL" that has one schema name "PlSchema" with one
		// one table name "PlTable"
		
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
		Statement stmt = con.createStatement();
		MockJDBCResultSet rs = (MockJDBCResultSet) stmt.getResultSet();
		
		// setup the result set
		rs.setColumnCount(3);
		rs.setColumnName(1, "table_catalog");
		rs.setColumnName(2, "table_owner");
		rs.setColumnName(3, "match_table");
		Object[] row = { "zoo", "lion", "roar" };
		rs.addRow(row);
		rs.next();
		String[] names = { "table_catalog", "table_owner", "match_table" };
		SQLTable table = (SQLTable) userType.nullSafeGet(rs, names, null);
		assertNotNull("The table should not be null", table);
		assertEquals("The table name is wrong", table.getName(), "roar");
		assertEquals("The table is in the wrong schema", table.getSchemaName(),
				"lion");
		assertEquals("The table is in the wrong catalog", table
				.getCatalogName(), "zoo");
	}

	/**
	 * @throws ArchitectException
	 * @throws SQLException
	 * @throws HibernateException
	 * 
	 */
	public void testNullSafeSet() throws ArchitectException,
			HibernateException, SQLException {
		SQLCatalog c = new SQLCatalog();
		c.setPopulated(true);
		c.setName("saveCatalog");
		SQLSchema s = new SQLSchema(c, "saveSchema", true);
		SQLTable t = new SQLTable(s, "saveTable", "", "TABLE", true);
		MockJDBCPreparedStatement st = new MockJDBCPreparedStatement(5);
		userType.nullSafeSet(st, t, 2);
		Object[] preparedValues = st.getParameters();
		assertEquals("Value missmatch for the catalog", "saveCatalog",
				preparedValues[2]);
		assertEquals("Value missmatch for the schema", "saveSchema",
				preparedValues[3]);
		assertEquals("Value missmatch for the table", "saveTable",
				preparedValues[4]);
	}

	public void testNullSafeSetAlternateStart() throws ArchitectException,
			HibernateException, SQLException {
		SQLCatalog c = new SQLCatalog();
		c.setPopulated(true);
		c.setName("saveCatalog");
		SQLSchema s = new SQLSchema(c, "saveSchema", true);
		SQLTable t = new SQLTable(s, "saveTable", "", "TABLE", true);
		MockJDBCPreparedStatement st = new MockJDBCPreparedStatement(5);
		userType.nullSafeSet(st, t, 0);
		Object[] preparedValues = st.getParameters();
		assertEquals("Value missmatch for the catalog", "saveCatalog",
				preparedValues[0]);
		assertEquals("Value missmatch for the schema", "saveSchema",
				preparedValues[1]);
		assertEquals("Value missmatch for the table", "saveTable",
				preparedValues[2]);
	}

	public void testNullSafeSetNoSchema() throws ArchitectException,
			HibernateException, SQLException {
		SQLCatalog c = new SQLCatalog();
		c.setPopulated(true);
		c.setName("saveCatalog");
		SQLTable t = new SQLTable(c, "saveTable", "", "TABLE", true);
		MockJDBCPreparedStatement st = new MockJDBCPreparedStatement(5);
		userType.nullSafeSet(st, t, 0);
		Object[] preparedValues = st.getParameters();
		assertEquals("Value missmatch for the catalog", "saveCatalog",
				preparedValues[0]);
		assertEquals("Value missmatch for the schema", null, preparedValues[1]);
		assertEquals("Value missmatch for the table", "saveTable",
				preparedValues[2]);
	}

	public void testNullSafeSetNoSchemaOrCatalog() throws ArchitectException,
			HibernateException, SQLException {
		SQLTable t = new SQLTable();
		t.setName("saveTable");
		MockJDBCPreparedStatement st = new MockJDBCPreparedStatement(5);
		userType.nullSafeSet(st, t, 0);
		Object[] preparedValues = st.getParameters();
		assertEquals("Value missmatch for the catalog", null, preparedValues[0]);
		assertEquals("Value missmatch for the schema", null, preparedValues[1]);
		assertEquals("Value missmatch for the table", "saveTable",
				preparedValues[2]);
	}

	public void testNullSafeSetNullValue() throws ArchitectException,
			HibernateException, SQLException {
		MockJDBCPreparedStatement st = new MockJDBCPreparedStatement(5);
		userType.nullSafeSet(st, null, 0);
		Object[] preparedValues = st.getParameters();
		assertEquals("Value missmatch for the catalog", null, preparedValues[0]);
		assertEquals("Value missmatch for the schema", null, preparedValues[1]);
		assertEquals("Value missmatch for the table", null, preparedValues[2]);
	}

	public void testReturnedClass() {
		assertTrue("This is not a sql table",
				userType.returnedClass() == SQLTable.class);
	}

	public void testSqlTypes() {
		int[] types = userType.sqlTypes();
		assertNotNull("The type should never be null", types);
		assertEquals("We have the wrong number of types", 3, types.length);
		assertEquals("Type 0 is of the wrong type", Types.VARCHAR, types[0]);
		assertEquals("Type 1 is of the wrong type", Types.VARCHAR, types[1]);
		assertEquals("Type 2 is of the wrong type", Types.VARCHAR, types[2]);
	}

}
