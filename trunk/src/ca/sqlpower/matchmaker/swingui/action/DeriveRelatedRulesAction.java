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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JDialog;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.swingui.DeriveRelatedRulesPanel;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.swingui.DataEntryPanelBuilder;
import ca.sqlpower.swingui.SPSUtils;

/**
 * Generate merge rules for the tables that might be related to the 
 * source table.  Allows the user to choose the primary key for the
 * source table, and tries to find it as a foreign key on other tables.
 * 
 * Currently it only find grand child tables if the grand child table
 * contains the source table's primary key.
 */
public class DeriveRelatedRulesAction extends AbstractAction {

	private static final Logger logger = Logger.getLogger(DeriveRelatedRulesAction.class);
	
	private final Project project;
	private final MatchMakerSwingSession swingSession;

	public DeriveRelatedRulesAction(MatchMakerSwingSession swingSession, Project project) {
		super("Derive Related Rules");
		this.project = project;
		this.swingSession = swingSession;
	}

	public void actionPerformed(ActionEvent e) {
		DeriveRelatedRulesPanel panel = new DeriveRelatedRulesPanel(swingSession, project);
		JDialog dialog = DataEntryPanelBuilder.createDataEntryPanelDialog(
				panel, 
				swingSession.getFrame(), 
				"Related Table Deriver", 
				"Derive Related Rules");
		SPSUtils.makeJDialogCancellable(dialog, null, true);
		dialog.pack();
		dialog.setLocationRelativeTo(swingSession.getFrame());
		dialog.setVisible(true);
	}
}
