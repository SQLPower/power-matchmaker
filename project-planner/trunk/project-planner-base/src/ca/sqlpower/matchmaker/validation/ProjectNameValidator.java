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

package ca.sqlpower.matchmaker.validation;

import ca.sqlpower.matchmaker.Project;
import ca.sqlpower.matchmaker.swingui.MatchMakerSwingSession;
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;
import ca.sqlpower.validation.Validator;

public class ProjectNameValidator implements Validator {

	private MatchMakerSwingSession session;
	private Project project;
    private static final int MAX_CHAR_PROJECT_NAME = 80;

	public ProjectNameValidator(MatchMakerSwingSession session, Project project) {
		this.session = session;
		this.project = project;
	}

	public ValidateResult validate(Object contents) {	    
		String value = (String)contents;
		if ( value == null || value.length() == 0 ) {
			return ValidateResult.createValidateResult(Status.FAIL,
					"Project name is required");
		} else if (value.length() > 40) {
			return ValidateResult.createValidateResult(Status.FAIL, "Project name must not be longer than 40 characters.");
		}
		else if ( !value.equals(project.getName()) &&
					!session.isThisProjectNameAcceptable(value) ) {
			return ValidateResult.createValidateResult(Status.FAIL,
					"Project name is invalid or already exists.");
		} else if (value.length() > MAX_CHAR_PROJECT_NAME){
		    return ValidateResult.createValidateResult(Status.FAIL, "Match ID cannot be more than "+
                    MAX_CHAR_PROJECT_NAME + " characters long");
        }
		return ValidateResult.createValidateResult(Status.OK, "");
	}
}