package ca.sqlpower.matchmaker.dao;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import ca.sqlpower.matchmaker.Match;

public abstract class AbstractPlMatchDAOTestCase extends AbstractDAOTestCase<Match,MatchDAO>  {

	Long count=0L;

	@Override
	public Match createNewObjectUnderTest() throws Exception {
		count++;
		Match match = new Match();
		match.setSession(getSession());
		try {
			setAllSetters(match, getNonPersitingProperties());
			match.setName("Match "+count);
            match.setParent(null);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		return match;
	}

	@Override
	public List<String> getNonPersitingProperties() {
		List<String> nonPersistingProperties = super.getNonPersitingProperties();
		nonPersistingProperties.add("lastUpdateOSUser");
		nonPersistingProperties.add("lastUpdateDate");
		nonPersistingProperties.add("lastUpdateAppUser");
		nonPersistingProperties.add("session");
        nonPersistingProperties.add("matchCriteriaGroups");
        
        //FIXME REMOVE THESE 
        nonPersistingProperties.add("resultTable");
        nonPersistingProperties.add("xrefTable");
        nonPersistingProperties.add("sourceTable");
		return nonPersistingProperties;
	}

    public void testIfChildrenLoadWorks() throws Exception {
        final long time = System.currentTimeMillis();
        final String matchName = "match_"+time;
        Connection con = null;
        Statement stmt = null;
        try {
            con = getSession().getConnection();
            stmt = con.createStatement();
            stmt.executeUpdate(
                    "INSERT INTO pl_match (match_oid, match_id, match_type) " +
                    "VALUES ("+time+", '"+matchName+"', '"+Match.MatchType.FIND_DUPES+"')");
            stmt.executeUpdate(
                    "INSERT INTO pl_match_group (group_oid, match_oid, group_id) " +
                    "VALUES ("+time+", "+time+", 'group_"+time+"')");
        } finally {
            try { stmt.close(); } catch (Exception e) { System.err.println("Couldn't close statement"); e.printStackTrace(); }
            // connection didn't come from a pool so we can't close it
        }
        
        Match match = getDataAccessObject().findByName(matchName);
        match.getMatchCriteriaGroups(); // this could fail if the DAO doesn't cascade the retrieval properly
    }
}
