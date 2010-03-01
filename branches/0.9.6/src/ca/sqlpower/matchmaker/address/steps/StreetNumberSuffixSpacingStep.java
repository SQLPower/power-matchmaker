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
 * Checks if the street number suffix is directly beside the street number or
 * not. If it is alphabetic it should be directly after the street number with
 * no space. If it is numeric there should be a space between the street number
 * and the street number suffix.
 */
public class StreetNumberSuffixSpacingStep implements ValidateStep {

    public boolean validate(PostalCode pc, Address a, Address suggestion,
            ValidateState state) {
        if (a.getStreetNumberSuffix() != null && a.getStreetNumberSuffix().length() > 0 && 
                a.getStreetNumberSuffix().charAt(0) > 64 && a.isStreetNumberSuffixSeparate() != null && 
                a.isStreetNumberSuffixSeparate()) {
            suggestion.setStreetNumberSuffixSeparate(false);
            state.incrementErrorCount("Street number suffix is alphabetic and should not be separate from the street number");
        } else if (a.getStreetNumberSuffix() != null && a.getStreetNumberSuffix().length() > 0 && 
                a.getStreetNumberSuffix().charAt(0) < 58 && a.isStreetNumberSuffixSeparate() != null && 
                !a.isStreetNumberSuffixSeparate()) {
            suggestion.setStreetNumberSuffixSeparate(true);
            state.incrementErrorCount("Street number suffix is numeric and should be separate from the street number");
        }
        return false;
    }

}
