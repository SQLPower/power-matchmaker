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



/**
 * This step returns a constant boolean or null.
 */
public class BooleanConstantMungeStep extends AbstractMungeStep {

	public static final String BOOLEAN_VALUE = "value";
	public static final String TRUE = "True";
	public static final String FALSE = "False";
	public static final String NULL = "Null";
	
	
	public BooleanConstantMungeStep() {
		super("Boolean Constant", false);
		setParameter(BOOLEAN_VALUE, TRUE);
		addChild(new MungeStepOutput<Boolean>("boolean out",Boolean.class));
	}
	
	
	public Boolean doCall() throws Exception {
		MungeStepOutput<Boolean> out = getOut();
		String val = getParameter(BOOLEAN_VALUE);
		if (val.equals(TRUE)) {
			out.setData(true);
		} else if (val.equals(FALSE)) {
			out.setData(false);
		} else {
			out.setData(null);
		}
		return true;
	}
}
