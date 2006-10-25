package ca.sqlpower.matchmaker.swingui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;

import javax.swing.table.AbstractTableModel;

import ca.sqlpower.matchmaker.hibernate.PlMatchCriterion;
import ca.sqlpower.matchmaker.hibernate.PlMatchGroup;

public class MatchCriteriaTableModel extends AbstractTableModel {

	private final class MatchCriteriaPropertyListener implements PropertyChangeListener {
		PlMatchGroup group;
		public MatchCriteriaPropertyListener(PlMatchGroup group) {
			this.group = group;
		}

		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getSource() instanceof PlMatchCriterion){
				int index = group.getChildren().indexOf(evt.getSource());
				fireTableRowsUpdated(index,index);
			} else if (evt.getSource() instanceof PlMatchGroup){
				// TODO a more efficient implementation
				fireTableDataChanged();
			} else {
				throw new UnsupportedOperationException("Not implemented for class "+evt.getSource().getClass());
			}
		}
	}

	private PlMatchGroup group;

	public PlMatchGroup getGroup() {
		return group;
	}

	public void setGroup(PlMatchGroup group) {
		this.group = group;
		PropertyChangeListener pcl = new MatchCriteriaPropertyListener(group);
		group.addHierarchicalChangeListener(pcl);
		
	}

	public MatchCriteriaTableModel(PlMatchGroup matchGroup) {
		setGroup(matchGroup);
	}

	public int getColumnCount() {
		return MatchCriteriaColumn.values().length;
	}

	public int getRowCount() {
		return group.getChildCount();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		return getFieldFromCriteria(
                MatchCriteriaColumn.values()[columnIndex],(PlMatchCriterion) group.getChildren().get(rowIndex));
	}
	
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		MatchCriteriaColumn column = MatchCriteriaColumn.values()[columnIndex];
		PlMatchCriterion criterion = (PlMatchCriterion) group.getChildren().get(rowIndex);
		
		switch (column) {	
		case COLUMN:
			criterion.setColumnName((String)aValue);
			break;
		case ALLOW_NULL:             
			criterion.setAllowNullInd((Boolean)aValue);
			break;
		case CASE_SENSITIVE_IND:             
		    criterion.setCaseSensitiveInd((Boolean)aValue);
		    break;
		case SUPPRESS_CHAR:
			criterion.setSuppressChar((String)aValue);
			break;
		case FIRST_N_CHAR:
			criterion.setFirstNChar((Long) aValue);
			break;
		case MATCH_START:
			if ((Boolean)aValue){
				criterion.setReorderInd(true);
			}
			criterion.setMatchStart((Boolean)aValue);
			break;
		case SOUND_IND:

			criterion.setSoundInd((Boolean)aValue);
			break;
		case TRANSLATE_GROUP_NAME:  
			criterion.setTranslateGroupName((String) aValue);
			break;
		case REMOVE_SPECIAL_CHARS:  
			criterion.setRemoveSpecialChars((Boolean)aValue);
			break;
		case COUNT_WORDS_IND:    
			criterion.setCountWordsInd((Boolean)aValue);
			break;
		case REPLACE_WITH_SPACE_IND:      
			criterion.setReplaceWithSpaceInd((Boolean)aValue);
			break;
		case REPLACE_WITH_SPACE:  
			criterion.setReplaceWithSpace((String)aValue);
			break;
		case REORDER_IND: 
			if (!(Boolean) aValue) {
				criterion.setMatchStart(false);
				criterion.setFirstNCharByWordInd(false);
			}
			criterion.setReorderInd((Boolean)aValue);
			break;
		case FIRST_N_CHARS_BY_WORD:     
			criterion.setFirstNCharByWord((Long) aValue);
			break;
		case MIN_WORDS_IN_COMMON:
			criterion.setMinWordsInCommon((Long) aValue);
			break;
		case MATCH_FIRST_PLUS_ONE_IND:
			if ((Boolean)aValue){
				criterion.setReorderInd(true);
			}
			criterion.setFirstNCharByWordInd((Boolean)aValue);
			break;
		default:
			throw new IllegalArgumentException("Invalid column");
		}
		criterion.setLastUpdateDate(new Date(System.currentTimeMillis()));
	}
	
	public PlMatchCriterion getRow(int row){
		return (PlMatchCriterion) group.getChildren().get(row);
	}
	
	@Override
	public String getColumnName(int column) {
		return MatchCriteriaColumn.values()[column].getName();
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return MatchCriteriaColumn.values()[columnIndex].getColumnClass();
		
	}
	
	private static Object getFieldFromCriteria(MatchCriteriaColumn column, PlMatchCriterion criteria) {
		switch (column) {	
		case COLUMN:
			return criteria.getColumnName();
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
