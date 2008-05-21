/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*MatchMaker.
 *
 * Power*MatchMaker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*MatchMaker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */


package ca.sqlpower.matchmaker;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

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
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.sql.PlDotIni;
import ca.sqlpower.sql.SPDataSource;
import ca.sqlpower.testutil.MockJDBCDriver;

public class ProjectTest extends MatchMakerTestCase<Project> {

    Project project;
	private TestingMatchMakerSession session;

    protected void setUp() throws Exception {
    	// The following two are ignored because they are to be used only by hibernate
    	// so they don't throw events
        propertiesToIgnoreForEventGeneration.add("mungeProcesses");
        propertiesToIgnoreForEventGeneration.add("tableMergeRules");
        // Ignored because they are delegates to support functions for the chached table class
        propertiesToIgnoreForEventGeneration.add("sourceTableCatalog");
        propertiesToIgnoreForEventGeneration.add("sourceTableSchema");
        propertiesToIgnoreForEventGeneration.add("sourceTableName");
        propertiesToIgnoreForEventGeneration.add("sourceTableSPDatasource");
        propertiesToIgnoreForEventGeneration.add("resultTableCatalog");
        propertiesToIgnoreForEventGeneration.add("resultTableSchema");
        propertiesToIgnoreForEventGeneration.add("resultTableName");
        propertiesToIgnoreForEventGeneration.add("resultTableSPDatasource");
        propertiesToIgnoreForEventGeneration.add("xrefTableCatalog");
        propertiesToIgnoreForEventGeneration.add("xrefTableSchema");
        propertiesToIgnoreForEventGeneration.add("xrefTableName");
        propertiesToIgnoreForEventGeneration.add("xrefTableSPDatasource");
        propertiesToIgnoreForEventGeneration.add("spDatasource");
        propertiesToIgnoreForDuplication.add("mungeProcesses");
        
        propertiesToIgnoreForDuplication.add("resultTableSPDatasource");
        propertiesToIgnoreForDuplication.add("sourceTableSPDatasource");
        propertiesToIgnoreForDuplication.add("xrefTableSPDatasource");

        // These set other properties to null that describe the same object
        propertiesToIgnoreForDuplication.add("resultTable");
        propertiesToIgnoreForDuplication.add("sourceTable");
        propertiesToIgnoreForDuplication.add("xrefTable");
        
        
        //These don't really differ on set and get but there are checks in
        //place that ensure that the dataSource exists and this on will not. 
        propertiesThatDifferOnSetAndGet.add("xrefTableSPDatasource");
        propertiesThatDifferOnSetAndGet.add("sourceTableSPDatasource");
        propertiesThatDifferOnSetAndGet.add("resultTableSPDatasource");
        
        
        
        propertiesToIgnoreForDuplication.add("sourceTableIndex");
        
        propertiesThatHaveSideEffects.add("xrefTable");
        propertiesThatHaveSideEffects.add("sourceTable");
        propertiesThatHaveSideEffects.add("resultTable");
        super.setUp();
        project = new Project();
        session = new TestingMatchMakerSession();
		session.setDatabase(new SQLDatabase());
        project.setSession(session);
    }
    @Override
    protected Project getTarget() {
        return project;
    }


	public void testEqual() {
		Project m1 = new Project();
		Project m2 = new Project();
		assertTrue("Project1 <> project2", (m1 != m2) );
		assertTrue("Project1 equals project2", m1.equals(m2) );
		m1.setName("project1");
		m2.setName("project2");
		assertFalse("Project1 should not equals project2", m1.equals(m2) );
		m1.setName("project");
		m2.setName("project");
		assertTrue("Project1 should equals project2", m1.equals(m2) );
	}

    public void testMatchMakerFolderFiresEventForMungeProcesses(){
        MatchMakerEventCounter l = new MatchMakerEventCounter();
        project.getMungeProcessesFolder().addMatchMakerListener(l);
        List<MungeProcess> mmoList = new ArrayList<MungeProcess>();
        project.setMungeProcesses(mmoList);
        assertEquals("Wrong number of events fired",1,l.getAllEventCounts());
        assertEquals("Wrong type of event fired",1,l.getStructureChangedCount());
    }
    
