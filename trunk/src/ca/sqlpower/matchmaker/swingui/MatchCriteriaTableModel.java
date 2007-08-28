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

import javax.swing.table.AbstractTableModel;

import ca.sqlpower.architect.SQLColumn;
import ca.sqlpower.matchmaker.MatchRuleSet;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerTranslateGroup;
import ca.sqlpower.matchmaker.MatchMakerUtils;
import ca.sqlpower.matchmaker.MatchRule;
import ca.sqlpower.matchmaker.event.MatchMakerEvent;
import ca.sqlpower.matchmaker.event.MatchMakerListener;
import ca.sqlpower.matchmaker.util.EditableJTable;

public class MatchCriteriaTableModel extends AbstractTableModel implements CleanupTableModel {

	private final class TableModelEventAdapter
		implements MatchMakerListener<MatchRuleSet, MatchRule> {

		public void mmChildrenInserted(MatchMakerEvent<MatchRuleSet, MatchRule> evt) {
			fireTableRowsInserted(evt.getChangeIndices()[0], evt.getChangeIndices()[0]);
			for ( MatchMakerObject c : evt.getChildren() ) {
				MatchMakerUtils.listenToHierarchy(this, c);
			}
		}

		public void mmChildrenRemoved(MatchMakerEvent<MatchRuleSet, MatchRule> evt) {
			fireTableRowsDeleted(evt.getChangeIndices()[0], evt.getChangeIndices()[0]);
			for ( MatchMakerObject c : evt.getChildren() ) {
				MatchMakerUtils.unlistenToHierarchy(this, c);
			}
		}

		public void mmPropertyChanged(MatchMakerEvent evt) {
			fireTableDataChanged();
		}

		public void mmStructureChanged(MatchMakerEvent evt) {
			fireTableStructureChanged();
		}
	}
	

	private final MatchRuleSet group;
	private final TableModelEventAdapter tableModelEventAdapter;

	public MatchRuleSet getGroup() {
		return group;
	}

	/**
	 * Creates a new TableModel for the given match group.  If you want a
	 * table model for a different match group, you have to create a new
	 * instance of MatchCriteriaTableModel.
	 * <p>
	 * Note, it is important to call cleanup() when you are done with this
	 * table model, because it listens to the matchGroup and its children,
	 * and without a call to cleanup(), you will have memory leaks of this
	 * table model, the JTable it's attached to, and all sorts of other stuff.
	 * The {@link EditableJTable} class knows how to call cleanup() when necessary,
	 * but if you use this model with another kind of JTable, you will have to
	 * do the cleanup yourself.
	 * 
	 * @param matchGroup The Match Group to use for table data.  Must be non-null.
	 */
	public MatchCriteriaTableModel(MatchRuleSet matchGroup) {
		this.group = matchGroup;
		tableModelEventAdapter = new TableModelEventAdapter();
		MatchMakerUtils.listenToHierarchy(tableModelEventAdapter, group);
	}

	/**
	 * Releases resources and listeners that this model was using.  It is
	 * important to call this method when you are done with the table model.
	 */
	public void cleanup() {
		MatchMakerUtils.unlistenToHierarchy(tableModelEventAdapter, group);
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
                (MatchRule)group.getChildren().get(rowIndex));
	}
	
	/**
	 * Gives the row index of where the translate_group_name in the table  
	 * @param translate_group_name
	 * @return
	 */
	public int getIndexOfClass(MatchCriteriaColumn translate_group_name){
		//Things have not been setup yet
		if (group.getChildren()==null || group.getChildren().size() ==0){
			return -1;
		}
		for (int i=0; i < getRowCount(); i++){			
			MatchRule criterion = 
				(MatchRule) group.getChildren().get(i);
			if (criterion.equals(translate_group_name)){
				return i;
			}
		}
		return -1;
	}
	
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		MatchCriteriaColumn column = MatchCriteriaColumn.values()[columnIndex];
		MatchRule criterion = 
			(MatchRule) group.getChildren().get(rowIndex);
		
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
			criterion.setMatchFirstPlusOneInd((Boolean)aValue);
			break;
		default:
			throw new IllegalArgumentException("Invalid column");
		}
	}
	
	public MatchRule getRow(int row){
		return (MatchRule) group.getChildren().get(row);
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
			MatchRule criteria) {
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