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
import java.util.List;

import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLIndex;
import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchMakerCriteriaGroup;
import ca.sqlpower.matchmaker.MatchRule;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.dao.hibernate.MatchMakerHibernateSession;
import ca.sqlpower.matchmaker.dao.hibernate.PlFolderDAOHibernate;

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
			
			PlFolder f = new PlFolder("test folder" + count);
			PlFolderDAOHibernate plFolderDAO = new PlFolderDAOHibernate((MatchMakerHibernateSession) getSession());
			plFolderDAO.save(f);
			
            match.setParent(f);
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
        nonPersistingProperties.add("tableMergeRules");
        
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
	
	public void testMatchRequiresFolder() throws Exception {
		Match m = new Match();
        m.setName("match no folder");
		
		try {
			getDataAccessObject().save(m);
			fail("The save did not throw an exception when we saved a match with no folder.");
		} catch (RuntimeException e) {
			// Expecting the method to throw a runtime exception because the folder is null
		}
	}

	public void testIndexSave() throws Exception {
		Match m = createNewObjectUnderTest();
		
		// have to hook up a parent table so the UserType can search it for columns
		SQLTable table = new SQLTable(null, "test_parent", null, "TABLE", true);
		table.addColumn(new SQLColumn(table, "test1", 4, 10, 0));
		table.addColumn(new SQLColumn(table, "test2", 4, 10, 0));
		table.addColumn(new SQLColumn(table, "test3", 4, 10, 0));
		
		SQLIndex idx = new SQLIndex("test_index", true, null, null, null);
		idx.addChild(idx.new Column("test1", false, false));
		idx.addChild(idx.new Column("test2", false, false));
		idx.addChild(idx.new Column("test3", false, false));
		m.setSourceTableIndex(idx);
		
		table.addIndex(idx);
		getDataAccessObject().save(m);
		resetSession();
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            con = getSession().getConnection();
            stmt = con.createStatement();
            rs = stmt.executeQuery(
                    "SELECT * FROM pl_match WHERE match_id='"+m.getName()+"'");
            
            if (!rs.next()) {
            	fail("No results found for match "+m.getName());
            }
            
            assertEquals("test1", rs.getString("index_column_name0"));
            assertEquals("test2", rs.getString("index_column_name1"));
            assertEquals("test3", rs.getString("index_column_name2"));
            assertEquals(null, rs.getString("index_column_name3"));
            assertEquals(null, rs.getString("index_column_name4"));
            assertEquals(null, rs.getString("index_column_name5"));
            assertEquals(null, rs.getString("index_column_name6"));
            assertEquals(null, rs.getString("index_column_name7"));
            assertEquals(null, rs.getString("index_column_name8"));
            assertEquals(null, rs.getString("index_column_name9"));
            
        } finally {
            try { rs.close(); } catch (Exception e) { System.err.println("Couldn't close result set"); e.printStackTrace(); }
            try { stmt.close(); } catch (Exception e) { System.err.println("Couldn't close statement"); e.printStackTrace(); }
            // connection didn't come from a pool so we can't close it
        }
        
        // this is not a good idea for a unit test, but Johnson insists
        Match loadedMatch = getDataAccessObject().findByName(m.getName());
        assertNotSame("Woops, got the same match back from cache", m, loadedMatch);
        assertNotSame("Woops, got the same index back from cache", m.getSourceTableIndex(), loadedMatch.getSourceTableIndex());
        assertNotSame("Woops, got the same indexColumn back from cache", m.getSourceTableIndex().getChild(0), loadedMatch.getSourceTableIndex().getChild(0));

        // since the table on the original match was fake, the source table
        // we get back from the DAO will have no columns.  We'll just put it back
        // before testing the index column resolution.
        loadedMatch.setSourceTable(table);
        
        assertEquals(3, loadedMatch.getSourceTableIndex().getChildCount());
        assertEquals("test1", loadedMatch.getSourceTableIndex().getChild(0).getName());
        assertNotNull(loadedMatch.getSourceTableIndex().getChild(0).getColumn());
        assertEquals("test2", loadedMatch.getSourceTableIndex().getChild(1).getName());
        assertNotNull(loadedMatch.getSourceTableIndex().getChild(1).getColumn());
        assertEquals("test3", loadedMatch.getSourceTableIndex().getChild(2).getName());
        assertNotNull(loadedMatch.getSourceTableIndex().getChild(2).getColumn());
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
        
        PlFolder f = new PlFolder();
		f.setName("test folder");
		PlFolderDAOHibernate plFolderDAO = new PlFolderDAOHibernate((MatchMakerHibernateSession) getSession());
		plFolderDAO.save(f);
        
        final long matchOid = insertSampleMatchData(matchName, f.getOid());
        final long groupOid = insertSampleMatchCriteriaGroupData(matchOid, "group_"+time);
        insertSampleMatchCriteriaData(groupOid, "test_crit_"+time);
        
        Match match = getDataAccessObject().findByName(matchName);
            List<MatchMakerCriteriaGroup> groups = match.getMatchCriteriaGroups();
		assertEquals("There should be one criteria group", 1, groups.size());

		MatchMakerCriteriaGroup group = groups.get(0);
		assertEquals("Wrong Group name", "group_" + time, group.getName());

		List<MatchRule> crits = group.getChildren();
		assertEquals("There is only one set of column criteria", 1, crits
				.size());

		MatchRule crit = crits.get(0);
		assertEquals("Wrong criteria last update user",
                    "test_crit_"+time, crit.getLastUpdateAppUser());
    }
    
    public void testCriteriaGroupMove() throws Exception {
        MatchMakerCriteriaGroup cg = new MatchMakerCriteriaGroup();
        cg.setName("criteria group");
        
        Match oldMatch = new Match();
        oldMatch.setName("old");
		PlFolder f = new PlFolder();
		oldMatch.setParent(f);
        
		f.setName("test folder");
		PlFolderDAOHibernate plFolderDAO = new PlFolderDAOHibernate((MatchMakerHibernateSession) getSession());
		plFolderDAO.save(f);
        
        Match newMatch = new Match();
        newMatch.setName("new");
		newMatch.setParent(f);
        
        oldMatch.addMatchCriteriaGroup(cg);
        MatchDAO dao = getDataAccessObject();
        
        dao.save(oldMatch);
        dao.save(newMatch);
        
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            con = getSession().getConnection();
            stmt = con.createStatement();
            try { 
                rs = stmt.executeQuery(
                    "SELECT match_id FROM pl_match,pl_match_group WHERE pl_match.match_oid=pl_match_group.match_oid AND pl_match_group.group_id='"+cg.getName()+"'");
            
                if (!rs.next()) {
                    fail("No results found for match "+cg.getName());
                }
            
                assertEquals("The setup failed to work","old", rs.getString("match_id"));
            } finally {
                try { rs.close(); } catch (Exception e) { System.err.println("Couldn't close result set"); e.printStackTrace(); }
            }
            
            oldMatch.removeMatchCriteriaGroup(cg);
            newMatch.addMatchCriteriaGroup(cg);
            dao.save(newMatch);
            dao.save(oldMatch);
            try { 
                rs = stmt.executeQuery(
                    "SELECT match_id FROM pl_match,pl_match_group WHERE pl_match.match_oid=pl_match_group.match_oid AND pl_match_group.group_id='"+cg.getName()+"'");
            
                if (!rs.next()) {
                    fail("No results found for match "+cg.getName());
                }
            
                assertEquals("move failed to work","new", rs.getString("match_id"));
            } finally {
                try { rs.close(); } catch (Exception e) { System.err.println("Couldn't close result set"); e.printStackTrace(); }
            }
        } finally {
            try { stmt.close(); } catch (Exception e) { System.err.println("Couldn't close statement"); e.printStackTrace(); }
            // connection didn't come from a pool so we can't close it
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
            String matchName, Long folderOid) throws Exception;
    
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
