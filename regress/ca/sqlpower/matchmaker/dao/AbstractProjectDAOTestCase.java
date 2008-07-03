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


package ca.sqlpower.matchmaker.dao;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;

import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.architect.SQLIndex.AscendDescend;
import ca.sqlpower.architect.SQLIndex.Column;
import ca.sqlpower.architect.ddl.DDLGenerator;
import ca.sqlpower.architect.ddl.DDLStatement;
import ca.sqlpower.architect.ddl.DDLUtils;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.Project.ProjectMode;
import ca.sqlpower.matchmaker.dao.hibernate.MatchMakerHibernateSession;
import ca.sqlpower.matchmaker.dao.hibernate.PlFolderDAOHibernate;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.sql.SPDataSource;

public abstract class AbstractProjectDAOTestCase extends AbstractDAOTestCase<Project,ProjectDAO>  {

	Long count=0L;

	@Override
	public Project createNewObjectUnderTest() throws Exception {
		count++;
		Project project = new Project();
		project.setSession(getSession());
		try {
			setAllSetters(project, getNonPersitingProperties());
			project.setName("Project "+count);
			
			PlFolder f = new PlFolder("test folder" + count);
			PlFolderDAOHibernate plFolderDAO = new PlFolderDAOHibernate((MatchMakerHibernateSession) getSession());
			plFolderDAO.save(f);
			
            project.setParent(f);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		return project;
	}

	@Override
	public List<String> getNonPersitingProperties() {
		List<String> nonPersistingProperties = super.getNonPersitingProperties();
		nonPersistingProperties.add("lastUpdateDate");
		nonPersistingProperties.add("session");
        nonPersistingProperties.add("mungeProcesses");
        nonPersistingProperties.add("tableMergeRules");
        nonPersistingProperties.add("cleansingEngine");
        nonPersistingProperties.add("matchingEngine");
        nonPersistingProperties.add("mergingEngine");
        
        // tested explicitly elsewhere
        nonPersistingProperties.add("sourceTable");
        nonPersistingProperties.add("sourceTableCatalog");
        nonPersistingProperties.add("sourceTableSchema");
        nonPersistingProperties.add("sourceTableName");
        nonPersistingProperties.add("sourceTableIndex");
        nonPersistingProperties.add("sourceTableSPDatasource");
        
        nonPersistingProperties.add("resultTableCatalog");
        nonPersistingProperties.add("resultTableSchema");
        nonPersistingProperties.add("resultTableName");
        nonPersistingProperties.add("resultTableSPDatasource");
        
        nonPersistingProperties.add("xrefTableCatalog");
        nonPersistingProperties.add("xrefTableSchema");
        nonPersistingProperties.add("xrefTableName");
        nonPersistingProperties.add("xrefTableSPDatasource");
      
		return nonPersistingProperties;
	}
	
	public void testProjectRequiresFolder() throws Exception {
		Project m = new Project();
        m.setName("project no folder");
		
		try {
			getDataAccessObject().save(m);
			fail("The save did not throw an exception when we saved a project with no folder.");
		} catch (RuntimeException e) {
			// Expecting the method to throw a runtime exception because the folder is null
		}
	}

	public void testIndexSave() throws Exception {
		Project m = createNewObjectUnderTest();
		
		// have to hook up a parent table so the UserType can search it for columns
		SQLTable table = new SQLTable(null, "test_parent", null, "TABLE", true);
		table.addColumn(new SQLColumn(table, "test1", 4, 10, 0));
		table.addColumn(new SQLColumn(table, "test2", 4, 10, 0));
		table.addColumn(new SQLColumn(table, "test3", 4, 10, 0));
		
		SQLIndex idx = new SQLIndex("test_index", true, null, null, null);
		idx.addChild(idx.new Column("test1", AscendDescend.UNSPECIFIED));
		idx.addChild(idx.new Column("test2", AscendDescend.UNSPECIFIED));
		idx.addChild(idx.new Column("test3", AscendDescend.UNSPECIFIED));
		m.setSourceTableIndex(idx);
		
		table.addIndex(idx);
		getDataAccessObject().save(m);
		resetSession();
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            con = getSession().getConnection();
            stmt = con.createStatement();
            rs = stmt.executeQuery(
                    "SELECT * FROM mm_project WHERE project_name='"+m.getName()+"'");
            
            if (!rs.next()) {
            	fail("No results found for project "+m.getName());
            }
            
            assertEquals("test1", rs.getString("index_column_name0"));
            assertEquals("test2", rs.getString("index_column_name1"));
            assertEquals("test3", rs.getString("index_column_name2"));
            assertEquals(null, rs.getString("index_column_name3"));
            assertEquals(null, rs.getString("index_column_name4"));
            assertEquals(null, rs.getString("index_column_name5"));
            assertEquals(null, rs.getString("index_column_name6"));
            assertEquals(null, rs.getString("index_column_name7"));
            assertEquals(null, rs.getString("index_column_name8"));
            assertEquals(null, rs.getString("index_column_name9"));
            
        } finally {
            try { rs.close(); } catch (Exception e) { System.err.println("Couldn't close result set"); e.printStackTrace(); }
            try { stmt.close(); } catch (Exception e) { System.err.println("Couldn't close statement"); e.printStackTrace(); }
            // connection didn't come from a pool so we can't close it
        }
        
        // this is not a good idea for a unit test, but Johnson insists
        Project loadedProject = getDataAccessObject().findByName(m.getName());
        assertNotSame("Woops, got the same project back from cache", m, loadedProject);
        assertNotSame("Woops, got the same index back from cache", m.getSourceTableIndex(), loadedProject.getSourceTableIndex());
        assertNotSame("Woops, got the same indexColumn back from cache", m.getSourceTableIndex().getChild(0), loadedProject.getSourceTableIndex().getChild(0));

        // since the table on the original project was fake, the source table
        // we get back from the DAO will have no columns.  We'll just put it back
        // before testing the index column resolution.
        loadedProject.setSourceTable(table);
        
        assertEquals(3, loadedProject.getSourceTableIndex().getChildCount());
        assertEquals("test1", loadedProject.getSourceTableIndex().getChild(0).getName());
        assertNotNull(loadedProject.getSourceTableIndex().getChild(0).getColumn());
        assertEquals("test2", loadedProject.getSourceTableIndex().getChild(1).getName());
        assertNotNull(loadedProject.getSourceTableIndex().getChild(1).getColumn());
        assertEquals("test3", loadedProject.getSourceTableIndex().getChild(2).getName());
        assertNotNull(loadedProject.getSourceTableIndex().getChild(2).getColumn());
	}
    
	
    /**
     * Inserts data directly into the tables, then uses the DAO to retrieve the
     * objects those rows represent, and checks that the objects are the ones we
     * inserted.  Also implicitly checks that the descdendants are fetched eagerly
     * (and don't depend on the hibernate session staying open).
     */
    public void testIfChildrenLoadWorks() throws Exception {
        final long time = System.currentTimeMillis();
        final String projectName = "project_"+time;
        
        PlFolder f = new PlFolder();
		f.setName("test folder");
		PlFolderDAOHibernate plFolderDAO = new PlFolderDAOHibernate((MatchMakerHibernateSession) getSession());
		plFolderDAO.save(f);
        
        final long projectOid = insertSampleProjectData(projectName, f.getOid());
        final long processOid = insertSampleMungeProcessData(projectOid, "group_"+time);
        insertSampleMungeStepData(processOid, "test_rule_"+time);
        
        Project project = getDataAccessObject().findByName(projectName);
            List<MungeProcess> mungeProcesses = project.getMungeProcesses();
		assertEquals("There should be one munge process", 1, mungeProcesses.size());

		MungeProcess mungeProcess = mungeProcesses.get(0);
		assertEquals("Wrong munge process name", "group_" + time, mungeProcess.getName());

        // TODO check that the munge rules were retrieved
    }
    
    
    /**
     * This tests a previous bug where the columns of a newly created result table
     * of a project would be cleared out after saving the project. 
     */
    public void testResultTableColumnsExistAfterSave() throws Exception {
    	SQLDatabase db = getSession().getDatabase();
    	SQLTable sourceTable = new SQLTable(db,
    			"my_COOL_source_table", "", "TABLE", true);
    	SQLIndex sourceTableIndex = new SQLIndex("my_COOL_source_table_index",
    			true, null, null, null);
    	SQLColumn sqlCol = new SQLColumn(sourceTable, "testSQLCol",
    			Types.INTEGER, 10, 10);
    	sourceTable.addColumn(sqlCol);
    	Column col = sourceTableIndex.new Column(sqlCol, AscendDescend.UNSPECIFIED);
    	sourceTableIndex.addChild(col);
    	
    	Project project = createNewObjectUnderTest();
    	project.setSourceTable(sourceTable);
    	project.setSourceTableIndex(sourceTableIndex);
    	
    	project.setResultTableName("my_COOL_result_table");
    	project.setResultTableSPDatasource(getDS().getName());
    	SQLTable resultTable = project.createResultTable();
    	
    	// Sets up the sql statements needed for the result table
    	final DDLGenerator ddlg = DDLUtils.createDDLGenerator(
    			getDS());
    	ddlg.setTargetCatalog(project.getResultTableCatalog());
    	ddlg.setTargetSchema(project.getResultTableSchema());
    	ddlg.dropTable(project.getResultTable());
    	ddlg.addTable(resultTable);

    	// Rus the sql statments to build the result table on the database
    	Connection con = null;
    	Statement stmt = null;
    	try {
    		con = getSession().getConnection();
    		stmt = con.createStatement();
    		for (DDLStatement sqlStatement : ddlg.getDdlStatements()) {
    			try {
    				stmt.executeUpdate(sqlStatement.getSQLText());
    			} catch(SQLException e) {
    				System.out.println("Ignoring exception (assuming table to drop did not exist).");
    				e.printStackTrace(System.out);
    			}
    		}
    	} finally {
    		try { 
    			stmt.close(); 
    		} catch (Exception e) {
    			System.err.println("Couldn't close statement"); 
    			e.printStackTrace(); 
    		}
    	}
    	
    	
    	resultTable = getSession().findPhysicalTableByName(project.getResultTableSPDatasource(), project.getResultTableCatalog(),
				project.getResultTableSchema(), project.getResultTableName());
    	
    	assertEquals("Wrong number of columns for result table before save",
    			15, project.getResultTable().getColumns().size());
    	
        getDataAccessObject().save(project);
        
    	assertEquals("Wrong number of columns for result table after save",
    			15, project.getResultTable().getColumns().size());
    }
    
