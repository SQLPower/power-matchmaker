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
import java.util.ArrayList;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerTranslateGroup;
import ca.sqlpower.matchmaker.TranslateGroupParent;
import ca.sqlpower.object.SPChildEvent;
import ca.sqlpower.object.SPListener;
import ca.sqlpower.util.TransactionEvent;

/**
 * Provides the glue that allows a combo box to display the list of translation
 * groups.  The first item in this list is always null, which allows users of the
 * combo box to choose not to use a translation group.
 */
public class TranslationComboBoxModel implements ComboBoxModel {

    private static final Logger logger = Logger
            .getLogger(TranslationComboBoxModel.class);
	private TranslateGroupParent tgp;
    private MatchMakerTranslateGroup selectedItem;
	
    /**
     * Controls the "first item null" feature, which keeps a null-valued item at the
     * beginning of the list of items.  Such an item lets users select no translation
     * group.
     */
    private boolean firstItemNull = false;
    
    List<ListDataListener> listeners = new ArrayList<ListDataListener>();
    
	public TranslationComboBoxModel(TranslateGroupParent tgp) {
		this.tgp = tgp;
        tgp.addSPListener(new ComboBoxModelEventAdapter());
	}
	
	public Object getElementAt(int index) {
		if (firstItemNull) {
			index--;
		}
		if (index == -1) {
			return null;
		} else {
			return tgp.getChildren().get(index);
		}
	}

	public int getSize() {
		if (firstItemNull) {
			return tgp.getChildren().size() + 1;
		} else {
			return tgp.getChildren().size();
		}
	}

	public Object getSelectedItem() {
		return selectedItem;
	}

	public void setSelectedItem(Object anItem) {		
		selectedItem = (MatchMakerTranslateGroup) anItem;		
	}

	public void addListDataListener(ListDataListener l) {
		listeners.add(l);
		
	}

	public void removeListDataListener(ListDataListener l) {
		listeners.remove(l);
	}

    private void fireIntervalAdded(SPChildEvent evt){
    	int correction;
    	if (firstItemNull) {
    		correction = 1;
    	} else {
    		correction = 0;
    	}
        sendOffListEvent(new ListDataEvent(
        		this,
        		ListDataEvent.INTERVAL_ADDED,
        		evt.getIndex() + correction,
        		evt.getIndex() + correction));
    }
    
    private void fireIntervalRemoved(SPChildEvent evt){
    	int correction;
    	if (firstItemNull) {
    		correction = 1;
    	} else {
    		correction = 0;
    	}
        sendOffListEvent(new ListDataEvent(
        		this,
        		ListDataEvent.INTERVAL_REMOVED,
        		evt.getIndex() + correction,
        		evt.getIndex() + correction));
    }
    
    
    private void sendOffListEvent(ListDataEvent lde) {
        logger.debug("Firing an event");
        for (int i = listeners.size()-1; i>=0; i--){
            if (lde.getType() == ListDataEvent.CONTENTS_CHANGED){
                logger.debug("Firing contents Changed");
                listeners.get(i).contentsChanged(lde);
            } else if (lde.getType() == ListDataEvent.INTERVAL_ADDED) {
                logger.debug("Firing Interval Added");
                listeners.get(i).intervalAdded(lde);
            } else if (lde.getType() == ListDataEvent.INTERVAL_REMOVED){
                logger.debug("Firing Interval Removed");
                listeners.get(i).intervalRemoved(lde);
            } else {
                throw new IllegalStateException("Woah now there is no listdataEvent type "+lde.getType());
            }
        }
    }
    
	public boolean isFirstItemNull() {
		return firstItemNull;
	}

	public void setFirstItemNull(boolean firstItemNull) {
		this.firstItemNull = firstItemNull;
	}
	
    private class ComboBoxModelEventAdapter implements SPListener {
    	
    	@Override
		public void childAdded(SPChildEvent e) {
            logger.debug("Received child added event");
            fireIntervalAdded(e);
		}

		@Override
		public void childRemoved(SPChildEvent e) {
            logger.debug("Received child removed event");
            if (!tgp.getChildren().contains(selectedItem))
            {
                selectedItem = null;
            }
            fireIntervalRemoved(e);
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

		@Override
		public void propertyChanged(PropertyChangeEvent evt) {
			//no-op
		}
    }

}
