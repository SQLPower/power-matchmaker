package ca.sqlpower.matchmaker.swingui;


public enum MatchCriteriaColumn {
	COLUMN("Column",String.class),
	ALLOW_NULL("Match Nulls", Boolean.class),
    CASE_SENSITIVE_IND("Case Insensitive", Boolean.class),
    REMOVE_SPECIAL_CHARS("Ignore Characters",Boolean.class),
    SUPPRESS_CHAR("Suppress Chars", String.class),
    REPLACE_WITH_SPACE_IND("Replace With Space", Boolean.class),
    REPLACE_WITH_SPACE("To Be Replaced With Space", String.class),  
    TRANSLATE_GROUP_NAME("Translate Words", String.class),
    REORDER_IND("Reorder", Boolean.class),
    FIRST_N_CHARS_BY_WORD("First N Chars By Word",Long.class),
    FIRST_N_CHAR("First N Chars",Long.class),
    MATCH_FIRST_PLUS_ONE_IND("Compare Words In Common", Boolean.class),
	MIN_WORDS_IN_COMMON("Min Words In Common",Long.class),
    MATCH_START("Match Start", Boolean.class),
    SOUND_IND("Use Soundex", Boolean.class),   
    COUNT_WORDS_IND("Count Words", Boolean.class);

    
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


	public static int getIndex(MatchCriteriaColumn translate_group_name) {
		MatchCriteriaColumn array[] =values();
		for (int i =0;i< array.length;i++){
			if (translate_group_name == array[i]){
				return i;
			}
		}
		return -1;
	}

}
