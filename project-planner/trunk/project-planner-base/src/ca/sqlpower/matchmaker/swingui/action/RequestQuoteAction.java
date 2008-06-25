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
import java.util.concurrent.Callable;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextPane;

import ca.sqlpower.matchmaker.FolderParent;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.swingui.MatchMakerObjectComboBoxCellRenderer;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.matchmaker.swingui.NoEditEditorPane;
import ca.sqlpower.swingui.DataEntryPanelBuilder;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * An action that handles sending a request for quotation on a project.
 */
public class RequestQuoteAction extends AbstractAction {

	private MatchMakerSwingSession swingSession;
	private Project project;

	public RequestQuoteAction(MatchMakerSwingSession swingSession, Project project) {
		super("Request Quote");
		this.swingSession = swingSession;
		this.project = project;
	}
	
	public void actionPerformed(ActionEvent e) {
		if (project == null) {
			showDialog();
		} else {
			sendRequest();
		}
	}
	
	/**
	 * Actually does the call to send request and displays a success message if appropriate.
	 */
	private void sendRequest() {
		if (swingSession.requestQuote(project)) {
			JOptionPane.showMessageDialog(swingSession.getFrame(), 
					"Thank you for your request. One of our sales representatives will contact you soon for the results!",
					"Quote Request Sent", JOptionPane.INFORMATION_MESSAGE);
		}	
	}
	
	private class RequestQuotePanel extends NoEditEditorPane {

		private final List<Project> projects;
		private JComboBox projectList;
		
		public RequestQuotePanel(List<Project> projects) {
	        this.projects = projects;
	        buildUI();
		}
		
		private void buildUI() {
	        FormLayout layout = new FormLayout("pref,4dlu,150dlu", "60dlu,4dlu,pref,4dlu");
	        JPanel panel = new JPanel(layout);
	        CellConstraints cc = new CellConstraints();
			
	        String message = "The <i>request quote</i> feature lets you send your project diagram to the SQL Power " +
	        		"sales team, who will review it and then provide you with an estimate of the work involved.";
	        JTextPane infoTextPane = new JTextPane();
	        infoTextPane.setContentType("text/html");
	        infoTextPane.setText(message);
	        infoTextPane.setBackground(null);
	        infoTextPane.setEditable(false);
	        panel.add(infoTextPane, cc.xyw(1, 1, 3));

	        JLabel projectLabel = new JLabel("Project:");
			panel.add(projectLabel, cc.xy(1, 3));
	        
	        projectList = new JComboBox(projects.toArray());
	        projectList.setEditable(false);
	        projectList.setRenderer(new MatchMakerObjectComboBoxCellRenderer());
	        panel.add(projectList, cc.xy(3, 3, "f,f"));

	        setPanel(panel);		
		}
		
		public Project getSelectedProject() {
			return (Project) projectList.getSelectedItem();
		}
	}

	/**
	 * Builds a dialog for the user to choose the project for quoting.
	 */
	private void showDialog() {
		// builds the list of all projects
		final List<Project> projects = new ArrayList<Project>();
		FolderParent folderParent = swingSession.getCurrentFolderParent();
		for (PlFolder<Project> folder : folderParent.getChildren()){
			projects.addAll(folder.getChildren());
		}
		
		if (projects.size() == 0) {
			JOptionPane.showMessageDialog(swingSession.getFrame(),
					"Please first create a project for quoting.",
					"Failed to Send Request for Quote", JOptionPane.WARNING_MESSAGE);
			return;
		}
		
		
		final RequestQuotePanel requestQuotePanel = new RequestQuotePanel(projects);

		final JDialog dialog = DataEntryPanelBuilder.createDataEntryPanelDialog (
				requestQuotePanel,
				swingSession.getFrame(),
				"Request Quote",
				"OK",
				new Callable<Boolean>(){
					public Boolean call() {
						// makes call to send request on selected project
						project = requestQuotePanel.getSelectedProject();
						sendRequest();
						
						// next call need to show dialog regardless
						project = null;
						
						return new Boolean(true);
					}
				}, 
				new Callable<Boolean>(){
					public Boolean call() {
						return new Boolean(true);
					}
				});

		dialog.pack();
		dialog.setLocationRelativeTo(swingSession.getFrame());
		dialog.setVisible(true);
	}
}
