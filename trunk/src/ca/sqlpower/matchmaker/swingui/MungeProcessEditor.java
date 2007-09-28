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

import javax.swing.JComponent;

import ca.sqlpower.matchmaker.Match;
import ca.sqlpower.matchmaker.MatchRuleSet;
import ca.sqlpower.matchmaker.dao.MatchMakerDAO;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.swingui.munge.MungePen;

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
    private final MungePen panel;
    
    /**
     * The instance that monitors the subtree we're editing for changes (so we know
     * if there are unsaved changes).
     */
    private final MMOChangeWatcher<MatchRuleSet, MungeStep> changeHandler;
    
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
        this.panel = new MungePen();
        this.changeHandler = new MMOChangeWatcher<MatchRuleSet, MungeStep>(process);
        
        if (process.getParentMatch() != null && process.getParentMatch() != parentMatch) {
            throw new IllegalStateException(
                    "The given process has a parent which is not the given parent match obejct!");
        }
    }

    /**
     * Saves the process, possibly adding it to the parent match given in the
     * constructor if the process is not already a child of that match.
     */
    public boolean doSave() {
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
        return changeHandler.getHasChanged();
    }

}
