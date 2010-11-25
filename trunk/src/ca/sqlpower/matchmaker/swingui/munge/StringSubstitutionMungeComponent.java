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
import ca.sqlpower.matchmaker.munge.StringSubstitutionMungeStep;
import ca.sqlpower.validation.swingui.FormValidationHandler;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This is the component for StringSubstitutionMungeStep. It has a 
 * JCheckBox for the USE_REGEX parameter and JTextFields for the 
 * FROM and TO parameter.
 */
public class StringSubstitutionMungeComponent extends AbstractMungeComponent {

	private JCheckBox useRegex;
	private JCheckBox caseSensitive;
	private JTextField from;
	private JTextField to;
	
	public StringSubstitutionMungeComponent(MungeStep step, FormValidationHandler handler, MatchMakerSession session) {
		super(step, handler, session);
	}
	
	@Override
	protected JPanel buildUI() {
		StringSubstitutionMungeStep step = (StringSubstitutionMungeStep) getStep();
		
		useRegex = new JCheckBox("Use Regular Expressions");
		useRegex.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				StringSubstitutionMungeStep step = (StringSubstitutionMungeStep) getStep();
				step.setRegex(useRegex.isSelected());
			}	
		});
		useRegex.setSelected(step.isRegex());
		
		caseSensitive = new JCheckBox("Case Sensitive");
		caseSensitive.setSelected(step.isCaseSensitive());
		caseSensitive.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				StringSubstitutionMungeStep temp = (StringSubstitutionMungeStep) getStep();
				temp.setCaseSensitive(caseSensitive.isSelected());
			}
			
		});
		
		from = new JTextField(step.getFrom());
		from.getDocument().addDocumentListener(new DocumentListener(){
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
				StringSubstitutionMungeStep step = (StringSubstitutionMungeStep) getStep();
				step.setFrom(from.getText());
            }
        });
		
		to = new JTextField(step.getTo());
		to.getDocument().addDocumentListener(new DocumentListener(){
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
				StringSubstitutionMungeStep step = (StringSubstitutionMungeStep) getStep();
				step.setTo(to.getText());
            }
        });
		RegexValidator validator = new RegexValidator();
		getHandler().addValidateObject(from, useRegex, validator, true, "");
		
		FormLayout layout = new FormLayout(
				"4dlu,pref,4dlu,fill:pref:grow,4dlu", // columns
				"4dlu,pref,4dlu,pref,4dlu,pref,4dlu"); // rows
		CellConstraints cc = new CellConstraints();

		JPanel content = new JPanel(layout);
		
		content.add(new JLabel("From:"), cc.xy(2,2));
		content.add(from, cc.xy(4,2));
		content.add(new JLabel("To:"), cc.xy(2,4));
		content.add(to, cc.xy(4,4));
		
		JPanel bottom = new JPanel(new GridLayout(2,1));
		bottom.add(useRegex);
		bottom.add(caseSensitive);
		content.add(bottom, cc.xyw(2,6,3, "c,f"));
		
		return content;
	}
}
