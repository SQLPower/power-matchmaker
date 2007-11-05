/*
 * Copyright (c) 2007, SQL Power Group Inc.
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
import java.sql.Statement;
import java.util.List;

import ca.sqlpower.matchmaker.ColumnMergeRules;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.TableMergeRules;
import ca.sqlpower.matchmaker.Project.ProjectMode;
import ca.sqlpower.matchmaker.dao.hibernate.MatchMakerHibernateSession;
import ca.sqlpower.matchmaker.dao.hibernate.PlFolderDAOHibernate;
import ca.sqlpower.matchmaker.dao.hibernate.ProjectDAOHibernate;

public abstract class AbstractTableMergeRulesDAOTestCase extends AbstractDAOTestCase<TableMergeRules,TableMergeRuleDAO>  {

	Long count=0L;
    Project project;
    PlFolder folder;
    public AbstractTableMergeRulesDAOTestCase() throws Exception {
        project= new Project();
        project.setName("Merge Rules Test Project");
        project.setType(ProjectMode.BUILD_XREF);
        folder = new PlFolder("test folder");
        project.setParent(folder);
        try {
            project.setSession(getSession());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public abstract MatchMakerHibernateSession getSession() throws Exception;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        PlFolderDAOHibernate plFolderDAO = new PlFolderDAOHibernate((MatchMakerHibernateSession) getSession());
		plFolderDAO.save(folder);
        ProjectDAO projectDAO = new ProjectDAOHibernate(getSession());
        projectDAO.save(project);
    }

	@Override
	public TableMergeRules createNewObjectUnderTest() throws Exception {
		count++;
		TableMergeRules mergeRules = new TableMergeRules();
		mergeRules.setSession(getSession());
		try {
			setAllSetters(mergeRules, getNonPersitingProperties());
			mergeRules.setName("MergeRule "+count);
			mergeRules.setTableName("table"+count);
            project.addTableMergeRule(mergeRules);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		return mergeRules;
	}
	
	public ColumnMergeRules createColumnMergeRules(TableMergeRules tmr) throws Exception {
		count++;
		ColumnMergeRules cmr = new ColumnMergeRules();
		cmr.setSession(getSession());
		cmr.setName("ColumnMergeRule " + count);
		cmr.setColumnName("Column " + count);
		tmr.addChild(cmr);
		return cmr;
		
	}

	@Override
	public List<String> getNonPersitingProperties() {
		List<String> nonPersistingProperties = super.getNonPersitingProperties();
		nonPersistingProperties.add("lastUpdateOSUser");
		nonPersistingProperties.add("lastUpdateDate");
		nonPersistingProperties.add("lastUpdateAppUser");
		nonPersistingProperties.add("session");
        nonPersistingProperties.add("parent");
        nonPersistingProperties.add("parentProject");
        nonPersistingProperties.add("oid");
        nonPersistingProperties.add("name");
		return nonPersistingProperties;
	}
	
	public void testTableParentIsTheCorrectObject() throws Exception {
		createNewObjectUnderTest();
		TableMergeRules mergeRules = project.getTableMergeRules().get(0);
		assertEquals("The merge rule "+mergeRules.toString()+ "'s  parent is not the folder it should be in",project.getTableMergeRulesFolder(),mergeRules.getParent());
	}

	public void testTableParentIsTheCorrectObjectAfterDAOSave() throws Exception {
		createNewObjectUnderTest();
		new ProjectDAOHibernate(getSession()).save(project);
		TableMergeRules mergeRules = project.getTableMergeRules().get(0);
		assertEquals("The merge rule "+mergeRules.toString()+ "'s  parent is not the folder it should be in",project.getTableMergeRulesFolder(),mergeRules.getParent());
	}
	
    public void testDelete() throws Exception {
        Connection con = getSession().getConnection();
        Statement stmt = null;
        createNewObjectUnderTest();
        try {

            TableMergeRules mergeRules = project.getTableMergeRules().get(0);
            String tableName = mergeRules.getTableName();
            TableMergeRuleDAO dao = getDataAccessObject();
            dao.save(mergeRules);
            
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM MM_TABLE_MERGE_RULE WHERE table_name = '"+tableName+"'");
            assertTrue("Table merge rule didn't save?!", rs.next());
            rs.close();
            
            dao.delete(mergeRules);

            rs = stmt.executeQuery("SELECT * FROM MM_TABLE_MERGE_RULE WHERE table_name = '"+tableName+"'");
            assertFalse("Table merge rule didn't delete", rs.next());
            rs.close();
        } finally {
        	if (stmt != null) {
        		stmt.close();
        	}
        }
    }
}
