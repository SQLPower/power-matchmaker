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

import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.dao.AbstractPlMatchDAOTestCase;
import ca.sqlpower.matchmaker.dao.MatchDAO;


public class MatchDAOOracleTest extends AbstractPlMatchDAOTestCase {
    
    private Match match;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        match = createNewObjectUnderTest();
    }
    
	@Override
	public MatchDAO getDataAccessObject() throws Exception {
		return new MatchDAOHibernate(getSession());
	}

    @Override
    public MatchMakerHibernateSession getSession() throws Exception {
        return HibernateTestUtil.getOracleHibernateSession();
    }

    public void testMatchSettingsNotNullByDefault() {
        assertNotNull(match.getMatchSettings());
    }

    public void testMergeSettingsNotNullByDefault() {
        assertNotNull(match.getMergeSettings());
    }

    @Override
    protected long insertSampleMatchData(String matchName, Long folderOid) throws Exception {
        final long time = System.currentTimeMillis();
        Connection con = null;
        Statement stmt = null;
        try {
            con = getSession().getConnection();
            stmt = con.createStatement();
            stmt.executeUpdate(
                    "INSERT INTO pl_match (match_oid, match_id, match_type) " +
                    "VALUES ("+time+", '"+matchName+"', '"+Match.MatchMode.FIND_DUPES+"')");
        } finally {
            try { stmt.close(); } catch (Exception e) { System.err.println("Couldn't close statement"); e.printStackTrace(); }
            // connection didn't come from a pool so we can't close it
        }
        return time;
    }

    @Override
    protected long insertSampleMatchCriteriaData(long parentGroupOid, String lastUpdateUser) throws Exception {
        final long time = System.currentTimeMillis();
        Connection con = null;
        Statement stmt = null;
        try {
            con = getSession().getConnection();
            stmt = con.createStatement();
            stmt.executeUpdate(
                    "INSERT INTO pl_match_criteria (group_oid, match_criteria_oid, column_name, last_update_user) " +
                    "VALUES ("+parentGroupOid+", "+time+", 'fake_column_"+time+"', '"+lastUpdateUser+"')");
        } finally {
            try { stmt.close(); } catch (Exception e) { System.err.println("Couldn't close statement"); e.printStackTrace(); }
            // connection didn't come from a pool so we can't close it
        }
        return time;
    }

    @Override
    protected long insertSampleMatchCriteriaGroupData(long parentMatchOid, String groupName) throws Exception {
        final long time = System.currentTimeMillis();
        Connection con = null;
        Statement stmt = null;
        try {
            con = getSession().getConnection();
            stmt = con.createStatement();
            stmt.executeUpdate(
                    "INSERT INTO pl_match_group (group_oid, match_oid, group_id) " +
                    "VALUES ("+time+", "+parentMatchOid+", '"+groupName+"')");
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
