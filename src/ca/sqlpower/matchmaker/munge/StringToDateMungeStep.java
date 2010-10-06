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

package ca.sqlpower.matchmaker.munge;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.Mutator;


/**
 * This munge step will convert a string to a date according to the given parsing pattern
 * and output format. If the conversion fails there are two options. If continue on error
 * has been selected it returns null. If not it throws a ParseException. 
 * <p>
 * Nothing is done to deal with the exact time in milliseconds or the time portion that
 * remains in a java.sql.Date format. If you are having problems with this, please contact
 * us online for help.
 * </p> 
 */
public class StringToDateMungeStep extends AbstractMungeStep {
	
	@SuppressWarnings("unchecked")
	public static final List<Class<? extends SPObject>> allowedChildTypes = 
		Collections.unmodifiableList(new ArrayList<Class<? extends SPObject>>(
				Arrays.asList(MungeStepOutput.class,MungeStepInput.class)));
	
	/**
	 * Whether to ignore conversion errors.
	 */
	private boolean ignoreError;

	/**
	 * The pattern used to format the input Date.
	 */
	private String inputFormat;
	
	/**
	 * The date portion of the pattern that helps to build the format.
	 */
	private String dateFormat;
	
	/**
	 * The time portion of the pattern that helps to build the format.
	 */
	private String timeFormat;
	
	/**
	 * The format to output as.
	 */
	private String outputFormat;
	
	/**
	 * List of default date formats.
	 */
	public static final List<String> DATE_FORMATS = Arrays.asList("", "M/d/yyyy", "M/d/yy",
			"MM/dd/yy", "MM/dd/yyyy", "yy/MM/dd", "yyyy/MM/dd", "dd/MMM/yy", "EEE/MMMM/dd/yyyy",
			"MMMM/dd/yyyy", "EEEE/dd/MMMM/yyyy", "dd/MMMM/yyyy");
	
	/**
	 * List of default time formats.
	 */
	public static final List<String> TIME_FORMATS = Arrays.asList("", "h:mm:ss a",
			"hh:mm:ss a", "H:mm:ss", "HH:mm:ss");
	
	/**
	 * Lists of the possible output formats.
	 */
	public static final List<String> OUTPUT_FORMATS = Arrays.asList("Date Only", "Time Only", "Date and Time");
	
	private void updateInputFormat() {
		String old = inputFormat;
		String format = getDateFormat() + (
				((getDateFormat() == null || getDateFormat().equals("")) ||
						((getTimeFormat() == null) || getTimeFormat().equals(""))) ?
						"" : " ") + getTimeFormat();
		inputFormat = format;
		firePropertyChange("inputFormat", old, inputFormat);
	}
	
	@Override
	public int addInput(InputDescriptor desc) {
		throw new UnsupportedOperationException("String to Date munge step does not support addInput()");
	}

	@Override
	public boolean removeInput(int index) {
		throw new UnsupportedOperationException("String to Date munge step does not support removeInput()");
	}

	public void connectInput(int index, MungeStepOutput o) {
		if (o.getType() != getInputDescriptor(index).getType()) {
			throw new UnexpectedDataTypeException(
			"String to Date munge step does not accept non-Date inputs");
		} else {
			super.connectInput(index, o);
		}
	}
	
	@Constructor
	public StringToDateMungeStep() {
		super("String to Date",false);
		setIgnoreError(false);
		setDateFormat(DATE_FORMATS.get(1));
		setTimeFormat(TIME_FORMATS.get(1));
		setOutputFormat(OUTPUT_FORMATS.get(0));
		
		MungeStepOutput<Date> out = new MungeStepOutput<Date>("stringToDateOutput", Date.class);
		addChild(out);
		InputDescriptor desc = new InputDescriptor("String", String.class);
		super.addInput(desc);
	}
	
	public Boolean doCall() throws Exception {
		MungeStepOutput<Date> out = getOut();
		MungeStepOutput<String> in = getMSOInputs().get(0);
		String data = in.getData();
		Date ret = null;
		if (in.getData() != null) {
			try {
				System.out.println(getInputFormat());
				System.out.println(data);
				SimpleDateFormat sdf = new SimpleDateFormat(getInputFormat());
				ret = sdf.parse(data);
				if (OUTPUT_FORMATS.get(0).equals(getOutputFormat())) {
					ret = new java.sql.Date(ret.getTime());
				} else if (OUTPUT_FORMATS.get(1).equals(getOutputFormat())) {
					ret = new Time(ret.getTime());
				}
			} catch (ParseException e) {
				if (isIgnoreError()) {
					logger.error("Problem occured when trying to convert \"" + data + "\" to a date!");
				} else {
					throw new ParseException("Error trying to convert \"" + data + "\" to a date!",
							e.getErrorOffset());
				}
			}
		}
		
		out.setData(ret);
		return true;
	}

	@Accessor
	public boolean isIgnoreError() {
		return ignoreError;
	}

	@Mutator
	public void setIgnoreError(boolean ignoreError) {
		boolean old = this.ignoreError;
		this.ignoreError = ignoreError;
		firePropertyChange("ignoreError", old, ignoreError);
	}

	@Accessor
	public String getInputFormat() {
		return inputFormat;
	}

	@Mutator
	public void setInputFormat(String inputFormat) {
		String old = this.inputFormat;
		this.inputFormat = inputFormat;
		firePropertyChange("inputFormat", old, inputFormat);
	}

	@Accessor
	public String getDateFormat() {
		return dateFormat;
	}

	@Mutator
	public void setDateFormat(String dateFormat) {
		String old = this.dateFormat;
		this.dateFormat = dateFormat;
		firePropertyChange("dateFormat", old, dateFormat);
		updateInputFormat();
	}

	@Accessor
	public String getTimeFormat() {
		return timeFormat;
	}

	@Mutator
	public void setTimeFormat(String timeFormat) {
		String old = this.timeFormat;
		this.timeFormat = timeFormat;
		firePropertyChange("timeFormat", old, timeFormat);
		updateInputFormat();
	}

	@Accessor
	public String getOutputFormat() {
		return outputFormat;
	}

	@Mutator
	public void setOutputFormat(String outputFormat) {
		String old = this.outputFormat;
		this.outputFormat = outputFormat;
		firePropertyChange("outputFormat", old, outputFormat);
	}
	
	@Override
	protected void copyPropertiesForDuplicate(MungeStep copy) {
		StringToDateMungeStep step = (StringToDateMungeStep) copy;
		step.setDateFormat(getDateFormat());
		step.setIgnoreError(isIgnoreError());
		step.setInputFormat(getInputFormat());
		step.setOutputFormat(getOutputFormat());
		step.setTimeFormat(getTimeFormat());
	}
}