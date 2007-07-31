package ca.sqlpower.matchmaker;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectUtils;
import ca.sqlpower.architect.SQLCatalog;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLSchema;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.SQLIndex.IndexType;
import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.DDLStatement;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.matchmaker.event.MatchMakerEventCounter;

public class MatchTest extends MatchMakerTestCase<Match> {

    Match match;
	private TestingMatchMakerSession session;

    protected void setUp() throws Exception {
    	// The following two are ignored because they are to be used only by hibernate
    	// so they don't throw events
        propertiesToIgnoreForEventGeneration.add("matchCriteriaGroups");
        propertiesToIgnoreForEventGeneration.add("tableMergeRules");
        // Ignored because they are delegates to support functions for the chached table class
        propertiesToIgnoreForEventGeneration.add("sourceTableCatalog");
        propertiesToIgnoreForEventGeneration.add("sourceTableSchema");
        propertiesToIgnoreForEventGeneration.add("sourceTableName");
        propertiesToIgnoreForEventGeneration.add("resultTableCatalog");
        propertiesToIgnoreForEventGeneration.add("resultTableSchema");
        propertiesToIgnoreForEventGeneration.add("resultTableName");
        propertiesToIgnoreForEventGeneration.add("xrefTableCatalog");
        propertiesToIgnoreForEventGeneration.add("xrefTableSchema");
        propertiesToIgnoreForEventGeneration.add("xrefTableName");
        propertiesToIgnoreForDuplication.add("matchGroups");
        // These set other properties to null that describe the same object
        propertiesToIgnoreForDuplication.add("resultTable");
        propertiesToIgnoreForDuplication.add("sourceTable");
        
        propertiesToIgnoreForDuplication.add("sourceTableIndex");
        
        propertiesThatHaveSideEffects.add("xrefTable");
        propertiesThatHaveSideEffects.add("sourceTable");
        propertiesThatHaveSideEffects.add("resultTable");
        super.setUp();
        match = new Match();
        session = new TestingMatchMakerSession();
		session.setDatabase(new SQLDatabase());
        match.setSession(session);
    }
    @Override
    protected Match getTarget() {
        return match;
    }


	public void testEqual() {
		Match m1 = new Match();
		Match m2 = new Match();
		assertTrue("Match1 <> match2", (m1 != m2) );
		assertTrue("Match1 equals match2", m1.equals(m2) );
		m1.setName("match1");
		m2.setName("match2");
		assertFalse("Match1 should not equals match2", m1.equals(m2) );
		m1.setName("match");
		m2.setName("match");
		assertTrue("Match1 should equals match2", m1.equals(m2) );
	}

    public void testMatchMakerFolderFiresEventForMatchCriteriaGroups(){
        MatchMakerEventCounter l = new MatchMakerEventCounter();
        match.getMatchCriteriaGroupFolder().addMatchMakerListener(l);
        List<MatchMakerCriteriaGroup> mmoList = new ArrayList<MatchMakerCriteriaGroup>();
        match.setMatchCriteriaGroups(mmoList);
        assertEquals("Wrong number of events fired",1,l.getAllEventCounts());
        assertEquals("Wrong type of event fired",1,l.getStructureChangedCount());
    }
    
