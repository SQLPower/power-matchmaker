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

package ca.sqlpower.matchmaker.validation;

import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;
import ca.sqlpower.validation.Validator;

public class ProjectDescriptionValidator implements Validator {
	
    private static final int MAX_CHAR_PROJECT_DESC = 15000;

	public ValidateResult validate(Object contents) {	    
		String value = (String)contents;
		if (value.length() > MAX_CHAR_PROJECT_DESC){
		    return ValidateResult.createValidateResult(Status.FAIL, "Project description cannot be more than "+
                    MAX_CHAR_PROJECT_DESC + " characters long");
        }
		return ValidateResult.createValidateResult(Status.OK, "");
	}
}
