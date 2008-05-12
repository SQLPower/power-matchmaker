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

package ca.sqlpower.matchmaker.swingui.action;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.concurrent.Callable;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.dao.ProjectDAO;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.matchmaker.swingui.NoEditEditorPane;
import ca.sqlpower.matchmaker.validation.ProjectNameValidator;
import ca.sqlpower.swingui.DataEntryPanelBuilder;
import ca.sqlpower.validation.Validator;
import ca.sqlpower.validation.swingui.FormValidationHandler;
import ca.sqlpower.validation.swingui.StatusComponent;

public class DuplicateProjectAction extends AbstractAction {
	
	private static final Logger logger = Logger.getLogger(DuplicateProjectAction.class);
	StatusComponent status = new StatusComponent();

	private MatchMakerSwingSession swingSession;
	private Project project;
	private Callable<Boolean> okCall;
	private Callable<Boolean> cancelCall;
	private FormValidationHandler handler;
	
	public DuplicateProjectAction(MatchMakerSwingSession swingSession, Project project) {
		super("Duplicate Project");
		this.swingSession = swingSession;
		this.project = project;
		handler = new FormValidationHandler(status);
	}
	
	private class DuplicatePanel extends NoEditEditorPane {

		private JTextField targetNameField;

		public DuplicatePanel(String newName) {
			JPanel panel = new JPanel(new GridLayout(3,1));
			panel.add(status);
			targetNameField = new JTextField(newName, 30);
			panel.add(targetNameField);
			setPanel(panel);
		}

		public String getDupName() {
			return targetNameField.getText();
		}

		public JTextField getProjectNameField() {
			return targetNameField;
		}
	}
	
	public void actionPerformed(ActionEvent e) {

		String newName = null;
		for (int count=0; ; count++) {
			newName = project.getName() +
								"_DUP" +
								(count==0?"":String.valueOf(count));
			if (swingSession.isThisProjectNameAcceptable(newName) )
				break;
		}
		final JDialog dialog;

		final DuplicatePanel archPanel = new DuplicatePanel(newName);

		okCall = new Callable<Boolean>() {
			public Boolean call() {
				String newName = archPanel.getDupName();
				Project newProject = ((ProjectDAO) swingSession.getDAO(Project.class)).duplicate(project, newName);
				swingSession.save(newProject);
				project.getParent().addChild(newProject);
				return new Boolean(true);
			}};
			
		cancelCall = new Callable<Boolean>() {
			public Boolean call() {
				return new Boolean(true);
			}};
			
		dialog = DataEntryPanelBuilder.createDataEntryPanelDialog(archPanel,
				swingSession.getFrame(),
				"Duplicate Project",
				"OK",
				okCall,
				cancelCall);
		
		dialog.pack();
		dialog.setLocationRelativeTo(swingSession.getFrame());
		dialog.setVisible(true);
		
		Validator v = new ProjectNameValidator(swingSession,new Project());
        handler.addValidateObject(archPanel.getProjectNameField(),v);
	}

}