    public void testCreateResultTable() throws ArchitectException {
    	SQLTable sourceTable = new SQLTable(match.getSession().getDatabase(), "match_source", null, "TABLE", true);
    	
    	SQLColumn pk1 = new SQLColumn(sourceTable, "pk1", Types.VARCHAR, 20, 0);
    	pk1.setNullable(DatabaseMetaData.columnNoNulls);
    	sourceTable.addColumn(pk1);
    
    	SQLColumn pk2 = new SQLColumn(sourceTable, "pk2", Types.INTEGER, 20, 0);
    	pk2.setNullable(DatabaseMetaData.columnNoNulls);
    	sourceTable.addColumn(pk2);
    	
    	SQLColumn col = new SQLColumn(sourceTable, "normal_col_1", Types.VARCHAR, 20, 0);
    	col.setNullable(DatabaseMetaData.columnNullable);
    	sourceTable.addColumn(col);
    	
    	SQLIndex idx = new SQLIndex("source_pk", true, null, IndexType.HASHED, null);
    	idx.addChild(idx.new Column(pk1, true, false));
    	idx.addChild(idx.new Column(pk2, true, false));
    	sourceTable.addIndex(idx);

    	match.setSourceTable(sourceTable);
    	match.setSourceTableIndex(idx);
    	
    	match.setResultTableName("my_result_table_that_almost_didnt_have_cow_in_its_name");
    	SQLTable resultTable = match.createResultTable();
    	
    	int i = 0;
    	assertEquals("dup_candidate_10", resultTable.getColumn(i++).getName());
    	assertEquals("dup_candidate_11", resultTable.getColumn(i++).getName());
    	assertEquals("dup_candidate_20", resultTable.getColumn(i++).getName());
    	assertEquals("dup_candidate_21", resultTable.getColumn(i++).getName());
    	assertEquals("current_candidate_10", resultTable.getColumn(i++).getName());
    	assertEquals("current_candidate_11", resultTable.getColumn(i++).getName());
    	assertEquals("current_candidate_20", resultTable.getColumn(i++).getName());
    	assertEquals("current_candidate_21", resultTable.getColumn(i++).getName());
    	assertEquals("dup_id0", resultTable.getColumn(i++).getName());
    	assertEquals("dup_id1", resultTable.getColumn(i++).getName());
    	assertEquals("master_id0", resultTable.getColumn(i++).getName());
    	assertEquals("master_id1", resultTable.getColumn(i++).getName());
    	assertEquals("candidate_10_mapped", resultTable.getColumn(i++).getName());
    	assertEquals("candidate_11_mapped", resultTable.getColumn(i++).getName());
    	assertEquals("candidate_20_mapped", resultTable.getColumn(i++).getName());
    	assertEquals("candidate_21_mapped", resultTable.getColumn(i++).getName());
    	assertEquals("match_percent", resultTable.getColumn(i++).getName());
    	assertEquals("group_id", resultTable.getColumn(i++).getName());
    	assertEquals("match_date", resultTable.getColumn(i++).getName());
    	assertEquals("match_status", resultTable.getColumn(i++).getName());
    	assertEquals("match_status_date", resultTable.getColumn(i++).getName());
    	assertEquals("match_status_user", resultTable.getColumn(i++).getName());
    	assertEquals("dup1_master_ind", resultTable.getColumn(i++).getName());
    	assertEquals(i, idx.getChildCount()*8 + 7); // sanity check for the test
    	
    	i = 0;
    	assertEquals(pk1.getType(), resultTable.getColumn(i++).getType());
    	assertEquals(pk2.getType(), resultTable.getColumn(i++).getType());
    	assertEquals(pk1.getType(), resultTable.getColumn(i++).getType());
    	assertEquals(pk2.getType(), resultTable.getColumn(i++).getType());
    	assertEquals(pk1.getType(), resultTable.getColumn(i++).getType());
    	assertEquals(pk2.getType(), resultTable.getColumn(i++).getType());
    	assertEquals(pk1.getType(), resultTable.getColumn(i++).getType());
    	assertEquals(pk2.getType(), resultTable.getColumn(i++).getType());
    	assertEquals(pk1.getType(), resultTable.getColumn(i++).getType());
    	assertEquals(pk2.getType(), resultTable.getColumn(i++).getType());
    	assertEquals(pk1.getType(), resultTable.getColumn(i++).getType());
    	assertEquals(pk2.getType(), resultTable.getColumn(i++).getType());
    	assertEquals(Types.VARCHAR, resultTable.getColumn(i++).getType());
    	assertEquals(Types.VARCHAR, resultTable.getColumn(i++).getType());
    	assertEquals(Types.VARCHAR, resultTable.getColumn(i++).getType());
    	assertEquals(Types.VARCHAR, resultTable.getColumn(i++).getType());
    	assertEquals(Types.INTEGER, resultTable.getColumn(i++).getType());
    	assertEquals(Types.VARCHAR, resultTable.getColumn(i++).getType());
    	assertEquals(Types.TIMESTAMP, resultTable.getColumn(i++).getType());
    	assertEquals(Types.VARCHAR, resultTable.getColumn(i++).getType());
    	assertEquals(Types.TIMESTAMP, resultTable.getColumn(i++).getType());
    	assertEquals(Types.VARCHAR, resultTable.getColumn(i++).getType());
    	assertEquals(Types.VARCHAR, resultTable.getColumn(i++).getType());
    	assertEquals(i, idx.getChildCount()*8 + 7); // sanity check for the test
    }
    
