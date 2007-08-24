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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.i 
 */

package ca.sqlpower.matchmaker.swingui;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.tree.TreeModel;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.dao.PlFolderDAO;
import ca.sqlpower.validation.AlwaysOKValidator;
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;
import ca.sqlpower.validation.Validator;
import ca.sqlpower.validation.swingui.FormValidationHandler;
import ca.sqlpower.validation.swingui.StatusComponent;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * An EditorPane for displaying and editing information about a folder such as
 * its name and description.
 */
public class FolderEditor implements EditorPane {

	private static final Logger logger = Logger.getLogger(MatchEditor.class);
	private JPanel panel;
	private final MatchMakerSwingSession swingSession;
	private PlFolder<Match> folder;

	StatusComponent status = new StatusComponent();
	private FormValidationHandler handler;

	private JTextField folderName = new JTextField(40);
	private JTextArea folderDesc = new JTextArea(4,40);

	public FolderEditor(MatchMakerSwingSession swingSession, PlFolder<Match> folder) {
		this.swingSession = swingSession;
		this.folder = folder;
		handler = new FormValidationHandler(status);
		buildUI();
		setDefaultSelection();
		handler.addPropertyChangeListener(new PropertyChangeListener(){
			public void propertyChange(PropertyChangeEvent evt) {
				refreshActionStatus();
			}
        });
		handler.resetHasValidated();
	}

	private void buildUI() {
		folderName.setName("Folder Name");
		folderDesc.setName("Folder Description");

    	JButton saveButton = new JButton(saveAction);

    	FormLayout layout = new FormLayout(
				"4dlu,pref,4dlu,pref,4dlu,pref,4dlu", // columns
				"10dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,10dlu"); // rows
    	//		 1     2    3    4    5    6    7    8    9

		PanelBuilder pb;

		JPanel p = logger.isDebugEnabled() ? new FormDebugPanel(layout) : new JPanel(layout);
		pb = new PanelBuilder(layout, p);
		CellConstraints cc = new CellConstraints();

		pb.add(status, cc.xy(4,2,"r,c"));
		pb.add(new JLabel("Folder Name:"), cc.xy(2,4,"r,c"));
		pb.add(new JLabel("Description:"), cc.xy(2,6,"r,c"));

		pb.add(folderName, cc.xy(4,4,"l,c"));
		pb.add(new JScrollPane(folderDesc), cc.xy(4,6,"l,c"));

		pb.add(saveButton, cc.xyw(2,8,3,"c,c"));
		panel = pb.getPanel();

    }

	private void setDefaultSelection() {
		folderName.setText(folder.getName());
		folderDesc.setText(folder.getFolderDesc());

		Validator v1 = new FolderNameValidator(swingSession);
        handler.addValidateObject(folderName,v1);

        Validator v2 = new AlwaysOKValidator();
        handler.addValidateObject(folderDesc,v2);
	}

	private void refreshActionStatus() {
		ValidateResult worst = handler.getWorstValidationStatus();
    	saveAction.setEnabled(true);

    	if ( worst.getStatus() == Status.FAIL ) {
    		saveAction.setEnabled(false);
    	}
	}

	/**
     * Saves the folder
     */
	private Action saveAction = new AbstractAction("Save") {

		public void actionPerformed(final ActionEvent e) {
            /*
			 * It is essiental that doSave() does do all the saving work since
			 * doSave needs to return a boolean representing the successfulness
			 * of the saving process for the swing session to know if it needs
			 * to bring the panel back or not.
			 */
            doSave();
        }
	};

	public boolean doSave() {
        List<String> fail = handler.getFailResults();
        List<String> warn = handler.getWarnResults();

        if ( fail.size() > 0 ) {
            StringBuffer failMessage = new StringBuffer();
            for ( String f : fail ) {
                failMessage.append(f).append("\n");
            }
            JOptionPane.showMessageDialog(swingSession.getFrame(),
                    "You have to fix these errors before saving:\n"+failMessage.toString(),
                    "Folder error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        } else if ( warn.size() > 0 ) {
            StringBuffer warnMessage = new StringBuffer();
            for ( String w : warn ) {
                warnMessage.append(w).append("\n");
            }
            JOptionPane.showMessageDialog(swingSession.getFrame(),
                    "Warning: match will be saved with these warnings:\n"+warnMessage.toString(),
                    "Folder warning",
                    JOptionPane.INFORMATION_MESSAGE);
        }

        if ( !folderName.getText().equals(folder.getName()) ) {
            if ( swingSession.findFolder(folderName.getText()) != null ) {
                JOptionPane.showMessageDialog(getPanel(),
                        "Folder name \""+folderName.getText()+
                        "\" exist or invalid. The folder can not be saved",
                        "Folder name invalid",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
            folder.setName(folderName.getText());
        }
        folder.setFolderDesc(folderDesc.getText());
        logger.debug("Saving folder:" + folder.getName());

        if ( !swingSession.getCurrentFolderParent().getChildren().contains(folder) ) {
            TreeModel treeModel = swingSession.getTree().getModel();
            MatchMakerObject<?,?> root = (MatchMakerObject<?,?>) treeModel.getRoot();
            if (treeModel.getIndexOfChild(root, folder) == -1){
                swingSession.getCurrentFolderParent().addNewChild(folder);
            }
        }

        PlFolderDAO dao = (PlFolderDAO)swingSession.getDAO(PlFolder.class);
        dao.save(folder);
        handler.resetHasValidated();

		return true;
	}

	private class FolderNameValidator implements Validator {

		private MatchMakerSwingSession session;

		public FolderNameValidator(MatchMakerSwingSession session) {
    		this.session = session;
		}

		public ValidateResult validate(Object contents) {

			String value = (String)contents;
			if ( value == null || value.length() == 0 ) {
				return ValidateResult.createValidateResult(Status.FAIL,
						"Folder name is required");
			} else if ( !value.equals(folder.getName()) &&
						session.findFolder(value) != null ) {
				return ValidateResult.createValidateResult(Status.FAIL,
						"Folder name is invalid or already exists.");
			}
			return ValidateResult.createValidateResult(Status.OK, "");
		}
    }

	public JPanel getPanel() {
		return panel;
	}

	public boolean hasUnsavedChanges() {
		return handler.hasPerformedValidation();
	}
}
