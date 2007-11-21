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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.munge.StringToBooleanMungeStep;
import ca.sqlpower.validation.swingui.FormValidationHandler;

/**
 * This is the component for a boolean to string munge component. This has to text boxes 
 * for entering either comma separated valuse or a regular expression. A regular expression
 * check box and an ignore case check box. Plus a default case check box. 
 */
public class StringToBooleanMungeComponent extends AbstractMungeComponent {

	private JCheckBox useRegex;
	private JCheckBox caseSensitive;
	private JTextField trueList;
	private JTextField falseList;
	private JComboBox defaultOption;

	public StringToBooleanMungeComponent(MungeStep step, FormValidationHandler handler, MatchMakerSession session) {
		super(step, handler, session);

		RegexValidator validator = new RegexValidator();
		handler.addValidateObject(trueList, useRegex, validator);
		handler.addValidateObject(falseList, useRegex, validator);
	}

	@Override
	protected JPanel buildUI() {
		JPanel content = new JPanel();
		StringToBooleanMungeStep temp = (StringToBooleanMungeStep) getStep();

		useRegex = new JCheckBox("Use Regular Expressions");
		useRegex.setSelected(temp.getBooleanParameter(temp.USE_REGEX_PARAMETER_NAME));
		useRegex.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				StringToBooleanMungeStep temp = (StringToBooleanMungeStep) getStep();
				temp.setParameter(StringToBooleanMungeStep.USE_REGEX_PARAMETER_NAME, useRegex.isSelected());
			}
		});
		
		caseSensitive = new JCheckBox("Case Sensitive");
		caseSensitive.setSelected(temp.getBooleanParameter(temp.CASE_SENSITIVE_PARAMETER_NAME));
		caseSensitive.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				StringToBooleanMungeStep temp = (StringToBooleanMungeStep) getStep();
				temp.setParameter(StringToBooleanMungeStep.CASE_SENSITIVE_PARAMETER_NAME, caseSensitive.isSelected());
			}
			
		});
		
		trueList = new JTextField(temp.getParameter(StringToBooleanMungeStep.TRUE_LIST_PARAMETER_NAME));
		trueList.getDocument().addDocumentListener(new DocumentListener(){
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
				getStep().setParameter(StringToBooleanMungeStep.TRUE_LIST_PARAMETER_NAME, trueList.getText());
            }
        });
		
		falseList = new JTextField(temp.getParameter(StringToBooleanMungeStep.TRUE_LIST_PARAMETER_NAME));
		falseList.getDocument().addDocumentListener(new DocumentListener(){
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
				getStep().setParameter(StringToBooleanMungeStep.FALSE_LIST_PARAMETER_NAME, falseList.getText());
            }
        });
		
		defaultOption = new JComboBox(new String[]{"True", "False", "Null", "Halt"});
		defaultOption.addItemListener(new ItemListener(){

			public void itemStateChanged(ItemEvent e) {
				getStep().setParameter(StringToBooleanMungeStep.NEITHER_PARAMETER_NAME, (String)defaultOption.getSelectedItem());
			}
			
		});
		
		content.setLayout(new GridLayout(8,1));
		content.add(new JLabel("True List (regex or comma seperated values):"));
		content.add(trueList);
		content.add(new JLabel("False List (regex or comma seperated values):"));
		content.add(falseList);
		content.add(useRegex);
		content.add(caseSensitive);
		content.add(new JLabel("If neither match:"));
		content.add(defaultOption);
		return content;
	}
}
