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
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.munge.SubstringByWordMungeStep;
import ca.sqlpower.validation.swingui.FormValidationHandler;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This is the component for SubstringByWordMungeStep. It has a 
 * JCheckBox for the USE_REGEX parameter and JTextFields for the 
 * delimiters and resultDelimiters parameter. It also contain 
 * two JSpinners for users to set the begin and end index.
 */
public class SubstringByWordMungeComponent extends AbstractMungeComponent {

	private JSpinner begin;
	private JSpinner end;
	private JCheckBox useRegex;
	private JCheckBox caseSensitive;
	private JTextField delimiter;
	private JTextField resultDelimiter;
	
	public SubstringByWordMungeComponent(MungeStep step, FormValidationHandler handler, MatchMakerSession session) {
		super(step, handler, session);

	}

	@Override
	protected JPanel buildUI() {
		SubstringByWordMungeStep step = (SubstringByWordMungeStep) getStep();
		
		useRegex = new JCheckBox("Use Regular Expressions");
		useRegex.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				SubstringByWordMungeStep step = (SubstringByWordMungeStep) getStep();
				step.setRegex(useRegex.isSelected());
			}	
		});
		useRegex.setSelected(step.isRegex());
		
		caseSensitive = new JCheckBox("Case Sensitive");
		caseSensitive.setSelected(step.isCaseSensitive());
		caseSensitive.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				SubstringByWordMungeStep step = (SubstringByWordMungeStep) getStep();
				step.setCaseSensitive(caseSensitive.isSelected());
			}
			
		});
		
		delimiter = new JTextField(step.getDelimiter());
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
				step.setDelimiter(delimiter.getText());
            }
        });
		RegexValidator validator = new RegexValidator();
		getHandler().addValidateObject(delimiter, useRegex, validator);
		
		resultDelimiter = new JTextField(step.getResultDelim());
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
				step.setResultDelim(resultDelimiter.getText());
            }
        });
		
		int beginIndex = step.getBegIndex();
		SpinnerNumberModel beginNumberModel = new SpinnerNumberModel(beginIndex, 0, Integer.MAX_VALUE, 1);
		begin = new JSpinner(beginNumberModel);
		begin.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				SubstringByWordMungeStep step = (SubstringByWordMungeStep) getStep();
				step.setBegIndex((Integer) begin.getValue());
			}
		
		});
		
		int endIndex = step.getEndIndex();
		SpinnerNumberModel endNumberModel = new SpinnerNumberModel(endIndex, 0, Integer.MAX_VALUE, 1);
		end = new JSpinner(endNumberModel);
		end.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				SubstringByWordMungeStep step = (SubstringByWordMungeStep) getStep();
				step.setEndIndex((Integer) end.getValue());
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
		
		JPanel bottom = new JPanel(new GridLayout(2,1));
		bottom.add(useRegex);
		bottom.add(caseSensitive);
		content.add(bottom, cc.xyw(2,10,3, "c,f"));
		
		return content;
	}
}
