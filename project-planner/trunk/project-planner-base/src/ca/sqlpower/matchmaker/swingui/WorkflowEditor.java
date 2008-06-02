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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.event.MatchMakerEvent;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.munge.MungeStepOutput;
import ca.sqlpower.matchmaker.swingui.munge.MungePen;
import ca.sqlpower.matchmaker.swingui.munge.MungePenSideBar;
import ca.sqlpower.matchmaker.swingui.munge.MungeStepInfoComponent;
import ca.sqlpower.matchmaker.swingui.munge.MungeStepLibrary;
import ca.sqlpower.matchmaker.undo.AbstractUndoableEditorPane;
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;
import ca.sqlpower.validation.Validator;
import ca.sqlpower.validation.swingui.FormValidationHandler;
import ca.sqlpower.validation.swingui.StatusComponent;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Implements the DataEntryPanel functionality for editing a "munge process" (workflow).
 */
public class WorkflowEditor extends AbstractUndoableEditorPane<MungeProcess, MungeStep> {
    private static final Logger logger = Logger.getLogger(WorkflowEditor.class);
	
	/**
	 * The dark blue colour to be used as a background to the project steps
	 * side bar title.
	 */
	private static final Color DARK_BLUE = new Color(0x003082);
    
    /**
     * The project that is or will be the parent of the process we're editing.
     * If this editor was created for a new process, it will not belong to this
     * project until the doSave() method has been called.
     */
    private final Project parentProject;
    
    /**
     * The actual GUI component that provides the editing interface.
     */
    private final JTextField name = new JTextField();
	
    private final MungePen mungePen;
    
    /**
     * Validator for handling errors within the munge steps
     */
    private final StatusComponent status = new StatusComponent();
    private final FormValidationHandler handler;
    
    private final MatchMakerSwingSession swingSession;
    
    /**
     * Creates a new editor for the given session's given "munge process" (workflow).
     * 
     * @param swingSession The session the given project and process belong to
     * @param project The project that is or will become the process's parent. If the
     * process is new, it will not currently have a parent, but this editor will
     * connect the process to this project when saving. 
     * @param process The process to edit
     */
    public WorkflowEditor(MatchMakerSwingSession swingSession,
            Project project, MungeProcess process) throws ArchitectException {
        super(swingSession, process);
		this.swingSession = swingSession;
        logger.debug("Creating a new workflow editor");
        
        this.parentProject = project;
        if (mmo.getParentProject() != null && mmo.getParentProject() != parentProject) {
        	throw new IllegalStateException(
        	"The given workflow has a parent which is not the given parent project!");
        }

        // the handler stuff
        ArrayList<Action> actions = new ArrayList<Action>();
        actions.add(saveAction);
        this.handler = new FormValidationHandler(status, actions);
        handler.addValidateObject(name, new MungeProcessNameValidator());

        //For some reason some process don't have sessions and this causes a null
        //pointer barrage when it tries to find the tree when it gets focus, then fails and 
        //gets focus again.
        if (process.getSession() == null) {
        	process.setSession(swingSession);
        }
        
        this.mungePen = new MungePen(process, handler, swingSession);
        
        buildUI(process);
        setDefaults();
        addListenerToComponents();

    }

	private void buildUI(MungeProcess process) throws ArchitectException {
		panel = new JPanel(new BorderLayout());
		FormLayout layout = new FormLayout(
				"4dlu,pref,4dlu,fill:pref:grow,4dlu,pref,4dlu", // columns
				"4dlu,pref,4dlu,pref,4dlu"); // rows
		CellConstraints cc = new CellConstraints();
		JPanel subPanel = new JPanel(layout);
        subPanel.add(status, cc.xyw(2, 2, 5));
        subPanel.add(new JLabel("Workflow Name: "), cc.xy(2, 4));
        
        subPanel.add(name, cc.xy(4, 4));
        
        // removes save button if the user does not have rights
        if (parentProject.isOwner()) {
        	subPanel.add(new JButton(saveAction), cc.xy(6,4));
        } 
        
        panel.add(subPanel,BorderLayout.NORTH);
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.8);
		
