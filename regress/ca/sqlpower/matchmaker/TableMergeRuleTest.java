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

import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.matchmaker.Match.MatchMode;
import ca.sqlpower.matchmaker.TableMergeRules.ChildMergeActionType;

public class TableMergeRuleTest extends MatchMakerTestCase<TableMergeRules>{

	TableMergeRules tableMergeRules;
	private TestingMatchMakerSession testingMatchMakerSession = new TestingMatchMakerSession();

	public TableMergeRuleTest() {
		propertiesToIgnoreForEventGeneration.add("tableName");
		propertiesToIgnoreForEventGeneration.add("schemaName");
		propertiesToIgnoreForEventGeneration.add("catalogName");
		propertiesToIgnoreForDuplication.add("sourceTable");
        propertiesToIgnoreForDuplication.add("name");
		// The index is either mutable or unequal so ignore it in this test
		propertiesToIgnoreForDuplication.add("tableIndex");
		
		propertiesThatDifferOnSetAndGet.add("name");
		propertiesThatDifferOnSetAndGet.add("parent");
	}
	@Override
	protected TableMergeRules getTarget() {
		TableMergeRules tmr = new TableMergeRules();
		tmr.setSession(testingMatchMakerSession);
		return tmr;
	}

	public void testEquals() {
		TableMergeRules m1 = getTarget();
		TableMergeRules m2 = getTarget();
		
		SQLTable t1 = new SQLTable();
		SQLTable t2 = new SQLTable();
		
		Match parent1 = new Match();
		parent1.setType(MatchMode.FIND_DUPES);
		parent1.setSession(new TestingMatchMakerSession());
		parent1.setName("match1");
		Match parent2 = new Match();
		parent2.setType(MatchMode.FIND_DUPES);
		parent2.setName("match2");
		parent2.setSession(new TestingMatchMakerSession());
		assertEquals("Two new objects should be equal",m1,m2);
		
		m1.setParent(parent1);
		m2.setParent(parent1);
		
		assertEquals("Two objects with the same parent should be equal",m1,m2);
		m2.setParent(parent2);
		assertFalse("Two objects with different parents should not be equal",m1.equals(m2));
		
		
		m2.setParent(parent1);
		m2.setTableName("t1");
		assertFalse("Two objects with different tables should not be equal",m1.equals(m2));
		
		m1.setTableName("t1");
		assertEquals("Two objects with the same parents and tables should be equal",m1,m2);
		
		m1.setDeleteDup(true);
		m2.setDeleteDup(false);
		assertFalse("Two objects with different delete dup values should not be equal",
				m1.equals(m2));
		
		m2.setDeleteDup(true);
		assertEquals("Two objects with same delete dup values should be equals", m1, m2);
		
		m1.setParentTable(t1);
		m2.setParentTable(t2);
		assertFalse("Two objects with different parent tables should not be equal",
				m1.equals(m2));
		
		m2.setParentTable(t1);
		assertEquals("Two objects with same parent tables should be equal", m1, m2);
		
		m1.setChildMergeAction(ChildMergeActionType.DELETE_ALL_DUP_CHILD);
		m2.setChildMergeAction(ChildMergeActionType.UPDATE_DELETE_ON_CONFLICT);
		assertFalse("Two objects with different child merge actions should not be equal",
				m1.equals(m2));
		
		m2.setChildMergeAction(ChildMergeActionType.DELETE_ALL_DUP_CHILD);
		assertEquals("Two objects with same child merge action types should equal", m1, m2);
	}
	
	public void testIsSourceMergeRule() {
		TableMergeRules m1 = getTarget();
		TableMergeRules m2 = getTarget();
		Match match = new Match();
		match.setType(MatchMode.FIND_DUPES);
		
		SQLTable t1 = new SQLTable();
		SQLTable t2 = new SQLTable();
		
		m1.setTable(t1);
		m2.setTable(t2);
		match.setSourceTable(t1);
		m1.setParentMatch(match);
		m2.setParentMatch(match);
		
		assertTrue("Same source table of its parent match, should be a source merge rule",
				m1.isSourceMergeRule());
		assertFalse("Different source table from its parent match, should not be a source merge rule",
				m2.isSourceMergeRule());
		
		m1.setParent(null);
		assertFalse("Parent match is null, should not be a source merge rule",
				m1.isSourceMergeRule());
	}
}
