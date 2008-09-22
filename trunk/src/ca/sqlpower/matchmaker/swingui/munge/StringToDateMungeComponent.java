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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.munge.AbstractMungeStep;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.matchmaker.munge.StringToDateMungeStep;
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.swingui.FormValidationHandler;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This is the component for a string to date munge step. It has four options,
 * combo boxes for choosing from lists of default date and time format patterns,
 * a text field for entering a custom format pattern, combo box for choosing 
 * the output format, and a checkbox that decides whether to ignore errors. 
 * Choosing from either lists has a side effect of updating the format pattern 
 * to the concatenation of the date and time portions.
 */
public class StringToDateMungeComponent extends AbstractMungeComponent {

	private JTextField sample;
	private JTextField inputFormat = new JTextField();
	
	private JComboBox dateFormat;
	private JComboBox timeFormat;
	
	private JComboBox outputFormat;
	
	private JCheckBox ignoreError;
	
	private static final Date SAMPLE_DATE = Calendar.getInstance().getTime();
	
	public StringToDateMungeComponent(MungeStep ms, FormValidationHandler handler, MatchMakerSession session) {
		super(ms, handler, session);
	}

	@Override
	protected JPanel buildUI() {
		JPanel content = new JPanel(new FormLayout(
				"4dlu,pref,4dlu,pref,4dlu",
				"4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu"));
		final StringToDateMungeStep temp = (StringToDateMungeStep) getStep();
		
		/**
		 * Gets the lists of formats from the step and converts them into arrays 
		 * so that it's easier to make the combo boxes.
		 */
		final String[] dateFormats = StringToDateMungeStep.DATE_FORMATS.toArray(new String[]{});
		final String[] timeFormats = StringToDateMungeStep.TIME_FORMATS.toArray(new String[]{});
		final String[] outputFormats = StringToDateMungeStep.OUTPUT_FORMATS.toArray(new String[]{});
		
		SimpleDateFormat sdf = new SimpleDateFormat(temp.getInputFormat());
		sample = new JTextField(sdf.format(StringToDateMungeComponent.SAMPLE_DATE));
		sample.setEditable(false);

		ignoreError = new JCheckBox("Continue on Error");
		ignoreError.setSelected(((AbstractMungeStep)getStep()).getBooleanParameter(
				StringToDateMungeStep.IGNORE_ERROR_PARAM));
		
		ignoreError.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				AbstractMungeStep temp = (AbstractMungeStep) getStep();
				temp.setParameter(StringToDateMungeStep.IGNORE_ERROR_PARAM,
						ignoreError.isSelected());
			}
			
		});
		
		dateFormat = new JComboBox(dateFormats);
		dateFormat.setSelectedItem(temp.getDateFormat());
		dateFormat.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				temp.setDateFormat((String) e.getItem());
				inputFormat.setText(temp.getInputFormat());
			}
		});
		
		timeFormat = new JComboBox(timeFormats);
		timeFormat.setSelectedItem(temp.getTimeFormat());
		timeFormat.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				temp.setTimeFormat((String) e.getItem());
				inputFormat.setText(temp.getInputFormat());
			}
		});
		
		inputFormat = new JTextField(temp.getInputFormat());
		inputFormat.getDocument().addDocumentListener(new DocumentListener(){
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
				temp.setInputFormat(inputFormat.getText());
				if (getHandler().getWorstValidationStatus().getStatus() == Status.OK) {
					SimpleDateFormat sdf = new SimpleDateFormat(temp.getInputFormat());
					sample.setText(sdf.format(StringToDateMungeComponent.SAMPLE_DATE));
				}
            }
        });
		getHandler().addValidateObject(inputFormat, new DateFormatPatternValidator());
		
		outputFormat = new JComboBox(outputFormats);
		outputFormat.setSelectedItem(temp.getOutputFormat());
		outputFormat.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				temp.setOutputFormat((String) e.getItem());
			}
		});
		
		CellConstraints cc = new CellConstraints();
		int row = 2;
		content.add(new JLabel("Example: "), cc.xy(2, row));
		content.add(sample, cc.xy(4, row));
		row += 2;
		content.add(new JLabel("Date Format: "), cc.xy(2, row));
		content.add(dateFormat, cc.xy(4, row));
		row += 2;
		content.add(new JLabel("Time Format: "), cc.xy(2, row));
		content.add(timeFormat, cc.xy(4, row));
		row += 2;
		content.add(new JLabel("Input Format: "), cc.xy(2, row));
		content.add(inputFormat, cc.xy(4, row));
		row += 2;
		content.add(new JLabel("Output Format: "), cc.xy(2, row));
		content.add(outputFormat, cc.xy(4, row));
		row += 2;
		content.add(ignoreError, cc.xyw(2, row, 3, "c,c"));
		
		return content;
	}
}