    public void testCreateResultTableIndex() throws ArchitectException {
    	SQLTable sourceTable = new SQLTable(match.getSession().getDatabase(), "match_source", null, "TABLE", true);
    	
    	SQLColumn pk1 = new SQLColumn(sourceTable, "pk1", Types.VARCHAR, 20, 0);
    	pk1.setNullable(DatabaseMetaData.columnNoNulls);
    	sourceTable.addColumn(pk1);
    
    	SQLColumn pk2 = new SQLColumn(sourceTable, "pk2", Types.INTEGER, 20, 0);
    	pk2.setNullable(DatabaseMetaData.columnNoNulls);
    	sourceTable.addColumn(pk2);
    	
    	SQLColumn col = new SQLColumn(sourceTable, "normal_col_1", Types.VARCHAR, 20, 0);
    	col.setNullable(DatabaseMetaData.columnNullable);
    	sourceTable.addColumn(col);
    	
    	SQLIndex idx = new SQLIndex("source_pk", true, null, IndexType.HASHED, null);
    	idx.addChild(idx.new Column(pk1, true, false));
    	idx.addChild(idx.new Column(pk2, true, false));
    	sourceTable.addIndex(idx);

    	match.setSourceTable(sourceTable);
    	match.setSourceTableIndex(idx);
    	
    	match.setResultTableName("my_result_table_that_almost_didnt_have_cow_in_its_name");
    	SQLTable resultTable = match.createResultTable();
    	
    	List<SQLIndex> indices = resultTable.getIndicesFolder().getChildren();
    	assertEquals(1, indices.size());

    	SQLIndex rtidx = indices.get(0);
    	assertEquals(true, rtidx.isUnique());
    	final int idxColCount = rtidx.getChildCount();
    	assertEquals(idx.getChildCount() * 2, idxColCount);
    	for (int i = 0; i < idxColCount/2; i++) {
    		assertEquals("dup_candidate_1"+i, rtidx.getChild(i).getName());
    	}
        for (int i = idxColCount/2; i < idxColCount; i++) {
            assertEquals("dup_candidate_2"+(i-idxColCount/2), rtidx.getChild(i).getName());
        }

    }
    
    public void testCreateResultTableInCorrectCatalogSchema() throws Exception {
    	// dumb source table and index with no columns to satisfy createResultsTable() preconditions
    	SQLTable tab = new SQLTable(session.getDatabase(), true);
    	SQLIndex idx = new SQLIndex("my_index", true, null, IndexType.CLUSTERED, null);
    	match.setSourceTable(tab);
    	match.setSourceTableIndex(idx);
    	
    	match.setResultTableCatalog("my_cat");
    	match.setResultTableSchema("my_dog");
    	match.setResultTableName("my_chinchilla");
    	
    	SQLTable resultTable = match.createResultTable();
    	
    	assertEquals("my_cat", resultTable.getCatalogName());
    	assertEquals("my_dog", resultTable.getSchemaName());
    	assertEquals("my_chinchilla", resultTable.getName());
    }
    
    public void testGetSourceTableIndex() throws Exception {
    	SQLIndex foo = new SQLIndex();
    	match.setSourceTableIndex(foo);
    	assertNotNull(match.getSourceTableIndex());
    	assertSame(foo, match.getSourceTableIndex());
    }
    
	public void testResultTableExistsWhenTrue() throws Exception {
		SPDataSource ds = new SPDataSource();
		ds.getParentType().setJdbcDriver("ca.sqlpower.util.MockJDBCDriver");
		ds
				.setUrl("jdbc:mock:dbmd.catalogTerm=Catalog&dbmd.schemaTerm=Schema&catalogs=farm&schemas.farm=cow&tables.farm.cow=moo");
		ds.setUser("n/a");
		ds.setPass("n/a");
		final SQLDatabase db = new SQLDatabase(ds);
		session.setDatabase(db);
		SQLTable resultTable = db.getTableByName("farm", "cow", "moo");
		assertNotNull(resultTable);
		match.setResultTable(resultTable);
		assertTrue(Match.doesResultTableExist(session, match));
	}
	

