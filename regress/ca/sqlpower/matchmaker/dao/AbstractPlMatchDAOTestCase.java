package ca.sqlpower.matchmaker.dao;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchMakerCriteria;
import ca.sqlpower.matchmaker.MatchMakerCriteriaGroup;
import ca.sqlpower.matchmaker.dao.hibernate.TestingMatchMakerHibernateSession;

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
		nonPersistingProperties.add("lastUpdateDate");
		nonPersistingProperties.add("session");
        nonPersistingProperties.add("matchCriteriaGroups");
        
        // tested explicitly elsewhere
        nonPersistingProperties.add("sourceTable");
        nonPersistingProperties.add("sourceTableCatalog");
        nonPersistingProperties.add("sourceTableSchema");
        nonPersistingProperties.add("sourceTableName");
        nonPersistingProperties.add("sourceTableIndex");
        
        nonPersistingProperties.add("resultTableCatalog");
        nonPersistingProperties.add("resultTableSchema");
        nonPersistingProperties.add("resultTableName");
        
        nonPersistingProperties.add("xrefTableCatalog");
        nonPersistingProperties.add("xrefTableSchema");
        nonPersistingProperties.add("xrefTableName");
      
		return nonPersistingProperties;
	}

    /**
     * Inserts data directly into the tables, then uses the DAO to retrieve the
     * objects those rows represent, and checks that the objects are the ones we
     * inserted.  Also implicitly checks that the descdendants are fetched eagerly
     * (and don't depend on the hibernate session staying open).
     */
    public void testIfChildrenLoadWorks() throws Exception {
        final long time = System.currentTimeMillis();
        final String matchName = "match_"+time;
        final long matchOid = insertSampleMatchData(matchName);
        final long groupOid = insertSampleMatchCriteriaGroupData(matchOid, "group_"+time);
        insertSampleMatchCriteriaData(groupOid, "test_crit_"+time);
        
        Match match = getDataAccessObject().findByName(matchName);
        
        try {
            ((TestingMatchMakerHibernateSession) getSession()).setConnectionDisabled(true);
            List<MatchMakerCriteriaGroup> groups = match.getMatchCriteriaGroups();
            assertEquals("There should be one criteria group",
                    1, groups.size());

            MatchMakerCriteriaGroup group = groups.get(0);
            assertEquals("Wrong Group name",
                    "group_"+time, group.getName());

            List<MatchMakerCriteria> crits = group.getChildren();
            assertEquals("There is only one set of column criteria",
                    1, crits.size());

            MatchMakerCriteria crit = crits.get(0);
            assertEquals("Wrong criteria last update user",
                    "test_crit_"+time, crit.getLastUpdateAppUser());
        } finally {
            ((TestingMatchMakerHibernateSession) getSession()).setConnectionDisabled(false);
        }

    }

    
    /**
     * Inserts a sample entry in PL_MATCH, and returns its OID.  The match will
     * have its name (MATCH_ID) set to the given string value.  This is useful for
     * verifying that you are retrieving the same record that this method inserted.
     * <p>
     * This method is abstract because the OID columns have to be handled
     * differently on different database platforms.
     * 
     * @return The MATCH_OID value of the new match that was inserted.
     */
    protected abstract long insertSampleMatchData(
            String matchName) throws Exception;
    
    /**
     * Inserts a sample entry in PL_MATCH_GROUP, and returns its OID.  The group will
     * its name (GROUP_ID) set to the given string value.  This is useful for
     * verifying that you are retrieving the same record that this method inserted.
     * <p>
     * This method is abstract because the OID columns have to be handled
     * differently on different database platforms.
     * 
     * @return The GROUP_OID value of the new match group that was inserted.
     */
    protected abstract long insertSampleMatchCriteriaGroupData(
            long parentMatchOid, String groupName) throws Exception;

    /**
     * Inserts a sample entry in PL_MATCH_CRITERIA, and returns its OID.  The criteria will
     * have its LAST_UPDATE_USER set to the given string value (it doesn't use the name
     * because criteria groups don't have names).  This is useful for
     * verifying that you are retrieving the same record that this method inserted.
     * <p>
     * This method is abstract because the OID columns have to be handled
     * differently on different database platforms.
     * 
     * @return The GROUP_OID value of the new match group that was inserted.
     */
    protected abstract long insertSampleMatchCriteriaData(
            long parentGroupOid, String lastUpdateUser) throws Exception;
}
