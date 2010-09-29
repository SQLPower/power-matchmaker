/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of DQguru
 *
 * DQguru is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DQguru is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */


package ca.sqlpower.matchmaker;

import ca.sqlpower.matchmaker.TableMergeRules.ChildMergeActionType;
import ca.sqlpower.object.SPObject;
import ca.sqlpower.sqlobject.SQLTable;

public class TableMergeRuleTest extends MatchMakerTestCase<TableMergeRules>{

	TableMergeRules m1;
	private TestingMatchMakerSession testingMatchMakerSession = new TestingMatchMakerSession();

	public TableMergeRuleTest(String name) {
		super(name);
	}
	
	public void setUp() throws Exception {

		propertiesToIgnoreForEventGeneration.add("tableName");
		propertiesToIgnoreForEventGeneration.add("schemaName");
		propertiesToIgnoreForEventGeneration.add("catalogName");
		propertiesToIgnoreForEventGeneration.add("spDataSource");
		
		propertiesToIgnoreForDuplication.add("sourceTable");
        propertiesToIgnoreForDuplication.add("name");
		// The index is either mutable or unequal so ignore it in this test
		propertiesToIgnoreForDuplication.add("tableIndex");
		propertiesToIgnoreForDuplication.add("primaryKeyFromIndex");
		propertiesToIgnoreForDuplication.add("uniqueKeyColumns");
		propertiesThatDifferOnSetAndGet.add("name");
		propertiesThatDifferOnSetAndGet.add("parent");
		
		//this should not differ but there is a check that ensues that the DS
		//exists
		propertiesThatDifferOnSetAndGet.add("spDataSource");
		
		super.setUp();
		m1 = new TableMergeRules();
		m1.setSession(testingMatchMakerSession);
	}
	
	@Override
	protected TableMergeRules getTarget() {
		return m1;
	}

	public void testEquals() {
		m1 = new TableMergeRules();
		TableMergeRules m2 = new TableMergeRules();
		m1.setSession(testingMatchMakerSession);
		m2.setSession(testingMatchMakerSession);
		
		Project parent1 = new Project();
		parent1.setSession(new TestingMatchMakerSession());
		parent1.setName("project1");
		Project parent2 = new Project();
		parent2.setName("project2");
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
		
		m1.setParentMergeRule(m2);
		m2.setParentMergeRule(m1);
		assertFalse("Two objects with different parent tables should not be equal",
				m1.equals(m2));
		
		m2.setParentMergeRule(m2);
		assertEquals("Two objects with same parent tables should be equal", m1, m2);
		
		m1.setChildMergeAction(ChildMergeActionType.DELETE_ALL_DUP_CHILD);
		m2.setChildMergeAction(ChildMergeActionType.UPDATE_DELETE_ON_CONFLICT);
		assertFalse("Two objects with different child merge actions should not be equal",
				m1.equals(m2));
		
		m2.setChildMergeAction(ChildMergeActionType.DELETE_ALL_DUP_CHILD);
		assertEquals("Two objects with same child merge action types should equal", m1, m2);
	}
	
	public void testIsSourceMergeRule() {
		m1 = new TableMergeRules();
		TableMergeRules m2 = new TableMergeRules();
		m1.setSession(testingMatchMakerSession);
		m2.setSession(testingMatchMakerSession);
		Project project = new Project();
		
		SQLTable t1 = new SQLTable();
		SQLTable t2 = new SQLTable();
		
		m1.setTable(t1);
		m2.setTable(t2);
		project.setSourceTable(t1);
		m1.setParent(project);
		m2.setParent(project);
		
		assertTrue("Same source table of its parent project, should be a source merge rule",
				m1.isSourceMergeRule());
		assertFalse("Different source table from its parent project, should not be a source merge rule",
				m2.isSourceMergeRule());
		
		m1.setParent(null);
		assertFalse("Parent project is null, should not be a source merge rule",
				m1.isSourceMergeRule());
	}
	@Override
	protected Class<? extends SPObject> getChildClassType() {
		return ColumnMergeRules.class;
	}
}