	/**
	 * Tests that new nonexistant handcrafted tables are nonexistant according
	 * to the Match object.
	 */
	public void testResultTableExistsWhenFalse() throws Exception {
		SPDataSource ds = new SPDataSource();
		ds.getParentType().setJdbcDriver("ca.sqlpower.util.MockJDBCDriver");
		ds
				.setUrl("jdbc:mock:dbmd.catalogTerm=Catalog&dbmd.schemaTerm=Schema&catalogs=farm&schemas.farm=cow&tables.farm.cow=moo");
		ds.setUser("n/a");
		ds.setPass("n/a");
		final SQLDatabase db = new SQLDatabase(ds);
		session.setDatabase(db);
		SQLCatalog farmCat = (SQLCatalog) db.getChildByName("farm");
		SQLSchema cowSchem = (SQLSchema) farmCat.getChildByName("cow");
		SQLTable resultTable = new SQLTable(cowSchem, "nonexistant", null,
				"TABLE", true);
		match.setResultTable(resultTable);
		assertFalse(Match.doesResultTableExist(session, match));
	}

	/**
	 * Tests that new nonexistant simulated tables that are really in the
	 * session's in-memory view of the database are nonexistant according to the
	 * Match object.
	 */
	public void testResultTableExistsWhenInMemoryButStillFalse()
			throws Exception {
		SPDataSource ds = new SPDataSource();
		ds.getParentType().setJdbcDriver("ca.sqlpower.util.MockJDBCDriver");
		ds
				.setUrl("jdbc:mock:dbmd.catalogTerm=Catalog&dbmd.schemaTerm=Schema&catalogs=farm&schemas.farm=cow&tables.farm.cow=moo");
		ds.setUser("n/a");
		ds.setPass("n/a");
		final SQLDatabase db = new SQLDatabase(ds);
		session.setDatabase(db);
		SQLTable resultTable = ArchitectUtils.addSimulatedTable(db, "cat",
				"sch", "faketab");
		match.setResultTable(resultTable);
		assertFalse(Match.doesResultTableExist(session, match));
	}
	
	public void testSourceTableExistsWhenTrue() throws Exception {
		SPDataSource ds = new SPDataSource();
		ds.getParentType().setJdbcDriver("ca.sqlpower.util.MockJDBCDriver");
		ds
				.setUrl("jdbc:mock:dbmd.catalogTerm=Catalog&dbmd.schemaTerm=Schema&catalogs=farm&schemas.farm=cow&tables.farm.cow=moo");
		ds.setUser("n/a");
		ds.setPass("n/a");
		final SQLDatabase db = new SQLDatabase(ds);
		session.setDatabase(db);
		SQLTable sourceTable = db.getTableByName("farm", "cow", "moo");
		assertNotNull(sourceTable);
		match.setSourceTable(sourceTable);
		assertTrue(Match.doesSourceTableExist(session, match));
	}

	/**
	 * Tests that new nonexistant handcrafted tables are nonexistant according
	 * to the Match object.
	 */
	public void testSourceTableExistsWhenFalse() throws Exception {
		SPDataSource ds = new SPDataSource();
		ds.getParentType().setJdbcDriver("ca.sqlpower.util.MockJDBCDriver");
		ds
				.setUrl("jdbc:mock:dbmd.catalogTerm=Catalog&dbmd.schemaTerm=Schema&catalogs=farm&schemas.farm=cow&tables.farm.cow=moo");
		ds.setUser("n/a");
		ds.setPass("n/a");
		final SQLDatabase db = new SQLDatabase(ds);
		session.setDatabase(db);
		SQLCatalog farmCat = (SQLCatalog) db.getChildByName("farm");
		SQLSchema cowSchem = (SQLSchema) farmCat.getChildByName("cow");
		SQLTable sourceTable = new SQLTable(cowSchem, "nonexistant", null,
				"TABLE", true);
		match.setSourceTable(sourceTable);
		assertFalse(Match.doesSourceTableExist(session, match));
	}

	/**
	 * Tests that new nonexistant simulated tables that are really in the
	 * session's in-memory view of the database are nonexistant according to the
	 * Match object.
	 */
	public void testSourceTableExistsWhenInMemoryButStillFalse()
			throws Exception {
		SPDataSource ds = new SPDataSource();
		ds.getParentType().setJdbcDriver("ca.sqlpower.util.MockJDBCDriver");
		ds.setUrl("jdbc:mock:dbmd.catalogTerm=Catalog&dbmd.schemaTerm=Sc" +
				"hema&catalogs=farm&schemas.farm=cow&tables.farm.cow=moo");
		ds.setUser("n/a");
		ds.setPass("n/a");
		final SQLDatabase db = new SQLDatabase(ds);
		session.setDatabase(db);
		SQLTable sourceTable = ArchitectUtils.addSimulatedTable(db, "cat",
				"sch", "faketab");
		match.setSourceTable(sourceTable);
		assertFalse(Match.doesSourceTableExist(session, match));
	}
	
