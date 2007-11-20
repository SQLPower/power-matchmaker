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

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * This munge step will return a string representation of the given date.
 * The options for this step are format and style. Formats include displaying
 * the date only, time only, or both. Styles include full, long, medium and short.
 */
public class DateToStringMungeStep extends AbstractMungeStep {

	/**
	 * This is the name of the parameter that determines the style of the
	 * date portion. The parameter will be stored as an integer according
	 * to the location in {@value #STYLES}. 
	 */
	public static final String DATE_STYLE_PARAMETER_NAME = "dateStyle";

	/**
	 * This is the name of the parameter that determines the style of the
	 * time portion. The parameter will be stored as an integer according
	 * to the location in {@value #STYLES}. 
	 */
	public static final String TIME_STYLE_PARAMETER_NAME = "timeStyle";

	/**
	 * This is the name of the parameter that determines the which portions
	 * of a Date to include in the string representation. The list of options
	 * are in {@value #FORMATS}.
	 */
	public static final String FORMAT_PARAMETER_NAME = "dateTime";

	/**
	 * This is a list of the different possible styles.
	 */
	public static final List<String> STYLES = Arrays.asList("Full", "Long", "Medium", "Short");

	/**
	 * This is a list of the different possible formats.
	 */
	public static final List<String> FORMATS = Arrays.asList("Date Only", "Time Only", "Date and Time");

	public DateToStringMungeStep() {
		super("Date to String", false);

		setParameter(DATE_STYLE_PARAMETER_NAME, 2);
		setParameter(TIME_STYLE_PARAMETER_NAME, 2);
		setParameter(FORMAT_PARAMETER_NAME, FORMATS.get(0));

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
		DateFormat formatter = null;
		if (data != null) {
			String format = getParameter(FORMAT_PARAMETER_NAME);
			int dateStyle = getIntegerParameter(DATE_STYLE_PARAMETER_NAME);
			int timeStyle = getIntegerParameter(TIME_STYLE_PARAMETER_NAME);
			int loc = FORMATS.indexOf(format);
			
			if (loc > -1) {
				if (loc == 0) {
					formatter = DateFormat.getDateInstance(dateStyle);
				} else if (loc == 1) {
					formatter = DateFormat.getTimeInstance(timeStyle);
				} else {
					formatter = DateFormat.getDateTimeInstance(dateStyle, timeStyle);
				}
				result = formatter.format(in.getData());
			}
		}
		out.setData(result);
		printOutputs();
		return true;
	}
	
	public void setFormat(String format) {
		if (FORMATS.contains(format)) {
			setParameter(FORMAT_PARAMETER_NAME, format);
		}
	}
	
	public String getFormat() {
		return getParameter(FORMAT_PARAMETER_NAME);
	}
	
	public void setDateStyle(String style) {
		int loc = STYLES.indexOf(style);
		if (loc > -1) {
			setParameter(DATE_STYLE_PARAMETER_NAME, loc);
		}
	}
	
	public String getDateStyle() {
		return STYLES.get(getIntegerParameter(DATE_STYLE_PARAMETER_NAME));
	}
	
	public void setTimeStyle(String style) {
		int loc = STYLES.indexOf(style);
		if (loc > -1) {
			setParameter(TIME_STYLE_PARAMETER_NAME, loc);
		}
	}
	
	public String getTimeStyle() {
		return STYLES.get(getIntegerParameter(TIME_STYLE_PARAMETER_NAME));
	}
}
