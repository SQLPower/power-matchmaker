package ca.sqlpower.matchmaker.swingui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerTranslateGroup;
import ca.sqlpower.matchmaker.TranslateGroupParent;
import ca.sqlpower.matchmaker.event.MatchMakerEvent;
import ca.sqlpower.matchmaker.event.MatchMakerListener;

public class TranslationComboBoxModel implements ComboBoxModel, MatchMakerListener<TranslateGroupParent, MatchMakerTranslateGroup> {

    private static final Logger logger = Logger
            .getLogger(TranslationComboBoxModel.class);
	private TranslateGroupParent tgp;
    private MatchMakerTranslateGroup selectedItem;
	
    List<ListDataListener> listeners = new ArrayList<ListDataListener>();
    
	public TranslationComboBoxModel(TranslateGroupParent tgp) {
		this.tgp = tgp;
        tgp.addMatchMakerListener(this);
	}
	
	public Object getElementAt(int index) {
		return tgp.getChildren().get(index);
	}

	public int getSize() {
		return tgp.getChildren().size();
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

    public void fireIntervalAdded(MatchMakerEvent<TranslateGroupParent, MatchMakerTranslateGroup> evt){
        sendOffListEvent(new ListDataEvent(this,ListDataEvent.INTERVAL_ADDED,evt.getChangeIndices()[0],evt.getChangeIndices()[0]));
    }
    
    public void fireIntervalRemoved(MatchMakerEvent<TranslateGroupParent, MatchMakerTranslateGroup> evt){
        sendOffListEvent(new ListDataEvent(this,ListDataEvent.INTERVAL_REMOVED,evt.getChangeIndices()[0],evt.getChangeIndices()[0]));
    }
    
    public void fireChanged(){
        sendOffListEvent(new ListDataEvent(this,ListDataEvent.CONTENTS_CHANGED,0,tgp.getChildCount()-1));
    }
    
    
    private void sendOffListEvent(ListDataEvent lde) {
        logger.debug("Firing an event");
        for (int i = listeners.size()-1; i>=0; i--){
            if (lde.getType() == lde.CONTENTS_CHANGED){
                logger.debug("Firing contents Changed");
                listeners.get(i).contentsChanged(lde);
            } else if (lde.getType() == lde.INTERVAL_ADDED) {
                logger.debug("Firing Interval Added");
                listeners.get(i).intervalAdded(lde);
            } else if (lde.getType() == lde.INTERVAL_REMOVED){
                logger.debug("Firing Interval Removed");
                listeners.get(i).intervalRemoved(lde);
            } else {
                throw new IllegalStateException("Woah now there is no listdataEvent type "+lde.getType());
            }
        }
    }
    public void mmChildrenInserted(MatchMakerEvent<TranslateGroupParent, MatchMakerTranslateGroup> evt) {
        logger.debug("Received child added event");
        fireIntervalAdded(evt);
    }

    public void mmChildrenRemoved(MatchMakerEvent<TranslateGroupParent, MatchMakerTranslateGroup> evt) {
        logger.debug("Received child removed event");
        if (!tgp.getChildren().contains(selectedItem))
        {
            selectedItem = null;
        }
        fireIntervalRemoved(evt);
    }

    public void mmPropertyChanged(MatchMakerEvent<TranslateGroupParent, MatchMakerTranslateGroup> evt) {
        // not used
    }

    public void mmStructureChanged(MatchMakerEvent<TranslateGroupParent, MatchMakerTranslateGroup> evt) {
        logger.debug("Received structure changed event");
        fireChanged();
    }



}