	public void testVertifyResultTableSS() throws SQLException, InstantiationException,
											IllegalAccessException, ArchitectException {
		
		SPDataSource ds = DBTestUtil.getSqlServerDS();
		SQLDatabase db = new SQLDatabase(ds);
		session.setDatabase(db);
		session.setConnection(db.getConnection());
		match.setSession(session);
		
		
		
    	SQLTable sourceTable = new SQLTable(db.getSchemaByName(ds.getPlSchema()), "match_source", null, "TABLE", true);
    	
    	SQLColumn pk1 = new SQLColumn(sourceTable, "pk1", Types.VARCHAR, 20, 0);
    	pk1.setNullable(DatabaseMetaData.columnNoNulls);
    	sourceTable.addColumn(pk1);
    
    	// precision can not be other value than 10 since the sql server does not 
    	// support column width for int, and the column size from jdbc will
    	// be 10.
    	SQLColumn pk2 = new SQLColumn(sourceTable, "pk2", Types.INTEGER, 10, 0);
    	pk2.setNullable(DatabaseMetaData.columnNoNulls);
    	sourceTable.addColumn(pk2);
    	
    	SQLColumn col = new SQLColumn(sourceTable, "normal_col_1", Types.VARCHAR, 20, 0);
    	col.setNullable(DatabaseMetaData.columnNullable);
    	sourceTable.addColumn(col);
    	
    	SQLIndex idx = new SQLIndex("source_pk", true, null, IndexType.HASHED, null);
    	idx.addChild(idx.new Column(pk1, true, false));
    	idx.addChild(idx.new Column(pk2, true, false));
    	sourceTable.addIndex(idx);

    	try {
    		match.vertifyResultTableStruct();
    		fail("match has no source table, but no exception caught.");
    	} catch (Exception e) {
		}
    	
    	match.setSourceTable(sourceTable);
    	
    	try {
    		match.vertifyResultTableStruct();
    		fail("match has no unique index, but no exception caught.");
    	} catch (Exception e) {
		}
    	
    	match.setSourceTableIndex(idx);
    	
    	try {
    		match.vertifyResultTableStruct();
    		fail("result table name has not been setup, but no exception caught.");
    	} catch (Exception e) {
		}
    	
    	SQLSchema sch = db.getSchemaByName(ds.getPlSchema());
    	SQLTable resultTable = new SQLTable(sch,"my_result_table",null,"TABLE",true);
    	match.setResultTable(resultTable);
    	resultTable = match.createResultTable();

    	Connection con = db.getConnection();
    	Statement stmt = null;
		String sql = "drop table " + DDLUtils.toQualifiedName(resultTable);
		execSQL(con,sql);

		try {
    		match.vertifyResultTableStruct();
    		fail("result table is not persistent, but no exception caught.");
    	} catch (Exception e) {
		}
    	
    	DDLGenerator ddlg = null;
    	try {
    		ddlg = DDLUtils.createDDLGenerator(ds);
    	} catch (ClassNotFoundException e) {
    		fail("DDLUtils.createDDLGenerator(SPDataSource ds) threw an exception!");
    	}
    	assertNotNull("DDLGenerator error", ddlg);
		ddlg.setTargetSchema(ds.getPlSchema());
		
		if (Match.doesResultTableExist(session, match)) {
			ddlg.dropTable(match.getResultTable());
		}
		ddlg.addTable(match.createResultTable());
		ddlg.addIndex((SQLIndex) match.getResultTable().getIndicesFolder().getChild(0));
		
		
		int successCount = 0;
	    for (DDLStatement sqlStatement : ddlg.getDdlStatements()) {
	    	sql = sqlStatement.getSQLText();
	    	if ( execSQL(con,sql))	successCount += 1;
	    }

	    assertEquals("Not all statements executed", ddlg.getDdlStatements().size(), successCount);
    	assertTrue("we should have a good result table.",
    			match.vertifyResultTableStruct());
    	
    	sql = "drop table " + DDLUtils.toQualifiedName(resultTable);
    	execSQL(con,sql);

    	try {
    		match.vertifyResultTableStruct();
    		fail("result table is droped, but no exception caught.");
    	} catch (Exception e) {
		}
    }
	
