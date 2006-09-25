package ca.sqlpower.matchmaker.swingui;

import javax.swing.table.AbstractTableModel;

import ca.sqlpower.matchmaker.hibernate.PlMatchCriteria;
import ca.sqlpower.matchmaker.hibernate.PlMatchGroup;

public class MatchCriteriaTableModel extends AbstractTableModel {

	private PlMatchGroup group;

	public PlMatchGroup getGroup() {
		return group;
	}

	public void setGroup(PlMatchGroup group) {
		this.group = group;
	}

	public MatchCriteriaTableModel(PlMatchGroup matchGroup) {
		this.group = matchGroup;
	}

	public int getColumnCount() {
		return MatchCriteriaColumn.values().length;
	}

	public int getRowCount() {
		return group.getChildCount();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		return getFieldFromCriteria(
                MatchCriteriaColumn.values()[columnIndex],(PlMatchCriteria) group.getChildren().get(rowIndex));
	}
	
	@Override
	public String getColumnName(int column) {
		return MatchCriteriaColumn.values()[column].getName();
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return MatchCriteriaColumn.values()[columnIndex].getColumnClass();
	}
	
	private static Object getFieldFromCriteria(MatchCriteriaColumn column, PlMatchCriteria criteria) {
		switch (column) {	
		case ALLOW_NULL:             
			return criteria.isAllowNullInd();
		case CASE_SENSITIVE_IND:             
			return criteria.isCaseSensitiveInd();         
		case SUPPRESS_CHAR:
			return criteria.getSuppressChar();
		case FIRST_N_CHAR:
			return criteria.getFirstNChar();         
		case MATCH_START:
			return criteria.isMatchStart();
		case SOUND_IND:          
			return criteria.isSoundInd();
		case TRANSLATE_GROUP_NAME:           
			return criteria.getTranslateGroupName();
		case REMOVE_SPECIAL_CHARS:        
			return criteria.isRemoveSpecialChars();
		case COUNT_WORDS_IND:         
			return criteria.isCountWordsInd();  
		case REPLACE_WITH_SPACE_IND:      
			return criteria.isReplaceWithSpaceInd(); 
		case REPLACE_WITH_SPACE:  
			return criteria.getReplaceWithSpace();			
		case REORDER_IND:   
			return criteria.isReorderInd();
		case FIRST_N_CHARS_BY_WORD_IND:   
			return criteria.isFirstNCharByWordInd();
		case FIRST_N_CHARS_BY_WORD:     
			return criteria.getFirstNCharByWord(); 
		case MIN_WORDS_IN_COMMON:
			return criteria.getMinWordsInCommon();
		case MATCH_FIRST_PLUS_ONE_IND:
			return criteria.isMatchFirstPlusOneInd();
		case LAST_UPDATE_DATE:
			return criteria.getLastUpdateDate();
		case LAST_UPDATE_USER: 
			return criteria.getLastUpdateUser();
		case LAST_UPDATED_OS_USER:
			return criteria.getLastUpdateOsUser();
		default:
			throw new IllegalArgumentException("Invalid column");
		}
	}


}
