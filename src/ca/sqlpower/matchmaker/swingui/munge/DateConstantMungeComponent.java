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
		handler.addValidateObject(date, new DateValidator());
	}

	@Override
	protected JPanel buildUI() {
		Date value = null;
		try {
			value = ((DateConstantMungeStep) getStep()).getValue();
		} catch (ParseException e) {
			SPSUtils.showExceptionDialogNoReport(getPen(), "Error Loading munge step", e);
			value = Calendar.getInstance().getTime();
			((DateConstantMungeStep) getStep()).setValue(value);
		}
		dc = new SpinnerDateModel(value, null, null, Calendar.SECOND);
		date = new JSpinner(dc);
		editor = new JSpinner.DateEditor(date, getFormateString());
		date.setEditor(editor);


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
				getStep().setParameter(DateConstantMungeStep.RETURN_NULL, String.valueOf(b));
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
				getStep().setParameter(DateConstantMungeStep.USE_CURRENT_TIME, String.valueOf(b));
			}
		});

		retNull.setSelected(Boolean.valueOf(getStep().getParameter(DateConstantMungeStep.RETURN_NULL)).booleanValue());
		if (retNull.isSelected()) {
			useCurrent.setEnabled(false);
		}
		useCurrent.setSelected(Boolean.valueOf(getStep().getParameter(DateConstantMungeStep.USE_CURRENT_TIME)).booleanValue());
		if (useCurrent.isSelected()) {
			retNull.setEnabled(false);
		}
		date.setEnabled(!retNull.isSelected() && !useCurrent.isSelected());



		opts = new JComboBox(DateConstantMungeStep.FORMAT.toArray());
		opts.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				getStep().setParameter(DateConstantMungeStep.FORMAT_PARAMETER_NAME,(String) opts.getSelectedItem());
				date.setEditor(new JSpinner.DateEditor(date,getFormateString()));
			}
		});

		opts.setSelectedItem(getStep().getParameter(DateConstantMungeStep.FORMAT_PARAMETER_NAME));
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
			((DateConstantMungeStep) getStep()).setValue(dc.getDate());
		}
	}

	private String getFormateString() {
		if (getStep().getParameter(DateConstantMungeStep.FORMAT_PARAMETER_NAME) != null) {
			if (getStep().getParameter(DateConstantMungeStep.FORMAT_PARAMETER_NAME).equals(DateConstantMungeStep.FORMAT.get(1))) {
				return "yyyy/MM/dd";
			} else if (getStep().getParameter(DateConstantMungeStep.FORMAT_PARAMETER_NAME).equals(DateConstantMungeStep.FORMAT.get(2))) { 
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