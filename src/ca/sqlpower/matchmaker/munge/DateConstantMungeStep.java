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
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.Mutator;
import ca.sqlpower.object.annotation.Transient;


/**
 * The Date Constant Step provides a user-specified Date value on its output
 * every time it is called, if the RETURN_NULL parameter is set it will always 
 * return null. If use current is set it will use the time that the engine calls it.
 */
public class DateConstantMungeStep extends AbstractMungeStep {

	@SuppressWarnings("unchecked")
	public static final List<Class<? extends SPObject>> allowedChildTypes = 
		Collections.unmodifiableList(new ArrayList<Class<? extends SPObject>>(
				Arrays.asList(MungeStepOutput.class,MungeStepInput.class)));
	
	public static final Logger mungeStepLogger = Logger.getLogger(DateConstantMungeStep.class);
	
    /**
     * The value this step should provide on its output.
     */
	private String value;
    
    /**
     * The format the date will be stored in memory
     */
    public static final String DATE_FORMAT = "yyyy/MM/dd HH:mm:ss";
    
    /**
     * The value to set if the step is to return null
     */
    private boolean returnNull;
    
    /**
     * The value to set to "true" if you want the current time the engine is 
     * run.
     */
    private boolean useCurrentTime;
    
    /**
     * The possible options for formating this date
     */
    public static final List<String> FORMAT = Collections.unmodifiableList(Arrays.asList("Date and Time", "Date Only", "Time Only"));
    
    /**
     * The parameter name for the storage of the format type
     */
    private String dateFormat;
    
    @Constructor
    public DateConstantMungeStep() throws Exception {
        super("Date Constant", false);
        returnNull = false;
        useCurrentTime = false;
        dateFormat = FORMAT.get(0);
        setValueAsDate(Calendar.getInstance().getTime());
        addChild(new MungeStepOutput<Date>("Value", Date.class));
    }
    
    @Override
    public Boolean doCall() throws Exception {
    	getOut().setData(getValueAsDate());
        return Boolean.TRUE;
    }
    
    @Transient @Mutator
    public void setValueAsDate(Date newValue) {
    	SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
    	String date = df.format(newValue);
    	setValue(date);
    }
    
    @Mutator
    public void setValue(String value) {
    	String oldVal = this.value;
    	this.value = value;
    	firePropertyChange("value", oldVal, value);
    }
    
    @Transient @Accessor
    public Date getValueAsDate() throws ParseException {
    	if (getUseCurrentTime()) {
    		return Calendar.getInstance().getTime();
    	} else if (!isReturningNull()) {
    		SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
    		Date date = df.parse(value);

    		if (dateFormat.equals(FORMAT.get(1))) {
    			return new java.sql.Date(date.getTime());
    		} else if (dateFormat.equals(FORMAT.get(2))) {
    			return new Time(date.getTime());
    		} else {
    			return date;
    		}
    	} else {
    		return null;
    	}
    }

    @Accessor
    public boolean isReturningNull() {
    	return returnNull;
    }
    
    @Mutator
    public void setUseCurrentTime(boolean b) {
    	boolean oldVal = this.useCurrentTime;
    	this.useCurrentTime = b;
    	firePropertyChange("useCurrentTime", oldVal, b);
    }
    
    @Accessor
    public boolean getUseCurrentTime() {
    	return useCurrentTime;
    }

    @Accessor
	public boolean isReturnNull() {
		return returnNull;
	}

    @Mutator
	public void setReturnNull(boolean returnNull) {
    	boolean oldVal = this.returnNull;
		this.returnNull = returnNull;
		firePropertyChange("returnNull", oldVal, returnNull);
	}

    @Accessor
	public String getDateFormat() {
		return dateFormat;
	}

    @Mutator
	public void setDateFormat(String dateFormat) {
    	String oldFormat = this.dateFormat;
		this.dateFormat = dateFormat;
		firePropertyChange("dateFormat", oldFormat, dateFormat);
	}

    @Accessor
	public String getValue() {
		return value;
	}
}
