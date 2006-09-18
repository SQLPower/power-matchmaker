package ca.sqlpower.matchmaker;

import java.util.Date;

import ca.sqlpower.persistance.CatNap;


public class MatchCriteria{
	//Keys
	private String matchId;
	private String groupId;
	private String columnName;

	// Non-keys
	private boolean caseSensitiveInd;
	private String suppressChar;
	private boolean soundInd;
	private long firstNChar;
	private Date lastUpdateDate;
	private String lastUpdateUser;
	private long seqNo;
	private String matchStart;
	private String matchEnd;
	private String dateFormat;
	private long varianceAmt;
	private String varianceType;
	private String lastUpdateOsUser;
	private boolean allowNullInd;
	private String translateGroupName;
	private boolean translateInd;
	private boolean purgeInd;
	private String removeSpecialChars;
	private boolean reorderInd;
	private boolean firstNCharByWordInd;
	private long firstNCharByWord;
	private boolean replaceWithSpaceInd;
	private String replaceWithSpace;
	private long minWordsInCommon;
	private boolean matchFirstPlusOneInd;
	private long wordsInCommonNumWords;

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + ((columnName == null) ? 0 : columnName.hashCode());
		result = PRIME * result + ((groupId == null) ? 0 : groupId.hashCode());
		result = PRIME * result + ((matchId == null) ? 0 : matchId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final MatchCriteria other = (MatchCriteria) obj;
		if (columnName == null) {
			if (other.columnName != null)
				return false;
		} else if (!columnName.equals(other.columnName))
			return false;
		if (groupId == null) {
			if (other.groupId != null)
				return false;
		} else if (!groupId.equals(other.groupId))
			return false;
		if (matchId == null) {
			if (other.matchId != null)
				return false;
		} else if (!matchId.equals(other.matchId))
			return false;
		return true;
	}

	public boolean isAllowNullInd() {
		return allowNullInd;
	}
	public void setAllowNullInd(boolean allowNullInd) {
		this.allowNullInd = allowNullInd;
	}

	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public String getDateFormat() {
		return dateFormat;
	}
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}
	public long getFirstNChar() {
		return firstNChar;
	}
	public void setFirstNChar(long firstNChar) {
		this.firstNChar = firstNChar;
	}
	public long getFirstNCharByWord() {
		return firstNCharByWord;
	}
	public void setFirstNCharByWord(long firstNCharByWord) {
		this.firstNCharByWord = firstNCharByWord;
	}
	public boolean isFirstNCharByWordInd() {
		return firstNCharByWordInd;
	}
	public void setFirstNCharByWordInd(boolean firstNCharByWordInd) {
		this.firstNCharByWordInd = firstNCharByWordInd;
	}
	public String getGroupId() {
		return groupId;
	}
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	public Date getLastUpdateDate() {
		return lastUpdateDate;
	}
	public void setLastUpdateDate(Date lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}
	public String getLastUpdateOsUser() {
		return lastUpdateOsUser;
	}
	public void setLastUpdateOsUser(String lastUpdateOsUser) {
		this.lastUpdateOsUser = lastUpdateOsUser;
	}
	public String getLastUpdateUser() {
		return lastUpdateUser;
	}
	public void setLastUpdateUser(String lastUpdateUser) {
		this.lastUpdateUser = lastUpdateUser;
	}
	public String getMatchEnd() {
		return matchEnd;
	}
	public void setMatchEnd(String matchEnd) {
		this.matchEnd = matchEnd;
	}
	public boolean isMatchFirstPlusOneInd() {
		return matchFirstPlusOneInd;
	}
	public void setMatchFirstPlusOneInd(boolean matchFirstPlusOneInd) {
		this.matchFirstPlusOneInd = matchFirstPlusOneInd;
	}
	public String getMatchId() {
		return matchId;
	}
	public void setMatchId(String matchId) {
		this.matchId = matchId;
	}
	public String getMatchStart() {
		return matchStart;
	}
	public void setMatchStart(String matchStart) {
		this.matchStart = matchStart;
	}
	public long getMinWordsInCommon() {
		return minWordsInCommon;
	}
	public void setMinWordsInCommon(long minWordsInCommon) {
		this.minWordsInCommon = minWordsInCommon;
	}
	public boolean isPurgeInd() {
		return purgeInd;
	}
	public void setPurgeInd(boolean purgeInd) {
		this.purgeInd = purgeInd;
	}
	public String getRemoveSpecialChars() {
		return removeSpecialChars;
	}
	public void setRemoveSpecialChars(String removeSpecialChars) {
		this.removeSpecialChars = removeSpecialChars;
	}
	public boolean isReorderInd() {
		return reorderInd;
	}
	public void setReorderInd(boolean reorderInd) {
		this.reorderInd = reorderInd;
	}
	public String getReplaceWithSpace() {
		return replaceWithSpace;
	}
	public void setReplaceWithSpace(String replaceWithSpace) {
		this.replaceWithSpace = replaceWithSpace;
	}
	public boolean isReplaceWithSpaceInd() {
		return replaceWithSpaceInd;
	}
	public void setReplaceWithSpaceInd(boolean replaceWithSpaceInd) {
		this.replaceWithSpaceInd = replaceWithSpaceInd;
	}
	public long getSeqNo() {
		return seqNo;
	}
	public void setSeqNo(long seqNo) {
		this.seqNo = seqNo;
	}
	public boolean isSoundInd() {
		return soundInd;
	}
	public void setSoundInd(boolean soundInd) {
		this.soundInd = soundInd;
	}
	public String getSuppressChar() {
		return suppressChar;
	}
	public void setSuppressChar(String suppressChar) {
		this.suppressChar = suppressChar;
	}
	public String getTranslateGroupName() {
		return translateGroupName;
	}
	public void setTranslateGroupName(String translateGroupName) {
		this.translateGroupName = translateGroupName;
	}
	public boolean isTranslateInd() {
		return translateInd;
	}
	public void setTranslateInd(boolean translateInd) {
		this.translateInd = translateInd;
	}
	public long getVarianceAmt() {
		return varianceAmt;
	}
	public void setVarianceAmt(long varianceAmt) {
		this.varianceAmt = varianceAmt;
	}
	public String getVarianceType() {
		return varianceType;
	}
	public void setVarianceType(String varianceType) {
		this.varianceType = varianceType;
	}
	public long getWordsInCommonNumWords() {
		return wordsInCommonNumWords;
	}
	public void setWordsInCommonNumWords(long wordsInCommonNumWords) {
		this.wordsInCommonNumWords = wordsInCommonNumWords;
	}

}
