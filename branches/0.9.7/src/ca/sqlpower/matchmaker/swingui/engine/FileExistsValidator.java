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

package ca.sqlpower.matchmaker.swingui.engine;

import java.io.File;

import org.apache.log4j.Logger;

import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;
import ca.sqlpower.validation.Validator;

/**
 * A validator that checks if the engine path points to an actual file.
 * If the file doesn't exist, the validation error message will be:
 * <p>
 * <b><i>fileTerm</i> not found at given location</b>.
 */
public class FileExistsValidator implements Validator {
	
	private static final Logger logger = Logger
			.getLogger(FileExistsValidator.class);
	
	/**
	 * The term used to refer to the file we're validating.
	 */
	private final String fileTerm;

	public FileExistsValidator(String fileTerm) {
		this.fileTerm = fileTerm;
	}

	/**
	 * Checks that the given object is the path name of a file that
	 * exists.
	 * 
	 * @param contents The path name to check.  Must be of type String.
	 */
	public ValidateResult validate(Object contents) {
		
		String fileLocation = (String) contents;
		File file = new File(fileLocation);
		if (!file.exists()) {
			return ValidateResult.createValidateResult(
					Status.FAIL,
					fileTerm + " not found at given location");
		}

		return ValidateResult.createValidateResult(Status.OK, "");
	}

}