		// adds a widget that allows the user to expand/collapse the splitpane
		splitPane.setDividerSize(10);
		splitPane.setOneTouchExpandable(true);
		
        splitPane.setLeftComponent(new JScrollPane(mungePen));
        MungeStepLibrary msl = new MungeStepLibrary(mungePen, ((SwingSessionContext) swingSession.getContext()).getStepMap());
        splitPane.setRightComponent(new MungePenSideBar(new MungeStepInfoComponent(mungePen).getPanel(),
        							  msl.getScrollPane(), "PROJECT STEPS", "(Drag into playpen)",
        							  DARK_BLUE).getToolbar());
        panel.add(splitPane, BorderLayout.CENTER);
    }
    
	private void addListenerToComponents() {
		name.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
			}
			public void keyReleased(KeyEvent e) {
				mmo.setName(name.getText());
			}
			public void keyTyped(KeyEvent e) {
			}});
	}
	
	private void setDefaults() {
		name.setText(mmo.getName());
	}
	
	Action saveAction = new AbstractAction("Save Workflow"){
		public void actionPerformed(ActionEvent e) {
            applyChanges();
		}
	};
    
    /**
     * Saves the process, possibly adding it to the parent project given in the
     * constructor if the process is not already a child of that project.
     */
    public boolean applyChanges() {
    	ValidateResult result = handler.getWorstValidationStatus();
        if ( result.getStatus() == Status.FAIL) {
            JOptionPane.showMessageDialog(swingSession.getFrame(),
                    "You have to fix the error before you can save the workflow",
                    "Save",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (parentProject == null) {
            parentProject.addMungeProcess(mmo);
        }
        
        return super.applyChanges();
    }

	public boolean hasUnsavedChanges() {
    	if (mmo.getParent() == null) {
			return true;
		} 
    	if (super.hasUnsavedChanges()) {
    		if (!parentProject.isOwner()) {
    			String warningMsg = "Only owners of the project can make changes!";
    			if (parentProject.canModify()) {
    				warningMsg += "\nPlease make a duplicate of the project instead.";
    			}
    			JOptionPane.showMessageDialog(swingSession.getFrame(), warningMsg, 
    					"Discarding changes", JOptionPane.WARNING_MESSAGE);
    			discardChanges();
    			return false;
    		}
    		return true;
    	}
    	return false;
    }
    
	private class MungeProcessNameValidator implements Validator {
		private static final int MAX_RULE_SET_NAME_CHAR = 30;
        public ValidateResult validate(Object contents) {
			String value = (String)contents;
			if ( value == null || value.length() == 0 ) {
				return ValidateResult.createValidateResult(Status.FAIL,
						"Workflow name is required");
			} else if ( !value.equals(mmo.getName()) &&
					parentProject.getMungeProcessByName(name.getText()) != null ) {
				return ValidateResult.createValidateResult(Status.FAIL,
						"Workflow name is invalid or already exists.");
			} else if (value.length() > MAX_RULE_SET_NAME_CHAR){
			    return ValidateResult.createValidateResult(Status.FAIL, 
                        "Workflow name cannot be more than " + MAX_RULE_SET_NAME_CHAR + " characters long");
            } else if (mmo.getParent() == null && parentProject.getMungeProcessByName(name.getText()) != null) {
            	return ValidateResult.createValidateResult(Status.FAIL, "Workflow name is invalid or already exists.");
            }
			return ValidateResult.createValidateResult(Status.OK, "");
		}
    }
	
	public MungeProcess getProcess() {
		return mmo;
	}
	
	public void setSelectedStep(MungeStep step) {
		mungePen.setSelectedStep(step);
	}
	
	public void setSelectedStepOutput(MungeStepOutput mso) {
		//TODO select the mso
	}

	@Override
	public void undoEventFired(MatchMakerEvent<MungeProcess, MungeStep> evt) {
		setDefaults();
	}

	public MungePen getMungePen() {
		return mungePen;
	}

}
