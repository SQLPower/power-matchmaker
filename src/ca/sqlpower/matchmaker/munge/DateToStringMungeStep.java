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
import ca.sqlpower.object.annotation.Transient;

/**
 * This munge step will return a string representation of the given date.
 * It uses a format parameter that specifies the date format pattern.
*/
public class DateToStringMungeStep extends AbstractMungeStep {

	@SuppressWarnings("unchecked")
	public static final List<Class<? extends SPObject>> allowedChildTypes = 
		Collections.unmodifiableList(new ArrayList<Class<? extends SPObject>>(
				Arrays.asList(MungeStepOutput.class,MungeStepInput.class)));
	
	/**
	 * The pattern used to format the Date.
	 */
	private String format;
	
	/**
	 * The date portion of the pattern that helps to build the format.
	 */
	private String dateFormat;
	
	/**
	 * The time portion of the pattern that helps to build the format.
	 */
	private String timeFormat;
	
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
	
	@Constructor
	public DateToStringMungeStep() {
		super("Date to String", false);
		setDateFormat(DATE_FORMATS.get(1));
		setTimeFormat(TIME_FORMATS.get(1));

		MungeStepOutput<String> out = new MungeStepOutput<String>("dateToStringOutput", String.class);
		addChild(out);
		InputDescriptor desc = new InputDescriptor("date", Date.class);
		super.addInput(desc);
	}

	@Override
	public int addInput(InputDescriptor desc) {
		throw new UnsupportedOperationException("Date to String munge step does not support addInput()");
	}

	@Override
	public boolean removeInput(int index) {
		throw new UnsupportedOperationException("Date to String munge step does not support removeInput()");
	}

	public void connectInput(int index, MungeStepOutput o) {
		if (o.getType() != getInputDescriptor(index).getType()) {
			throw new UnexpectedDataTypeException(
			"Date to String munge step does not accept non-Date inputs");
		} else {
			super.connectInput(index, o);
		}
	}

	public Boolean doCall() throws Exception {
		MungeStepOutput<String> out = getOut();
		MungeStepOutput<Date> in = getMSOInputs().get(0);
		Date data = in.getData();
		String result = null;
		String format = this.format;
		
		if (data != null) {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			result = sdf.format(data);
		}
		
		out.setData(result);
		printOutputs();
		return true;
	}
	
	@Transient @Mutator
	public void setFormat(String format) {
		String oldFormat = this.format;
		this.format = format;
		firePropertyChange("format", oldFormat, format);
	}
	
	@Transient @Accessor
	public String getFormat() {
		return format;
	}
	
	/**
	 * Sets the date portion of the pattern and updates the format pattern.
	 */
	@Mutator
	public void setDateFormat(String format) {
		String oldFormat = dateFormat;
		dateFormat = format;
		firePropertyChange("dateFormat", oldFormat, format);
		updateFormat();
	}
	
	/**
	 * Returns the date portion of the format pattern. 
	 */
	@Accessor
	public String getDateFormat() {
		return dateFormat;
	}
	
	/**
	 * Sets the time portion of the pattern and updates the format pattern.
	 */
	@Mutator
	public void setTimeFormat(String format) {
		String oldFormat = timeFormat;
		timeFormat = format;
		firePropertyChange("timeFormat", oldFormat, format);
		updateFormat();
	}
	
	/**
	 * Returns the time portion of the format pattern. 
	 */
	@Accessor
	public String getTimeFormat() {
		return timeFormat;
	}
	
	/**
	 * Updates the format parameter to the concatenation of the date format
	 * and the time format.
	 */
	private void updateFormat() {
		format = getDateFormat() + " " + getTimeFormat();
	}
}
