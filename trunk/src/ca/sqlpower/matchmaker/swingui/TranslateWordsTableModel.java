/*
 * Copyright (c) 2008, SQL Power Group Inc.
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

import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerTranslateGroup;
import ca.sqlpower.matchmaker.MatchMakerTranslateWord;

public class TranslateWordsTableModel extends AbstractMatchMakerTableModel<MatchMakerTranslateGroup, MatchMakerTranslateWord> {
    
    
	
	public  TranslateWordsTableModel(MatchMakerTranslateGroup translate){ 
		super(translate);
	}

	public int getColumnCount() {		
		return 2;
	}
    
    public MatchMakerObject getMatchMakerObject(int index){
        return mmo.getChildren().get(index);
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
			for (MatchMakerTranslateWord t: mmo.getChildren()){
				if ((t.getFrom().equals(null) ? t.getFrom().equals(aValue) : t.getFrom().equals(aValue)) &&
						(t.getTo().equals(null) ? t.getTo().equals(trans.getTo()) : t.getTo().equals(trans.getTo()))) {
					// We would have a collision.
					return;
				}
			}
			trans.setFrom((String)aValue);
			break;
		case 1:
			for (MatchMakerTranslateWord t: mmo.getChildren()){
				if ((t.getTo().equals(null) ? t.getTo().equals(aValue) : t.getTo().equals(aValue)) &&
						(t.getFrom().equals(null) ? t.getFrom().equals(trans.getFrom()) : t.getFrom().equals(trans.getFrom()))) {
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
		return mmo.getChildren().get(rowIndex);
	}

}
