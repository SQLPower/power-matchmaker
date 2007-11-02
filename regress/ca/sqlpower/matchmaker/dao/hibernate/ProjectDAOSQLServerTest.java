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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import ca.sqlpower.matchmaker.DBTestUtil;
import ca.sqlpower.matchmaker.Project.ProjectMode;
import ca.sqlpower.matchmaker.dao.AbstractProjectDAOTestCase;
import ca.sqlpower.matchmaker.dao.ProjectDAO;
import ca.sqlpower.matchmaker.munge.ConcatMungeStep;
import ca.sqlpower.sql.SPDataSource;


public class ProjectDAOSQLServerTest extends AbstractProjectDAOTestCase {
    
	protected SPDataSource getDS() {
		return DBTestUtil.getSqlServerDS();
	}
	
    @Override
    public ProjectDAO getDataAccessObject() throws Exception {
        return new ProjectDAOHibernate(getSession());
    }
    
    @Override
    public MatchMakerHibernateSession getSession() throws Exception {
        return HibernateTestUtil.getSqlServerHibernateSession();
    }
    
    @Override
	public void resetSession() throws Exception {
		((TestingMatchMakerHibernateSession) getSession()).resetSession();
	}

    @Override
    protected long insertSampleMungeStepData(long parentProcessOid, String lastUpdateUser) throws Exception {
        Connection con = null;
        Statement stmt = null;
        ResultSet keysRS = null;
        final long time = System.currentTimeMillis();
        try {
            con = getSession().getConnection();
            stmt = con.createStatement();
            stmt.executeUpdate(
                    "INSERT INTO mm_munge_step (munge_process_oid, step_name, step_class, last_update_user) " +
                    "VALUES ("+parentProcessOid+", 'fake_column_"+time+"', '"+ConcatMungeStep.class.getName()+"', '"+lastUpdateUser+"')");
            keysRS = stmt.executeQuery(
                    "SELECT munge_step_oid FROM mm_munge_step " +
                    "WHERE munge_process_oid='"+parentProcessOid+"' AND step_name='fake_column_"+time+"'");
            long critOid;
            if (keysRS.next()) {
                critOid = keysRS.getLong(1);
            } else {
                throw new SQLException("Couldn't get autogenerated key for new match criteria object");
            }
            return critOid;
        } finally {
            try {
                if (keysRS != null)
                    keysRS.close();
            } catch (Exception e) {
                System.err.println("Couldn't close result set with keys");
                e.printStackTrace();
            }
            try {
                if (stmt != null)
                    stmt.close();
            } catch (Exception e) {
                System.err.println("Couldn't close statement");
                e.printStackTrace();
            }
            // connection didn't come from a pool so we can't close it
        }
    }

    @Override
    protected long insertSampleMungeProcessData(long parentProjectOid, String processName) throws Exception {
        Connection con = null;
        Statement stmt = null;
        ResultSet keysRS = null;
        try {
            con = getSession().getConnection();
            stmt = con.createStatement();
            
            stmt.executeUpdate(
                    "INSERT INTO mm_munge_process (project_oid, process_name) " +
                    "VALUES ("+parentProjectOid+", '"+processName+"')");
            keysRS = stmt.executeQuery("SELECT munge_process_oid FROM mm_munge_process WHERE process_name='"+processName+"'");
            long groupOid;
            if (keysRS.next()) {
                groupOid = keysRS.getLong(1);
            } else {
                throw new SQLException("Couldn't get autogenerated key for new munge process");
            }
            return groupOid;
        } finally {
            try {
                if (keysRS != null)
                    keysRS.close();
            } catch (Exception e) {
                System.err.println("Couldn't close result set with keys");
                e.printStackTrace();
            }
            try {
                if (stmt != null)
                    stmt.close();
            } catch (Exception e) {
                System.err.println("Couldn't close statement");
                e.printStackTrace();
            }
            // connection didn't come from a pool so we can't close it
        }
    }

    @Override
    protected long insertSampleProjectData(String projectName, Long folderOid) throws Exception {
        Connection con = null;
        Statement stmt = null;
        ResultSet keysRS = null;
        try {
            con = getSession().getConnection();
            stmt = con.createStatement();
            stmt.executeUpdate(
                    "INSERT INTO mm_project (project_name, project_type, folder_oid) " +
                    "VALUES ('"+projectName+"', '"+ProjectMode.FIND_DUPES+"', " + folderOid.longValue() + ")");
            
            keysRS = stmt.executeQuery("SELECT project_oid FROM mm_project WHERE project_name='"+projectName+"'");
            long projectOid;
            if (keysRS.next()) {
                projectOid = keysRS.getLong(1);
            } else {
                throw new SQLException("Couldn't get autogenerated key for new project object");
            }
            return projectOid;
        } finally {
            try {
                if (keysRS != null)
                    keysRS.close();
            } catch (Exception e) {
                System.err.println("Couldn't close result set with keys");
                e.printStackTrace();
            }
            try {
                if (stmt != null)
                    stmt.close();
            } catch (Exception e) {
                System.err.println("Couldn't close statement");
                e.printStackTrace();
            }
            // connection didn't come from a pool so we can't close it
        }
    }

}
