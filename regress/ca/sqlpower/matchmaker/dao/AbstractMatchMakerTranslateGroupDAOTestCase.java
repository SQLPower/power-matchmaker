package ca.sqlpower.matchmaker.dao;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;

import ca.sqlpower.matchmaker.MatchMakerTranslateGroup;
import ca.sqlpower.matchmaker.MatchMakerTranslateWord;

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
    
    public void testChildrenMovePersists() throws Exception {
    	MatchMakerTranslateGroup group = new MatchMakerTranslateGroup();
    	group.setName("parent");
    	
    	MatchMakerTranslateWord word1 = new MatchMakerTranslateWord();
    	word1.setFrom("1");
    	word1.setLocation(1L);
    	MatchMakerTranslateWord word2 = new MatchMakerTranslateWord();
    	word2.setFrom("2");
    	word2.setLocation(2L);
    	MatchMakerTranslateWord word3 = new MatchMakerTranslateWord();
    	word3.setFrom("3");
    	word3.setLocation(3L);
    	
    	group.addChild(word1);
    	group.addChild(word2);
    	group.addChild(word3);
    	
    	getDataAccessObject().save(group);
    	Collections.swap(group.getChildren(), 1, 2);
    	getDataAccessObject().save(group);
    	
    	Connection con = getSession().getConnection();
    	Statement stmt =null;
    	ResultSet oidRs = null;
    	ResultSet rs = null;
    	try {
    	stmt = con.createStatement();
    	oidRs = stmt.executeQuery("select translate_group_oid from pl_match_translate_group where translate_group_name='parent'");
    	oidRs.next();
    	Long oid = oidRs.getLong("translate_group_oid"); 
    	
    	rs = stmt.executeQuery("select * from pl_match_translate order by seq_no");
    	assertTrue("There should be 3 children not 0",rs.next());
    	assertEquals("Wrong child in position 1","1",rs.getObject("from_word"));
    	assertTrue("There should be 3 children not 1",rs.next());
    	assertEquals("Wrong child in position 2","3",rs.getObject("from_word"));
    	assertTrue("There should be 3 children not 2",rs.next());
    	assertEquals("Wrong child in position 3","2",rs.getObject("from_word"));
    	} finally {
    		try {
                if (oidRs != null)
                	oidRs.close();
            } catch (Exception e) {
                System.err.println("Couldn't close oid result set ");
                e.printStackTrace();
            }
            try {
                if (rs != null)
                    rs.close();
            } catch (Exception e) {
                System.err.println("Couldn't close result set for translate groups");
                e.printStackTrace();
            }
            try {
                if (stmt != null)
                    stmt.close();
            } catch (Exception e) {
                System.err.println("Couldn't close statement");
                e.printStackTrace();
            }
    	}
    
    	
    }
}
