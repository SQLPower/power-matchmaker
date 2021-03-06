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
import ca.sqlpower.matchmaker.address.PostalCode.RecordType;

/**
 * This will correct the street type if the address is a street address and
 * there is a difference between the given address and the given postal code.
 */
public class StreetAndRouteForStreetTypeStep implements ValidateStep {

    public boolean validate(PostalCode pc, Address a, Address suggestion,
            ValidateState state) {
        if (suggestion.getType() == null) {
            if (pc.getRecordType() == RecordType.STREET) {
                suggestion.setType(RecordType.STREET);
            } else if (pc.getRecordType() == RecordType.STREET_AND_ROUTE) {
                suggestion.setType(RecordType.STREET_AND_ROUTE);
                if (suggestion.isUrbanBeforeRural() == null) {
                    suggestion.setUrbanBeforeRural(true);
                }
            }
        } else if (suggestion.getType() != PostalCode.RecordType.STREET && suggestion.getType() != PostalCode.RecordType.STREET_AND_ROUTE) {
            state.incrementErrorCount("Address type does not match best suggestion.");
        } else if (suggestion.isUrbanBeforeRural() == null) {
            suggestion.setUrbanBeforeRural(true);
        }
        return false;
    }

}
