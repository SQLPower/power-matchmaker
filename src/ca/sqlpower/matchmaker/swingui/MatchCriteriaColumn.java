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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */


package ca.sqlpower.matchmaker.swingui;

import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.matchmaker.MatchMakerTranslateGroup;


public enum MatchCriteriaColumn {
	COLUMN("Column",SQLColumn.class),
	ALLOW_NULL("Match Nulls", Boolean.class),
    CASE_SENSITIVE_IND("Case Insensitive", Boolean.class),
    REMOVE_SPECIAL_CHARS("Ignore Characters",Boolean.class),
    SUPPRESS_CHAR("Suppress Chars", String.class),
    REPLACE_WITH_SPACE_IND("Replace With Space", Boolean.class),
    REPLACE_WITH_SPACE("To Be Replaced With Space", String.class),  
    TRANSLATE_GROUP("Translate Words", MatchMakerTranslateGroup.class),
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
