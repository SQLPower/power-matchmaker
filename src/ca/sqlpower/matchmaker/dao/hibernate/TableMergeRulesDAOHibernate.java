package ca.sqlpower.matchmaker.dao.hibernate;

import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.TableMergeRules;
import ca.sqlpower.matchmaker.dao.TableMergeRuleDAO;

public class TableMergeRulesDAOHibernate extends AbstractMatchMakerDAOHibernate<TableMergeRules>
		implements TableMergeRuleDAO {

	public TableMergeRulesDAOHibernate(MatchMakerHibernateSession matchMakerSession) {
		super(matchMakerSession);
		
	}

	public void delete(TableMergeRules deleteMe) {
		Match parent = deleteMe.getParentMatch();
		if (parent != null ){
			parent.removeTableMergeRule(deleteMe);
		}
		super.delete(deleteMe);
	}

	public Class<TableMergeRules> getBusinessClass() {
		return TableMergeRules.class;
	}
	

}
