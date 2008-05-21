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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;


/**
 * The Date Constant Step provides a user-specified Date value on its output
 * every time it is called, if the RETURN_NULL parameter is set it will always 
 * return null. If use current is set it will use the time that the engine calls it.
 */
public class DateConstantMungeStep extends AbstractMungeStep {

	public static final Logger mungeStepLogger = Logger.getLogger(DateConstantMungeStep.class);
	
    /**
     * The value this step should provide on its output.
     */
    public static final String VALUE_PARAMETER_NAME = "value";
    
    /**
     * The format the date will be stored in memory
     */
    public static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";
    
    /**
     * The value to set if the step is to return null
     */
    public static final String RETURN_NULL = "return null";
    
    /**
     * The value to set to "true" if you want the current time the engine is 
     * run.
     */
    public static final String USE_CURRENT_TIME = "use current time";
    
    /**
     * The possible options for formating this date
     */
    public static final String[] FORMAT = new String[]{"Date and Time", "Date Only", "Time Only"};
    
    /**
     * The parameter name for the storage of the format type
     */
    public static final String FORMAT_PARAMETER_NAME = "format type";
    
    public DateConstantMungeStep() throws Exception {
        super("Date Constant", false);
        setParameter(RETURN_NULL, "False");
        setParameter(USE_CURRENT_TIME, "False");
        setParameter(FORMAT_PARAMETER_NAME, FORMAT[0]);
        addChild(new MungeStepOutput<Date>("Value", Date.class));
    }
    
    @Override
    public Boolean doCall() throws Exception {
    	getOut().setData(getValue());
        return Boolean.TRUE;
    }
    
    public void setValue(Date newValue) throws Exception {
    	SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
    	String date = df.format(newValue);
	    setParameter(VALUE_PARAMETER_NAME, date);
    }
    
    public Date getValue() throws Exception {
    if (getUseCurrentTime()) {
    	return Calendar.getInstance().getTime();
    } else if (!isReturningNull()) {
    		SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
    		Date date = df.parse(getParameter(VALUE_PARAMETER_NAME));
    		
    		if (getParameter(FORMAT_PARAMETER_NAME).equals(FORMAT[1])) {
    			return new java.sql.Date(date.getTime());
    		} else if (getParameter(FORMAT_PARAMETER_NAME).equals(FORMAT[2])) {
    			return new Time(date.getTime());
    		} else {
    			return date;
    		}
    	} else {
    		return null;
    	}
    }
    
    public boolean isReturningNull() {
    	return getBooleanParameter(RETURN_NULL).booleanValue();
    }
    
    public void setReturningNull(boolean b) {
    	setParameter(RETURN_NULL, String.valueOf(b));
    }
    
    public void setUseCurrentTime(boolean b) {
    	setParameter(USE_CURRENT_TIME, String.valueOf(b));
    }
    
    public boolean getUseCurrentTime() {
    	return getBooleanParameter(USE_CURRENT_TIME).booleanValue();
    }
}
