package ca.sqlpower.matchmaker.dao;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import ca.sqlpower.matchmaker.MatchMakerTranslateGroup;

public abstract class AbstractMatchMakerTranslateGroupDAOTestCase extends AbstractDAOTestCase<MatchMakerTranslateGroup,MatchMakerTranslateGroupDAO>  {

	Long count=0L;

	@Override
	public MatchMakerTranslateGroup createNewObjectUnderTest() throws Exception {
		count++;
        MatchMakerTranslateGroup translateGroup = new MatchMakerTranslateGroup();
        translateGroup.setSession(getSession());
		try {
			setAllSetters(translateGroup, getNonPersitingProperties());
            translateGroup.setName("Translate Group "+count);
            translateGroup.setParent(null);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		return translateGroup;
	}

	@Override
	public List<String> getNonPersitingProperties() {
		List<String> nonPersistingProperties = super.getNonPersitingProperties();
        nonPersistingProperties.add("lastUpdateOSUser");
        nonPersistingProperties.add("lastUpdateDate");
        nonPersistingProperties.add("lastUpdateAppUser");
		return nonPersistingProperties;
	}

    public void testIfChildrenLoadWorks() throws Exception {
        final long time = System.currentTimeMillis();
        final String translateGroupName = "translateGroup_"+time;
        Connection con = null;
        Statement stmt = null;
        try {
            con = getSession().getConnection();
            stmt = con.createStatement();
            stmt.executeUpdate(
                    "INSERT INTO pl_match_translate_group (translate_group_oid, translate_group_name) " +
                    "VALUES ("+time+", '"+translateGroupName+"')");
            stmt.executeUpdate(
                    "INSERT INTO pl_match_translate (translate_group_oid, match_translate_oid) " +
                    "VALUES ("+time+", "+time+")");
        } finally {
            try { stmt.close(); } catch (Exception e) { System.err.println("Couldn't close statement"); e.printStackTrace(); }
            // connection didn't come from a pool so we can't close it
        }
        
        MatchMakerTranslateGroup translateGroup = getDataAccessObject().findByName(translateGroupName);
        translateGroup.getChildren(); // this could fail if the DAO doesn't cascade the retrieval properly
    }
}