	public void testVertifyResultTableORA() throws SQLException, InstantiationException,
											IllegalAccessException, ArchitectException {
		
		SPDataSource ds = DBTestUtil.getOracleDS();
		SQLDatabase db = new SQLDatabase(ds);
		session.setDatabase(db);
		session.setConnection(db.getConnection());
		match.setSession(session);
		
		
		
    	SQLTable sourceTable = new SQLTable(db.getSchemaByName(ds.getPlSchema()), "match_source", null, "TABLE", true);
    	
    	SQLColumn pk1 = new SQLColumn(sourceTable, "pk1", Types.VARCHAR, 20, 0);
    	pk1.setNullable(DatabaseMetaData.columnNoNulls);
    	sourceTable.addColumn(pk1);
    
    	// precision can not be other value since the sql server does not 
    	// support column width for int, and the column size from jdbc will
    	// be 10.
    	SQLColumn pk2 = new SQLColumn(sourceTable, "pk2", Types.DECIMAL, 10, 0);
    	pk2.setNullable(DatabaseMetaData.columnNoNulls);
    	sourceTable.addColumn(pk2);
    	
    	SQLColumn col = new SQLColumn(sourceTable, "normal_col_1", Types.VARCHAR, 20, 0);
    	col.setNullable(DatabaseMetaData.columnNullable);
    	sourceTable.addColumn(col);
    	
    	SQLIndex idx = new SQLIndex("source_pk", true, null, IndexType.HASHED, null);
    	idx.addChild(idx.new Column(pk1, true, false));
    	idx.addChild(idx.new Column(pk2, true, false));
    	sourceTable.addIndex(idx);

    	try {
    		match.vertifyResultTableStruct();
    		fail("match has no source table, but no exception caught.");
    	} catch (Exception e) {
		}
    	
    	match.setSourceTable(sourceTable);
    	
    	try {
    		match.vertifyResultTableStruct();
    		fail("match has no unique index, but no exception caught.");
    	} catch (Exception e) {
		}
    	
    	match.setSourceTableIndex(idx);
    	
    	try {
    		match.vertifyResultTableStruct();
    		fail("result table name has not been setup, but no exception caught.");
    	} catch (Exception e) {
		}
    	
    	SQLSchema sch = db.getSchemaByName(ds.getPlSchema());
    	SQLTable resultTable = new SQLTable(sch,"my_result_table",null,"TABLE",true);
    	match.setResultTable(resultTable);
    	resultTable = match.createResultTable();

    	Connection con = db.getConnection();
    	Statement stmt = null;
		String sql = "drop table " + DDLUtils.toQualifiedName(resultTable);
		execSQL(con,sql);

		try {
    		match.vertifyResultTableStruct();
    		fail("result table is not persistent, but no exception caught.");
    	} catch (Exception e) {
		}
    	
    	DDLGenerator ddlg = null;
    	try {
    		DDLUtils.createDDLGenerator(ds);
    	} catch (ClassNotFoundException e) {
    		fail("DDLUtils.createDDLGenerator(SPDataSource) threw a ClassNotFoundException");
    	}
    	assertNotNull("DDLGenerator error", ddlg);
		ddlg.setTargetSchema(ds.getPlSchema());
		
		if (Match.doesResultTableExist(session, match)) {
			ddlg.dropTable(match.getResultTable());
		}
		ddlg.addTable(match.createResultTable());
		ddlg.addIndex((SQLIndex) match.getResultTable().getIndicesFolder().getChild(0));
		
		
		int successCount = 0;
	    for (DDLStatement sqlStatement : ddlg.getDdlStatements()) {
	    	sql = sqlStatement.getSQLText();
	    	if ( execSQL(con,sql))	successCount += 1;
	    }

	    assertEquals("Not all statements executed", ddlg.getDdlStatements().size(), successCount);
    	assertTrue("we should have a good result table.",
    			match.vertifyResultTableStruct());
    	
    	sql = "drop table " + DDLUtils.toQualifiedName(resultTable);
    	execSQL(con,sql);

    	try {
    		match.vertifyResultTableStruct();
    		fail("result table is droped, but no exception caught.");
    	} catch (Exception e) {
		}
    }

	private boolean execSQL(Connection conn, String sql) {
		Statement stmt = null;
		try {
    		stmt = conn.createStatement();
   			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			System.out.println("SQL ERROR:["+sql+"]\n"+e.getMessage());
			return false;
		} finally {
			try {
				if (stmt != null) stmt.close();
			} catch (SQLException ex) {
			}
	    }
		return true;
	}
}
