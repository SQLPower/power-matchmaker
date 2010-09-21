/*
 * Copyright (c) 2008, SQL Power Group Inc.
 *
 * This file is part of DQguru
 *
 * DQguru is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DQguru is distributed in the hope that it will be useful,
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
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import org.apache.log4j.Logger;

import ca.sqlpower.graph.DepthFirstSearch;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.event.MatchMakerEvent;
import ca.sqlpower.matchmaker.munge.AbstractMungeStep;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.munge.MungeProcessGraphModel;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.munge.MungeStepOutput;
import ca.sqlpower.matchmaker.swingui.munge.MungePen;
import ca.sqlpower.matchmaker.swingui.munge.MungeStepLibrary;
import ca.sqlpower.matchmaker.undo.AbstractUndoableEditorPane;
import ca.sqlpower.sqlobject.SQLObjectException;
import ca.sqlpower.swingui.ColorCellRenderer;
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;
import ca.sqlpower.validation.Validator;
import ca.sqlpower.validation.swingui.FormValidationHandler;
import ca.sqlpower.validation.swingui.StatusComponent;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Implements the DataEntryPanel functionality for editing a munge process (MatchRuleSet).
 */
public class MungeProcessEditor extends AbstractUndoableEditorPane {
    private static final Logger logger = Logger.getLogger(MungeProcessEditor.class);

	
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
    private final JTextField desc = new JTextField();
    private final JComboBox color = new JComboBox(ColorScheme.BREWER_SET19.toArray());
	
    private final MungePen mungePen;
    
    /**
     * Validator for handling errors within the munge steps
     */
    private final StatusComponent status = new StatusComponent();
    private final FormValidationHandler handler;
    
