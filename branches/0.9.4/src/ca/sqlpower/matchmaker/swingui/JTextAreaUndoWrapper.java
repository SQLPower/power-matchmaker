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


package ca.sqlpower.matchmaker.swingui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Document;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import com.jgoodies.forms.builder.ButtonBarBuilder;

public class JTextAreaUndoWrapper extends JPanel{

    JButton undoButton;
    JButton redoButton;
    JTextArea textArea;
    
    public JTextAreaUndoWrapper(JTextArea textArea){
        if (textArea == null){
            throw new NullPointerException("TextArea argument cannot be null");
        }
        this.textArea = textArea;
        textArea.setWrapStyleWord(true);
        textArea.setLineWrap(true);
        undoButton = new JButton(undoAction);
        redoButton = new JButton(redoAction);
        
        Document doc = textArea.getDocument();
        
        doc.addUndoableEditListener(
                new UndoableEditListener() {
                    public void undoableEditHappened(UndoableEditEvent evt) {
                        undo.addEdit(evt.getEdit());
                    }
                });

        textArea.getActionMap().put("Undo", undoAction);
        textArea.getInputMap().put(KeyStroke.getKeyStroke("control Z"), "Undo");

        textArea.getActionMap().put("Redo",redoAction);
        textArea.getInputMap().put(KeyStroke.getKeyStroke("control Y"), "Redo");

        setLayout(new BorderLayout());
        add(new JScrollPane(textArea), BorderLayout.CENTER);        
        ButtonBarBuilder bb= new ButtonBarBuilder();
        bb.addGridded(undoButton);
        bb.addRelatedGap();        
        bb.addGridded(redoButton);
        bb.addRelatedGap();        
        add(bb.getPanel(), BorderLayout.SOUTH);
        
    }
    
    final UndoManager undo = new UndoManager();
    
    final AbstractAction redoAction = new AbstractAction("Redo") {
        public void actionPerformed(ActionEvent evt) {
            try {
                if (undo.canRedo()) {
                    undo.redo();
                }
            } catch (CannotRedoException e) {
            }
        }
    };
    
    final AbstractAction undoAction = new AbstractAction("Undo") {
        public void actionPerformed(ActionEvent evt) {
            try {
                if (undo.canUndo()) {
                    undo.undo();
                }
            } catch (CannotUndoException e) {
            }
        }
    };

}
