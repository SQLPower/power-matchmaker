
package ca.sqlpower.matchmaker.dao.hibernate;

import ca.sqlpower.matchmaker.dao.AbstractTableMergeRulesDAOTestCase;
import ca.sqlpower.matchmaker.dao.TableMergeRuleDAO;


public class TableMergeRulesDAOOracleTest extends AbstractTableMergeRulesDAOTestCase {
    
    public TableMergeRulesDAOOracleTest() throws Exception {
		super();
		// TODO Auto-generated constructor stub
	}
	@Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    @Override
	public void resetSession() throws Exception {
		((TestingMatchMakerHibernateSession) getSession()).resetSession();
	}
    
	@Override
	public TableMergeRuleDAO getDataAccessObject() throws Exception {
		return new TableMergeRulesDAOHibernate(getSession());
	}

    @Override
    public MatchMakerHibernateSession getSession() throws Exception {
        return HibernateTestUtil.getSqlServerHibernateSession();
    }
}
