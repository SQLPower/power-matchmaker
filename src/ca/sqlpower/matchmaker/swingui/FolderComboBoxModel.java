package ca.sqlpower.matchmaker.swingui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.hibernate.DefaultHibernateObject;

public class FolderComboBoxModel <t extends MatchMakerObject>
	implements ComboBoxModel, FolderListChangeListener {

	List<ListDataListener> listenerList;
	private DefaultHibernateObject selectedItem;
	List<t> list;


	public FolderComboBoxModel( List<t> list) {
		super();
		this.list = list;
		listenerList = new ArrayList<ListDataListener>();
	}

	public void setSelectedItem(Object anItem) {

		int selectedIndex = list.indexOf(anItem);
		if (selectedIndex >= 0) {
			if ( anItem instanceof DefaultHibernateObject ) {
				selectedItem = (DefaultHibernateObject) anItem;
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
		return list.size();
	}

	public Object getElementAt(int index) {
		return list.get(index);
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