    public void testCreateResultTable() throws ArchitectException {
    	SQLTable sourceTable = new SQLTable(project.getSession().getDatabase(), "match_source", null, "TABLE", true);
    	
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

    	project.setSourceTable(sourceTable);
    	project.setSourceTableIndex(idx);
    	
    	project.setResultTableName("my_result_table_that_almost_didnt_have_cow_in_its_name");
    	SQLTable resultTable = project.createResultTable();
    	
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
    	SQLTable sourceTable = new SQLTable(project.getSession().getDatabase(), "match_source", null, "TABLE", true);
    	
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

    	project.setSourceTable(sourceTable);
    	project.setSourceTableIndex(idx);
    	
    	project.setResultTableName("my_result_table_that_almost_didnt_have_cow_in_its_name");
    	SQLTable resultTable = project.createResultTable();
    	
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
    	project.setSourceTable(tab);
    	project.setSourceTableIndex(idx);
    	
    	project.setResultTableCatalog("my_cat");
    	project.setResultTableSchema("my_dog");
    	project.setResultTableName("my_chinchilla");
    	
    	SQLTable resultTable = project.createResultTable();
    	
    	assertEquals("my_cat", resultTable.getCatalogName());
    	assertEquals("my_dog", resultTable.getSchemaName());
    	assertEquals("my_chinchilla", resultTable.getName());
    }
    
    public void testGetSourceTableIndex() throws Exception {
    	SQLIndex foo = new SQLIndex();
    	project.setSourceTableIndex(foo);
    	assertNotNull(project.getSourceTableIndex());
    	assertSame(foo, project.getSourceTableIndex());
    }
    
	public void testResultTableExistsWhenTrue() throws Exception {
		SPDataSource ds = new SPDataSource(new PlDotIni());
		ds.getParentType().setJdbcDriver(MockJDBCDriver.class.getName());
		ds
				.setUrl("jdbc:mock:dbmd.catalogTerm=Catalog&dbmd.schemaTerm=Schema&catalogs=farm&schemas.farm=cow&tables.farm.cow=moo");
		ds.setUser("n/a");
		ds.setPass("n/a");
		final SQLDatabase db = new SQLDatabase(ds);
		session.setDatabase(db);
		SQLTable resultTable = db.getTableByName("farm", "cow", "moo");
		assertNotNull(resultTable);
		project.setResultTable(resultTable);
		assertTrue(Project.doesResultTableExist(session, project));
	}
	

	/**
	 * Tests that new nonexistant handcrafted tables are nonexistant according
	 * to the Project object.
	 */
	public void testResultTableExistsWhenFalse() throws Exception {
		SPDataSource ds = new SPDataSource(new PlDotIni());
		ds.getParentType().setJdbcDriver(MockJDBCDriver.class.getName());
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
		project.setResultTable(resultTable);
		assertFalse(Project.doesResultTableExist(session, project));
	}

	/**
	 * Tests that new nonexistant simulated tables that are really in the
	 * session's in-memory view of the database are nonexistant according to the
	 * Project object.
	 */
	public void testResultTableExistsWhenInMemoryButStillFalse()
			throws Exception {
		SPDataSource ds = new SPDataSource(new PlDotIni());
		ds.getParentType().setJdbcDriver(MockJDBCDriver.class.getName());
		ds
				.setUrl("jdbc:mock:dbmd.catalogTerm=Catalog&dbmd.schemaTerm=Schema&catalogs=farm&schemas.farm=cow&tables.farm.cow=moo");
		ds.setUser("n/a");
		ds.setPass("n/a");
		final SQLDatabase db = new SQLDatabase(ds);
		session.setDatabase(db);
		SQLTable resultTable = ArchitectUtils.addSimulatedTable(db, "cat",
				"sch", "faketab");
		project.setResultTable(resultTable);
		assertFalse(Project.doesResultTableExist(session, project));
	}
	
