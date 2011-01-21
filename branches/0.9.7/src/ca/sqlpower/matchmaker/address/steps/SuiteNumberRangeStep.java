/*
 * Copyright (c) 2009, SQL Power Group Inc.
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

package ca.sqlpower.matchmaker.address.steps;

import ca.sqlpower.matchmaker.address.Address;
import ca.sqlpower.matchmaker.address.PostalCode;

import com.sleepycat.je.DatabaseException;

/**
 * This step will check if the suite number is inside the valid range of suite
 * numbers in a postal code. The {@link SuiteNumberMissingStep} checks if the
 * suite number is missing.
 */
public class SuiteNumberRangeStep implements ValidateStep {

	public boolean validate(PostalCode pc, Address a, Address suggestion,
			ValidateState state) throws DatabaseException {
		//Page D-10 of the SERP handbook. If the suite goes from non-numeric to numeric, accept anything
		boolean fromIsNumeric;
		try {
			Integer.parseInt(pc.getSuiteFromNumber());
			fromIsNumeric = true;
		} catch (NumberFormatException e) {
			fromIsNumeric = false;
		}
		boolean toIsNumeric;
		try {
			Integer.parseInt(pc.getSuiteToNumber());
			toIsNumeric = true;
		} catch (NumberFormatException e) {
			toIsNumeric = false;
		}
		if ((fromIsNumeric && !toIsNumeric) || (!fromIsNumeric && toIsNumeric)) {
			return false;
		}
		
		if (pc.getStreetAddressFromNumber().equals(a.getStreetNumber())) {
			if (fromIsNumeric) {
				if (a.getSuite() == null || a.getSuite().trim().length() == 0) {
					return false;
				}
				Integer suiteNumber;
				try {
					suiteNumber = Integer.parseInt(a.getSuite());
				} catch (NumberFormatException e) {
					state.incrementErrorAndSetValidate("Suite number non-numeric when a numeric suite number is required.", false);
					return false;
				}
				if (Integer.parseInt(pc.getSuiteFromNumber()) > suiteNumber) {
					state.incrementErrorAndSetValidate("Suite number falls before allowed suite range.", false);
					return false;
				}
			} else {
				//TODO parse and handle non-numeric suite numbers.
			}
		}
		
		if (pc.getStreetAddressToNumber().equals(a.getStreetNumber())) {
			if (toIsNumeric) {
				if (a.getSuite() == null || a.getSuite().trim().length() == 0) {
					return false;
				}
				Integer suiteNumber;
				try {
					suiteNumber = Integer.parseInt(a.getSuite());
				} catch (NumberFormatException e) {
					state.incrementErrorAndSetValidate("Suite number non-numeric when a numeric suite number is required.", false);
					return false;
				}
				if (Integer.parseInt(pc.getSuiteToNumber()) < suiteNumber) {
					state.incrementErrorAndSetValidate("Suite number falls after allowed suite range.", false);
					return false;
				}
			} else {
				//TODO parse and handle non-numeric suite numbers.
			}
		}
				
        return false;
	}

}
