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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.tree.TreePath;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.graph.DepthFirstSearch;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.Project.ProjectMode;
import ca.sqlpower.matchmaker.dao.MatchMakerDAO;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.munge.MungeProcessGraphModel;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.munge.MungeStepOutput;
import ca.sqlpower.matchmaker.swingui.munge.AbstractMungeComponent;
import ca.sqlpower.matchmaker.swingui.munge.MungePen;
import ca.sqlpower.matchmaker.swingui.munge.MungeStepLibrary;
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;
import ca.sqlpower.validation.Validator;
import ca.sqlpower.validation.swingui.FormValidationHandler;
import ca.sqlpower.validation.swingui.StatusComponent;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * Implements the EditorPane functionality for editing a munge process (MatchRuleSet).
 */
public class MungeProcessEditor implements EditorPane {
    
    /**
     * The session this editor belongs to.
     */
    private final MatchMakerSwingSession swingSession;
    
    /**
     * The project that is or will be the parent of the process we're editing.
     * If this editor was created for a new process, it will not belong to this
     * project until the doSave() method has been called.
     */
    private final Project parentProject;
    
    /**
     * The munge process this editor is responsible for editing.
     */
    private MungeProcess process;
    
    /**
     * The actual GUI component that provides the editing interface.
     */
    private final JPanel panel = new JPanel(new BorderLayout());
    private final JTextField name = new JTextField();
    private final JSpinner priority = new JSpinner();
    private final JTextField desc = new JTextField();
    private final JComboBox color = new JComboBox(ColorScheme.BREWER_SET19);
    
    private final MungePen mungePen;
    
