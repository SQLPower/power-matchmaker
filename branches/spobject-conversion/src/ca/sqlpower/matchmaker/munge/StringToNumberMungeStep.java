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

import ca.sqlpower.object.annotation.Accessor;
import ca.sqlpower.object.annotation.Constructor;
import ca.sqlpower.object.annotation.Mutator;


/**
 * This munge step will convert a string to a number. If the conversion fails there are two options.
 * If continue on error has been selected it returns null. If not it throws a Numberformat error.
 */
public class StringToNumberMungeStep extends AbstractMungeStep {
	
	/**
	 * Whether the munge step should continue in the face of evil, malformed numbers.
	 */
	private boolean allowMalformed;

	@Constructor
	public StringToNumberMungeStep() {
		super("String to Number",false);
		allowMalformed = false;
		MungeStepOutput<BigDecimal> out = new MungeStepOutput<BigDecimal>("StringToNumberOutput", BigDecimal.class);
		addChild(out);
		InputDescriptor desc = new InputDescriptor("String", String.class);
		super.addInput(desc);
	}
	
	public Boolean doCall() throws Exception {
		MungeStepOutput<BigDecimal> out = getOut();
		MungeStepOutput<String> in = getMSOInputs().get(0);
		String data = in.getData();
		BigDecimal ret = null;
		if (in.getData() != null) {
			try {
			ret = new BigDecimal(data);
			} catch (NumberFormatException e) {
				if (isAllowMalformed()) {
					logger.error("Problem occured when trying to convert \"" + data + "\" to a number!");
					ret = null;
				} else {
					throw new NumberFormatException("Error trying to convert \"" + data + "\" to a number!");
				}
			}
		}
		
		out.setData(ret);
		return true;
	}

	@Mutator
	public void setAllowMalformed(boolean allowMalformed) {
			boolean old = this.allowMalformed;
			this.allowMalformed = allowMalformed;
			firePropertyChange("allowMalformed", old, allowMalformed);
	}

	@Accessor
	public boolean isAllowMalformed() {
		return allowMalformed;
	}
}



