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

package ca.sqlpower.matchmaker.undo;

import javax.swing.undo.AbstractUndoableEdit;

import ca.sqlpower.architect.SQLTable;
import ca.sqlpower.matchmaker.ColumnMergeRules;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.TableMergeRules;
import ca.sqlpower.matchmaker.ColumnMergeRules.MergeActionType;
import ca.sqlpower.matchmaker.event.MatchMakerEvent;

public class UndoableEditClass extends AbstractUndoableEdit{
	private MatchMakerEvent undoEvent;
	private MatchMakerObject mmo;

	public UndoableEditClass(MatchMakerEvent e, MatchMakerObject mmo){
		super();
		undoEvent = e;
		this.mmo = mmo;
	}

	public void undo(){
		super.undo();
		//TODO: undo the MatchMakerEvent
		undoOrRedo(true);
		
	}

	public void redo(){
		super.redo();
		//TODO: redo the MatchMakerEvent
		undoOrRedo(false);
	}
	
	public void undoOrRedo(boolean undo) {
		MatchMakerObject source = undoEvent.getSource();
		Object value;
		if (undo) {
			value = undoEvent.getOldValue();
		} else {
			value = undoEvent.getNewValue();
		}
		System.out.println(source);
		System.out.println(undo);
		System.out.println(value);
		System.out.println(undoEvent.getPropertyName());
		if (source instanceof TableMergeRules) {
			TableMergeRules tableMergeRule = (TableMergeRules) source;
			String propertyName = undoEvent.getPropertyName();
			if ("deleteDup".equals(propertyName)) {
				boolean deleteDup = (Boolean) value;
				tableMergeRule.setDeleteDup(deleteDup, true);
			} else if ("parentTable".equals(propertyName)) {
				SQLTable table = (SQLTable) value;
				tableMergeRule.setParentTable(table, true);
			}
		} else if (source instanceof ColumnMergeRules) {
			ColumnMergeRules columnMergeRule = (ColumnMergeRules) source;
			String propertyName = undoEvent.getPropertyName();
			if ("actionType".equals(propertyName)) {
				MergeActionType mat = (MergeActionType) value;
				columnMergeRule.setActionType(mat, true);
			}
		} 
	}
}
