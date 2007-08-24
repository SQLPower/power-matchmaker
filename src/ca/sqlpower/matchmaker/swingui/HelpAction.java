/*
 * Copyright (c) 2007, SQL Power Group Inc.
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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JFrame;

/**
 * Intended to provide a help action for the main program;
 * for now it's just a placeholder.
 */
public class HelpAction extends AbstractAction {
	JFrame parent;
	public HelpAction(JFrame parent) {
		this.parent = parent;
		super.putValue(AbstractAction.NAME, "Help");
	}

	public void actionPerformed(ActionEvent e) {
		// XXX Hook up real help someday.
		JOptionPane.showMessageDialog(parent,
				"Help is not yet available. We apologize for the inconvenience");
	}
};
