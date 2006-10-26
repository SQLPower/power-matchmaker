package ca.sqlpower.matchmaker.swingui;

import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

import ca.sqlpower.matchmaker.hibernate.PlMatchTranslateGroup;

public class TranslationComboBoxModel implements ComboBoxModel {
	List<PlMatchTranslateGroup> translations;
	PlMatchTranslateGroup selectedItem;
	
	public TranslationComboBoxModel() {
		
		translations = MatchMakerFrame.getMainInstance().getTranslations();
	}
	
	public Object getElementAt(int index) {
		return translations.get(index);
	}

	public int getSize() {
		return translations.size();
	}

	public Object getSelectedItem() {
		return selectedItem;
	}

	public void setSelectedItem(Object anItem) {
		
		selectedItem = (PlMatchTranslateGroup) anItem;
		
	}

	public void addListDataListener(ListDataListener l) {
		// TODO Auto-generated method stub
		
	}

	public void removeListDataListener(ListDataListener l) {
		// TODO Auto-generated method stub
		
	}



}
