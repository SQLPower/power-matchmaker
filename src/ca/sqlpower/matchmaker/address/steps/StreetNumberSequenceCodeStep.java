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
import ca.sqlpower.matchmaker.address.AddressSequenceType;
import ca.sqlpower.matchmaker.address.PostalCode;

/**
 * Checks if the street number is allowed in the postal code depending if the
 * postal code covers even, odd, or both numbers.
 */
public class StreetNumberSequenceCodeStep implements ValidateStep {

    public boolean validate(PostalCode pc, Address a, Address suggestion,
            ValidateState state) {
        if (a.getStreetNumber() != null) {
            if (pc.getStreetAddressSequenceType() == AddressSequenceType.ODD && a.getStreetNumber() % 2 == 0) {
                state.incrementErrorAndSetValidate("Street number is even when it should be odd.", false);
            } else if (pc.getStreetAddressSequenceType() == AddressSequenceType.EVEN && a.getStreetNumber() % 2 == 1) {
                state.incrementErrorAndSetValidate("Street number is odd when it should be even.", false);
            }
        }
        return false;
    }

}