	public void testSourceTableExistsWhenTrue() throws Exception {
		SPDataSource ds = new SPDataSource(new PlDotIni());
		ds.getParentType().setJdbcDriver(MockJDBCDriver.class.getName());
		ds
				.setUrl("jdbc:mock:dbmd.catalogTerm=Catalog&dbmd.schemaTerm=Schema&catalogs=farm&schemas.farm=cow&tables.farm.cow=moo");
		ds.setUser("n/a");
		ds.setPass("n/a");
		final SQLDatabase db = new SQLDatabase(ds);
		session.setDatabase(db);
		SQLTable sourceTable = db.getTableByName("farm", "cow", "moo");
		assertNotNull(sourceTable);
		project.setSourceTable(sourceTable);
		assertTrue(Project.doesSourceTableExist(session, project));
	}

	/**
	 * Tests that new nonexistant handcrafted tables are nonexistant according
	 * to the Project object.
	 */
	public void testSourceTableExistsWhenFalse() throws Exception {
		SPDataSource ds = new SPDataSource(new PlDotIni());
		ds.getParentType().setJdbcDriver(MockJDBCDriver.class.getName());
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
		project.setSourceTable(sourceTable);
		assertFalse(Project.doesSourceTableExist(session, project));
	}

	/**
	 * Tests that new nonexistant simulated tables that are really in the
	 * session's in-memory view of the database are nonexistant according to the
	 * Project object.
	 */
	public void testSourceTableExistsWhenInMemoryButStillFalse()
			throws Exception {
		SPDataSource ds = new SPDataSource(new PlDotIni());
		ds.getParentType().setJdbcDriver(MockJDBCDriver.class.getName());
		ds.setUrl("jdbc:mock:dbmd.catalogTerm=Catalog&dbmd.schemaTerm=Sc" +
				"hema&catalogs=farm&schemas.farm=cow&tables.farm.cow=moo");
		ds.setUser("n/a");
		ds.setPass("n/a");
		final SQLDatabase db = new SQLDatabase(ds);
		session.setDatabase(db);
		SQLTable sourceTable = ArchitectUtils.addSimulatedTable(db, "cat",
				"sch", "faketab");
		project.setSourceTable(sourceTable);
		assertFalse(Project.doesSourceTableExist(session, project));
	}
	
	public void testVerifyResultTableSS() throws SQLException, InstantiationException,
											IllegalAccessException, ArchitectException {
		
		SPDataSource ds = DBTestUtil.getSqlServerDS();
		SQLDatabase db = new SQLDatabase(ds);
		session.setDatabase(db);
		session.setConnection(db.getConnection());
		project.setSession(session);
		
		
		
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
    		project.verifyResultTableStruct();
    		fail("project has no source table, but no exception caught.");
    	} catch (Exception e) {
		}
    	
    	project.setSourceTable(sourceTable);
    	
    	try {
    		project.verifyResultTableStruct();
    		fail("project has no unique index, but no exception caught.");
    	} catch (Exception e) {
		}
    	
    	project.setSourceTableIndex(idx);
    	
    	try {
    		project.verifyResultTableStruct();
    		fail("result table name has not been setup, but no exception caught.");
    	} catch (Exception e) {
		}
    	
    	SQLSchema sch = db.getSchemaByName(ds.getPlSchema());
    	SQLTable resultTable = new SQLTable(sch,"my_result_table",null,"TABLE",true);
    	project.setResultTable(resultTable);
    	resultTable = project.createResultTable();

    	Connection con = db.getConnection();
    	Statement stmt = null;
		String sql = "drop table " + DDLUtils.toQualifiedName(resultTable);
		execSQL(con,sql);

		try {
    		project.verifyResultTableStruct();
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
		
//		List<SPDataSource> dss = new ArrayList<SPDataSource>();
//		dss.add(ds);
//		((TestingMatchMakerContext)session.getContext()).setDataSources(dss);
		
		if (Project.doesResultTableExist(session, project)) {
			ddlg.dropTable(project.getResultTable());
		}
		ddlg.addTable(project.createResultTable());
		ddlg.addIndex((SQLIndex) project.getResultTable().getIndicesFolder().getChild(0));
		
		
		int successCount = 0;
	    for (DDLStatement sqlStatement : ddlg.getDdlStatements()) {
	    	sql = sqlStatement.getSQLText();
	    	if ( execSQL(con,sql))	successCount += 1;
	    }

	    assertEquals("Not all statements executed", ddlg.getDdlStatements().size(), successCount);
    	assertTrue("we should have a good result table.",
    			project.verifyResultTableStruct());
    	
    	sql = "drop table " + DDLUtils.toQualifiedName(resultTable);
    	execSQL(con,sql);

    	try {
    		project.verifyResultTableStruct();
    		fail("result table is droped, but no exception caught.");
    	} catch (Exception e) {
		}
    }
	
