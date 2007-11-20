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

package ca.sqlpower.matchmaker.swingui.munge;

import java.text.SimpleDateFormat;

import org.apache.log4j.Logger;

import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;
import ca.sqlpower.validation.Validator;

/**
 * This validator is used to check the date form pattern in the MungeComponent.
 * It gets the pattern from the text field and tries to compile it
 * into a {@link SimpleDateFormat}. If the compilation throws a {@link IllegalArgumentException},
 * the syntax of the given pattern is invalid and status fails.
 */
public class DateFormatPatternValidator implements Validator {

    private static final Logger logger = Logger.getLogger(DateFormatPatternValidator.class);
    
    public DateFormatPatternValidator(){
    }
    
    public ValidateResult validate(Object contents) {
    	try {
    		String pattern = (String) contents;
    		SimpleDateFormat dfs = new SimpleDateFormat(pattern);
    	} catch (IllegalArgumentException e) {
    		return ValidateResult.createValidateResult(Status.FAIL, 
    				"The given date format pattern is invalid.");
    	}
        return ValidateResult.createValidateResult(Status.OK, "");
    }
}
