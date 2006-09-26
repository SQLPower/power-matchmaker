package ca.sqlpower.matchmaker.swingui;


public enum MatchCriteriaColumn {
	COLUMN("Column",String.class),
	ALLOW_NULL("Match Nulls", Boolean.class),
    CASE_SENSITIVE_IND("Case Insensitive", Boolean.class),
    SUPPRESS_CHAR("Suppress Chars", String.class),
    FIRST_N_CHAR("First N Chars",Long.class),
    MATCH_START("Match Start", Boolean.class),
    SOUND_IND("Use Soundex", Boolean.class),   
    TRANSLATE_GROUP_NAME("Translate Words", String.class),
    REMOVE_SPECIAL_CHARS("Set Chars to Null?",Boolean.class),
    COUNT_WORDS_IND("Count Words", Boolean.class),
    REPLACE_WITH_SPACE_IND("Replace With Space", Boolean.class),
    REPLACE_WITH_SPACE("To Be Replaced With Space", String.class),  
    REORDER_IND("Reorder", Boolean.class),
    FIRST_N_CHARS_BY_WORD_IND("Match First N Chars By Word", Boolean.class),
    FIRST_N_CHARS_BY_WORD("First N Chars By Word",Long.class),
    MIN_WORDS_IN_COMMON("Min Words In Common",Long.class),
    MATCH_FIRST_PLUS_ONE_IND("Compare Words In Common", Boolean.class),
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