	public void testVerifyResultTableORA() throws SQLException, InstantiationException,
											IllegalAccessException, ArchitectException {
		
		SPDataSource ds = DBTestUtil.getOracleDS();
		SQLDatabase db = new SQLDatabase(ds);
		session.setDatabase(db);
		session.setConnection(db.getConnection());
		project.setSession(session);
		
		
		
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
    		project.verifyResultTableStruct();
    		fail("project has no source table, but no exception caught.");
    	} catch (Exception e) {
		}
    	
    	project.setSourceTable(sourceTable);
    	
    	try {
    		project.verifyResultTableStruct();
    		fail("project has no unique index, but no exception caught.");
    	} catch (Exception e) {
		}
    	
    	project.setSourceTableIndex(idx);
    	
    	try {
    		project.verifyResultTableStruct();
    		fail("result table name has not been setup, but no exception caught.");
    	} catch (Exception e) {
		}
    	
    	SQLSchema sch = db.getSchemaByName(ds.getPlSchema());
    	SQLTable resultTable = new SQLTable(sch,"my_result_table",null,"TABLE",true);
    	project.setResultTable(resultTable);
    	resultTable = project.createResultTable();

    	Connection con = db.getConnection();
    	Statement stmt = null;
		String sql = "drop table " + DDLUtils.toQualifiedName(resultTable);
		execSQL(con,sql);

		try {
    		project.verifyResultTableStruct();
    		fail("result table is not persistent, but no exception caught.");
    	} catch (Exception e) {
		}
    	
    	DDLGenerator ddlg = null;
    	try {
    		ddlg = DDLUtils.createDDLGenerator(ds);
    	} catch (ClassNotFoundException e) {
    		fail("DDLUtils.createDDLGenerator(SPDataSource) threw a ClassNotFoundException");
    	}
    	assertNotNull("DDLGenerator error", ddlg);
		ddlg.setTargetSchema(ds.getPlSchema());
		
		if (Project.doesResultTableExist(session, project)) {
			ddlg.dropTable(project.getResultTable());
		}
		ddlg.addTable(project.createResultTable());
		ddlg.addIndex((SQLIndex) project.getResultTable().getIndicesFolder().getChild(0));
		
		
		int successCount = 0;
	    for (DDLStatement sqlStatement : ddlg.getDdlStatements()) {
	    	sql = sqlStatement.getSQLText();
	    	if ( execSQL(con,sql))	successCount += 1;
	    }

	    assertEquals("Not all statements executed", ddlg.getDdlStatements().size(), successCount);
    	assertTrue("we should have a good result table.",
    			project.verifyResultTableStruct());
    	
    	sql = "drop table " + DDLUtils.toQualifiedName(resultTable);
    	execSQL(con,sql);

    	try {
    		project.verifyResultTableStruct();
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
    
    public void testNoEmptySimulatedTable() throws Exception {
        
        SQLTable sourceTable = new SQLTable(session.getDatabase(), true);
        session.getDatabase().addChild(sourceTable);
        sourceTable.addColumn(new SQLColumn(sourceTable, "pk1", Types.INTEGER, 10, 0));
        sourceTable.getColumn(0).setPrimaryKeySeq(0);
        
        project.setSourceTable(sourceTable);
        project.setSourceTableIndex(sourceTable.getPrimaryKeyIndex());
        
        project.setResultTableCatalog(null);
        project.setResultTableSchema(null);
        project.setResultTableName("new_table_that_doesnt_exist");
        
        project.createResultTable();
        
        SQLTable newTable = project.getResultTable();
        assertTrue(newTable.getColumns().size() > 0);
        
        SQLTable newTableInDatabase = (SQLTable) session.getDatabase().getChildByName("new_table_that_doesnt_exist");
        assertNotNull(newTableInDatabase);
        assertTrue(newTableInDatabase.getColumns().size() > 0);
        assertSame(newTable, newTableInDatabase);
    }
}
