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

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.address.Address;
import ca.sqlpower.matchmaker.address.PostalCode;

/**
 * Corrects a street number.
 */
public class StreetNumberStep implements ValidateStep {
    
    private static final Logger logger = Logger.getLogger(StreetNameStep.class);

    public boolean validate(PostalCode pc, Address a, Address suggestion,
            ValidateState state) {
        if (a.getStreetNumber() == null) {
            state.incrementErrorAndSetValidate("Street number missing from urban address.", false);
            logger.debug("Urban address missing street number is " + a.getAddress());
        } else if (pc.getStreetAddressFromNumber() != null && pc.getStreetAddressToNumber() != null &&
                (pc.getStreetAddressFromNumber() > a.getStreetNumber() || pc.getStreetAddressToNumber() < a.getStreetNumber())) {
            state.incrementErrorAndSetValidate("Street number does not fall into the range of allowed street numbers for this postal code.", false);
        }
        return false;
    }

}
