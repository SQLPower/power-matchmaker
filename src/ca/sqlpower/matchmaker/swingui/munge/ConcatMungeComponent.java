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

package ca.sqlpower.matchmaker.swingui.munge;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.munge.ConcatMungeStep;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.validation.swingui.FormValidationHandler;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This is a component for a concat munge step. It has two options, one button
 * that adds inputs and one button to clean up the unused inputs.
 */
public class ConcatMungeComponent extends AbstractMungeComponent {
	
	private JButton addInputButton;
	private JButton removeInputsButton;
	private JTextField delimiterField;
    
	public ConcatMungeComponent(MungeStep ms, FormValidationHandler handler, MatchMakerSession session) {
		super(ms, handler,session);
	}
	
	@Override
	protected JPanel buildUI() {
		ConcatMungeStep step = (ConcatMungeStep) getStep();
		
		addInputButton = new JButton(new AddInputAction("Add Input"));
		removeInputsButton = new JButton(new RemoveUnusedInputAction("Clean Up"));
        
        delimiterField = new JTextField(step.getParameter(ConcatMungeStep.DELIMITER_PARAMETER_NAME));
		delimiterField.getDocument().addDocumentListener(new DocumentListener(){
            public void insertUpdate(DocumentEvent e) {
                doStuff();
            }
            public void removeUpdate(DocumentEvent e) {
                doStuff();
            }
            public void changedUpdate(DocumentEvent e) {
                doStuff();
            }
            private void doStuff() {
            	ConcatMungeStep step = (ConcatMungeStep) getStep();
				step.setParameter(ConcatMungeStep.DELIMITER_PARAMETER_NAME, delimiterField.getText());
            }
        });
        
        FormLayout fl = new FormLayout("pref:grow,4dlu,pref:grow");
        DefaultFormBuilder b = new DefaultFormBuilder(fl);
        b.append("Delimiter", delimiterField);
		b.append(ButtonBarFactory.buildAddRemoveBar(addInputButton, removeInputsButton), 3);
		
        content = b.getPanel();
		return content;
	}

}
