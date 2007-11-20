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

package ca.sqlpower.matchmaker.munge;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * This munge step will return a string representation of the given date.
 * It uses a format parameter that specifies the date format pattern.
*/
public class DateToStringMungeStep extends AbstractMungeStep {

	/**
	 * The pattern used to format the Date.
	 */
	public static final String FORMAT_PARAM = "format";
	
	/**
	 * The date portion of the pattern that helps to build the format.
	 */
	public static final String DATE_FORMAT_PARAM = "dateFormat";
	
	/**
	 * The time portion of the pattern that helps to build the format.
	 */
	public static final String TIME_FORMAT_PARAM = "timeFormat";
	
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
	public void removeInput(int index) {
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
		String format = getParameter(FORMAT_PARAM);
		
		if (data != null) {
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			result = sdf.format(data);
		}
		
		out.setData(result);
		printOutputs();
		return true;
	}
	
	public void setFormat(String format) {
		setParameter(FORMAT_PARAM, format);
	}
	
	public String getFormat() {
		return getParameter(FORMAT_PARAM);
	}
	
	/**
	 * Sets the date portion of the pattern and updates the format pattern.
	 */
	public void setDateFormat(String format) {
		setParameter(DATE_FORMAT_PARAM, format);
		updateFormat();
	}
	
	/**
	 * Returns the date portion of the format pattern. 
	 */
	public String getDateFormat() {
		return getParameter(DATE_FORMAT_PARAM);
	}
	
	/**
	 * Sets the time portion of the pattern and updates the format pattern.
	 */
	public void setTimeFormat(String format) {
		setParameter(TIME_FORMAT_PARAM, format);
		updateFormat();
	}
	
	/**
	 * Returns the time portion of the format pattern. 
	 */
	public String getTimeFormat() {
		return getParameter(TIME_FORMAT_PARAM);
	}
	
	/**
	 * Updates the format parameter to the concatenation of the date format
	 * and the time format.
	 */
	private void updateFormat() {
		setParameter(FORMAT_PARAM, getDateFormat() + " " + getTimeFormat());
	}
}