    public void testMungeProcessMove() throws Exception {
        MungeProcess process = new MungeProcess();
        process.setName("munge process");
        
        Project oldProject = new Project();
        oldProject.setName("old");
        oldProject.setType(ProjectMode.FIND_DUPES);
        oldProject.setResultTableName("old_result_table");
		oldProject.setSession(getSession());

        PlFolder f = new PlFolder();
		oldProject.setParent(f);
        
		f.setName("test folder");
		PlFolderDAOHibernate plFolderDAO = new PlFolderDAOHibernate((MatchMakerHibernateSession) getSession());
		plFolderDAO.save(f);
        
        Project newProject = new Project();
        newProject.setName("new");
		newProject.setParent(f);
        newProject.setType(ProjectMode.FIND_DUPES);
        newProject.setResultTableName("new_result_table");
		newProject.setSession(getSession());
        
        oldProject.addMungeProcess(process);

        ProjectDAO dao = getDataAccessObject();
        
        dao.save(oldProject);
        dao.save(newProject);
        
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            con = getSession().getConnection();
            stmt = con.createStatement();
            try { 
                rs = stmt.executeQuery(
                    "SELECT project_name FROM mm_project,mm_munge_process" +
                    " WHERE mm_project.project_oid=mm_munge_process.project_oid" +
                    " AND mm_munge_process.process_name='"+process.getName()+"'");
            
                if (!rs.next()) {
                    fail("No results found for project "+process.getName());
                }
            
                assertEquals("The setup failed to work","old", rs.getString("project_name"));
            } finally {
                try { rs.close(); } catch (Exception e) { System.err.println("Couldn't close result set"); e.printStackTrace(); }
            }
            
            oldProject.removeMungeProcess(process);
            dao.save(oldProject);
            
            //A temporary fix for moving munge processes. This makes a copy of the mungeProcess and 
            //adds it to the newProject.
            MungeProcess mungeProcess2 = process.duplicate(newProject, getSession());
            newProject.addMungeProcess(mungeProcess2);
            dao.save(newProject);
            
            try { 
                rs = stmt.executeQuery(
                		"SELECT project_name FROM mm_project,mm_munge_process" +
                        " WHERE mm_project.project_oid=mm_munge_process.project_oid" +
                        " AND mm_munge_process.process_name='"+process.getName()+"'");
            
                if (!rs.next()) {
                    fail("No results found for project "+process.getName());
                }
            
                assertEquals("move failed to work","new", rs.getString("project_name"));
            } finally {
                try { rs.close(); } catch (Exception e) { System.err.println("Couldn't close result set"); e.printStackTrace(); }
            }
        } finally {
            try { stmt.close(); } catch (Exception e) { System.err.println("Couldn't close statement"); e.printStackTrace(); }
            // connection didn't come from a pool so we can't close it
        }
     
    }
    
    /**
     * Test spDataSources persist
     */
    public void testSPDataSourcesPersist() throws Exception{
   	
    	 MungeProcess process = new MungeProcess();
         process.setName("munge process");
         
         Project oldProject = new Project();
         oldProject.setName("old");
         oldProject.setType(ProjectMode.FIND_DUPES);
        
         oldProject.setSession(getSession());
         
         String dsName = getAlternateDS().getName();
         oldProject.setResultTableSPDatasource(dsName);
         oldProject.setSourceTableSPDatasource(dsName);
         oldProject.setXrefTableSPDatasource(dsName);
         
         PlFolder f = new PlFolder();
         oldProject.setParent(f);

         f.setName("test folder");
         PlFolderDAOHibernate plFolderDAO = new PlFolderDAOHibernate((MatchMakerHibernateSession) getSession());
         plFolderDAO.save(f);

         oldProject.addMungeProcess(process);

         ProjectDAO dao = getDataAccessObject();

         dao.save(oldProject);
         
         Project loadedProject = getDataAccessObject().findByName("old");
         assertEquals(loadedProject.getSourceTableSPDatasource(), oldProject.getSourceTableSPDatasource());
         assertEquals(loadedProject.getResultTableSPDatasource(), oldProject.getResultTableSPDatasource());
         assertEquals(loadedProject.getXrefTableSPDatasource(), oldProject.getXrefTableSPDatasource());
    	
    }
    
    /**
     * Inserts a sample entry in mm_project, and returns its OID.  The match will
     * have its name (MATCH_ID) set to the given string value.  This is useful for
     * verifying that you are retrieving the same record that this method inserted.
     * <p>
     * This method is abstract because the OID columns have to be handled
     * differently on different database platforms.
     * 
     * @return The MATCH_OID value of the new project that was inserted.
     */
    protected abstract long insertSampleProjectData(
            String projectName, Long folderOid) throws Exception;
    
    /**
     * Inserts a sample entry in mm_munge_process, and returns its OID.  The munge process will
     * its name (GROUP_ID) set to the given string value.  This is useful for
     * verifying that you are retrieving the same record that this method inserted.
     * <p>
     * This method is abstract because the OID columns have to be handled
     * differently on different database platforms.
     * 
     * @return The GROUP_OID value of the new munge process that was inserted.
     */
    protected abstract long insertSampleMungeProcessData(
            long parentProjectOid, String mungeProcessName) throws Exception;

    /**
     * Inserts a sample entry in mm_munge_step, and returns its OID.  The rule will
     * have its LAST_UPDATE_USER set to the given string value (it doesn't use the name
     * because rule sets don't have names).  This is useful for
     * verifying that you are retrieving the same record that this method inserted.
     * <p>
     * This method is abstract because the OID columns have to be handled
     * differently on different database platforms.
     * 
     * @return The GROUP_OID value of the new munge process that was inserted.
     */
    protected abstract long insertSampleMungeStepData(
            long parentGroupOid, String lastUpdateUser) throws Exception;
    
	protected abstract SPDataSource getDS();
	protected abstract SPDataSource getAlternateDS();
}
