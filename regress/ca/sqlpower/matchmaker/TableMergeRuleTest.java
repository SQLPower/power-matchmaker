package ca.sqlpower.matchmaker;

public class TableMergeRuleTest extends MatchMakerTestCase<TableMergeRules>{

	TableMergeRules tableMergeRules;
	private TestingMatchMakerSession testingMatchMakerSession = new TestingMatchMakerSession();

	public TableMergeRuleTest() {
		propertiesToIgnoreForEventGeneration.add("tableName");
		propertiesToIgnoreForEventGeneration.add("schemaName");
		propertiesToIgnoreForEventGeneration.add("catalogName");
		propertiesToIgnoreForDuplication.add("sourceTable");
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
		
		Match parent1 = new Match();
		parent1.setSession(new TestingMatchMakerSession());
		parent1.setName("match1");
		Match parent2 = new Match();
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
	}
}
