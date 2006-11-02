package ca.sqlpower.matchmaker.hibernate;

// Generated Sep 18, 2006 4:34:38 PM by Hibernate Tools 3.2.0.beta7

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * PlMatchCriterion generated by hbm2java, but maintained by hand
 */
public class PlMatchCriterion extends DefaultHibernateObject<PlMatchCriterion> implements Serializable,
		Comparable<PlMatchCriterion> {

	// Fields

	private Long id;

	private String columnName;

	private PlMatchGroup plMatchGroup;

	/**
	 * True if the search should be case insensitive False if the search should
	 * be case sensitive
	 */
	private boolean caseSensitiveInd;

	private String suppressChar;

	private boolean soundInd;

	private Long firstNChar;

	private Date lastUpdateDate;

	private String lastUpdateUser;

	private BigDecimal seqNo;

	private boolean matchStart;

	private boolean matchEnd;

	private BigDecimal varianceAmt;

	private String varianceType;

	private String lastUpdateOsUser;

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

	// Constructors

	/** default constructor */
	public PlMatchCriterion() {
	}

	public PlMatchGroup getPlMatchGroup() {
		return this.plMatchGroup;
	}

	public void setPlMatchGroup(PlMatchGroup plMatchGroup) {
		this.plMatchGroup = plMatchGroup;
	}

	public boolean isCaseSensitiveInd() {
		return this.caseSensitiveInd;
	}

	public void setCaseSensitiveInd(boolean caseSensitiveInd) {
		this.caseSensitiveInd = caseSensitiveInd;
	}

	public String getSuppressChar() {
		return this.suppressChar;
	}

	public boolean isSoundInd() {
		return this.soundInd;
	}

	public Long getFirstNChar() {
		return this.firstNChar;
	}

	public Date getLastUpdateDate() {
		return this.lastUpdateDate;
	}

	public String getLastUpdateUser() {
		return this.lastUpdateUser;
	}

	public BigDecimal getSeqNo() {
		return this.seqNo;
	}

	public boolean isMatchStart() {
		return this.matchStart;
	}

	public boolean isMatchEnd() {
		return this.matchEnd;
	}

	public BigDecimal getVarianceAmt() {
		return this.varianceAmt;
	}

	public String getVarianceType() {
		return this.varianceType;
	}

	public String getLastUpdateOsUser() {
		return this.lastUpdateOsUser;
	}

	public boolean isAllowNullInd() {
		return this.allowNullInd;
	}


	public PlMatchTranslateGroup getTranslateGroup() {
		return translateGroup;
	}

	public void setTranslateGroup(PlMatchTranslateGroup translateGroup) {
		if (this.translateGroup != translateGroup) {
			firePropertyChange("translateGroup", this.translateGroup, translateGroup);
			this.translateGroup = translateGroup;
		}
	}

	public boolean isRemoveSpecialChars() {
		return this.removeSpecialChars;
	}

	public boolean isCountWordsInd() {
		return this.countWordsInd;
	}

	public boolean isReplaceWithSpaceInd() {
		return this.replaceWithSpaceInd;
	}

	public String getReplaceWithSpace() {
		return this.replaceWithSpace;
	}

	public boolean isReorderInd() {
		return this.reorderInd;
	}

	public boolean isFirstNCharByWordInd() {
		return this.firstNCharByWordInd;
	}

	public Long getFirstNCharByWord() {
		return this.firstNCharByWord;
	}

	public Long getMinWordsInCommon() {
		return this.minWordsInCommon;
	}

	public Long getWordsInCommonNumWords() {
		return this.wordsInCommonNumWords;
	}

	public boolean isMatchFirstPlusOneInd() {
		return this.matchFirstPlusOneInd;
	}
	
	public PlMatch getMatch(){
		if (plMatchGroup == null) return null;
		return plMatchGroup.getPlMatch();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		final PlMatchCriterion other = (PlMatchCriterion) obj;
		if (columnName == null) {
			if (other.columnName != null)
				return false;
		} else if (!columnName.equals(other.columnName))
			return false;
		if (plMatchGroup == null) {
			if (other.plMatchGroup != null)
				return false;
		} else if (!plMatchGroup.equals(other.plMatchGroup))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result=17;
		result = PRIME * result + ((columnName == null) ? 0 : columnName.hashCode());
		result = PRIME * result + ((plMatchGroup == null) ? 0 : plMatchGroup.hashCode());
		return result;
	}

	public int compareTo(PlMatchCriterion o) {
		if (this.equals(o))	return 0;
		PlMatchCriterion other = (PlMatchCriterion) o;
		if (plMatchGroup.compareTo(other.plMatchGroup) != 0) {
			return plMatchGroup.compareTo(other.plMatchGroup);
		} else if (getColumnName() != null) {
			if (other.getColumnName() == null) return -1;
			return getColumnName().compareTo(other.getColumnName());
		} else {
			return other.getColumnName() == null ? 0 : 1;
		}
	}


	public void setCountWordsInd(boolean countWordsInd) {
		if (this.countWordsInd != countWordsInd) {
			this.countWordsInd = countWordsInd;
			firePropertyChange("this.countWordsInd", this.countWordsInd, countWordsInd);
		}
	}

	public void setFirstNChar(Long firstNChar) {
		if (this.firstNChar != firstNChar) {
			this.firstNChar = firstNChar;
			firePropertyChange("this.firstNChar", this.firstNChar, firstNChar);
		}
	}

	public void setFirstNCharByWord(Long firstNCharByWord) {
		if (this.firstNCharByWord != firstNCharByWord) {
			this.firstNCharByWord = firstNCharByWord;
			firePropertyChange("this.firstNCharByWord", this.firstNCharByWord, firstNCharByWord);
		}
	}

	public void setFirstNCharByWordInd(boolean firstNCharByWordInd) {
		if (this.firstNCharByWordInd != firstNCharByWordInd) {
			this.firstNCharByWordInd = firstNCharByWordInd;
			firePropertyChange("this.firstNCharByWordInd", this.firstNCharByWordInd, firstNCharByWordInd);
		}
	}

	public void setLastUpdateDate(Date lastUpdateDate) {
		if (this.lastUpdateDate != lastUpdateDate) {
			this.lastUpdateDate = lastUpdateDate;
			firePropertyChange("this.lastUpdateDate", this.lastUpdateDate, lastUpdateDate);
		}
	}

	public void setLastUpdateOsUser(String lastUpdateOsUser) {
		if (this.lastUpdateOsUser != lastUpdateOsUser) {
			this.lastUpdateOsUser = lastUpdateOsUser;
			firePropertyChange("this.lastUpdateOsUser", this.lastUpdateOsUser, lastUpdateOsUser);
		}
	}

	public void setLastUpdateUser(String lastUpdateUser) {
		if (this.lastUpdateUser != lastUpdateUser) {
			this.lastUpdateUser = lastUpdateUser;
			firePropertyChange("this.lastUpdateUser", this.lastUpdateUser, lastUpdateUser);
		}
	}

	public void setMatchEnd(boolean matchEnd) {
		if (this.matchEnd != matchEnd) {
			this.matchEnd = matchEnd;
			firePropertyChange("this.matchEnd", this.matchEnd, matchEnd);
		}
	}

	public void setMatchFirstPlusOneInd(boolean matchFirstPlusOneInd) {
		if (this.matchFirstPlusOneInd != matchFirstPlusOneInd) {
			this.matchFirstPlusOneInd = matchFirstPlusOneInd;
			firePropertyChange("this.matchFirstPlusOneInd", this.matchFirstPlusOneInd, matchFirstPlusOneInd);
		}
	}

	public void setMatchStart(boolean matchStart) {
		if (this.matchStart != matchStart) {
			this.matchStart = matchStart;
			firePropertyChange("this.matchStart", this.matchStart, matchStart);
		}
	}

	public void setMinWordsInCommon(Long minWordsInCommon) {
		if (this.minWordsInCommon != minWordsInCommon) {
			this.minWordsInCommon = minWordsInCommon;
			firePropertyChange("this.minWordsInCommon", this.minWordsInCommon, minWordsInCommon);
		}
	}

	public void setRemoveSpecialChars(boolean removeSpecialChars) {
		if (this.removeSpecialChars != removeSpecialChars) {
			this.removeSpecialChars = removeSpecialChars;
			firePropertyChange("this.removeSpecialChars", this.removeSpecialChars, removeSpecialChars);
		}
	}

	public void setReorderInd(boolean reorderInd) {
		if (this.reorderInd != reorderInd) {
			this.reorderInd = reorderInd;
			firePropertyChange("this.reorderInd", this.reorderInd, reorderInd);
		}
	}

	public void setReplaceWithSpace(String replaceWithSpace) {
		if (this.replaceWithSpace != replaceWithSpace) {
			this.replaceWithSpace = replaceWithSpace;
			firePropertyChange("this.replaceWithSpace", this.replaceWithSpace, replaceWithSpace);
		}
	}

	public void setReplaceWithSpaceInd(boolean replaceWithSpaceInd) {
		if (this.replaceWithSpaceInd != replaceWithSpaceInd) {
			this.replaceWithSpaceInd = replaceWithSpaceInd;
			firePropertyChange("this.replaceWithSpaceInd", this.replaceWithSpaceInd, replaceWithSpaceInd);
		}
	}

	public void setSeqNo(BigDecimal seqNo) {
		if (this.seqNo != seqNo) {
			this.seqNo = seqNo;
			firePropertyChange("this.seqNo", this.seqNo, seqNo);
		}
	}

	public void setSoundInd(boolean soundInd) {
		if (this.soundInd != soundInd) {
			this.soundInd = soundInd;
			firePropertyChange("this.soundInd", this.soundInd, soundInd);
		}
	}

	public void setSuppressChar(String suppressChar) {
		if (this.suppressChar != suppressChar) {
			this.suppressChar = suppressChar;
			firePropertyChange("this.suppressChar", this.suppressChar, suppressChar);
		}
	}



	public void setVarianceAmt(BigDecimal varianceAmt) {
		if (this.varianceAmt != varianceAmt) {
			this.varianceAmt = varianceAmt;
			firePropertyChange("this.varianceAmt", this.varianceAmt, varianceAmt);
		}
	}

	public void setVarianceType(String varianceType) {
		if (this.varianceType != varianceType) {
			this.varianceType = varianceType;
			firePropertyChange("this.varianceType", this.varianceType, varianceType);
		}
	}

	public void setWordsInCommonNumWords(Long wordsInCommonNumWords) {
		if (this.wordsInCommonNumWords != wordsInCommonNumWords) {
			this.wordsInCommonNumWords = wordsInCommonNumWords;
			firePropertyChange("this.wordsInCommonNumWords", this.wordsInCommonNumWords, wordsInCommonNumWords);
		}
	}

	public void setAllowNullInd(boolean allowNullInd) {
		if (this.allowNullInd != allowNullInd) {
			this.allowNullInd = allowNullInd;
			firePropertyChange("this.allowNullInd", this.allowNullInd, allowNullInd);
		}
	}

	public void setCaseSensitiveInd(String val) {
		if (val != null && val.length() > 0 && !val.equalsIgnoreCase("null")) {
			setCaseSensitiveInd(val.charAt(0) == 'y' || val.charAt(0) == 'Y');
		}
	}

	public void setSoundInd(String val) {
		if (val != null && val.length() > 0 && !val.equalsIgnoreCase("null")) {
			setSoundInd(val.charAt(0) == 'y' || val.charAt(0) == 'Y');
		}
	}

	public void setMatchStart(String val) {
		if (val != null && val.length() > 0 && !val.equalsIgnoreCase("null")) {
			setMatchStart(val.charAt(0) == 'y' || val.charAt(0) == 'Y');
		}
	}

	public void setMatchEnd(String val) {
		if (val != null && val.length() > 0 && !val.equalsIgnoreCase("null")) {
			setMatchEnd(val.charAt(0) == 'y' || val.charAt(0) == 'Y');
		}
	}

	public void setAllowNullInd(String val) {
		if (val != null && val.length() > 0 && !val.equalsIgnoreCase("null")) {
			setAllowNullInd(val.charAt(0) == 'y' || val.charAt(0) == 'Y');
		}
	}

	public void setRemoveSpecialChars(String val) {
		if (val != null && val.length() > 0 && !val.equalsIgnoreCase("null")) {
			setRemoveSpecialChars(val.charAt(0) == 'y' || val.charAt(0) == 'Y');
		}
	}

	public void setCountWordsInd(String val) {
		if (val != null && val.length() > 0 && !val.equalsIgnoreCase("null")) {
			setCountWordsInd(val.charAt(0) == 'y' || val.charAt(0) == 'Y');
		}
	}

	public void setReplaceWithSpaceInd(String val) {
		if (val != null && val.length() > 0 && !val.equalsIgnoreCase("null")) {
			setReplaceWithSpaceInd(val.charAt(0) == 'y' || val.charAt(0) == 'Y');
		}
	}

	public void setReorderInd(String val) {
		if (val != null && val.length() > 0 && !val.equalsIgnoreCase("null")) {
			setReorderInd(val.charAt(0) == 'y' || val.charAt(0) == 'Y');
		}
	}

	public void setFirstNCharByWordInd(String val) {
		if (val != null && val.length() > 0 && !val.equalsIgnoreCase("null")) {
			setFirstNCharByWordInd(val.charAt(0) == 'y' || val.charAt(0) == 'Y');
		}
	}

	public void setMatchFirstPlusOneInd(String val) {
		if (val != null && val.length() > 0 && !val.equalsIgnoreCase("null")) {
			setMatchFirstPlusOneInd(val.charAt(0) == 'y' || val.charAt(0) == 'Y');
		}
	}

	public void setFirstNChar(String val) {
		if (val != null && val.length() > 0 && !val.equalsIgnoreCase("null")) {
			setFirstNChar(Long.valueOf(val));
		}
	}

	public void setFirstNCharByWord(String val) {
		if (val != null && val.length() > 0 && !val.equalsIgnoreCase("null")) {
			setFirstNCharByWord(Long.valueOf(val));
		}
	}

	public void setMinWordsInCommon(String val) {
		if (val != null && val.length() > 0 && !val.equalsIgnoreCase("null")) {
			setMinWordsInCommon(Long.valueOf(val));
		}
	}

	public void setWordsInCommonNumWords(String val) {
		if (val != null && val.length() > 0 && !val.equalsIgnoreCase("null")) {
			setWordsInCommonNumWords(Long.valueOf(val));
		}
	}

	public void setLastUpdateDate(String val) throws ParseException {
		if (val != null && val.length() > 0 && !val.equalsIgnoreCase("null")) {
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			setLastUpdateDate(df.parse(val));
		}
	}

	public void setSeqNo(String val) {
		if (val != null && val.length() > 0 && !val.equalsIgnoreCase("null")) {
			setSeqNo(BigDecimal.valueOf(Long.valueOf(val)));
		}
	}

	public void setVarianceAmt(String val) {
		if (val != null && val.length() > 0 && !val.equalsIgnoreCase("null")) {
			setVarianceAmt(BigDecimal.valueOf(Long.valueOf(val)));
		}
	}

	public void setId(Long id) {
		if (this.id != id) {
			this.id = id;
			firePropertyChange("id", this.id, id);
		}
	}

	public String getColumnName() {
		return columnName;
	}

	public Long getId() {
		return id;
	}

	public void setColumnName(String columnName) {
		if (this.columnName != columnName) {
			this.columnName = columnName;
			firePropertyChange("columnName", this.columnName, columnName);
		}
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString() + " " + columnName;
	}
}
