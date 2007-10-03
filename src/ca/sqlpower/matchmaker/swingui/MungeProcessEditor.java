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
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchRuleSet;
import ca.sqlpower.matchmaker.dao.MatchMakerDAO;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.swingui.munge.MungePen;
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
    private final MatchRuleSet process;
    
    /**
     * The actual GUI component that provides the editing interface.
     */
    private final JPanel panel = new JPanel(new BorderLayout());
    private final JTextField name = new JTextField();
    
    /**
     * The instance that monitors the subtree we're editing for changes (so we know
     * if there are unsaved changes).
     */
    private final MMOChangeWatcher<MatchRuleSet, MungeStep> changeHandler;
    
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
            MatchRuleSet process) {
        super();
        this.swingSession = swingSession;
        this.parentMatch = match;
        this.process = process;
        this.changeHandler = new MMOChangeWatcher<MatchRuleSet, MungeStep>(process);
        ArrayList<Action> actions = new ArrayList<Action>();
        actions.add(saveAction);
        this.handler = new FormValidationHandler(status, actions);
        buildUI();
        if (process.getParentMatch() != null && process.getParentMatch() != parentMatch) {
            throw new IllegalStateException(
                    "The given process has a parent which is not the given parent match obejct!");
        }
    }

    private void buildUI() {
		FormLayout layout = new FormLayout(
				"4dlu,pref,4dlu,fill:pref:grow,4dlu,pref,4dlu", // columns
				"4dlu,pref,4dlu,pref,4dlu"); // rows
		CellConstraints cc = new CellConstraints();
		JPanel subPanel = new JPanel(layout);
        subPanel.add(status, cc.xyw(2, 2, 5));
        subPanel.add(new JLabel("Munge Process Name: "), cc.xy(2, 4));
        name.setText(process.getName());
        subPanel.add(name, cc.xy(4, 4));
		subPanel.add(new JButton(saveAction), cc.xy(6,4));

        panel.add(subPanel,BorderLayout.NORTH);
        JScrollPane p = new JScrollPane(new MungePen(process, handler));
        panel.add(p,BorderLayout.CENTER);
        
    }
    
	Action saveAction = new AbstractAction("Save Munge Process"){
		public void actionPerformed(ActionEvent e) {
            doSave();
		}
	};
    
    /**
     * Saves the process, possibly adding it to the parent match given in the
     * constructor if the process is not already a child of that match.
     */
    public boolean doSave() {
    	process.setName(name.getText());
        if (process.getParentMatch() == null) {
            parentMatch.addMatchRuleSet(process);
            MatchMakerDAO<Match> dao = swingSession.getDAO(Match.class);
            dao.save(parentMatch);
        } else {
            MatchMakerDAO<MatchRuleSet> dao = swingSession.getDAO(MatchRuleSet.class);
            dao.save(process);
        }
        changeHandler.setHasChanged(false);
        return true;
    }

    public JComponent getPanel() {
        return panel;
    }

    public boolean hasUnsavedChanges() {
    	boolean nameChanged;
    	if (process.getName() == null) {
    		if (!name.getText().equals("")){
    			return true;
    		}
    	} else if (!process.getName().equals(name.getText())) {
    		return true;
    	}
        return changeHandler.getHasChanged();
    }

}
