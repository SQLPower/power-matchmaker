package ca.sqlpower.matchmaker.swingui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import ca.sqlpower.matchmaker.MatchMakerFrame;
import ca.sqlpower.matchmaker.hibernate.DefaultHibernateObject;
import ca.sqlpower.matchmaker.hibernate.PlFolder;

public class FolderComboBoxModel
	implements ComboBoxModel, FolderListChangeListener {

	private MatchMakerFrame parent;
	List<ListDataListener> listenerList;
	private DefaultHibernateObject selectedItem;


	public FolderComboBoxModel(MatchMakerFrame parent) {
		super();
		this.parent = parent;
		listenerList = new ArrayList<ListDataListener>();
	}

	public void setSelectedItem(Object anItem) {

		int selectedIndex = parent.getFolders().indexOf(anItem);
		if (selectedIndex >= 0) {
			if ( anItem instanceof PlFolder ) {
				selectedItem = (PlFolder)anItem;
			} else if ( anItem== null ) {
				selectedItem = null;
			}
			fireContentChangedEvent(selectedIndex);
		}

	}

	public Object getSelectedItem() {
		return selectedItem;
	}

	public int getSize() {
		return parent.getFolders().size();
	}

	public Object getElementAt(int index) {
		return parent.getFolders().get(index);
	}

	public void addListDataListener(ListDataListener l) {
		listenerList.add(l);

	}

	public void removeListDataListener(ListDataListener l) {
		listenerList.remove(l);
	}

	private void fireContentChangedEvent(int index) {
		for (int i = listenerList.size() -1 ; i>=0; i--) {
			listenerList.get(i).contentsChanged(
					new ListDataEvent(this,
							ListDataEvent.CONTENTS_CHANGED,
							index,index));
		}
	}
	public void folderAdded(ListDataEvent e) {
		for (int i = listenerList.size() -1 ; i>=0; i--) {
			listenerList.get(i).contentsChanged(e);
		}

	}

	public void folderRemove(ListDataEvent e) {
		for (int i = listenerList.size() -1 ; i>=0; i--) {
			listenerList.get(i).contentsChanged(e);
		}
	}


}
