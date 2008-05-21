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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.MatchMakerUtils;
import ca.sqlpower.matchmaker.event.MatchMakerEvent;
import ca.sqlpower.matchmaker.event.MatchMakerListener;

public abstract class AbstractMatchMakerTableModel<T extends MatchMakerObject, C extends MatchMakerObject> extends AbstractTableModel implements CleanupModel {
	
	private static final Logger logger = Logger.getLogger(AbstractMatchMakerTableModel.class);

	protected T mmo;
	private TableModelEventAdapter mmoListener = new TableModelEventAdapter();
	
	@SuppressWarnings("unchecked")
	protected AbstractMatchMakerTableModel (T mmo) {
		this.mmo = mmo;
		MatchMakerUtils.listenToShallowHierarchy(mmoListener, mmo);
		logger.debug("Table Model initialized.");
	}
	
	public int getRowCount() {
		return mmo.getChildren().size();
	}
	
	@SuppressWarnings("unchecked")
	public void cleanup() {
		MatchMakerUtils.unlistenToHierarchy(mmoListener, mmo);
	}
	
	private class TableModelEventAdapter implements MatchMakerListener {

		@SuppressWarnings("unchecked")
		public void mmPropertyChanged(MatchMakerEvent evt) {
			List<MatchMakerObject> children = mmo.getChildren();
			Object source = evt.getSource();
			if (source != mmo && children.contains(source)) {
				int index = children.indexOf(source);
				fireTableRowsUpdated(index, index);
			}
		}

		@SuppressWarnings("unchecked")
		public void mmChildrenInserted(MatchMakerEvent evt) {
			if(evt.getSource() == mmo){
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
	            for (Object addedChild : evt.getChildren()){
	                ((MatchMakerObject) addedChild).addMatchMakerListener(this);
	            }
	            fireTableRowsInserted(changedIndices.get(0), changedIndices.get(changedIndices.size()-1));
	        }
		}

		@SuppressWarnings("unchecked")
		public void mmChildrenRemoved(MatchMakerEvent evt) {
			if(evt.getSource() == mmo) {
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
	            for (Object addedChild : evt.getChildren()){
	                ((MatchMakerObject) addedChild).removeMatchMakerListener(this);
	            }
	            fireTableRowsDeleted(changedIndices.get(0), changedIndices.get(changedIndices.size()-1));
	        }
		}

		/**
		 * Currently no structure changed event should be fired because it is not
		 * undoable.
		 */
		public void mmStructureChanged(MatchMakerEvent evt) {
			fireTableStructureChanged();
		}
		
	}

}
