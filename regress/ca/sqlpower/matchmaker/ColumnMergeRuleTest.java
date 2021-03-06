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

import ca.sqlpower.object.SPObject;
import ca.sqlpower.sqlobject.SQLColumn;
import ca.sqlpower.sqlobject.SQLDatabase;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.sqlobject.SQLTable;

public class ColumnMergeRuleTest extends MatchMakerTestCase<ColumnMergeRules> {

	ColumnMergeRules m1;
	private TestingMatchMakerSession testingMatchMakerSession = new TestingMatchMakerSession(false);

	public ColumnMergeRuleTest(String name) {
		
		super(name);
		
		propertiesToIgnoreForEventGeneration.add("columnName");
		propertiesToIgnoreForEventGeneration.add("importedKeyColumnName");
		propertiesToIgnoreForDuplication.add("columnName");
		propertiesToIgnoreForDuplication.add("importedKeyColumnName");
		propertiesToIgnoreForDuplication.add("cachedColumn");
		
		propertiesShareInstanceForDuplication.add("column");
		
		propertiesThatDifferOnSetAndGet.add("name");
		
		propertiesThatHaveSideEffects.add("column");
		propertiesThatHaveSideEffects.add("importedKeyColumn");
		propertiesThatHaveSideEffects.add("parent");
	}
	
	protected void setUp() throws Exception {
		super.setUp();
		TableMergeRules tmr = (TableMergeRules) createNewValueMaker(
        		getRootObject(), null).makeNewValue(
        				TableMergeRules.class, null, "parent table rules");
		tmr.setSession(testingMatchMakerSession);
		tmr.setTable(new SQLTable(new SQLDatabase(),true));
		tmr.getSourceTable().addColumn(new SQLColumn(tmr.getSourceTable(),"new",1,1,1));
		m1 = new ColumnMergeRules();
		m1.setParent(tmr);
		m1.setSession(tmr.getSession());
		tmr.addChild(m1);
		
	}
	
	@Override
	protected ColumnMergeRules getTarget() {
		return m1;
	}

	public void testNamesSynced() throws Exception {
		m1.setName("moo");
		assertEquals("Child name not updated when parent name updates", "moo", m1.getCachedColumn().getName());
		m1.getCachedColumn().setName("meow");
		assertEquals("Parent name not updated when child name updates", "meow", m1.getName());
		
		
	}
	
	public void testEquals() throws SQLObjectException {
		m1 = getTarget();
		ColumnMergeRules m2 = new ColumnMergeRules();
		m1.setParent(null);
		m2.setParent(null);
		
		SQLColumn c1 = new SQLColumn();
		SQLColumn c2 = new SQLColumn();
		
		TableMergeRules parent1 = new TableMergeRules();
		parent1.setSession(new TestingMatchMakerSession());
		parent1.setName("tmr1");
		
		Project gp1 = new Project();
		parent1.setParent(gp1);
		gp1.setName("gp 1");
		
		TableMergeRules parent2 = new TableMergeRules();
		parent2.setName("tmr2");
		parent2.setSession(new TestingMatchMakerSession());
		
		Project gp2 = new Project();
		parent2.setParent(gp2);
		gp2.setName("gp 2");
		
		assertEquals("Two new objects should be equal",m1,m2);
		
		m1.setParent(parent1);
		m2.setParent(parent1);
		
		assertEquals("Two objects with the same parent should be equal",m1,m2);
		m2.setParent(parent2);
		assertFalse("Two objects with different parents should not be equal",m1.equals(m2));
		
		
		m2.setParent(parent1);
		SQLColumn column = new SQLColumn();
		m2.setColumn(column);
		assertFalse("Two objects with different columns should not be equal",m1.equals(m2));
		
		m1.setColumn(column);
		assertEquals("Two objects with the same parents and columns should be equal",m1,m2);
		
		m1.setInPrimaryKey(true);
		m2.setInPrimaryKey(false);
		assertFalse("Two objects with different in primary key values should not be equal",
				m1.equals(m2));
		
		m2.setInPrimaryKey(true);
		assertEquals("Two objects with same primary key values should equal", m1, m2);
		
		m1.setImportedKeyColumn(c1);
		m2.setImportedKeyColumn(c2);
		assertFalse("Two objects with different imported key columns should not be equal",
				m1.equals(m2));
		
		m2.setImportedKeyColumn(c1);
		assertEquals("Two objects with same imported key columns should equal", m1, m2);
	}
	@Override
	protected Class<? extends SPObject> getChildClassType() {
		return null;
	}
}
