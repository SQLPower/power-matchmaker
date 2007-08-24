/*
 * Copyright (c) 2007, SQL Power Group Inc.
 *
 * This file is part of Power*MatchMaker.
 *
 * Power*MatchMaker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*MatchMaker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.i 
 */

package ca.sqlpower.matchmaker;

import java.math.BigDecimal;

import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;

public class MatchMakerCriteria 
    extends AbstractMatchMakerObject<MatchMakerCriteria, MatchMakerObject> {

	
	public class MatchMakerCriteriaCachableTable extends CachableColumn {
		public MatchMakerCriteriaCachableTable() {
			super(MatchMakerCriteria.this, "column");
		}
		
		public SQLTable getTable() {
			
			MatchMakerCriteriaGroup group = (MatchMakerCriteriaGroup) eventSource.getParent();
	        if (group == null) throw new NullPointerException("Not attached to a parent");
	        Match match = (Match) group.getParentMatch();
	        if (group == null) throw new NullPointerException("Not attached to a grandparent");
	        SQLTable st = match.getSourceTable();
			return st;
		}

	}
	
    /**
     * Unique ID for this instance. Required by ORM tools.
     */
    @SuppressWarnings("unused")
    private Long oid;

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

    private MatchMakerTranslateGroup translateGroup;

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


    private MatchMakerCriteriaCachableTable cachableTable = new MatchMakerCriteriaCachableTable();
    
    public MatchMakerCriteria( ) {
    }

    /**
     * Overridden to narrow the return type.
     */
    @Override
    public MatchMakerCriteriaGroup getParent() {
        return (MatchMakerCriteriaGroup) super.getParent();
    }
    
    /**
     * Overridden because match criteria don't really have names.  This
     * method returns the column name which this set of criteria is associated with.
     */
    @Override
    public String getName() {
        return getColumnName();
    }
    
    public boolean isAllowNullInd() {
        return allowNullInd;
    }

    public boolean isCaseSensitiveInd() {
        return caseSensitiveInd;
    }



    public boolean isCountWordsInd() {
        return countWordsInd;
    }


    public Long getFirstNChar() {
        return firstNChar;
    }


    public Long getFirstNCharByWord() {
        return firstNCharByWord;
    }


    public boolean isFirstNCharByWordInd() {
        return firstNCharByWordInd;
    }


    public boolean isMatchEnd() {
        return matchEnd;
    }


    public boolean isMatchFirstPlusOneInd() {
        return matchFirstPlusOneInd;
    }


    public boolean isMatchStart() {
        return matchStart;
    }


    public Long getMinWordsInCommon() {
        return minWordsInCommon;
    }


    public boolean isRemoveSpecialChars() {
        return removeSpecialChars;
    }


    public boolean isReorderInd() {
        return reorderInd;
    }


    public String getReplaceWithSpace() {
        return replaceWithSpace;
    }


    public boolean isReplaceWithSpaceInd() {
        return replaceWithSpaceInd;
    }


    public BigDecimal getSeqNo() {
        return seqNo;
    }


    public boolean isSoundInd() {
        return soundInd;
    }


    public String getSuppressChar() {
        return suppressChar;
    }


    public MatchMakerTranslateGroup getTranslateGroup() {
        return translateGroup;
    }


    public BigDecimal getVarianceAmt() {
        return varianceAmt;
    }


    public String getVarianceType() {
        return varianceType;
    }


    public Long getWordsInCommonNumWords() {
        return wordsInCommonNumWords;
    }

    public void setAllowNullInd(boolean allowNullInd) {
        boolean oldVal = this.allowNullInd;
        this.allowNullInd = allowNullInd;
        getEventSupport().firePropertyChange("allowNullInd", oldVal, allowNullInd);
    }


    public void setCountWordsInd(boolean countWordsInd) {
        boolean oldVal = this.countWordsInd;
        this.countWordsInd = countWordsInd;
        getEventSupport().firePropertyChange("countWordsInd", oldVal, countWordsInd);
    }


    public void setFirstNChar(Long firstNChar) {
        Long oldVal = this.firstNChar;
        this.firstNChar = firstNChar;
        getEventSupport().firePropertyChange("firstNChar", oldVal, firstNChar);
    }


    public void setFirstNCharByWord(Long firstNCharByWord) {
        Long oldVal = this.firstNCharByWord;
        this.firstNCharByWord = firstNCharByWord;
        getEventSupport().firePropertyChange("firstNCharByWord", oldVal, firstNCharByWord);
    }


    public void setFirstNCharByWordInd(boolean firstNCharByWordInd) {
        boolean oldVal = this.firstNCharByWordInd;
        this.firstNCharByWordInd = firstNCharByWordInd;
        getEventSupport().firePropertyChange("firstNCharByWordInd", oldVal, firstNCharByWordInd);
    }


    public void setMatchEnd(boolean matchEnd) {
        boolean oldVal = this.matchEnd;
        this.matchEnd = matchEnd;
        getEventSupport().firePropertyChange("matchEnd", oldVal, matchEnd);
    }


    public void setMatchFirstPlusOneInd(boolean matchFirstPlusOneInd) {
        boolean oldVal = this.matchFirstPlusOneInd;
        this.matchFirstPlusOneInd = matchFirstPlusOneInd;
        getEventSupport().firePropertyChange("matchFirstPlusOneInd", oldVal, matchFirstPlusOneInd);
    }


    public void setMatchStart(boolean matchStart) {
        boolean oldVal = this.matchStart;
        this.matchStart = matchStart;
        getEventSupport().firePropertyChange("matchStart", oldVal, matchStart);
    }


    public void setMinWordsInCommon(Long minWordsInCommon) {
        Long oldVal = this.minWordsInCommon;
        this.minWordsInCommon = minWordsInCommon;
        getEventSupport().firePropertyChange("minWordsInCommon", oldVal, minWordsInCommon);
    }


    public void setRemoveSpecialChars(boolean removeSpecialChars) {
        boolean oldVal = this.removeSpecialChars;
        this.removeSpecialChars = removeSpecialChars;
        getEventSupport().firePropertyChange("removeSpecialChars", oldVal, removeSpecialChars);
    }


    public void setReorderInd(boolean reorderInd) {
        boolean oldVal = this.reorderInd;
        this.reorderInd = reorderInd;
        getEventSupport().firePropertyChange("reorderInd", oldVal, reorderInd);
    }


    public void setReplaceWithSpace(String replaceWithSpace) {
        String oldVal = this.replaceWithSpace;
        this.replaceWithSpace = replaceWithSpace;
        getEventSupport().firePropertyChange("replaceWithSpace", oldVal, replaceWithSpace);
    }


    public void setReplaceWithSpaceInd(boolean replaceWithSpaceInd) {
        boolean oldVal = this.replaceWithSpaceInd;
        this.replaceWithSpaceInd = replaceWithSpaceInd;
        getEventSupport().firePropertyChange("replaceWithSpaceInd", oldVal, replaceWithSpaceInd);
    }


    public void setSeqNo(BigDecimal seqNo) {
        BigDecimal oldVal = this.seqNo;
        this.seqNo = seqNo;
        getEventSupport().firePropertyChange("seqNo", oldVal, seqNo);
    }


    public void setSoundInd(boolean soundInd) {
        boolean oldVal = this.soundInd;
        this.soundInd = soundInd;
        getEventSupport().firePropertyChange("soundInd", oldVal, soundInd);
    }


    public void setSuppressChar(String suppressChar) {
        String oldVal = this.suppressChar;
        this.suppressChar = suppressChar;
        getEventSupport().firePropertyChange("suppressChar", oldVal, suppressChar);
    }


    public void setTranslateGroup(MatchMakerTranslateGroup translateGroup) {
        MatchMakerTranslateGroup oldVal = this.translateGroup;
        this.translateGroup = translateGroup;
        getEventSupport().firePropertyChange("translateGroup", oldVal, translateGroup);
    }


    public void setVarianceAmt(BigDecimal varianceAmt) {
        BigDecimal oldVal = this.varianceAmt;
        this.varianceAmt = varianceAmt;
        getEventSupport().firePropertyChange("varianceAmt", oldVal, varianceAmt);
    }


    public void setVarianceType(String varianceType) {
        String oldVal = this.varianceType;
        this.varianceType = varianceType;
        getEventSupport().firePropertyChange("varianceType", oldVal, varianceType);
    }


    public void setWordsInCommonNumWords(Long wordsInCommonNumWords) {
        Long oldVal = this.wordsInCommonNumWords;
        this.wordsInCommonNumWords = wordsInCommonNumWords;
        getEventSupport().firePropertyChange("wordsInCommonNumWords", oldVal, wordsInCommonNumWords);
    }


    public void setCaseSensitiveInd(boolean caseSensitiveInd) {
        boolean oldVal = this.caseSensitiveInd;
        this.caseSensitiveInd = caseSensitiveInd;
        getEventSupport().firePropertyChange("caseSensitiveInd", oldVal, caseSensitiveInd);
    }

    @Override
    public boolean allowsChildren() {
        return false;
    }

    @Override
    public void addChild(MatchMakerObject child) {
        throw new IllegalStateException("MatchMakerCriteria class does NOT allow child!");
    }

    @Override
    public String toString() {
        return "MatchMakerCriteria for "+getColumnName();
    }
    
    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 0;
        result = PRIME * result + ((oid == null) ? 0 : oid.hashCode());
        result = PRIME * result + ((getColumnName() == null) ? 0 : getColumnName().hashCode());

        return result;
    }


    /**
     * Compares this MatchMakerCriteria to another one.  Equality is defined according
     * to the following criteria (all must be true for the given object to be considered
     * equal to this one):
     * 
     * <ul>
     *  <li>The other object is also a MatchMakerCriteria (hence not null)
     *  <li>The other object has the same oid, or both instances are oidless
     *  <li>The other object references a column having the same name as this one
     * </ul>
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof MatchMakerCriteria)) return false;
        final MatchMakerCriteria other = (MatchMakerCriteria) obj;
        
        boolean same;
        
        same = (oid == null ? other.oid == null : oid.equals(other.oid));
        if (!same) return false;
        
        // SQLColumn.equals() is only reference equality, so this comparison is self-serve.
        String colName = getColumnName();
        String otherColName = other.getColumnName();

        // unfortunately, when using an Oracle backend, the ORM layer can't help but
        // give back empty strings when the actual original value was null. So we
        // have to consider them equivalent.
        if ("".equals(colName)) colName = null;
        if ("".equals(otherColName)) otherColName = null;

        same = (colName == null ? otherColName == null : colName.equals(otherColName));
        System.err.println("columns equal? \""+colName+"\" \""+otherColName+"\": "+same);
        if (!same) return false;

        return true;
    }

    /**
     * duplicate all criteria properties except parent and oid
     * @return new MatchmakerCriteria object with all original 
     * properties except parent and oid
     */
	public MatchMakerCriteria duplicate(MatchMakerObject parent,MatchMakerSession s){
		MatchMakerCriteria criteria = new MatchMakerCriteria();
		criteria.setAllowNullInd(isAllowNullInd());
		criteria.setCaseSensitiveInd(isCaseSensitiveInd());
		criteria.setColumn(getColumn());
		criteria.setColumnName(getColumnName());
		criteria.setCountWordsInd(isCountWordsInd());
		criteria.setFirstNChar(getFirstNChar());
		criteria.setFirstNCharByWord(getFirstNCharByWord());
		criteria.setFirstNCharByWordInd(isFirstNCharByWordInd());
		criteria.setMatchEnd(isMatchEnd());
		criteria.setMatchFirstPlusOneInd(isMatchFirstPlusOneInd());
		criteria.setMatchStart(isMatchStart());
		criteria.setMinWordsInCommon(getMinWordsInCommon());
		criteria.setName(getName());
		criteria.setRemoveSpecialChars(isRemoveSpecialChars());
		criteria.setReorderInd(isReorderInd());
		criteria.setReplaceWithSpace(getReplaceWithSpace());
		criteria.setReplaceWithSpaceInd(isReplaceWithSpaceInd());
		criteria.setSeqNo(getSeqNo());
		criteria.setSession(s);
		criteria.setSoundInd(isSoundInd());
		criteria.setSuppressChar(getSuppressChar());
		criteria.setTranslateGroup(getTranslateGroup());
		criteria.setVarianceAmt(getVarianceAmt());
		criteria.setVarianceType(getVarianceType());
		criteria.setWordsInCommonNumWords(getWordsInCommonNumWords());

		return criteria;
	}

	public SQLColumn getColumn() {
		return cachableTable.getColumn();
	}

	public String getColumnName() {
		return cachableTable.getColumnName();
	}

	public void setColumn(SQLColumn column) {
		cachableTable.setColumn(column);
	}

	public void setColumnName(String columnName) {
		cachableTable.setColumnName(columnName);
	}
}