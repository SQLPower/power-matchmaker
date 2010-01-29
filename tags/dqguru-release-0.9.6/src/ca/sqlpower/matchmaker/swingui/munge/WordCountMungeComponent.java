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
import ca.sqlpower.matchmaker.munge.WordCountMungeStep;
import ca.sqlpower.validation.swingui.FormValidationHandler;

/**
 * This is the component for WordCountMungeStep. It has a JCheckBox
 * for the USE_REGEX parameter and a JTextField for the DELIMITER
 * parameter.
 */
public class WordCountMungeComponent extends AbstractMungeComponent {

	private JCheckBox useRegex;
	private JTextField delimiters;
	private JCheckBox caseSensitive;
	FormValidationHandler validHandler;

	public WordCountMungeComponent(MungeStep step, FormValidationHandler handler, MatchMakerSession session) {
		super(step, handler, session);
	}
	
	@Override
	protected JPanel buildUI() {
		JPanel content = new JPanel();
		WordCountMungeStep step = (WordCountMungeStep) getStep();
		
		useRegex = new JCheckBox("Use Regular Expressions");
		delimiters = new JTextField(step.getParameter(step.DELIMITER_PARAMETER_NAME));
		
		RegexValidator validator = new RegexValidator();
		getHandler().addValidateObject(delimiters, useRegex, validator);
		
		useRegex.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				WordCountMungeStep step = (WordCountMungeStep) getStep();
				step.setParameter(step.USE_REGEX_PARAMETER_NAME, useRegex.isSelected());
			}	
		});
		useRegex.setSelected(step.getBooleanParameter(step.USE_REGEX_PARAMETER_NAME));
		
		caseSensitive = new JCheckBox("Case Sensitive");
		caseSensitive.setSelected(step.getBooleanParameter(step.CASE_SENSITIVE_PARAMETER_NAME));
		caseSensitive.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				WordCountMungeStep step = (WordCountMungeStep) getStep();
				step.setParameter(step.CASE_SENSITIVE_PARAMETER_NAME, caseSensitive.isSelected());
			}
			
		});
		
		
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
				WordCountMungeStep step = (WordCountMungeStep) getStep();
				step.setParameter(step.DELIMITER_PARAMETER_NAME, delimiters.getText());
            }
        });

		content.setLayout(new GridLayout(4,1));
		content.add(new JLabel("Enter the delimiters"));
		content.add(delimiters);
		content.add(useRegex);
		content.add(caseSensitive);
		return content;
	}
}
