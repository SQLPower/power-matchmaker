package ca.sqlpower.matchmaker;

import java.math.BigDecimal;

import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.matchmaker.hibernate.PlMatchTranslateGroup;

public class MatchMakerCriteriaTest extends MatchMakerTestCase<MatchmakerCriteria> {

	private MatchmakerCriteria target;
	final String appUserName = "Test User";

	protected void setUp() throws Exception {
		super.setUp();
		target = new MatchmakerCriteria(appUserName);
	}

	@Override
	protected MatchmakerCriteria getTarget() {
		return target;
	}

	private void checkNull() {
		assertNull("The default last_update_user in match object should be null",
				target.getLastUpdateAppUser());
	}

	private void checkAppUserName() {
		assertEquals("The last_update_user should be [" +
				appUserName +"], because user1 has changed this match object",
				appUserName, target.getLastUpdateAppUser());
	}

	public void testSetAllowNullInd() {
		checkNull();
		target.setAllowNullInd(true);
		checkAppUserName();
	}

	public void testSetCaseSensitiveInd() {
		checkNull();
		target.setCaseSensitiveInd(true);
		checkAppUserName();
	}

	public void testSetColumn() {
		checkNull();
		target.setColumn(new SQLColumn());
		checkAppUserName();
	}

	public void testSetCountWordsInd() {
		checkNull();
		target.setCountWordsInd(true);
		checkAppUserName();
	}

	public void testSetFirstNChar() {
		checkNull();
		target.setFirstNChar(new Long(100L));
		checkAppUserName();
	}

	public void testSetFirstNCharByWord() {
		checkNull();
		target.setFirstNCharByWord(new Long(200L));
		checkAppUserName();
	}

	public void testSetFirstNCharByWordInd() {
		checkNull();
		target.setFirstNCharByWordInd(true);
		checkAppUserName();
	}

	public void testSetMatchEnd() {
		checkNull();
		target.setMatchEnd(true);
		checkAppUserName();
	}

	public void testSetMatchFirstPlusOneInd() {
		checkNull();
		target.setMatchFirstPlusOneInd(true);
		checkAppUserName();
	}

	public void testSetMatchStart() {
		checkNull();
		target.setMatchStart(true);
		checkAppUserName();
	}

	public void testSetMinWordsInCommon() {
		checkNull();
		target.setMinWordsInCommon(new Long(121L));
		checkAppUserName();
	}

	public void testSetRemoveSpecialChars() {
		checkNull();
		target.setRemoveSpecialChars(true);
		checkAppUserName();
	}

	public void testSetReorderInd() {
		checkNull();
		target.setReorderInd(true);
		checkAppUserName();
	}

	public void testSetReplaceWithSpace() {
		checkNull();
		target.setReplaceWithSpace("A");
		checkAppUserName();
	}

	public void testSetReplaceWithSpaceInd() {
		checkNull();
		target.setReplaceWithSpaceInd(true);
		checkAppUserName();
	}

	public void testSetSeqNo() {
		checkNull();
		target.setSeqNo(new BigDecimal(100));
		checkAppUserName();
	}

	public void testSetSoundInd() {
		checkNull();
		target.setSoundInd(true);
		checkAppUserName();
	}

	public void testSetSuppressChar() {
		checkNull();
		target.setSuppressChar("xxx");
		checkAppUserName();
	}

	public void testSetTranslateGroup() {
		checkNull();
		target.setTranslateGroup(new PlMatchTranslateGroup());
		checkAppUserName();
	}

	public void testSetVarianceAmt() {
		checkNull();
		target.setVarianceAmt(new BigDecimal(98));
		checkAppUserName();
	}

	public void testSetVarianceType() {
		checkNull();
		target.setVarianceType("xx");
		checkAppUserName();
	}

	public void testSetWordsInCommonNumWords() {
		checkNull();
		target.setWordsInCommonNumWords(new Long(789));
		checkAppUserName();
	}

	public void testAddChild() {
		try {
			target.addChild(new MatchmakerCriteria(appUserName));
			fail("MatchMakerCriteria class does not allow child!");
		} catch ( IllegalStateException e ) {
			// this is what we want
		}
	}



}
