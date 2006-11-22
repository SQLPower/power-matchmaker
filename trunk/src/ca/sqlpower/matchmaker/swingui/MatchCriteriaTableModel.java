package ca.sqlpower.matchmaker.swingui;

import javax.swing.table.AbstractTableModel;

import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.matchmaker.MatchMakerCriteriaGroup;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerTranslateGroup;
import ca.sqlpower.matchmaker.MatchmakerCriteria;
import ca.sqlpower.matchmaker.event.MatchMakerEvent;
import ca.sqlpower.matchmaker.event.MatchMakerListener;

public class MatchCriteriaTableModel extends AbstractTableModel {

	private final class TableModelEventAdapter<T extends MatchMakerObject, C extends MatchMakerObject>
		implements MatchMakerListener<T, C> {

		public void mmChildrenInserted(MatchMakerEvent evt) {
			fireTableRowsInserted(evt.getChangeIndices()[0], evt.getChangeIndices()[0]);
		}

		public void mmChildrenRemoved(MatchMakerEvent evt) {
			fireTableRowsDeleted(evt.getChangeIndices()[0], evt.getChangeIndices()[0]);
		}

		public void mmPropertyChanged(MatchMakerEvent evt) {
System.out.println("PropertyChanged:"+evt.getPropertyName()+"  "+evt.getSource().getName()+"  "+evt.getNewValue().toString());
		}

		public void mmStructureChanged(MatchMakerEvent evt) {
			// nothing
		}
	}
	

	private MatchMakerCriteriaGroup group;

	public MatchMakerCriteriaGroup getGroup() {
		return group;
	}

	public void setGroup(MatchMakerCriteriaGroup group) {
		this.group = group;
		group.addMatchMakerListener(
				new TableModelEventAdapter<MatchMakerObject, MatchMakerObject>());
	}

	public MatchCriteriaTableModel(MatchMakerCriteriaGroup matchGroup) {
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
                MatchCriteriaColumn.values()[columnIndex],
                (MatchmakerCriteria)group.getChildren().get(rowIndex));
	}
	
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		MatchCriteriaColumn column = MatchCriteriaColumn.values()[columnIndex];
		MatchmakerCriteria criterion = 
			(MatchmakerCriteria) group.getChildren().get(rowIndex);
		
		switch (column) {	
		case COLUMN:
			// Don't allow us to put a null value the DB don't like it.
			if (aValue == null) return;
			criterion.setColumn((SQLColumn)aValue);
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
		case TRANSLATE_GROUP:  
			criterion.setTranslateGroup((MatchMakerTranslateGroup) aValue);
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
	}
	
	public MatchmakerCriteria getRow(int row){
		return (MatchmakerCriteria) group.getChildren().get(row);
	}
	
	@Override
	public String getColumnName(int column) {
		return MatchCriteriaColumn.values()[column].getName();
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return MatchCriteriaColumn.values()[columnIndex].getColumnClass();
		
	}
	
	private static Object getFieldFromCriteria(MatchCriteriaColumn column,
			MatchmakerCriteria criteria) {
		switch (column) {	
		case COLUMN:
			return criteria.getColumn();
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
		case TRANSLATE_GROUP:        
			if ( criteria.getTranslateGroup() != null) {
				return criteria.getTranslateGroup();
			} else {
				return "";
			}
			
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
