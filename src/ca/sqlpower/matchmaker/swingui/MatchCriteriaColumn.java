package ca.sqlpower.matchmaker.swingui;

import java.math.BigDecimal;

public enum MatchCriteriaColumn {
	ALLOW_NULL("Allow Nulls", Boolean.class),
    CASE_SENSITIVE_IND("Case Sensitive", Boolean.class),
    SUPPRESS_CHAR("Suppress Char", String.class),
    FIRST_N_CHAR("First N Chars",BigDecimal.class),
    SEQ_NO("Sequence #",BigDecimal.class),
    MATCH_START("Match Start", Boolean.class),
    MATCH_END("Match End", Boolean.class),
    VARIANCE_AMOUNT("Variance",BigDecimal.class),
    VARIANCE_TYPE("Variance Type", String.class),
    SOUND_IND("Use Soundex", Boolean.class),   
    TRANSLATE_GROUP_NAME("Translate Group", String.class),
    REMOVE_SPECIAL_CHARS("Remove Special Chars",Boolean.class),
    COUNT_WORDS_IND("Count Words", Boolean.class),
    REPLACE_WITH_SPACE_IND("Replace With Space", Boolean.class),
    REPLACE_WITH_SPACE("To Be Replaced With Space", String.class),  
    REORDER_IND("Reorder", Boolean.class),
    FIRST_N_CHARS_BY_WORD_IND("Match First N Chars By Word", Boolean.class),
    FIRST_N_CHARS_BY_WORD("First N Chars By Word",BigDecimal.class),
    MIN_WORDS_IN_COMMON("Min Words In Common",BigDecimal.class),
    WORDS_IN_COMMON_NUM_WORDS("Words in Common?",BigDecimal.class),
    MATCH_FIRST_PLUS_ONE_IND("Match First Plus One", Boolean.class),
    LAST_UPDATE_DATE("Last Updated", String.class),
    LAST_UPDATE_USER("Last Updated by", String.class),
    LAST_UPDATED_OS_USER("Last updated using", String.class);
    
    String name;
    Class columnClass;

    MatchCriteriaColumn(String name, Class t)  {
        this.name = name;
        this.columnClass = t;
    }
    

    public String getName() {
        return name;
    }


	public Class getColumnClass() {
		return columnClass;
	}

}
