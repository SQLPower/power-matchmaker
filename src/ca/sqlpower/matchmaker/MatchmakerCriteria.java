package ca.sqlpower.matchmaker;

import java.math.BigDecimal;

import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.matchmaker.hibernate.PlMatchTranslateGroup;

public class MatchmakerCriteria extends AbstractMatchMakerObject<MatchmakerCriteria> {

	private SQLColumn column;

	/**
	 * True if the search should be case insensitive False if the search should
	 * be case sensitive
	 */
	private boolean caseSensitiveInd;

	private String suppressChar;

	private boolean soundInd;

	private Long firstNChar;

	private BigDecimal seqNo;

	private boolean matchStart;

	private boolean matchEnd;

	private BigDecimal varianceAmt;

	private String varianceType;

	private boolean allowNullInd;

	private PlMatchTranslateGroup translateGroup;

	private boolean removeSpecialChars;

	private boolean countWordsInd;

	private boolean replaceWithSpaceInd;

	private String replaceWithSpace;

	private boolean reorderInd;

	private boolean firstNCharByWordInd;

	private Long firstNCharByWord;

	private Long minWordsInCommon;

	private Long wordsInCommonNumWords;

	private boolean matchFirstPlusOneInd;
	
	
	public MatchmakerCriteria(String appUserName) {
		super(appUserName);
	}

	@Override
	public void addChild(MatchmakerCriteria child) {
		throw new IllegalStateException("MatchMaker Criteria does NOT allow child!");
	}
	
	
}
