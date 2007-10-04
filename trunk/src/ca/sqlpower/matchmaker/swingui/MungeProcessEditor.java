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
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;

import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.dao.MatchMakerDAO;
import ca.sqlpower.matchmaker.munge.MungeProcess;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.swingui.munge.MungePen;
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
     * The match that is or will be the parent of the process we're editing.
     * If this editor was created for a new process, it will not belong to this
     * match until the doSave() method has been called.
     */
    private final Match parentMatch;
    
    /**
     * The munge process this editor is responsible for editing.
     */
    private final MungeProcess process;
    
    /**
     * The actual GUI component that provides the editing interface.
     */
    private final JPanel panel = new JPanel(new BorderLayout());
    private final JTextField name = new JTextField();
    private final JSpinner priority = new JSpinner();
    private final JTextField desc = new JTextField();
    private final JComboBox color = new JComboBox(ColorScheme.BREWER_SET19);
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
     * @param swingSession The session the given match and process belong to
     * @param match The match that is or will become the process's parent. If the
     * process is new, it will not currently have a parent, but this editor will
     * connect the process to this match when saving. 
     * @param process The process to edit
     */
    public MungeProcessEditor(
            MatchMakerSwingSession swingSession,
            Match match,
            MungeProcess process) {
        super();
        this.swingSession = swingSession;
        this.parentMatch = match;
        this.process = process;
        this.changeHandler = new MMOChangeWatcher<MungeProcess, MungeStep>(process);
        ArrayList<Action> actions = new ArrayList<Action>();
        actions.add(saveAction);
        this.handler = new FormValidationHandler(status, actions);
        buildUI();
        if (process.getParentMatch() != null && process.getParentMatch() != parentMatch) {
            throw new IllegalStateException(
                    "The given process has a parent which is not the given parent match obejct!");
        }
        handler.addValidateObject(name, new MatchRuleSetNameValidator());
        //handler.addValidateObject(priority, new MatchRuleSetPercentValidator());
    }

    private void buildUI() {
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
        color.setSelectedItem(process.getColour());
        subPanel.add(color, cc.xy(8, 6));
        
		subPanel.add(new JButton(saveAction), cc.xy(2,8));
		subPanel.add(new JButton(customColour), cc.xy(8,8));
		
        panel.add(subPanel,BorderLayout.NORTH);
        JScrollPane p = new JScrollPane(new MungePen(process, handler));
        panel.add(p,BorderLayout.CENTER);
        
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
     * Saves the process, possibly adding it to the parent match given in the
     * constructor if the process is not already a child of that match.
     */
    public boolean doSave() {
    	process.setName(name.getText());
    	process.setDesc(desc.getText());
    	process.setColour((Color)color.getSelectedItem());
    	process.setMatchPercent(Short.valueOf(priority.getValue().toString()));
    	
        if (process.getParentMatch() == null) {
            parentMatch.addMatchRuleSet(process);
            MatchMakerDAO<Match> dao = swingSession.getDAO(Match.class);
            dao.save(parentMatch);
        } else {
            MatchMakerDAO<MungeProcess> dao = swingSession.getDAO(MungeProcess.class);
            dao.save(process);
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
    
	private class MatchRuleSetNameValidator implements Validator {
		private static final int MAX_RULE_SET_NAME_CHAR = 30;
        public ValidateResult validate(Object contents) {
			String value = (String)contents;
			if ( value == null || value.length() == 0 ) {
				return ValidateResult.createValidateResult(Status.FAIL,
						"Munge Process name is required");
			} else if ( !value.equals(process.getName()) &&
					parentMatch.getMatchRuleSetByName(name.getText()) != null ) {
				return ValidateResult.createValidateResult(Status.FAIL,
						"Munge Process name is invalid or already exists.");
			} else if (value.length() > MAX_RULE_SET_NAME_CHAR){
			    return ValidateResult.createValidateResult(Status.FAIL, 
                        "Munge Process name cannot be more than " + MAX_RULE_SET_NAME_CHAR + " characters long");
            }
			return ValidateResult.createValidateResult(Status.OK, "");
		}
    }

}
