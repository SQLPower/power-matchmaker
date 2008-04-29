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

import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.architect.ArchitectRuntimeException;
import ca.sqlpower.matchmaker.PlFolder;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.validation.ProjectNameValidator;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.validation.AlwaysOKValidator;
import ca.sqlpower.validation.Validator;
import ca.sqlpower.validation.swingui.FormValidationHandler;
import ca.sqlpower.validation.swingui.StatusComponent;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * The MatchEditor is the GUI for editing all aspects of a {@link Project} instance.
 */
public class ProjectEditor implements MatchMakerEditorPane<Project> {

	private static final Logger logger = Logger.getLogger(ProjectEditor.class);

	/**
	 * The panel that holds this editor's GUI.
	 */
	private final JPanel panel;

	private StatusComponent status = new StatusComponent();
    private JTextField projectName = new JTextField();
    private JTextArea desc = new JTextArea();

    private final MatchMakerSwingSession swingSession;

    /**
     * The project that this editor is editing.  If you want to edit a different match,
     * create a new ProjectEditor.
     */
	private final Project project;
	private FormValidationHandler handler;

    private PlFolder<Project> folder;

	
	/**
	 * Construct a ProjectEditor; for a project that is not new, we create a backup for it,
	 * and give it the name of the old one, when we save it, we will remove
	 * the backup from the folder, and insert the new one.
	 * @param swingSession  -- a MatchMakerSession
	 * @param project the project Object to be edited
	 * @param folder the project's parent folder
	 */
    public ProjectEditor(final MatchMakerSwingSession swingSession, Project project, PlFolder<Project> folder) throws ArchitectException {
        if (project == null) throw new IllegalArgumentException("You can't edit a null project");
        folder = swingSession.getDefaultPlFolder();
        
        this.swingSession = swingSession;
        this.project = project;
        this.folder = folder;
        handler = new FormValidationHandler(status, true);
        handler.setValidatedAction(saveAction);
        panel = buildUI();
        setDefaultSelections();
        addValidators();
        
        handler.resetHasValidated(); // avoid false hits when newly created
    }

    private void addValidators() {
    	Validator v = new ProjectNameValidator(swingSession,project);
        handler.addValidateObject(projectName,v);
        	
        Validator v6 = new AlwaysOKValidator();
        handler.addValidateObject(desc, v6);
    }
    
    /**
     * Saves the current project (which is referenced in the plMatch member variable of this editor instance).
     * If there is no current plMatch, a new one will be created and its properties will be set just like
     * they would if one had existed.  In either case, this action will then use Hibernate to save the
     * project object back to the database (but it should use the MatchHome interface instead).
     */
	private Action saveAction = new AbstractAction("Save") {
		public void actionPerformed(final ActionEvent e) {
            try {
                boolean ok = applyChanges();
                if (!ok) { 
                	JOptionPane.showMessageDialog(swingSession.getFrame(),
                			"Project Not Saved",
                			"Not Saved",JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception ex) {
                MMSUtils.showExceptionDialog(swingSession.getFrame(),
                		"Project Not Saved", ex);
            }
		}
	};

	private Window getParentWindow() {
	    return SPSUtils.getWindowInHierarchy(panel);
	}

    /**
     * Returns the parent (owning) frame of this project editor.  If the owner
     * isn't a frame (it might be a dialog or AWT Window) then null is returned.
     * You should always use {@link #getParentWindow()} in preference to
     * this method unless you really really need a JFrame.
     *
     * @return the parent JFrame of this project editor's panel, or null if
     * the owner is not a JFrame.
     */
    private JFrame getParentFrame() {
        Window owner = getParentWindow();
        if (owner instanceof JFrame) return (JFrame) owner;
        else return null;
    }

    private JPanel buildUI() {

    	projectName.setName("Project Name");
    	JButton saveProject = new JButton(saveAction);

    	FormLayout layout = new FormLayout(
				"4dlu,pref,4dlu,fill:min(pref;"+new JComboBox().getMinimumSize().width+"px):grow, 4dlu,pref,4dlu", // columns
				"10dlu,pref,4dlu,pref,4dlu,80dlu,4dlu,pref,10dlu"); // rows

		PanelBuilder pb;

		JPanel p = logger.isDebugEnabled() ? new FormDebugPanel(layout) : new JPanel(layout);
		pb = new PanelBuilder(layout, p);
		CellConstraints cc = new CellConstraints();
		int row = 2;
		pb.add(status, cc.xy(4,row));
		row += 2;
		pb.add(new JLabel("Project Name:"), cc.xy(2,row,"r,c"));
		pb.add(projectName, cc.xy(4,row));
		row += 2;
        desc.setWrapStyleWord(true);
        desc.setLineWrap(true);
		pb.add(new JLabel("Description:"), cc.xy(2,row,"r,t"));
		pb.add(new JScrollPane(desc), cc.xy(4,row,"f,f"));
		row += 2;
		
		// We don't want the save button to take up the whole column width
		// so we wrap it in a JPanel with a FlowLayout. If there is a better
		// way, please fix this.
		JPanel savePanel = new JPanel(new FlowLayout());
		savePanel.add(saveProject);
		pb.add(savePanel, cc.xy(4, row));

		return pb.getPanel();
    }


    private void setDefaultSelections() throws ArchitectException {

        projectName.setText(project.getName());
        desc.setText(project.getMungeSettings().getDescription());

    }

	public JPanel getPanel() {
		return panel;
	}

    /**
     * Copies all the values from the GUI components into the PlMatch
     * object this component is editing, then persists it to the database.
     * @return true if save OK
     * @throws ArchitectRuntimeException if we cannot set the result table on a project
     */
    public boolean applyChanges() {
    	List<String> fail = handler.getFailResults();

    	if ( fail.size() > 0 ) {
    		StringBuffer failMessage = new StringBuffer();
    		for ( String f : fail ) {
    			failMessage.append(f).append("\n");
    		}
    		JOptionPane.showMessageDialog(swingSession.getFrame(),
    				"You have to fix these errors before saving:\n"+failMessage.toString(),
    				"Project error",
    				JOptionPane.ERROR_MESSAGE);
    		return false;
    	}

        //sets the project name, id and desc
        project.getMungeSettings().setDescription(desc.getText());
        String id = projectName.getText();

		if (!id.equals(project.getName())) {
        	if (!swingSession.isThisProjectNameAcceptable(id)) {
        		JOptionPane.showMessageDialog(getPanel(),
        				"<html>Project name \"" + projectName.getText() +
        					"\" does not exist or is invalid.\n" +
        					"The project has not been saved",
        				"Project name invalid",
        				JOptionPane.ERROR_MESSAGE);
        		return false;
        	}
        	project.setName(id);
        }

        logger.debug(project.getResultTable());
		logger.debug("Saving Project:" + project.getName());
		handler.resetHasValidated();
        
        if (project.getParent() != swingSession.getDefaultPlFolder()) {
        	swingSession.getDefaultPlFolder().addChild(project);
        }
        logger.debug("Parent is " + project.getParent().getName());
        
        logger.debug(project.getResultTable());
        logger.debug("saving");
        swingSession.save(project);

		return true;
    }
    
	public boolean hasUnsavedChanges() {
		return handler.hasPerformedValidation();
	}

	public void discardChanges() {
		logger.error("Cannot discard changes");
	}

	public Project getCurrentEditingMMO() {
		return project;
	}
}