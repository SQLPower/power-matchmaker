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
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import ca.sqlpower.matchmaker.FolderParent;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.swingui.MatchMakerObjectComboBoxCellRenderer;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.swingui.JDefaultButton;
import ca.sqlpower.swingui.SPSUtils;

import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * An action that handles sending a request for quotation on a project.
 */
public class RequestQuoteAction extends AbstractAction {

	private MatchMakerSwingSession swingSession;
	
	// if not null, dialog appears with selected
	private Project project;
	
	private JList projectList;
	private JTextPane comments;
	private JDialog dialog;

	public RequestQuoteAction(MatchMakerSwingSession swingSession) {
		super("Request Quote");
		this.swingSession = swingSession;
	}
	
	public RequestQuoteAction(MatchMakerSwingSession swingSession, Project project) {
		this(swingSession);
		this.project = project;
	}
	
	public void actionPerformed(ActionEvent e) {
		// builds the list of all projects
		final List<Project> projects = new ArrayList<Project>();
		FolderParent folderParent = swingSession.getCurrentFolderParent();
		for (PlFolder<Project> folder : folderParent.getChildren()){
			projects.addAll(folder.getChildren());
		}
		
		if (projects.size() == 0) {
			JOptionPane.showMessageDialog(swingSession.getFrame(),
					"Please first create a project for quoting.",
					"Failed to Send Quote Request", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		buildUI(projects);
		
		if (project != null) {
			projectList.setSelectedValue(project, true);
			projectList.ensureIndexIsVisible(projectList.getSelectedIndex());
		}

		dialog.setLocationRelativeTo(swingSession.getFrame());
		dialog.setVisible(true);
	}

	private void buildUI(List<Project> projects) {
		FormLayout layout = new FormLayout("10dlu,fill:250dlu:grow,10dlu",
				"10dlu,pref,4dlu,pref,4dlu,fill:default:grow,4dlu,pref,4dlu,50dlu,4dlu,pref,10dlu");
		JPanel panel = new JPanel(layout);
		CellConstraints cc = new CellConstraints();

		int row = 2;

		String message = "The <i>request quote</i> feature lets you send your project diagrams to the SQL Power " +
		"sales team, who will review it and then provide you with an estimate of the work involved.";
		JTextPane infoTextPane = new JTextPane();
		infoTextPane.setContentType("text/html");
		infoTextPane.setText(message);
		infoTextPane.setBackground(null);
		infoTextPane.setEditable(false);
		panel.add(infoTextPane, cc.xy(2, row));

		row += 2;
		panel.add(new JLabel("Projects:"), cc.xy(2, row));

		row += 2;
		projectList = new JList(projects.toArray());
		projectList.setCellRenderer(new MatchMakerObjectComboBoxCellRenderer());
		panel.add(new JScrollPane(projectList), cc.xy(2, row));

		row += 2;
		panel.add(new JLabel("Comment:"), cc.xy(2, row));

		row += 2;
		comments = new JTextPane();
		panel.add(new JScrollPane(comments), cc.xy(2, row, "f,f"));

		JDefaultButton okButton = new JDefaultButton(new AbstractAction("OK") {
			public void actionPerformed(ActionEvent arg0) {
				// find selected projects
				List<Project> projects = new ArrayList<Project>();
				for (Object obj : projectList.getSelectedValues()) {
					projects.add((Project) obj);
				}

				if (projects.size() > 0) {
					// makes call to send request on selected projects
					if (swingSession.requestQuote(projects, comments.getText())) {
						JOptionPane.showMessageDialog(dialog, 
								"Thank you for your request. One of our sales representatives will contact you soon for the results!",
								"Quote Request Sent", JOptionPane.INFORMATION_MESSAGE);
					}
					
					dialog.dispose();
				} else {
					JOptionPane.showMessageDialog(dialog, "Please select at least one project!" ,
						"No Projects Selected", JOptionPane.WARNING_MESSAGE);
				}
			}
		});

		Action cancelAction = new AbstractAction("Cancel") {
			public void actionPerformed(ActionEvent arg0) {
				dialog.dispose();
			}
			
		};
		JButton cancelButton = new JButton(cancelAction);
		
		row += 2;
		panel.add(ButtonBarFactory.buildOKCancelBar(okButton, cancelButton), cc.xy(2, row));

		dialog = new JDialog(swingSession.getFrame());
		dialog.getRootPane().setDefaultButton(okButton);
		dialog.setContentPane(panel);
		dialog.setTitle("Request Quote");
		dialog.pack();
		
		// we don't want the user to be modifying projects at this point
		dialog.setModal(true);
		
		SPSUtils.makeJDialogCancellable(dialog, cancelAction);
	}
	
	public JDialog getDialog() {
		return dialog;
	}
}
