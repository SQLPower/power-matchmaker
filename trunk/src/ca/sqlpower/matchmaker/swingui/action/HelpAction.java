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


package ca.sqlpower.matchmaker.swingui.action;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.net.URL;

import javax.help.CSH;
import javax.help.HelpBroker;
import javax.help.HelpSet;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JFrame;

import ca.sqlpower.swingui.SPSUtils;

/**
 * Opens up the help file.
 */
public class HelpAction extends AbstractAction {
	JFrame parent;
	
	public HelpAction(JFrame parent, Icon icon) {
		super("Help", icon);
		this.parent = parent;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			String helpHS = "jhelpset.hs";
			ClassLoader cl = getClass().getClassLoader();
			URL hsURL = HelpSet.findHelpSet(cl, helpHS);
			HelpSet hs = new HelpSet(null, hsURL);
			HelpBroker hb = hs.createHelpBroker();
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

			// Default HelpBroker size is too small, make bigger unless on anciente "VGA" resolution
			if (d.width >= 1024 && d.height >= 800) {
				hb.setSize(new Dimension(1024, 700));
			} else {
				hb.setSize(new Dimension(640, 480));
			}
			CSH.DisplayHelpFromSource helpDisplay = new CSH.DisplayHelpFromSource(hb);
			helpDisplay.actionPerformed(e);

		} catch (Exception ev) {
			setEnabled(false);
			SPSUtils.showExceptionDialogNoReport(parent,
					"Could not load the help file. The MatchMakerHelp.jar file either " +
					"doesn't exist or isn't in your classpath.\nThis error usually " +
					"occurrs because you are running the MatchMaker within an IDE.\n" +
					"The Help function is now disabled",
					ev);
		}         
	}
};
