package ca.sqlpower.matchmaker.swingui;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.PlFolder;

public class FolderComboBoxCellRenderer extends DefaultListCellRenderer {
	Logger logger = Logger.getLogger(FolderComboBoxCellRenderer.class);

	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		setText(((PlFolder)value).getName());
		return this;
	}
}