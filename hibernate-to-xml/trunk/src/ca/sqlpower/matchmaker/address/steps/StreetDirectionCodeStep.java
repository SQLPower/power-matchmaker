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
 * This step will correct the street direction if the given direction is
 * different from the postal code we are comparing against.
 */
public class StreetDirectionCodeStep implements ValidateStep {

    public boolean validate(PostalCode pc, Address a, Address suggestion,
            ValidateState state) {
        if (ValidateStepUtil.different(pc.getStreetDirectionCode(), a.getStreetDirection()) && !Address.isStreetDirectionsEquivalent(pc.getStreetDirectionCode(), a.getStreetDirection())) {
            suggestion.setStreetDirection(pc.getStreetDirectionCode());
            state.incrementErrorCount("Street direction does not agree with postal code");
        }
        return false;
    }

}
