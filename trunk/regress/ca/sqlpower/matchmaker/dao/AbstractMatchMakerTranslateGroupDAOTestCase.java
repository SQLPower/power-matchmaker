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
                    "INSERT INTO pl_match_translate (translate_group_oid,match_translate_oid,seq_no) " +
                    "VALUES ("+time+", "+time+", 0)");
            
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
    	MatchMakerTranslateWord word2 = new MatchMakerTranslateWord();
    	word2.setFrom("2");
    	MatchMakerTranslateWord word3 = new MatchMakerTranslateWord();
    	word3.setFrom("3");
    	
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
    
    public void testChildrenDeletePersists() throws Exception {
    	MatchMakerTranslateGroup group = new MatchMakerTranslateGroup();
    	group.setName("parent");
    	
    	MatchMakerTranslateWord word1 = new MatchMakerTranslateWord();
    	word1.setFrom("1");
    	MatchMakerTranslateWord word2 = new MatchMakerTranslateWord();
    	word2.setFrom("2");
    	MatchMakerTranslateWord word3 = new MatchMakerTranslateWord();
    	word3.setFrom("3");
    	
    	group.addChild(word1);
    	group.addChild(word2);
    	group.addChild(word3);
    	getDataAccessObject().save(group);
    	
    	group.removeChild(word1);
    	group.removeChild(word2);
    	group.removeChild(word3);
       	getDataAccessObject().save(group);
    	
       	Connection con = getSession().getConnection();
    	Statement stmt =null;
    	ResultSet oidRs = null;
    	ResultSet rs = null;
    	try {
    	stmt = con.createStatement();
    	oidRs = stmt.executeQuery("select translate_group_oid from pl_match_translate_group where translate_group_name='parent'");
    	oidRs.next();
    	
    	rs = stmt.executeQuery("select * from pl_match_translate order by seq_no");
    	assertTrue("There should be 0 children",!rs.next());
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
    
    /**Test case to identify a bug where if the user added several items 
     * then saved the data then removed the ones at the end of the list
     * and added several more without saving. This caused the seq_number 
     * to be reused too early
     * 
     */
    public void testChildrenDeleteAddCombo() throws Exception {
    	MatchMakerTranslateGroup group = new MatchMakerTranslateGroup();
    	group.setName("parent");
    	
    	MatchMakerTranslateWord word1 = new MatchMakerTranslateWord();
    	word1.setFrom("1");
    	MatchMakerTranslateWord word2 = new MatchMakerTranslateWord();
    	word2.setFrom("2");
    	MatchMakerTranslateWord word3 = new MatchMakerTranslateWord();
    	word3.setFrom("3");
        
    	group.addChild(word1);
        group.addChild(word2);
        group.addChild(word3);
    	
    	getDataAccessObject().save(group);
    	
    	group.removeChild(word3);
    	group.removeChild(word2);

    	group.addChild(word3);
    	getDataAccessObject().save(group);
    	
       	Connection con = getSession().getConnection();
    	Statement stmt =null;
    	ResultSet oidRs = null;
    	ResultSet rs = null;
    	try {
    	stmt = con.createStatement();
    	oidRs = stmt.executeQuery("select translate_group_oid from pl_match_translate_group where translate_group_name='parent'");
    	oidRs.next();
    	
    	rs = stmt.executeQuery("select * from pl_match_translate order by seq_no");
    	assertTrue("There should be 2 children not 0",rs.next());
    	assertEquals("Wrong child in position 1","1",rs.getObject("from_word"));
    	assertTrue("There should be 2 children not 1",rs.next());
    	assertEquals("Wrong child in position 2","3",rs.getObject("from_word"));
        
        assertFalse("There should only be two children", rs.next());
        
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
    
    /**
     * Tests a case where a translate group's children is set while the seq_no's
     * are not correct. 
     */
    public void testIfNullChildrenLoadWorks() throws Exception {
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
                    "INSERT INTO pl_match_translate (translate_group_oid,match_translate_oid,seq_no) " +
                    "VALUES ("+time+", "+time+", 1)");
            
        } finally {
            try { stmt.close(); } catch (Exception e) { System.err.println("Couldn't close statement"); e.printStackTrace(); }
            // connection didn't come from a pool so we can't close it
        }
        
        MatchMakerTranslateGroup translateGroup = getDataAccessObject().findByName(translateGroupName);
        try {
        	translateGroup.getChildren(); // this could fail if the DAO doesn't cascade the retrieval properly
        	assertTrue("getChildren should fail because there are null children in the list", false);
       } catch (NullPointerException e){
        	assertEquals("Wrong exception caught", e.getMessage(),"Translate word has not been populated correctly.");
        }
        
    }
}
