
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
