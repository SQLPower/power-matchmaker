package ca.sqlpower.matchmaker.swingui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;

import javax.swing.table.AbstractTableModel;

import ca.sqlpower.matchmaker.hibernate.PlMatchCriterion;
import ca.sqlpower.matchmaker.hibernate.PlMatchCriterionId;
import ca.sqlpower.matchmaker.hibernate.PlMatchGroup;
import ca.sqlpower.matchmaker.util.HibernateUtil;

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
		group.addHierachialChangeListener(pcl);
		
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
		PlMatchCriterion criteria = (PlMatchCriterion) group.getChildren().get(rowIndex);
		
		PlMatchCriterionId id = criteria.getId();
		switch (column) {	
		case COLUMN:
			if (aValue == null) return;
			PlMatchCriterion saveCriterion = new PlMatchCriterion(new PlMatchCriterionId(id.getMatchId(),id.getGroupId(),(String)aValue),group,criteria);
			HibernateUtil.primarySession().delete(criteria);					
			HibernateUtil.primarySession().persist(saveCriterion);
			
			group.removePlMatchCriteria(criteria);
			group.addPlMatchCriteria(saveCriterion);
			
			criteria = saveCriterion;
			
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
		criteria.setLastUpdateDate(new Date(System.currentTimeMillis()));
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
