package ca.sqlpower.matchmaker;

import java.util.Collections;

import ca.sqlpower.matchmaker.PotentialMatchRecord.MatchType;
import ca.sqlpower.matchmaker.PotentialMatchRecord.StoreState;

import junit.framework.TestCase;

public class PotentialMatchRecordTest extends TestCase {

	PotentialMatchRecord pmr;
	
	@Override
	protected void setUp() throws Exception {
		MatchMakerSession session = new TestingMatchMakerSession();
		Match match = new Match();
		match.setSession(session);
		MatchRuleSet ruleSet = new MatchRuleSet();
		ruleSet.setName("ruleset");
		match.getMatchCriteriaGroupFolder().addChild(ruleSet);
		MatchPool pool = new MatchPool(match);
		SourceTableRecord str1 = new SourceTableRecord(session, match, Collections.singletonList("str1"));
		SourceTableRecord str2 = new SourceTableRecord(session, match, Collections.singletonList("str2"));
		pmr = new PotentialMatchRecord(ruleSet, MatchType.UNMATCH, str1, str2, false);
		pool.addPotentialMatch(pmr);
	}
	
	public void testDirtyAfterPropertyChange() {
		pmr.setStoreState(StoreState.CLEAN);
		pmr.setMatchStatus(MatchType.AUTOMATCH);
		pmr.setMatchStatus(MatchType.NOMATCH);
		assertSame(StoreState.DIRTY, pmr.getStoreState());
		pmr.setStoreState(StoreState.CLEAN);
		pmr.setMaster(pmr.getOriginalLhs());
		pmr.setMaster(pmr.getOriginalRhs());
		assertSame(StoreState.DIRTY, pmr.getStoreState());
	}
	
	public void testAlwaysNewAfterPropertyChange() {
		pmr.setStoreState(StoreState.NEW);
		pmr.setMatchStatus(MatchType.AUTOMATCH);
		pmr.setMatchStatus(MatchType.NOMATCH);
		assertSame(StoreState.NEW, pmr.getStoreState());
		pmr.setStoreState(StoreState.NEW);
		pmr.setMaster(pmr.getOriginalLhs());
		pmr.setMaster(pmr.getOriginalRhs());
		assertSame(StoreState.NEW, pmr.getStoreState());
	}
}
