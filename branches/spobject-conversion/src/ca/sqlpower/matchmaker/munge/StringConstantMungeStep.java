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

import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.Mutator;


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
    private String outValue;
    
    /**
     * Whether the step is to return null
     */
    private boolean returnNull;
    
    @Accessor
    public String getOutValue() {
		return outValue;
	}

    @Mutator
	public void setOutValue(String outValue) {
    	String old = this.outValue;
		this.outValue = outValue;
		firePropertyChange("outValue", old, outValue);
	}

    @Accessor
	public boolean isReturnNull() {
		return returnNull;
	}

    @Mutator
	public void setReturnNull(boolean returnNull) {
    	boolean old = this.returnNull;
		this.returnNull = returnNull;
		firePropertyChange("returnNull", old, returnNull);
	}

	@Constructor
    public StringConstantMungeStep() {
        super("String constant", false);
        setReturnNull(false);
        addChild(new MungeStepOutput<String>("Value", String.class));
    }
    
    @Override
    public Boolean doCall() throws Exception {
    	getOut().setData(getOutValue());
        return Boolean.TRUE;
    }
}