    /**
     * Creates a new editor for the given session's given munge process.
     * 
     * @param swingSession The session the given project and process belong to
     * @param project The project that is or will become the process's parent. If the
     * process is new, it will not currently have a parent, but this editor will
     * connect the process to this project when saving. 
     * @param process The process to edit
     */
    public MungeProcessEditor(MatchMakerSwingSession swingSession,
            Project project, MungeProcess process) throws SQLObjectException {
        super(swingSession, process);
        logger.debug("Creating a new munge process editor");
        
        this.parentProject = project;
        if (mmo.getParentProject() != null && mmo.getParentProject() != parentProject) {
        	throw new IllegalStateException(
        	"The given process has a parent which is not the given parent match obejct!");
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
        
        this.mungePen = new MungePen(process, handler, parentProject);
        
        stepPrecheckResults = new ArrayList<ValidateResult>();
        
        for (MungeStep step : process.getChildren()) {
			if (step instanceof AbstractMungeStep) {
				((AbstractMungeStep) step).setPreviewMode(true);
			}
			
			stepPrecheckResults.addAll(step.checkPreconditions());
        }
        
        buildUI();
        setDefaults();
        addListenerToComponents();
        

    }

	private void buildUI() throws SQLObjectException {
		panel = new JPanel(new BorderLayout());
		FormLayout layout = new FormLayout(
				"4dlu,pref,4dlu,fill:pref:grow,4dlu,pref,4dlu,pref,4dlu", // columns
				"4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu"); // rows
		CellConstraints cc = new CellConstraints();
		JPanel subPanel = new JPanel(layout);
        subPanel.add(status, cc.xyw(2, 2, 7));
        subPanel.add(new JLabel("Process Name: "), cc.xy(2, 4));
        subPanel.add(name, cc.xy(4, 4));
        
        subPanel.add(new JLabel("Description: "), cc.xy(2, 6));
        subPanel.add(desc, cc.xy(4, 6));
        
        subPanel.add(new JLabel("Colour: "), cc.xy(6, 6));
        ColorCellRenderer renderer = new ColorCellRenderer(85, 50);
        color.setRenderer(renderer);
        subPanel.add(color, cc.xy(8, 6));
		subPanel.add(new JButton(saveAction), cc.xy(2,8));
		subPanel.add(mungePen.getEnablePreviewCheckBox(), cc.xy(4, 8));
		subPanel.add(new JButton(customColour), cc.xy(8,8));
        panel.add(subPanel,BorderLayout.NORTH);
        
        JToolBar t = new JToolBar();
        
        MungeStepLibrary msl = new MungeStepLibrary(mungePen, ((SwingSessionContext) swingSession.getContext()).getStepMap());
        t.setBackground(Color.WHITE);
        t.setLayout(new BorderLayout());
        t.add(msl.getHideShowButton(), BorderLayout.NORTH);
        t.add(new JScrollPane(msl.getList()), BorderLayout.CENTER);
        t.setBorder(BorderFactory.createRaisedBevelBorder());
        t.setFloatable(false);
        panel.add(new JScrollPane(mungePen), BorderLayout.CENTER);
        panel.add(t,BorderLayout.EAST);
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
		desc.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
			}
			public void keyReleased(KeyEvent e) {
				mmo.setDesc(desc.getText());
			}
			public void keyTyped(KeyEvent e) {
			}});
		color.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
		    	mmo.setColour((Color)color.getSelectedItem());
			}});
	}
	
	private void setDefaults() {
		name.setText(mmo.getName());
		
		desc.setText(mmo.getDesc());
		boolean hasColour = false;
       	for (int i = 0; i < color.getItemCount(); i++) {
       		if (color.getItemAt(i).equals(mmo.getColour())) {
       			hasColour = true;
       			break;
       		}
       	}
       	if (!hasColour) {
       		color.addItem(mmo.getColour());
       	}
       	if (mmo.getColour() != null) {
       		color.setSelectedItem(mmo.getColour());
       	} else {
       		color.setSelectedIndex(0);
       		mmo.setColour((Color)color.getSelectedItem());
       	}
	}
	
	Action saveAction = new AbstractAction("Save Transformation"){
		public void actionPerformed(ActionEvent e) {
            applyChanges();
		}
	};
	Action customColour = new AbstractAction("Custom Colour...") {
		public void actionPerformed(ActionEvent arg0) {
			Color colour = swingSession.getCustomColour(mmo.getColour());
		    if (colour != null) {
		    	// TODO add colour(Color) only if it's not in the color(JComboBox) yet
		    	color.addItem(colour);
		    	color.setSelectedItem(colour);
		    }
		}
	};


	private List<ValidateResult> stepPrecheckResults;
    
    /**
     * Saves the process, possibly adding it to the parent project given in the
     * constructor if the process is not already a child of that project.
     */
    public boolean applyChanges() {
    	ValidateResult result = handler.getWorstValidationStatus();
        if ( result.getStatus() == Status.FAIL) {
            JOptionPane.showMessageDialog(swingSession.getFrame(),
                    "You have to fix the error before you can save the transformation",
                    "Save",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        MungeProcessGraphModel gm = new MungeProcessGraphModel(mmo.getChildren());
        DepthFirstSearch<MungeStep, MungeProcessGraphModel.Edge> dfs = new DepthFirstSearch<MungeStep, MungeProcessGraphModel.Edge>();
        dfs.performSearch(gm);
        
        if (dfs.isCyclic()) {
        	int responds = JOptionPane.showConfirmDialog(swingSession.getFrame(),
				"Your transformation contains at least one cycle, " + 
				"and may result in unexpected results. \n" + 
				"Do you want to continue saving?", 
				"Save",
                JOptionPane.WARNING_MESSAGE);
			if (responds != JOptionPane.YES_OPTION) {
				return false;
	        }
        }

        if (mmo.getParentProject() == null) {
            parentProject.addMungeProcess(mmo);
        }
        return super.applyChanges();
    }

    public boolean hasUnsavedChanges() {
    	if (mmo.getParent() == null) {
			return true;
		}
        return super.hasUnsavedChanges();
    }
    
    @Override
    public void cleanup() {
        for (MungeStep step : getCurrentEditingMMO().getChildren()) {
			if (step instanceof AbstractMungeStep) {
				((AbstractMungeStep) step).setPreviewMode(false);
			}
        }
        mungePen.cleanup();
        super.cleanup();
    }
    
	private class MungeProcessNameValidator implements Validator {
		private static final int MAX_RULE_SET_NAME_CHAR = 30;
        public ValidateResult validate(Object contents) {
			String value = (String)contents;
			if ( value == null || value.length() == 0 ) {
				return ValidateResult.createValidateResult(Status.FAIL,
						"Transformation name is required");
			} else if ( !value.equals(mmo.getName()) &&
					parentProject.getMungeProcessByName(name.getText()) != null ) {
				return ValidateResult.createValidateResult(Status.FAIL,
						"Transformation name is invalid or already exists.");
			} else if (value.length() > MAX_RULE_SET_NAME_CHAR){
			    return ValidateResult.createValidateResult(Status.FAIL, 
                        "Transformation name cannot be more than " + MAX_RULE_SET_NAME_CHAR + " characters long");
            } else if (mmo.getParent() == null && parentProject.getMungeProcessByName(name.getText()) != null) {
            	return ValidateResult.createValidateResult(Status.FAIL, "Transformation name is invalid or already exists.");
            }
			return ValidateResult.createValidateResult(Status.OK, "");
		}
    }
	
	private class CleanseProjectProcessPriorityValidator implements Validator {
        public ValidateResult validate(Object contents) {
        	
        	if (contents == null) {
        		return ValidateResult.createValidateResult(Status.WARN, "No priority set, assuming 0");
        	}
        	
			short value = Short.parseShort((String)contents.toString());
		
			for (MungeProcess mp : parentProject.getMungeProcessesFolder().getChildren()) {
                if (mp == null) throw new NullPointerException("Null munge process in project!");
				short otherPriority = 0;
                if (mp.getMatchPriority() != null) {
                    otherPriority = mp.getMatchPriority().shortValue();
                }
                if (otherPriority == value && mp != mmo) {
					return ValidateResult.createValidateResult(Status.WARN, "Duplicate Priority. " + 
							"If two cleansing process have the same priority, they may not always run in the same order.");
				}
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
