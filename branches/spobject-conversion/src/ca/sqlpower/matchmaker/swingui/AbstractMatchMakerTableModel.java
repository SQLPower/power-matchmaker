/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of DQguru
 *
 * DQguru is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DQguru is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker.swingui;

import java.beans.PropertyChangeEvent;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.util.SQLPowerUtils;
import ca.sqlpower.util.TransactionEvent;

public abstract class AbstractMatchMakerTableModel extends AbstractTableModel implements CleanupModel {
	
	private static final Logger logger = Logger.getLogger(AbstractMatchMakerTableModel.class);

	protected Project mmo;
	private TableModelEventAdapter mmoListener = new TableModelEventAdapter();
	
	protected AbstractMatchMakerTableModel (Project mmo) {
		this.mmo = mmo;
		SQLPowerUtils.listenToShallowHierarchy(mmoListener, mmo);
		logger.debug("Table Model initialized.");
	}
	
	public int getRowCount() {
		return mmo.getChildren().size();
	}
	
	public void cleanup() {
		SQLPowerUtils.unlistenToHierarchy(mmo, mmoListener);
	}
	
	private class TableModelEventAdapter implements SPListener {

		public void propertyChanged(PropertyChangeEvent evt) {
			List<MatchMakerObject> children = mmo.getChildren(MatchMakerObject.class);
			Object source = evt.getSource();
			if (source != mmo && children.contains(source)) {
				int index = children.indexOf(source);
				fireTableRowsUpdated(index, index);
			}
		}

		@Override
		public void childAdded(SPChildEvent e) {
			if(e.getSource() == mmo){
	            fireTableStructureChanged();
	            Object addedChild = e.getChild();
	            ((MatchMakerObject) addedChild).addSPListener(this);
            }
            fireTableRowsInserted(e.getIndex(), e.getIndex());
		}

		@Override
		public void childRemoved(SPChildEvent e) {
			if(e.getSource() == mmo) {
	            Object addedChild = e.getChild();
	            ((MatchMakerObject) addedChild).removeSPListener(this);
	            }
	            fireTableRowsDeleted(e.getIndex(),e.getIndex());
	        }

		@Override
		public void transactionStarted(TransactionEvent e) {
			//no-op
		}

		@Override
		public void transactionEnded(TransactionEvent e) {
			//no-op
		}

		@Override
		public void transactionRollback(TransactionEvent e) {
			//no-op
		}
		
	}

}
