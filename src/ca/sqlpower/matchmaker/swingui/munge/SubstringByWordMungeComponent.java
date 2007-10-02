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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Action;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.munge.SubstringByWordMungeStep;
import ca.sqlpower.validation.swingui.FormValidationHandler;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This is the component for SubstringByWordMungeStep. It has a 
 * JCheckBox for the USE_REGEX parameter and JTextFields for the 
 * FROM and TO parameter.
 */
public class SubstringByWordMungeComponent extends AbstractMungeComponent {

	private JSpinner begin;
	private JSpinner end;
	private JCheckBox useRegex;
	private JTextField delimiter;
	private JTextField resultDelimiter;
	
	public SubstringByWordMungeComponent(MungeStep step, FormValidationHandler handler) {
		super(step);
		RegexValidator validator = new RegexValidator(new ArrayList<Action>());
		handler.addValidateObject(delimiter, useRegex, validator);
	}

	@Override
	protected JPanel buildUI() {
		SubstringByWordMungeStep step = (SubstringByWordMungeStep) getStep();
		
		useRegex = new JCheckBox("Use Regular Expressions");
		useRegex.setBackground(getBg());
		useRegex.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				SubstringByWordMungeStep step = (SubstringByWordMungeStep) getStep();
				step.setParameter(step.USE_REGEX_PARAMETER_NAME, useRegex.isSelected());
			}	
		});
		useRegex.setSelected(step.getBooleanParameter(step.USE_REGEX_PARAMETER_NAME));
		
		delimiter = new JTextField(step.getParameter(step.DELIMITER_PARAMETER_NAME));
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
            	SubstringByWordMungeStep step = (SubstringByWordMungeStep) getStep();
				step.setParameter(step.DELIMITER_PARAMETER_NAME, delimiter.getText());
            }
        });
		
		resultDelimiter = new JTextField(step.getParameter(step.RESULT_DELIM_PARAMETER_NAME));
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
            	SubstringByWordMungeStep step = (SubstringByWordMungeStep) getStep();
				step.setParameter(step.RESULT_DELIM_PARAMETER_NAME, resultDelimiter.getText());
            }
        });
		
		int beginIndex = step.getIntegerParameter(step.BEGIN_PARAMETER_NAME);
		SpinnerNumberModel beginNumberModel = new SpinnerNumberModel(beginIndex, 0, Integer.MAX_VALUE, 1);
		begin = new JSpinner(beginNumberModel);
		begin.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				SubstringByWordMungeStep step = (SubstringByWordMungeStep) getStep();
				step.setParameter(step.BEGIN_PARAMETER_NAME, begin.getValue().toString());
			}
		
		});
		
		int endIndex = step.getIntegerParameter(step.END_PARAMETER_NAME);
		SpinnerNumberModel endNumberModel = new SpinnerNumberModel(endIndex, 0, Integer.MAX_VALUE, 1);
		end = new JSpinner(endNumberModel);
		end.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				SubstringByWordMungeStep step = (SubstringByWordMungeStep) getStep();
				step.setParameter(step.END_PARAMETER_NAME, end.getValue().toString());
			}
		
		});
		
		FormLayout layout = new FormLayout(
				"4dlu,pref,4dlu,fill:pref:grow,4dlu", // columns
				"4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu"); // rows
		CellConstraints cc = new CellConstraints();

		JPanel content = new JPanel(layout);
		
		content.add(new JLabel("Begin Index:"), cc.xy(2,2));
		content.add(begin, cc.xy(4,2));
		content.add(new JLabel("End Index:"), cc.xy(2,4));
		content.add(end, cc.xy(4,4));
		content.add(new JLabel("Delimiter:"), cc.xy(2,6));
		content.add(delimiter, cc.xy(4,6));
		content.add(new JLabel("Result Delim:"), cc.xy(2,8));
		content.add(resultDelimiter, cc.xy(4,8));
		content.add(useRegex, cc.xyw(2,10,3));
		return content;
	}
}
