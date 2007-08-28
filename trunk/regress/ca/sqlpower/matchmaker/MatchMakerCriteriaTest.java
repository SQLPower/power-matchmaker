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

package ca.sqlpower.matchmaker;


public class MatchMakerCriteriaTest extends MatchMakerTestCase<MatchRule> {

	private MatchRule target;
	final String appUserName = "Test User";

	protected void setUp() throws Exception {
		super.setUp();
		target = new MatchRule();
		MatchMakerCriteriaGroup g = new MatchMakerCriteriaGroup();
		g.addChild(target);
		Match match = new Match();
		match.setSession(new TestingMatchMakerSession());
		match.addMatchCriteriaGroup(g);
		match.setSourceTableCatalog("cat");
		match.setSourceTableName("name");
		match.setSourceTableSchema("schema");
		MatchMakerSession session = new TestingMatchMakerSession();
		((TestingMatchMakerSession)session).setAppUser(appUserName);
		target.setSession(session);
        propertiesToIgnoreForEventGeneration.add("columnName");
        // ignored due to inter-related behavior
        propertiesToIgnoreForDuplication.add("column");
        propertiesToIgnoreForDuplication.add("columnName");
        propertiesToIgnoreForDuplication.add("name");
        // removed as this should be the same object
        propertiesToIgnoreForDuplication.add("translateGroup");
        
        propertiesThatDifferOnSetAndGet.add("name");
        propertiesThatDifferOnSetAndGet.add("parent");
        
        propertiesThatHaveSideEffects.add("column");
	}

	@Override
	protected MatchRule getTarget() {
		return target;
	}
    
    /**
     * Tests that getColumnName() and setColumnName() work together
     * in the absence of calls to getColumn() and setColumn().
     */
    public void testGetAndSetColumnName() throws Exception {
        target.setColumnName("testcow");
        assertEquals("testcow", target.getColumnName());
    }
	
	public void testAddChild() {
		try {
			target.addChild(null);
			fail("MatchMakerCriteria class should not allow child!");
		} catch ( IllegalStateException e ) {
			// this is what we excepted
		}
	}

    public void testAssertDoesNotAllowChildren(){
        assertFalse(target.allowsChildren());
    }
    
    public void testTwoUninitializedInstancesEqual() {
        MatchRule other = new MatchRule();
        target.getParent().addChild(other);
        assertTrue(target.equals(other));
    }

    public void testEqualsSelf() {
        assertTrue(target.equals(target));
    }
    
    public void testDifferentColumnNamesNotEqual() {
        target.setColumnName("bar");

        MatchRule other = new MatchRule();
        other.setColumnName("foo");
        target.getParent().addChild(other);

        assertFalse(target.equals(other));
    }

    public void testSameColumnNamesEqual() {
        target.setColumnName("bar");

        MatchRule other = new MatchRule();
        other.setColumnName("bar");
        target.getParent().addChild(other);

        assertTrue(other.equals(target));
        assertTrue(target.equals(other));
    }

}