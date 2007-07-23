package ca.sqlpower.matchmaker;

import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLDatabase;
import ca.sqlpower.architect.SQLTable;

public class ColumnMergeRuleTest extends MatchMakerTestCase<ColumnMergeRules>{

	ColumnMergeRules rule;
	private TestingMatchMakerSession testingMatchMakerSession = new TestingMatchMakerSession();

	public ColumnMergeRuleTest() {
		propertiesToIgnoreForEventGeneration.add("columnName");
		propertiesToIgnoreForDuplication.add("columnName");
		propertiesThatDifferOnSetAndGet.add("name");
		
		propertiesThatHaveSideEffects.add("column");
		propertiesThatHaveSideEffects.add("parent");
	}
	@Override
	protected ColumnMergeRules getTarget() {
		
		TableMergeRules tmr = new TableMergeRules();
		tmr.setSession(testingMatchMakerSession);
		tmr.setTable(new SQLTable(new SQLDatabase(),true));
		tmr.getSourceTable().addColumn(new SQLColumn(tmr.getSourceTable(),"new",1,1,1));
		ColumnMergeRules cmr = new ColumnMergeRules();
		cmr.setParent(tmr);
		return cmr;
	}

	public void testEquals() {
		ColumnMergeRules m1 = getTarget();
		ColumnMergeRules m2 = getTarget();
		m1.setParent(null);
		m2.setParent(null);
		
		TableMergeRules parent1 = new TableMergeRules();
		parent1.setSession(new TestingMatchMakerSession());
		parent1.setName("tmr1");
		
		Match gp1 = new Match();
		parent1.setParent(gp1);
		gp1.setName("gp 1");
		
		TableMergeRules parent2 = new TableMergeRules();
		parent2.setName("tmr2");
		parent2.setSession(new TestingMatchMakerSession());
		
		Match gp2 = new Match();
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
	}
}
