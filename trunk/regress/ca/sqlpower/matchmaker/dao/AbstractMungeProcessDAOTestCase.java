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

import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.Project.ProjectMode;
import ca.sqlpower.matchmaker.dao.hibernate.MatchMakerHibernateSession;
import ca.sqlpower.matchmaker.dao.hibernate.PlFolderDAOHibernate;
import ca.sqlpower.matchmaker.dao.hibernate.ProjectDAOHibernate;
import ca.sqlpower.matchmaker.munge.MungeProcess;

public abstract class AbstractMungeProcessDAOTestCase extends AbstractDAOTestCase<MungeProcess,MungeProcessDAO>  {

	Long count=0L;
    Project project;
    PlFolder folder;
    
    public abstract MatchMakerHibernateSession getSession() throws Exception;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        PlFolderDAOHibernate plFolderDAO = new PlFolderDAOHibernate((MatchMakerHibernateSession) getSession());
        folder = new PlFolder("main test folder");
        plFolderDAO.save(folder);
        
        project= new Project();
        project.setName("Munge Process Test Project");
        project.setType(ProjectMode.BUILD_XREF);
        project.setParent(folder);
        try {
        	project.setSession(getSession());
        } catch (Exception e) {
        	throw new RuntimeException(e);
        }
        
        assertNotNull(project.getSession());
        ProjectDAO matchDAO = new ProjectDAOHibernate(getSession());
        matchDAO.save(project);
    }

	@Override
	public MungeProcess createNewObjectUnderTest() throws Exception {
		count++;
		MungeProcess mungeProcess = new MungeProcess();
        mungeProcess.setSession(getSession());
		try {
			setAllSetters(mungeProcess, getNonPersitingProperties());
            mungeProcess.setName("Munge Process "+count);
            project.addMungeProcess(mungeProcess);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		return mungeProcess;
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

		return nonPersistingProperties;
	}

    public void testDelete() throws Exception {
        Connection con = getSession().getConnection();
        ProjectDAO projectDAO = new ProjectDAOHibernate(getSession());
        Statement stmt = null;
        try {

            MungeProcess process = project.getMungeProcesses().get(0);
            String processId = process.getName();

            MungeProcessDAO dao = getDataAccessObject();
            dao.save(process);
            
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM pl_match_group WHERE group_id = '"+processId+"'");
            assertTrue("munge process didn't save?!", rs.next());
            rs.close();

            dao.delete(process);

            rs = stmt.executeQuery("SELECT * FROM pl_match_group WHERE group_id = '"+processId+"'");
            assertFalse("munge process didn't delete", rs.next());
            rs.close();
        } finally {
            stmt.close();
        }
    }
}
