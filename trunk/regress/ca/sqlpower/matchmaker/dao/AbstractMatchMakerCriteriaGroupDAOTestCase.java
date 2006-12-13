package ca.sqlpower.matchmaker.dao;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchMakerCriteriaGroup;
import ca.sqlpower.matchmaker.dao.hibernate.MatchDAOHibernate;
import ca.sqlpower.matchmaker.dao.hibernate.MatchMakerHibernateSession;

public abstract class AbstractMatchMakerCriteriaGroupDAOTestCase extends AbstractDAOTestCase<MatchMakerCriteriaGroup,MatchCriteriaGroupDAO>  {

	Long count=0L;
    Match match;
    public AbstractMatchMakerCriteriaGroupDAOTestCase() {
        match= new Match();
        match.setName("Criteria Group Test Match");
        match.setType(Match.MatchMode.BUILD_XREF);
        match.setParent(null);
        try {
            match.setSession(getSession());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public abstract MatchMakerHibernateSession getSession() throws Exception;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MatchDAO matchDAO = new MatchDAOHibernate(getSession());
        matchDAO.save(match);
    }

	@Override
	public MatchMakerCriteriaGroup createNewObjectUnderTest() throws Exception {
		count++;
		MatchMakerCriteriaGroup criteriaGroup = new MatchMakerCriteriaGroup();
        criteriaGroup.setSession(getSession());
		try {
			setAllSetters(criteriaGroup, getNonPersitingProperties());
            criteriaGroup.setName("Group "+count);
            match.addMatchCriteriaGroup(criteriaGroup);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		return criteriaGroup;
	}

	@Override
	public List<String> getNonPersitingProperties() {
		List<String> nonPersistingProperties = super.getNonPersitingProperties();
		nonPersistingProperties.add("lastUpdateOSUser");
		nonPersistingProperties.add("lastUpdateDate");
		nonPersistingProperties.add("lastUpdateAppUser");
		nonPersistingProperties.add("session");
        nonPersistingProperties.add("parent");
        nonPersistingProperties.add("parentMatch");
        nonPersistingProperties.add("oid");

		return nonPersistingProperties;
	}

    public void testDelete() throws Exception {
        Connection con = getSession().getConnection();
        MatchDAO matchDAO = new MatchDAOHibernate(getSession());
        Statement stmt = null;
        try {

            MatchMakerCriteriaGroup group = match.getMatchCriteriaGroups().get(0);
            String groupId = group.getName();

            MatchCriteriaGroupDAO dao = getDataAccessObject();
            dao.save(group);
            
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM pl_match_group WHERE group_id = '"+groupId+"'");
            assertTrue("match group didn't save?!", rs.next());
            rs.close();

            dao.delete(group);

            rs = stmt.executeQuery("SELECT * FROM pl_match_group WHERE group_id = '"+groupId+"'");
            assertFalse("match group didn't delete", rs.next());
            rs.close();
        } finally {
            stmt.close();
        }
    }
}
