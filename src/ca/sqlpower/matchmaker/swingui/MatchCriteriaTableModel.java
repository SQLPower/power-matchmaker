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
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		MatchCriteriaColumn column = MatchCriteriaColumn.values()[columnIndex];
		PlMatchCriteria criteria = (PlMatchCriteria) group.getChildren().get(rowIndex);
		
		switch (column) {	
		case COLUMN:
			
			
			
			break;
		case ALLOW_NULL:             
			criteria.setAllowNullInd((Boolean)aValue);
			break;
		case CASE_SENSITIVE_IND:             
		    criteria.setCaseSensitiveInd((Boolean)aValue);
		    break;
		case SUPPRESS_CHAR:
			criteria.setSuppressChar((String)aValue);
			break;
		case FIRST_N_CHAR:
			criteria.setFirstNChar((Long) aValue);
			break;
		case MATCH_START:
			if ((Boolean)aValue){
				criteria.setReorderInd(true);
			}
			criteria.setMatchStart((Boolean)aValue);
			break;
		case SOUND_IND:

			criteria.setSoundInd((Boolean)aValue);
			break;
		case TRANSLATE_GROUP_NAME:  
			criteria.setTranslateGroupName((String) aValue);
			break;
		case REMOVE_SPECIAL_CHARS:  
			criteria.setRemoveSpecialChars((Boolean)aValue);
			break;
		case COUNT_WORDS_IND:    
			criteria.setCountWordsInd((Boolean)aValue);
			break;
		case REPLACE_WITH_SPACE_IND:      
			criteria.setReplaceWithSpaceInd((Boolean)aValue);
			break;
		case REPLACE_WITH_SPACE:  
			criteria.setReplaceWithSpace((String)aValue);
			break;
		case REORDER_IND: 
			if (!(Boolean) aValue) {
				criteria.setMatchStart(false);
				criteria.setFirstNCharByWordInd(false);
			}
			criteria.setReorderInd((Boolean)aValue);
			break;
		case FIRST_N_CHARS_BY_WORD:     
			criteria.setFirstNCharByWord((Long) aValue);
			break;
		case MIN_WORDS_IN_COMMON:
			criteria.setMinWordsInCommon((Long) aValue);
			break;
		case MATCH_FIRST_PLUS_ONE_IND:
			if ((Boolean)aValue){
				criteria.setReorderInd(true);
			}
			criteria.setFirstNCharByWordInd((Boolean)aValue);
			break;
		default:
			throw new IllegalArgumentException("Invalid column");
		}
	}
	
	public PlMatchCriteria getRow(int row){
		return (PlMatchCriteria) group.getChildren().get(row);
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
		case COLUMN:
			return criteria.getId().getColumnName();
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
		case FIRST_N_CHARS_BY_WORD:     
			return criteria.getFirstNCharByWord(); 
		case MIN_WORDS_IN_COMMON:
			return criteria.getMinWordsInCommon();
		case MATCH_FIRST_PLUS_ONE_IND:
			return criteria.isMatchFirstPlusOneInd();
		default:
			throw new IllegalArgumentException("Invalid column");
		}
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}

}
