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
 * This step will check if a route number falls outside the allowed range for
 * the given postal code. This step will not modify the suggestion.
 */
public class RouteNumberStep implements ValidateStep {

    public boolean validate(PostalCode pc, Address a, Address suggestion,
            ValidateState state) throws DatabaseException {
        if (a.getRuralRouteNumber() == null && pc.getRouteServiceNumber() != null && pc.getRouteServiceNumber().trim().length() > 0) {
            suggestion.setRuralRouteNumber(new Integer(pc.getRouteServiceNumber()).toString());
            state.incrementErrorCount("Missing rural route number.");
        }
        if (pc.getRouteServiceNumber() != null && pc.getRouteServiceNumber().trim().length() > 0 && ValidateStepUtil.different(suggestion.getRuralRouteNumber(), new Integer(pc.getRouteServiceNumber()).toString())) {
            suggestion.setRuralRouteNumber(new Integer(pc.getRouteServiceNumber()).toString());
            state.incrementErrorCount("Incorrect rural route number.");
        }
        return false;
    }

}
