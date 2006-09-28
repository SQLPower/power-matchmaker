package ca.sqlpower.matchmaker.swingui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

import ca.sqlpower.matchmaker.hibernate.PlMatchTranslate;

public class TranslationComboBoxModel implements ComboBoxModel {
	List<String> translations;
	String selectedItem;
	
	public TranslationComboBoxModel() {
		Set<String> translationTopics = new TreeSet<String>();
		translationTopics.add("");
		for (PlMatchTranslate t : MatchMakerFrame.getMainInstance().getTranslations() ){
			translationTopics.add(t.getId().getGroupName());
		}
		translations = new ArrayList<String>(translationTopics);
		Collections.sort(translations);
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
		
		selectedItem = (String)anItem;
		
	}

	public void addListDataListener(ListDataListener l) {
		// TODO Auto-generated method stub
		
	}

	public void removeListDataListener(ListDataListener l) {
		// TODO Auto-generated method stub
		
	}



}
