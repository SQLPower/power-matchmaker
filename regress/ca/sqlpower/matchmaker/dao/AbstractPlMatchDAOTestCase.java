package ca.sqlpower.matchmaker.dao;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import ca.sqlpower.matchmaker.Match;

public abstract class AbstractPlMatchDAOTestCase extends AbstractDAOTestCase<Match,MatchDAO>  {

	Long count=0L;

	@Override
	public Match getNewObjectUnderTest() {
		count++;
		Match match = new Match();
		match.setSession(this.session);
		try {
			setAllSetters(match, getNonPersitingProperties());
			match.setName("Match "+count);
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
		nonPersistingProperties.add("lastUpdateOSUser");
		nonPersistingProperties.add("lastUpdateDate");
		nonPersistingProperties.add("lastUpdateAppUser");
		nonPersistingProperties.add("session");
		return nonPersistingProperties;
	}
	
}