    /**
     * The instance that monitors the subtree we're editing for changes (so we know
     * if there are unsaved changes).
     */
    private final MMOChangeWatcher<MungeProcess, MungeStep> changeHandler;
    
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
            Project project, MungeProcess process) throws ArchitectException {
        super();
        this.swingSession = swingSession;
        this.parentProject = project;
        this.process = process;
        this.changeHandler = new MMOChangeWatcher<MungeProcess, MungeStep>(process);
        
        //For some reason some process don't have sessions and this causes a null
        //pointer barrage when it tries to find the tree when it gets focus, then fails and 
        //gets focus again.
        if (process.getSession() == null) {
        	process.setSession(swingSession);
        }
        ArrayList<Action> actions = new ArrayList<Action>();
        actions.add(saveAction);
        this.handler = new FormValidationHandler(status, actions);
        this.mungePen = new MungePen(process, handler, parentProject);

        buildUI();
        if (process.getParentProject() != null && process.getParentProject() != parentProject) {
            throw new IllegalStateException(
                    "The given process has a parent which is not the given parent match obejct!");
        }
        handler.addValidateObject(name, new MungeProcessNameValidator());
        
        if (project.getType() == ProjectMode.CLEANSE) {
        	handler.addValidateObject(priority, new CleanseProjectProcessPriorityValidator());
        }
        
        
    }

	private void buildUI() throws ArchitectException {
		FormLayout layout = new FormLayout(
				"4dlu,pref,4dlu,fill:pref:grow,4dlu,pref,4dlu,pref,4dlu", // columns
				"4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu"); // rows
		CellConstraints cc = new CellConstraints();
		JPanel subPanel = new JPanel(layout);
        subPanel.add(status, cc.xyw(2, 2, 7));
        subPanel.add(new JLabel("Munge Process Name: "), cc.xy(2, 4));
        name.setText(process.getName());
        subPanel.add(name, cc.xy(4, 4));
        subPanel.add(new JLabel("Priority: "), cc.xy(6, 4));
        if (process.getMatchPercent() != null) {
        	priority.setValue(process.getMatchPercent());
        }
        priority.setPreferredSize(new Dimension(100, 20));
        subPanel.add(priority, cc.xy(8, 4));
        
        subPanel.add(new JLabel("Munge Process Desc: "), cc.xy(2, 6));
        desc.setText(process.getDesc());
        subPanel.add(desc, cc.xy(4, 6));
        subPanel.add(new JLabel("Color: "), cc.xy(6, 6));
        ColorCellRenderer renderer = new ColorCellRenderer();
        color.setRenderer(renderer);
       	boolean hasColour = false;
       	for (int i = 0; i < color.getItemCount(); i++) {
       		if (color.getItemAt(i).equals(process.getColour())) {
       			hasColour = true;
       			break;
       		}
       	}
       	if (!hasColour) {
       		color.addItem(process.getColour());
       	}
       	if (process.getColour() != null) {
       		color.setSelectedItem(process.getColour());
       	} else {
       		color.setSelectedIndex(0);
       	}
        subPanel.add(color, cc.xy(8, 6));
        
		subPanel.add(new JButton(saveAction), cc.xy(2,8));
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
    
	Action saveAction = new AbstractAction("Save Munge Process"){
		public void actionPerformed(ActionEvent e) {
            doSave();
		}
	};
	Action customColour = new AbstractAction("Custom Colour") {
		public void actionPerformed(ActionEvent arg0) {
			Color colour = swingSession.getCustomColour(process.getColour());
		    if (colour != null) {
		    	color.addItem(colour);
		    	color.setSelectedItem(colour);
		    }
		}
	};
    
    /**
     * Saves the process, possibly adding it to the parent project given in the
     * constructor if the process is not already a child of that project.
     */
    public boolean doSave() {
    	ValidateResult result = handler.getWorstValidationStatus();
        if ( result.getStatus() == Status.FAIL) {
            JOptionPane.showMessageDialog(swingSession.getFrame(),
                    "You have to fix the error before you can save the munge process",
                    "Save",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        MungeProcessGraphModel gm = new MungeProcessGraphModel(process.getChildren());
        DepthFirstSearch<MungeStep, MungeProcessGraphModel.Edge> dfs = new DepthFirstSearch<MungeStep, MungeProcessGraphModel.Edge>();
        dfs.performSearch(gm);
        
        if (dfs.isCyclic()) {
        	 JOptionPane.showMessageDialog(swingSession.getFrame(),
                     "Your munge process contains at least one cycle.\nThis may result in unexpected results.",
                     "Save",
                     JOptionPane.WARNING_MESSAGE);
        }
        
    	
    	process.setName(name.getText());
    	process.setDesc(desc.getText());
    	process.setColour((Color)color.getSelectedItem());
    	process.setMatchPercent(Short.valueOf(priority.getValue().toString()));
    	
        if (process.getParentProject() == null) {
            parentProject.addMungeProcess(process);
            MatchMakerDAO<Project> dao = swingSession.getDAO(Project.class);
            dao.save(parentProject);
            
            MatchMakerTreeModel treeModel = (MatchMakerTreeModel) swingSession.getTree().getModel();
			TreePath menuPath = treeModel.getPathForNode(process);
			swingSession.getTree().setSelectionPath(menuPath);
        } else {
            MatchMakerDAO<MungeProcess> dao = swingSession.getDAO(MungeProcess.class);
            dao.save(process);
        }
        
        //save all the positions of the components
        for (Component com : mungePen.getComponents()) {
        	if (com instanceof AbstractMungeComponent) {
				AbstractMungeComponent mcom = (AbstractMungeComponent) com;
				mcom.updateStepProperties();
			}
        }
        
        changeHandler.setHasChanged(false);
        return true;
    }

    public JComponent getPanel() {
        return panel;
    }

    public boolean hasUnsavedChanges() {
    	if (!name.getText().equals(process.getName())) {
    		if (!(process.getName() == null && name.getText().equals(""))) {
    			return true;
    		}
    	}
    	if (process.getMatchPercent() == null) {
    		return true;
    	}
    	if (Integer.parseInt(priority.getValue().toString()) != Integer.parseInt(process.getMatchPercent().toString()) ) {
    		return true;
    	}
    	if (!((Color) color.getSelectedItem()).equals(process.getColour())) {
    		return true;
    	}
    	if (!desc.getText().equals(process.getDesc())) {
    		if (!(process.getDesc() == null && desc.getText().equals(""))) {
    			return true;
    		}
    	}
        return changeHandler.getHasChanged();
    }
    /**
     * Renders a rectangle of colour in a list cell.  The colour is determined
     * by the list item value, which must be of type java.awt.Color.
     */
    private class ColorCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, "", index, isSelected, cellHasFocus);
            if (value == null) {
            	value = Color.BLACK;
            }
            setBackground((Color) value);
            setOpaque(true);
            setPreferredSize(new Dimension(50, 50));
            setIcon(new ColorIcon((Color) value));
            return this;
        }
    }
    
    /**
     * This class converts a Color into an icon that has width of 85 pixels
     * and height of 50 pixels.
     */
    private class ColorIcon implements Icon {
        private int HEIGHT = 50;
        
        // width of 50 would make sense as the cell has dimensions 50x50 but
        // the cell would only fill with the color icon if width is 85.
        private int WIDTH = 85;
        private Color colour;
     
        public ColorIcon(Color colour) {
            this.colour = colour;
        }
     
        public int getIconHeight() {
            return HEIGHT;
        }
     
        public int getIconWidth() {
            return WIDTH;
        }
     
        public void paintIcon(Component c, Graphics g, int x, int y) {
            g.setColor(colour);
            g.fillRect(x, y, WIDTH - 1, HEIGHT - 1);
        }
    }
    
	private class MungeProcessNameValidator implements Validator {
		private static final int MAX_RULE_SET_NAME_CHAR = 30;
        public ValidateResult validate(Object contents) {
			String value = (String)contents;
			if ( value == null || value.length() == 0 ) {
				return ValidateResult.createValidateResult(Status.FAIL,
						"Munge Process name is required");
			} else if ( !value.equals(process.getName()) &&
					parentProject.getMungeProcessByName(name.getText()) != null ) {
				return ValidateResult.createValidateResult(Status.FAIL,
						"Munge Process name is invalid or already exists.");
			} else if (value.length() > MAX_RULE_SET_NAME_CHAR){
			    return ValidateResult.createValidateResult(Status.FAIL, 
                        "Munge Process name cannot be more than " + MAX_RULE_SET_NAME_CHAR + " characters long");
            } else if (process.getParent() == null && parentProject.getMungeProcessByName(name.getText()) != null) {
            	return ValidateResult.createValidateResult(Status.FAIL, "Munge Process name is invalid or already exists.");
            }
			return ValidateResult.createValidateResult(Status.OK, "");
		}
    }
	
	private class CleanseProjectProcessPriorityValidator implements Validator {
        public ValidateResult validate(Object contents) {
        	
        	if (contents == null) {
        		return ValidateResult.createValidateResult(Status.WARN, "No priority set, assuming 0");
        	}
        	
			int value = Integer.parseInt((String)contents);
		
			for (MungeProcess mp : parentProject.getMungeProcessesFolder().getChildren()) {
				if (mp.getMatchPercent() == value && mp != process) {
					return ValidateResult.createValidateResult(Status.WARN, "Duplicate Priority. " + 
							"If both the cleansing process are run at the same time there is no way to know there relitive order.");
				}
			}
			return ValidateResult.createValidateResult(Status.OK, "");
		}
    }
	
	public MungeProcess getProcess() {
		return process;
	}
	
	public void setSelectedStep(MungeStep step) {
		mungePen.setSelectedStep(step);
	}
	
	public void setSelectedStepOutput(MungeStepOutput mso) {
		//TODO select the mso
	}
}
