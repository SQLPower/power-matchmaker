/*
 * Copyright (c) 2008, SQL Power Group Inc.
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

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.JDialog;

import bsh.Interpreter;
import bsh.util.JConsole;
import ca.sqlpower.matchmaker.MatchMakerObject;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;

public class ScriptAction extends AbstractMatchMakerAction {
    
    private final MatchMakerObject targetObject;

    public ScriptAction(MatchMakerSwingSession session, MatchMakerObject targetObject) {
        super(session, "Script...", "Interactive script console for this object");
        this.targetObject = targetObject;
    }

    public void actionPerformed(ActionEvent e) {
        try {
            JConsole c = new JConsole();
            Interpreter bshInterp = new Interpreter(c);
            bshInterp.set("mmo", targetObject);
            bshInterp.set("session", session);
            
            // TODO stop this thread when the dialog closes!
            Thread thread = new Thread(bshInterp);
            thread.start();
            
            JDialog d = new JDialog(session.getFrame(), "Script console for " + targetObject);
            d.setContentPane(c);
            d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            d.setSize(new Dimension(320, 200));
            d.setVisible(true);
        } catch (Exception ex) {
            throw new RuntimeException();
        }
    }
}
