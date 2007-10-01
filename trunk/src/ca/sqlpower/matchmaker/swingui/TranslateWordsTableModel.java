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

import java.util.ArrayList;
import java.util.Collections;

import javax.swing.table.AbstractTableModel;

import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerTranslateGroup;
import ca.sqlpower.matchmaker.MatchMakerTranslateWord;
import ca.sqlpower.matchmaker.MatchMakerUtils;
import ca.sqlpower.matchmaker.event.MatchMakerEvent;
import ca.sqlpower.matchmaker.event.MatchMakerListener;

public class TranslateWordsTableModel extends AbstractTableModel implements MatchMakerListener {
    
    
	MatchMakerTranslateGroup translate;
	
	public  TranslateWordsTableModel(MatchMakerTranslateGroup translate){ 
		super();
		this.translate = translate; 
		MatchMakerUtils.listenToHierarchy(this, this.translate);
	}

	public int getColumnCount() {		
		return 2;
	}
    
    public MatchMakerObject getMatchMakerObject(int index){
        return translate.getChildren().get(index);
    }

	public int getRowCount() {
		if(translate == null) return 0;
		int size = translate.getChildCount();		
		return size;
	}

	@Override
	public String getColumnName(int column) {
		switch(column) {
		case 0:
			return "From";
		case 1:
			return "To";
		default:
			throw new IndexOutOfBoundsException("Invalid column index");
		}
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}
	
	public Object getValueAt(int rowIndex, int columnIndex) {
        MatchMakerTranslateWord trans = getRow(rowIndex);
		switch(columnIndex) {
		case 0:
			return trans.getFrom();
		case 1:
			return trans.getTo();
		default:
			throw new IndexOutOfBoundsException("Invalid column index");
		}
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		MatchMakerTranslateWord trans = getRow(rowIndex);
		switch(columnIndex) {
		case 0:
			for (MatchMakerTranslateWord t: translate.getChildren()){
				if ((t.getFrom() == null ? t.getFrom() == aValue : t.getFrom().equals(aValue)) &&
						(t.getTo() == null ? t.getTo() == trans.getTo() : t.getTo().equals(trans.getTo()))) {
					// We would have a collision.
					return;
				}
			}
			trans.setFrom((String)aValue);
			break;
		case 1:
			for (MatchMakerTranslateWord t: translate.getChildren()){
				if ((t.getTo() == null ? t.getTo() == aValue : t.getTo().equals(aValue)) &&
						(t.getFrom() == null ? t.getFrom() == trans.getFrom() : t.getFrom().equals(trans.getFrom()))) {
					// We would have a collision.
					return;
				}
			}
			trans.setTo((String) aValue);
			break;
		default:
			throw new IndexOutOfBoundsException("Invalid column index");
		}	
	}
	
	public MatchMakerTranslateWord getRow(int rowIndex) {
		return translate.getChildren().get(rowIndex );
	}

    public void mmChildrenInserted(MatchMakerEvent evt) {
        if(evt.getSource() instanceof MatchMakerTranslateGroup){
            int[] changed = evt.getChangeIndices();
            ArrayList<Integer> changedIndices = new ArrayList<Integer>();
            for (int selectedRowIndex:changed){
                changedIndices.add(new Integer(selectedRowIndex));
            }
            Collections.sort(changedIndices);
            for (int i=1; i < changedIndices.size(); i++){
                if (changedIndices.get(i-1)!=changedIndices.get(i)-1){
                    fireTableStructureChanged();
                    return;
                }
            }
            for (Object word:evt.getChildren()){
                ((MatchMakerTranslateWord) word).addMatchMakerListener(this);
            }
            fireTableRowsInserted(changedIndices.get(0), changedIndices.get(changedIndices.size()-1));
        }
    }

    public void mmChildrenRemoved(MatchMakerEvent evt) {
        if(evt.getSource() instanceof MatchMakerTranslateGroup) {
            int[] changed = evt.getChangeIndices();
            ArrayList<Integer> changedIndices = new ArrayList<Integer>();
            for (int selectedRowIndex:changed){
                changedIndices.add(new Integer(selectedRowIndex));
            }
            Collections.sort(changedIndices);
            for (int i=1; i < changedIndices.size(); i++) {
                if (changedIndices.get(i-1)!=changedIndices.get(i)-1) {
                    fireTableStructureChanged();
                    return;
                }
            }
            for (Object word:evt.getChildren()) {
                ((MatchMakerTranslateWord) word).removeMatchMakerListener(this);
            }
            fireTableRowsDeleted(changedIndices.get(0), changedIndices.get(changedIndices.size()-1));
        }
    }

    public void mmPropertyChanged(MatchMakerEvent evt) { 
        if(evt.getSource() instanceof MatchMakerTranslateWord) {
            fireTableRowsUpdated(translate.getChildren().indexOf(evt.getSource()), translate.getChildren().indexOf(evt.getSource()));
        }
    }

    public void mmStructureChanged(MatchMakerEvent evt) {
        fireTableStructureChanged();
    }
}
