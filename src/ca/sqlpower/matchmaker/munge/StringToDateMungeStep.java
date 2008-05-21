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

package ca.sqlpower.matchmaker.munge;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


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
	
	/**
	 * Determines whether to ignore conversion errors.
	 */
	public static final String IGNORE_ERROR_PARAM = "ignoreError";

	/**
	 * The pattern used to format the input Date.
	 */
	public static final String INPUT_FORMAT_PARAM = "inputFormat";
	
	/**
	 * The date portion of the pattern that helps to build the format.
	 */
	public static final String DATE_FORMAT_PARAM = "dateFormat";
	
	/**
	 * The time portion of the pattern that helps to build the format.
	 */
	public static final String TIME_FORMAT_PARAM = "timeFormat";
	
	/**
	 * The format to output as.
	 */
	public static final String OUTPUT_FORMAT_PARAM = "outputFormat";
	
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
	
	@Override
	public int addInput(InputDescriptor desc) {
		throw new UnsupportedOperationException("String to Date munge step does not support addInput()");
	}

	@Override
	public void removeInput(int index) {
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
	
	public StringToDateMungeStep() {
		super("String to Date",false);
		setParameter(IGNORE_ERROR_PARAM, false);
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
				SimpleDateFormat sdf = new SimpleDateFormat(getInputFormat());
				ret = sdf.parse(data);
				if (OUTPUT_FORMATS.get(0).equals(getOutputFormat())) {
					ret = new java.sql.Date(ret.getTime());
				} else if (OUTPUT_FORMATS.get(1).equals(getOutputFormat())) {
					ret = new Time(ret.getTime());
				}
			} catch (ParseException e) {
				if (getBooleanParameter(IGNORE_ERROR_PARAM)) {
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
	
	public void setInputFormat(String format) {
		setParameter(INPUT_FORMAT_PARAM, format);
	}
	
	public String getInputFormat() {
		return getParameter(INPUT_FORMAT_PARAM);
	}
	
	/**
	 * Sets the date portion of the pattern and updates the format pattern.
	 */
	public void setDateFormat(String format) {
		setParameter(DATE_FORMAT_PARAM, format);
		updateInputFormat();
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
		updateInputFormat();
	}
	
	/**
	 * Returns the time portion of the format pattern.
	 */
	public String getTimeFormat() {
		return getParameter(TIME_FORMAT_PARAM);
	}
	
	/**
	 * Updates the input format parameter to the concatenation of the date format
	 * and the time format.
	 */
	private void updateInputFormat() {
		String format = getDateFormat() + " " + getTimeFormat();
		setParameter(INPUT_FORMAT_PARAM, format.trim());
	}
	
	public void setOutputFormat(String format) {
		if (OUTPUT_FORMATS.contains(format)) {
			setParameter(OUTPUT_FORMAT_PARAM, format);
		}
	}
	
	public String getOutputFormat() {
		return getParameter(OUTPUT_FORMAT_PARAM);
	}
}



