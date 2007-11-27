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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.munge.DateToStringMungeStep;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.swingui.FormValidationHandler;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * This is the component for a date to string munge step. It has three options,
 * combo boxes for choosing from lists of default date and time format patterns,
 * a text field for entering a custom format pattern. Choosing from either lists
 * has a side effect of updating the format pattern to the concatenation of the
 * date and time portions.
 */
public class DateToStringMungeComponent extends AbstractMungeComponent {

	private JTextField sample;
	private JTextField format;
	private JComboBox dateFormat;
	private JComboBox timeFormat;
	
	private static final Date SAMPLE_DATE = Calendar.getInstance().getTime();
	
	public DateToStringMungeComponent(MungeStep ms, FormValidationHandler handler, MatchMakerSession session) {
		super(ms, handler, session);
		handler.addValidateObject(format, new DateFormatPatternValidator());
	}

	@Override
	protected JPanel buildUI() {
		JPanel content = new JPanel(new FormLayout(
				"4dlu,pref,4dlu,pref,4dlu",
				"4dlu,pref,4dlu,pref,4dlu,pref,4dlu,pref,4dlu"));
		final DateToStringMungeStep temp = (DateToStringMungeStep) getStep();
		final String[] dateFormats = DateToStringMungeStep.DATE_FORMATS.toArray(new String[]{});
		final String[] timeFormats = DateToStringMungeStep.TIME_FORMATS.toArray(new String[]{});
		
		dateFormat = new JComboBox(dateFormats);
		dateFormat.setSelectedItem(temp.getDateFormat());
		dateFormat.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				temp.setDateFormat((String) e.getItem());
				format.setText(temp.getFormat());
			}
		});
		
		timeFormat = new JComboBox(timeFormats);
		timeFormat.setSelectedItem(temp.getTimeFormat());
		timeFormat.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				temp.setTimeFormat((String) e.getItem());
				format.setText(temp.getFormat());
			}
		});
		
		format = new JTextField(temp.getFormat());
		format.getDocument().addDocumentListener(new DocumentListener(){
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
				temp.setFormat(format.getText());
				if (getHandler().getWorstValidationStatus().getStatus() == Status.OK) {
					SimpleDateFormat sdf = new SimpleDateFormat(temp.getFormat());
					sample.setText(sdf.format(DateToStringMungeComponent.SAMPLE_DATE));
				}
            }
        });
		SimpleDateFormat sdf = new SimpleDateFormat(temp.getFormat());
		sample = new JTextField(sdf.format(DateToStringMungeComponent.SAMPLE_DATE));
		sample.setEditable(false);
		
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
		content.add(new JLabel("Format: "), cc.xy(2, row));
		content.add(format, cc.xy(4, row));
		
		return content;
	}
}
