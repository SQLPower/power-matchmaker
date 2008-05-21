/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of Power*MatchMaker.
 *
 * Power*MatchMaker is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Power*MatchMaker is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
 */

package ca.sqlpower.matchmaker.swingui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerObject;

/**
 * General-purpose cell renderer that deals with all types of MatchMakerObjects
 * by printing their name, or a customizable string if the object itself is null.
 */
public class MatchMakerObjectComboBoxCellRenderer extends DefaultListCellRenderer {
	Logger logger = Logger.getLogger(MatchMakerObjectComboBoxCellRenderer.class);

	/**
	 * The string to display when the value to render is null.  Defaults
	 * to <tt>"(none)"</tt>.
	 */
	private String nullValueString = "(none)";
	
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {

		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if (value == null) {
			setText(nullValueString);
			setForeground(Color.GRAY);
		} else {
			setText(((MatchMakerObject)value).getName());
		}
		return this;
	}

	public String getNullValueString() {
		return nullValueString;
	}

	public void setNullValueString(String nullValueString) {
		this.nullValueString = nullValueString;
	}
}