package ca.sqlpower.matchmaker.swingui;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.SQLObject;

public class SQLObjectComboBoxCellRenderer extends DefaultListCellRenderer {

	Logger logger = Logger.getLogger(SQLObjectComboBoxCellRenderer.class);

	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if ( value != null ) {
			setText(((SQLObject)value).getName());
		}
		return this;
	}
}


