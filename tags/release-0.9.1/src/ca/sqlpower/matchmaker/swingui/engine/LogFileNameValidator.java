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

package ca.sqlpower.matchmaker.swingui.engine;

import java.io.File;
import java.io.IOException;

import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;
import ca.sqlpower.validation.Validator;

/**
 * A Validator to ensure that the supplied log file path is a valid
 * file, or if it doesn't exist, then checks if it can be created.
 */
public class LogFileNameValidator implements Validator {
	
	/**
	 * Returns a {@link ValidateResult} with {@link Status#FAIL} if:
	 * <li> the log file name is empty</li>
	 * <li> the log file path provided is not a file</li>
	 * <li> the log file doesn't exist and cannot be created</li>
	 * <p>
	 * Otherwise, it returns a ValidateResult with {@link Status#OK}
	 */
	public ValidateResult validate(Object contents) {
		String name = (String) contents;
		if (name == null || name.length() == 0) {
			return ValidateResult.createValidateResult(Status.FAIL,
					"Log file is required.");
		}
		File log = new File(name);
		if (log.exists()) {
			if (!log.isFile()) {
				return ValidateResult.createValidateResult(Status.FAIL,
						"Log file name is invalid.");
			}

			// can't reliably check if file is writable on Windows,
			// so we'll just assume it is.
		} else {
			try {
				if (!log.createNewFile()) {
					return ValidateResult.createValidateResult(Status.FAIL,
							"Log file can not be created.");
				}
			} catch (IOException e) {
				return ValidateResult.createValidateResult(Status.FAIL,
						"Log file can not be created.");
			} finally {
				log.delete();
			}
		}
		return ValidateResult.createValidateResult(Status.OK, "");
	}

}