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
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;

import ca.sqlpower.architect.ArchitectException;
import ca.sqlpower.graph.DepthFirstSearch;
import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.Project.ProjectMode;
import ca.sqlpower.matchmaker.event.MatchMakerEvent;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.munge.MungeProcessGraphModel;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.munge.MungeStepOutput;
import ca.sqlpower.matchmaker.swingui.munge.MungePen;
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
 * Implements the EditorPane functionality for editing a munge process (MatchRuleSet).
 */
public class MungeProcessEditor extends AbstractUndoableEditorPane<MungeProcess, MungeStep> {
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
    private final JSpinner priority = new JSpinner();
    private final JTextField desc = new JTextField();
    private final JComboBox color = new JComboBox(ColorScheme.BREWER_SET19);
	
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
            Project project, MungeProcess process) throws ArchitectException {
        super(swingSession, process);
        
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
        if (project.getType() == ProjectMode.CLEANSE) {
        	handler.addValidateObject(priority, new CleanseProjectProcessPriorityValidator());
        }

        //For some reason some process don't have sessions and this causes a null
        //pointer barrage when it tries to find the tree when it gets focus, then fails and 
        //gets focus again.
        if (process.getSession() == null) {
        	process.setSession(swingSession);
        }
        this.mungePen = new MungePen(process, handler, parentProject);
        
        buildUI();
        setDefaults();
        addListenerToComponents();
    }

	private void buildUI() throws ArchitectException {
		panel = new JPanel(new BorderLayout());
		FormLayout layout = new FormLayout(
				"4dlu,pref,4dlu,fill:pref:grow,4dlu,pref,4dlu,pref,4dlu", // columns
				"4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu"); // rows
		CellConstraints cc = new CellConstraints();
		JPanel subPanel = new JPanel(layout);
        subPanel.add(status, cc.xyw(2, 2, 7));
        subPanel.add(new JLabel("Process Name: "), cc.xy(2, 4));
        
        subPanel.add(name, cc.xy(4, 4));
        subPanel.add(new JLabel("Priority: "), cc.xy(6, 4));
        
        priority.setPreferredSize(new Dimension(100, 20));
        subPanel.add(priority, cc.xy(8, 4));
        
        subPanel.add(new JLabel("Description: "), cc.xy(2, 6));
        
        subPanel.add(desc, cc.xy(4, 6));
        subPanel.add(new JLabel("Color: "), cc.xy(6, 6));
        ColorCellRenderer renderer = new ColorCellRenderer();
        color.setRenderer(renderer);
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
    
	private void addListenerToComponents() {
		name.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
			}
			public void keyReleased(KeyEvent e) {
				mmo.setName(name.getText());
			}
			public void keyTyped(KeyEvent e) {
			}});
		priority.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				mmo.setMatchPercent(Short.valueOf(priority.getValue().toString()));
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
		if (mmo.getMatchPercent() != null) {
        	priority.setValue(mmo.getMatchPercent());
        }else {
        	mmo.setMatchPercent(new Short((short)0));
        }
		
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
	
	Action saveAction = new AbstractAction("Save Munge Process"){
		public void actionPerformed(ActionEvent e) {
            doSave();
            
            // if the munge process is new, select it on the tree
            if (mmo.getParentProject() == null) {
                MatchMakerTreeModel treeModel = (MatchMakerTreeModel) swingSession.getTree().getModel();
    			TreePath menuPath = treeModel.getPathForNode(mmo);
    			swingSession.getTree().setSelectionPath(menuPath);
            }
		}
	};
	Action customColour = new AbstractAction("Custom Colour") {
		public void actionPerformed(ActionEvent arg0) {
			Color colour = swingSession.getCustomColour(mmo.getColour());
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
        
        MungeProcessGraphModel gm = new MungeProcessGraphModel(mmo.getChildren());
        DepthFirstSearch<MungeStep, MungeProcessGraphModel.Edge> dfs = new DepthFirstSearch<MungeStep, MungeProcessGraphModel.Edge>();
        dfs.performSearch(gm);
        
        if (dfs.isCyclic()) {
        	int responds = JOptionPane.showConfirmDialog(swingSession.getFrame(),
				"Your munge process contains at least one cycle, " + 
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
        return super.doSave();
    }

    public boolean hasUnsavedChanges() {
    	if (mmo.getParent() == null) {
			return true;
		}
        return super.hasUnsavedChanges();
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
			} else if ( !value.equals(mmo.getName()) &&
					parentProject.getMungeProcessByName(name.getText()) != null ) {
				return ValidateResult.createValidateResult(Status.FAIL,
						"Munge Process name is invalid or already exists.");
			} else if (value.length() > MAX_RULE_SET_NAME_CHAR){
			    return ValidateResult.createValidateResult(Status.FAIL, 
                        "Munge Process name cannot be more than " + MAX_RULE_SET_NAME_CHAR + " characters long");
            } else if (mmo.getParent() == null && parentProject.getMungeProcessByName(name.getText()) != null) {
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
        	
			short value = Short.parseShort((String)contents);
		
			for (MungeProcess mp : parentProject.getMungeProcessesFolder().getChildren()) {
                if (mp == null) throw new NullPointerException("Null munge process in project!");
				short otherPriority = 0;
                if (mp.getMatchPercent() != null) {
                    otherPriority = mp.getMatchPercent().shortValue();
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

}
