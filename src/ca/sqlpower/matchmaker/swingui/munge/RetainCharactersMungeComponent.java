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

package ca.sqlpower.matchmaker.swingui.munge;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.munge.RetainCharactersMungeStep;
import ca.sqlpower.validation.swingui.FormValidationHandler;

/**
 * This is the component for a retain characters munge step. It has three options,
 * one check box to decide whether to use regular expressions, one check box
 * to decide whether to be case sensitive, and one text field that sets the
 * characters to retain.
 */
public class RetainCharactersMungeComponent extends AbstractMungeComponent {

	private JCheckBox useRegex;
	private JCheckBox caseSensitive;
	private JTextField delimiters;

	public RetainCharactersMungeComponent(MungeStep step, FormValidationHandler handler, MatchMakerSession session) {
		super(step, handler, session);

		RegexValidator validator = new RegexValidator();
		handler.addValidateObject(delimiters, useRegex, validator);
	}

	@Override
	protected JPanel buildUI() {
		JPanel content = new JPanel();
		RetainCharactersMungeStep temp = (RetainCharactersMungeStep) getStep();

		useRegex = new JCheckBox("Use Regular Expressions");
		useRegex.setSelected(temp.getBooleanParameter(temp.USE_REGEX_PARAMETER_NAME));
		useRegex.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				RetainCharactersMungeStep temp = (RetainCharactersMungeStep) getStep();
				temp.setParameter(temp.USE_REGEX_PARAMETER_NAME, useRegex.isSelected());
			}
			
		});
		
		caseSensitive = new JCheckBox("Case Sensitive");
		caseSensitive.setSelected(temp.getBooleanParameter(temp.CASE_SENSITIVE_PARAMETER_NAME));
		caseSensitive.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				RetainCharactersMungeStep temp = (RetainCharactersMungeStep) getStep();
				temp.setParameter(temp.CASE_SENSITIVE_PARAMETER_NAME, caseSensitive.isSelected());
			}
			
		});
		
		delimiters = new JTextField(temp.getParameter(temp.RETAIN_CHARACTERS_PARAMETER_NAME));
		delimiters.getDocument().addDocumentListener(new DocumentListener(){
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
            	RetainCharactersMungeStep temp = (RetainCharactersMungeStep) getStep();
				temp.setParameter(temp.RETAIN_CHARACTERS_PARAMETER_NAME, delimiters.getText());
            }
        });
		
		content.setLayout(new GridLayout(4,1));
		content.add(new JLabel("Retain Characters:"));
		content.add(delimiters);
		content.add(useRegex);
		content.add(caseSensitive);
		return content;
	}
}
