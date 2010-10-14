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

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.munge.DateConstantMungeStep;
import ca.sqlpower.matchmaker.munge.MungeStep;
import ca.sqlpower.swingui.SPSUtils;
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;
import ca.sqlpower.validation.Validator;
import ca.sqlpower.validation.swingui.FormValidationHandler;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

public class DateConstantMungeComponent extends AbstractMungeComponent {

	JCheckBox useCurrent;
	JCheckBox retNull;
	SpinnerDateModel dc;
	JSpinner date;
	JComboBox opts;
	JSpinner.DateEditor editor;

	public DateConstantMungeComponent(MungeStep step, FormValidationHandler handler, MatchMakerSession session) {
		super(step, handler, session);		
	}

	@Override
	protected JPanel buildUI() {
		final DateConstantMungeStep step = (DateConstantMungeStep) getStep();
		Date value;
		try {
			value = ((DateConstantMungeStep) getStep()).getValueAsDate();
		} catch (ParseException e) {
			SPSUtils.showExceptionDialogNoReport(getPen(), "Error Loading munge step", e);
			value = null;
		}
		if (value == null) {
		    value = new Date();
		    ((DateConstantMungeStep) getStep()).setValueAsDate(value);
		}
		dc = new SpinnerDateModel(value, null, null, Calendar.SECOND);
		date = new JSpinner(dc);
		editor = new JSpinner.DateEditor(date, getFormateString());
		date.setEditor(editor);
		getHandler().addValidateObject(date, new DateValidator());


		dc.addChangeListener(new ChangeListener(){
			public void stateChanged(ChangeEvent e) {
				setDate();
			}
		});

		retNull = new JCheckBox("Return null");
		retNull.addActionListener(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				boolean b = retNull.isSelected();
				if (b) {
					useCurrent.setEnabled(false);
					useCurrent.setSelected(false);
					date.setEnabled(false);
					opts.setEnabled(!retNull.isSelected());
				} else if (!useCurrent.isSelected()) {
					date.setEnabled(true);
					useCurrent.setEnabled(true);
					opts.setEnabled(!retNull.isSelected());
				}
				step.setReturnNull(b);
			}
		});

		useCurrent = new JCheckBox("Use Time of engine run");
		useCurrent.addActionListener(new AbstractAction() {
			public void actionPerformed(ActionEvent e) {
				boolean b = useCurrent.isSelected();
				if (b) {
					retNull.setEnabled(false);
					retNull.setSelected(false);
					date.setEnabled(false);
					opts.setEnabled(!retNull.isSelected());
				} else if (!retNull.isSelected()) {
					date.setEnabled(true);
					opts.setEnabled(!retNull.isSelected());
					retNull.setEnabled(true);
				}
				step.setUseCurrentTime(b);
			}
		});

		retNull.setSelected(step.isReturnNull());
		if (retNull.isSelected()) {
			useCurrent.setEnabled(false);
		}
		useCurrent.setSelected(step.getUseCurrentTime());
		if (useCurrent.isSelected()) {
			retNull.setEnabled(false);
		}
		date.setEnabled(!retNull.isSelected() && !useCurrent.isSelected());



		opts = new JComboBox(DateConstantMungeStep.FORMAT.toArray());
		opts.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				step.setDateFormat((String) opts.getSelectedItem());
				date.setEditor(new JSpinner.DateEditor(date,getFormateString()));
			}
		});

		
		opts.setSelectedItem(step.getDateFormat());
		opts.setEnabled(!retNull.isSelected());

		FormLayout layout = new FormLayout("pref,4dlu,pref:grow");
		DefaultFormBuilder fb = new DefaultFormBuilder(layout);
		fb.append("Value:", date);
		fb.append("Format", opts);
		fb.append("",retNull);
		fb.append("",useCurrent);

		return fb.getPanel();
	}

	private void setDate() {
		if (dc.getDate() != null) {
			((DateConstantMungeStep) getStep()).setValueAsDate(dc.getDate());
		}
	}

	private String getFormateString() {
		DateConstantMungeStep step = (DateConstantMungeStep) getStep();
		if (step.getDateFormat() != null) {
			if (step.getDateFormat().equals(DateConstantMungeStep.FORMAT.get(1))) {
				return "yyyy/MM/dd";
			} else if (step.getDateFormat().equals(DateConstantMungeStep.FORMAT.get(2))) { 
				return "HH:mm:ss";
			}
		}
		return "yyyy/MM/dd HH:mm:ss";

	}

	private class DateValidator implements Validator {
		public ValidateResult validate(Object contents) {
			if (contents == null && date.isEnabled()) {
				return ValidateResult.createValidateResult(Status.FAIL, "Invalid Date");
			} else {
				return ValidateResult.createValidateResult(Status.OK, "");
			}
		}
	}
}