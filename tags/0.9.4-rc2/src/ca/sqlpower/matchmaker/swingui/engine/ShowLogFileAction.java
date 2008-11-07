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

package ca.sqlpower.matchmaker.swingui.engine;

import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JTextField;

import com.darwinsys.notepad.Notepad;

/**
 * An action for displaying the contents of a text file in a popup
 * window.  Which file to show is determined by the contents of a text
 * field.
 */
class ShowLogFileAction extends AbstractAction {

	/**
	 * The text field that contains the pathname of the file to view.
	 */
	private final JTextField filenameField;
	
	/**
	 * Creates a new action that tries to load and display the text file at
	 * the pathname in the text field given.
	 * 
	 * @param filenameField The field that contains the pathname of the file
	 * to read.  This text field will be asked for its contents every time the
	 * action is invoked, in case its contents change over time.
	 */
	public ShowLogFileAction(JTextField filenameField) {
		super("Show Log File...");
		this.filenameField = filenameField;
	}

	public void actionPerformed(ActionEvent e) {
		String logFileName = filenameField.getText();
		try {
			Notepad notepad = new Notepad(false);
			notepad.doLoad(logFileName);
		} catch (IOException e1) {
			throw new RuntimeException("Unable to view log file "
					+ logFileName, e1);
		}

	}
}