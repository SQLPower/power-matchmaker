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



package ca.sqlpower.matchmaker.dao.hibernate;

import java.sql.Connection;
import java.sql.Statement;

import ca.sqlpower.matchmaker.DBTestUtil;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.Project.ProjectMode;
import ca.sqlpower.matchmaker.dao.AbstractProjectDAOTestCase;
import ca.sqlpower.matchmaker.dao.ProjectDAO;
import ca.sqlpower.matchmaker.munge.ConcatMungeStep;
import ca.sqlpower.sql.SPDataSource;


public class ProjectDAOOracleTest extends AbstractProjectDAOTestCase {
    
    private Project project;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        project = createNewObjectUnderTest();
    }
    
	protected SPDataSource getDS() {
		return DBTestUtil.getOracleDS();
	}
    
	@Override
	public ProjectDAO getDataAccessObject() throws Exception {
		return new ProjectDAOHibernate(getSession());
	}

    @Override
    public MatchMakerHibernateSession getSession() throws Exception {
        return HibernateTestUtil.getOracleHibernateSession();
    }

    public void testMungeSettingsNotNullByDefault() {
        assertNotNull(project.getMungeSettings());
    }

    public void testMergeSettingsNotNullByDefault() {
        assertNotNull(project.getMergeSettings());
    }

    @Override
    protected long insertSampleProjectData(String projectName, Long folderOid) throws Exception {
        final long time = System.currentTimeMillis();
        Connection con = null;
        Statement stmt = null;
        try {
            con = getSession().getConnection();
            stmt = con.createStatement();
            stmt.executeUpdate(
                    "INSERT INTO mm_project (project_oid, project_name, project_type) " +
                    "VALUES ("+time+", '"+projectName+"', '"+ProjectMode.FIND_DUPES+"')");
        } finally {
            try { stmt.close(); } catch (Exception e) { System.err.println("Couldn't close statement"); e.printStackTrace(); }
            // connection didn't come from a pool so we can't close it
        }
        return time;
    }

    @Override
    protected long insertSampleMungeStepData(long parentProcessOid, String lastUpdateUser) throws Exception {
        final long time = System.currentTimeMillis();
        Connection con = null;
        Statement stmt = null;
        try {
            con = getSession().getConnection();
            stmt = con.createStatement();
            stmt.executeUpdate(
                    "INSERT INTO mm_munge_step (munge_process_oid, munge_step_oid, step_name, step_class, last_update_user) " +
                    "VALUES ("+parentProcessOid+", "+time+", 'fake_column_"+time+"', '"+ConcatMungeStep.class.getName()+"', '"+lastUpdateUser+"')");
        } finally {
            try { stmt.close(); } catch (Exception e) { System.err.println("Couldn't close statement"); e.printStackTrace(); }
            // connection didn't come from a pool so we can't close it
        }
        return time;
    }

    @Override
    protected long insertSampleMungeProcessData(long parentProjectOid, String processName) throws Exception {
        final long time = System.currentTimeMillis();
        Connection con = null;
        Statement stmt = null;
        try {
            con = getSession().getConnection();
            stmt = con.createStatement();
            stmt.executeUpdate(
                    "INSERT INTO mm_munge_process (munge_process_oid, project_oid, process_name) " +
                    "VALUES ("+time+", "+parentProjectOid+", '"+processName+"')");
        } finally {
            try { stmt.close(); } catch (Exception e) { System.err.println("Couldn't close statement"); e.printStackTrace(); }
            // connection didn't come from a pool so we can't close it
        }
        return time;
    }

	@Override
	public void resetSession() throws Exception {
		((TestingMatchMakerHibernateSession) getSession()).resetSession();
	}
    
    
}
