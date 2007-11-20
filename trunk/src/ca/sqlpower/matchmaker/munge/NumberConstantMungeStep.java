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

import java.math.BigDecimal;


/**
 * The Number Constant Step provides a user-specified Number value on its output
 * every time it is called, if the RETURN_NULL parameter is set it will always 
 * return null. It is useful where boilerplate values are required
 * as inputs to other steps, or in a data cleansing situation where each row
 * processed needs a column set to a specific value (perhaps to indicate that
 * the cleansing process has been completed on that row).
 */
public class NumberConstantMungeStep extends AbstractMungeStep {

    /**
     * The value this step should provide on its output.
     */
    public static final String VALUE_PARAMETER_NAME = "value";
    
    /**
     * The value to set if the step is to return null
     */
    public static final String RETURN_NULL = "return null";
    
    public NumberConstantMungeStep() {
        super("Number Constant", false);
        setParameter(RETURN_NULL, "False");
        addChild(new MungeStepOutput<BigDecimal>("Value", BigDecimal.class));
    }
    
    @Override
    public Boolean doCall() throws Exception {
    	getOut().setData(getValue());
        return Boolean.TRUE;
    }
    
    public void setValue(BigDecimal newValue) {
        setParameter(VALUE_PARAMETER_NAME, newValue.toPlainString());
    }
    
    public BigDecimal getValue() {
    	if (!isReturningNull()) {
    		return new BigDecimal(getParameter(VALUE_PARAMETER_NAME));
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
}
