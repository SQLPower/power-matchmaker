package ca.sqlpower.matchmaker.dao;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.TableMergeRules;
import ca.sqlpower.matchmaker.dao.hibernate.MatchDAOHibernate;
import ca.sqlpower.matchmaker.dao.hibernate.MatchMakerHibernateSession;
import ca.sqlpower.matchmaker.dao.hibernate.PlFolderDAOHibernate;

public abstract class AbstractTableMergeRulesDAOTestCase extends AbstractDAOTestCase<TableMergeRules,TableMergeRuleDAO>  {

	Long count=0L;
    Match match;
    PlFolder folder;
    public AbstractTableMergeRulesDAOTestCase() throws Exception {
        match= new Match();
        match.setName("Merge Rules Test Match");
        match.setType(Match.MatchMode.BUILD_XREF);
        folder = new PlFolder("test folder");
        match.setParent(folder);
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
        PlFolderDAOHibernate plFolderDAO = new PlFolderDAOHibernate((MatchMakerHibernateSession) getSession());
		plFolderDAO.save(folder);
        MatchDAO matchDAO = new MatchDAOHibernate(getSession());
        matchDAO.save(match);
    }

	@Override
	public TableMergeRules createNewObjectUnderTest() throws Exception {
		count++;
		TableMergeRules mergeRules = new TableMergeRules();
		mergeRules.setSession(getSession());
		try {
			setAllSetters(mergeRules, getNonPersitingProperties());
			mergeRules.setName("MergeRule "+count);
			mergeRules.setTableName("table"+count);
            match.addTableMergeRule(mergeRules);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
		return mergeRules;
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
        nonPersistingProperties.add("name");
		return nonPersistingProperties;
	}

	public void testTableParentIsTheCorrectObject() throws Exception {
		createNewObjectUnderTest();
		TableMergeRules mergeRules = match.getTableMergeRules().get(0);
		assertEquals("The merge rule "+mergeRules.toString()+ "'s  parent is not the folder it should be in",match.getTableMergeRulesFolder(),mergeRules.getParent());
		
	}
	
    public void testDelete() throws Exception {
        Connection con = getSession().getConnection();
        Statement stmt = null;
        createNewObjectUnderTest();
        try {

            TableMergeRules mergeRules = match.getTableMergeRules().get(0);
            String tableName = mergeRules.getTableName();
            TableMergeRuleDAO dao = getDataAccessObject();
            dao.save(mergeRules);
            
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM pl_merge_criteria WHERE table_name = '"+tableName+"'");
            assertTrue("match group didn't save?!", rs.next());
            rs.close();
            
            dao.delete(mergeRules);

            rs = stmt.executeQuery("SELECT * FROM pl_merge_criteria WHERE table_name = '"+tableName+"'");
            assertFalse("match group didn't delete", rs.next());
            rs.close();
        } finally {
        	if (stmt != null) {
        		stmt.close();
        	}
        }
    }
}
