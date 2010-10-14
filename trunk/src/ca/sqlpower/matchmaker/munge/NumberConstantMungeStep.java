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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ca.sqlpower.object.SPObject;
import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.Mutator;


/**
 * The Number Constant Step provides a user-specified Number value on its output
 * every time it is called, if the RETURN_NULL parameter is set it will always 
 * return null. It is useful where boilerplate values are required
 * as inputs to other steps, or in a data cleansing situation where each row
 * processed needs a column set to a specific value (perhaps to indicate that
 * the cleansing process has been completed on that row).
 */
public class NumberConstantMungeStep extends AbstractMungeStep {

	@SuppressWarnings("unchecked")
	public static final List<Class<? extends SPObject>> allowedChildTypes = 
		Collections.unmodifiableList(new ArrayList<Class<? extends SPObject>>(
				Arrays.asList(MungeStepOutput.class,MungeStepInput.class)));
	
    /**
     * The value this step should provide on its output.
     */
	private BigDecimal value;
    
    @Constructor
    public NumberConstantMungeStep() {
        super("Number Constant", false);
        addChild(new MungeStepOutput<BigDecimal>("Value", BigDecimal.class));
    }
    
    @Override
    public Boolean doCall() throws Exception {
    	getOut().setData(getValue());
        return Boolean.TRUE;
    }
    
    @Mutator
    public void setValue(BigDecimal newValue) {
    	BigDecimal oldValue = value;
    	value = newValue;
    	firePropertyChange("value", oldValue, newValue);
    }
    
    @Accessor
    public BigDecimal getValue() {
    	return value;
    }

    @Override
    protected void copyPropertiesForDuplicate(MungeStep copy) {
    	NumberConstantMungeStep step = (NumberConstantMungeStep) copy;
    	step.setValue(getValue());
    }
}
