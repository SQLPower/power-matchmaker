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

/**
 * Checks if the suite number is missing if the postal code requires it.
 */
public class SuiteNumberMissingStep implements ValidateStep {

    public boolean validate(PostalCode pc, Address a, Address suggestion,
            ValidateState state) {
        if (pc.getStreetAddressFromNumber() != null && pc.getStreetAddressToNumber() != null && pc.getStreetAddressFromNumber().equals(pc.getStreetAddressToNumber()) 
                && pc.getSuiteFromNumber() != null && pc.getSuiteToNumber() != null && pc.getSuiteFromNumber().trim().length() > 0 && pc.getSuiteToNumber().trim().length() > 0
                && suggestion.getSuite() == null) {
            state.incrementErrorAndSetValidate("Suite number missing when postal code requires it.", false);
        }
        return false;
    }

}
