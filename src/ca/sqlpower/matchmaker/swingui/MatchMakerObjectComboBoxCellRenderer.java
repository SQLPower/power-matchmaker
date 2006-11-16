package ca.sqlpower.matchmaker.swingui;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerObject;

public class MatchMakerObjectComboBoxCellRenderer extends DefaultListCellRenderer {
	Logger logger = Logger.getLogger(MatchMakerObjectComboBoxCellRenderer.class);

	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if ( value != null ) {
			setText(((MatchMakerObject)value).getName());
		}
		return this;
	}
}