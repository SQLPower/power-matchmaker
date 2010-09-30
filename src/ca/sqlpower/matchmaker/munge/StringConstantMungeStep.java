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

import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.NonProperty;


/**
 * The String Constant Step provides a user-specified String value on its output
 * every time it is called, if the RETURN_NULL parameter is set it will always 
 * return null. It is useful where boilerplate values are required
 * as inputs to other steps, or in a data cleansing situation where each row
 * processed needs a column set to a specific value (perhaps to indicate that
 * the cleansing process has been completed on that row).
 */
public class StringConstantMungeStep extends AbstractMungeStep {

    /**
     * The value this step should provide on its output.
     */
    public static final String VALUE_PARAMETER_NAME = "value";
    
    /**
     * The value to set if the step is to return null
     */
    public static final String RETURN_NULL = "return null";
    
    @Constructor
    public StringConstantMungeStep() {
        super("String constant", false);
        setParameter(RETURN_NULL, "False");
        addChild(new MungeStepOutput<String>("Value", String.class));
    }
    
    @Override
    public Boolean doCall() throws Exception {
    	getOut().setData(getValue());
        return Boolean.TRUE;
    }
    
    @NonProperty
    public void setValue(String newValue) {
        setParameter(VALUE_PARAMETER_NAME, newValue);
    }
    
    @NonProperty
    public String getValue() {
    	if (!isReturningNull()) {
    		return getParameter(VALUE_PARAMETER_NAME);
    	} else {
    		return null;
    	}
    }
    
    @NonProperty
    public boolean isReturningNull() {
    	return getBooleanParameter(RETURN_NULL).booleanValue();
    }
    
    @NonProperty
    public void setReturningNull(boolean b) {
    	setParameter(RETURN_NULL, String.valueOf(b));
    }
}
