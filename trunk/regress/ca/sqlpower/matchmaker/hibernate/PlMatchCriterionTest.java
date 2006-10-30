package ca.sqlpower.matchmaker.hibernate;



public class PlMatchCriterionTest extends AutoDifferentValueObjectTestCase {

	@Override
	Object createInstance() {
		return new PlMatchCriterion();
	}

	@Override
	protected Object createControlInstance() throws Exception {
		PlMatchCriterion plMatchCriterion = new PlMatchCriterion();
		return plMatchCriterion;
	}

}
