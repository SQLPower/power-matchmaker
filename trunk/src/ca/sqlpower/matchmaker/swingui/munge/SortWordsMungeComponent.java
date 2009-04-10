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
import ca.sqlpower.matchmaker.munge.SortWordsMungeStep;
import ca.sqlpower.validation.swingui.FormValidationHandler;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * The GUI component for {@link SortWordsMungeStep}.
 */
public class SortWordsMungeComponent extends AbstractMungeComponent {

	private JCheckBox useRegex;
	private JCheckBox caseSensitive;
	private JTextField delimiter;
	private JTextField resultDelimiter;
	
	public SortWordsMungeComponent(MungeStep step, FormValidationHandler handler, MatchMakerSession session) {
		super(step, handler, session);
	}

	@Override
	public SortWordsMungeStep getStep() {
	    return (SortWordsMungeStep) super.getStep();
	}
	
	@Override
	protected JPanel buildUI() {
		final SortWordsMungeStep step = getStep();
		
		useRegex = new JCheckBox("Use Regular Expressions");
		useRegex.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				step.setParameter(SortWordsMungeStep.USE_REGEX_PARAMETER_NAME, useRegex.isSelected());
			}	
		});
		useRegex.setSelected(step.getBooleanParameter(SortWordsMungeStep.USE_REGEX_PARAMETER_NAME));
		
		caseSensitive = new JCheckBox("Case Sensitive");
		caseSensitive.setSelected(step.getBooleanParameter(SortWordsMungeStep.CASE_SENSITIVE_PARAMETER_NAME));
		caseSensitive.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				step.setParameter(SortWordsMungeStep.CASE_SENSITIVE_PARAMETER_NAME, caseSensitive.isSelected());
			}
			
		});
		
		delimiter = new JTextField(step.getParameter(SortWordsMungeStep.DELIMITER_PARAMETER_NAME));
		delimiter.getDocument().addDocumentListener(new DocumentListener(){
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
				step.setParameter(SortWordsMungeStep.DELIMITER_PARAMETER_NAME, delimiter.getText());
            }
        });
		RegexValidator validator = new RegexValidator();
		getHandler().addValidateObject(delimiter, useRegex, validator);
		
		resultDelimiter = new JTextField(step.getParameter(SortWordsMungeStep.RESULT_DELIM_PARAMETER_NAME));
		resultDelimiter.getDocument().addDocumentListener(new DocumentListener(){
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
				step.setParameter(SortWordsMungeStep.RESULT_DELIM_PARAMETER_NAME, resultDelimiter.getText());
            }
        });
		
		FormLayout layout = new FormLayout(
				"4dlu,pref,4dlu,fill:pref:grow,4dlu", // columns
				"4dlu,pref,4dlu,pref,4dlu,pref,4dlu"); // rows
		CellConstraints cc = new CellConstraints();

		JPanel content = new JPanel(layout);
		
		content.add(new JLabel("Delimiter:"), cc.xy(2,2));
		content.add(delimiter, cc.xy(4,2));
		content.add(new JLabel("Result Delim:"), cc.xy(2,4));
		content.add(resultDelimiter, cc.xy(4,4));
		
		JPanel bottom = new JPanel(new GridLayout(2,1));
		bottom.add(useRegex);
		bottom.add(caseSensitive);
		content.add(bottom, cc.xyw(2,6,3, "c,f"));
		
		return content;
	}
}
