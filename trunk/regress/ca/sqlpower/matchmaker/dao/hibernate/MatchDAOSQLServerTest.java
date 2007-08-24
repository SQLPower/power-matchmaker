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

import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.dao.AbstractPlMatchDAOTestCase;
import ca.sqlpower.matchmaker.dao.MatchDAO;


public class MatchDAOSQLServerTest extends AbstractPlMatchDAOTestCase {
    
    @Override
    public MatchDAO getDataAccessObject() throws Exception {
        return new MatchDAOHibernate(getSession());
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
    protected long insertSampleMatchCriteriaData(long parentGroupOid, String lastUpdateUser) throws Exception {
        Connection con = null;
        Statement stmt = null;
        ResultSet keysRS = null;
        final long time = System.currentTimeMillis();
        try {
            con = getSession().getConnection();
            stmt = con.createStatement();
            stmt.executeUpdate(
                    "INSERT INTO pl_match_criteria (group_oid, column_name, last_update_user) " +
                    "VALUES ("+parentGroupOid+", 'fake_column_"+time+"', '"+lastUpdateUser+"')");
            keysRS = stmt.executeQuery(
                    "SELECT match_criteria_oid FROM pl_match_criteria " +
                    "WHERE group_oid='"+parentGroupOid+"' AND column_name='fake_column_"+time+"'");
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
    protected long insertSampleMatchCriteriaGroupData(long parentMatchOid, String groupName) throws Exception {
        Connection con = null;
        Statement stmt = null;
        ResultSet keysRS = null;
        try {
            con = getSession().getConnection();
            stmt = con.createStatement();
            
            stmt.executeUpdate(
                    "INSERT INTO pl_match_group (match_oid, group_id) " +
                    "VALUES ("+parentMatchOid+", '"+groupName+"')");
            keysRS = stmt.executeQuery("SELECT group_oid FROM pl_match_group WHERE group_id='"+groupName+"'");
            long groupOid;
            if (keysRS.next()) {
                groupOid = keysRS.getLong(1);
            } else {
                throw new SQLException("Couldn't get autogenerated key for new match criteria group object");
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
    protected long insertSampleMatchData(String matchName, Long folderOid) throws Exception {
        Connection con = null;
        Statement stmt = null;
        ResultSet keysRS = null;
        try {
            con = getSession().getConnection();
            stmt = con.createStatement();
            stmt.executeUpdate(
                    "INSERT INTO pl_match (match_id, match_type, folder_oid) " +
                    "VALUES ('"+matchName+"', '"+Match.MatchMode.FIND_DUPES+"', " + folderOid.longValue() + ")");
            
            keysRS = stmt.executeQuery("SELECT match_oid FROM pl_match WHERE match_id='"+matchName+"'");
            long matchOid;
            if (keysRS.next()) {
                matchOid = keysRS.getLong(1);
            } else {
                throw new SQLException("Couldn't get autogenerated key for new match object");
            }
            return matchOid;
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
