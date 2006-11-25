package ca.sqlpower.matchmaker;

import java.math.BigDecimal;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.architect.SQLTable;

public class MatchMakerCriteria 
    extends AbstractMatchMakerObject<MatchMakerCriteria, MatchMakerObject> {

    /**
     * Unique ID for this instance. Required by ORM tools.
     */
    private Long oid;

    /**
     * The column that all criteria in this instance applies to.  There can only
     * be one instance of MatchMakerCriteria per column in a MatchMakerCriteriaGroup.
     * <p>
     * This cached version of the column is populated on demand in getColumn().
     */
    private SQLColumn cachedColumn;

    /**
     * The column name of the column in the match's source table that these
     * criteria refer to.
     */
    private String columnName;
    
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
        return (cachedColumn == null ? columnName : cachedColumn.getName());
    }
    
    public boolean isAllowNullInd() {
        return allowNullInd;
    }

    public boolean isCaseSensitiveInd() {
        return caseSensitiveInd;
    }

    /**
     * Returns the name of the column this set of criteria applies to.
     * You should use {@link #getColumn()} under normal circumstances.
     */
    public String getColumnName() throws ArchitectException {
        if (cachedColumn == null) return columnName;
        else return cachedColumn.getName();
    }

    /**
     * Sets the column name, and nulls out the cached SQLColumn.  The next
     * call to getColumn() will result in an attempt to resolve the SQLColumn
     * that this columnName string refers to.
     * <p>
     * Note, this property is not bound.  However, it is coordinated with the
     * bound property <tt>column</tt>, so setting the column name like this
     * may eventually result in a property change event for the "column" property.
     * 
     * @param columnName the name of the match's source table column these match
     * criteria are associated with.
     */
    public void setColumnName(String columnName) {
        cachedColumn = null;
        this.columnName = columnName;
    }

    /**
     * Attempts to resolve the given column name to a column of the owning
     * Match object's source table.  This functionality is provided for the benefit of the
     * ORM layer, which has difficulty using the business model.
     * 
     * @throws ArchitectException if there is an error populating the SQLTable
     * @throws NullPointerException if any of the business objects required for
     * resolving the column object are missing
     */
    public SQLColumn getColumn() throws ArchitectException {
        if (cachedColumn != null) return cachedColumn;
        if (columnName == null) return null;
        
        MatchMakerCriteriaGroup group = getParent();
        if (group == null) throw new NullPointerException("Not attached to a parent");
        Match match = (Match) group.getParentMatch();
        if (group == null) throw new NullPointerException("Not attached to a grandparent");
        SQLTable st = match.getSourceTable();
        if (st == null) throw new NullPointerException("The owning match has no source table specified");
        SQLColumn newColumn = st.getColumnByName(columnName);
        
        // did we actually make it here?
        setColumn(newColumn);
        return newColumn;
    }

    /**
     * Sets the cached column as well as the simple columnName string.
     */
    public void setColumn(SQLColumn column) {
        SQLColumn oldVal = this.cachedColumn;
        this.cachedColumn = column;
        this.columnName = (column == null ? null : column.getName());
        getEventSupport().firePropertyChange("column", oldVal, column);
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
    public int hashCode() {
        final int PRIME = 31;
        int result = 0;
        result = PRIME * result + ((cachedColumn == null) ? 0 : cachedColumn.hashCode());
        result = PRIME * result + ((getParent() == null) ? 0 : getParent().hashCode());

        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if(!(obj instanceof MatchMakerCriteria)) 
            return false;
        if (getClass() != obj.getClass())
            return false;
        final MatchMakerCriteria other = (MatchMakerCriteria) obj;
        if (cachedColumn == null) {
            if (other.cachedColumn != null)
                return false;
        } else if (!cachedColumn.equals(other.cachedColumn))
            return false;
        if (getParent() == null) {
            if (other.getParent() != null)
                return false;
        } else if (!getParent().equals(other.getParent()))
            return false;
        return true;
    }
}