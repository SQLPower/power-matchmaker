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
 * This test confirms a lock box number of the given address falls in 
 * the range of the lock box numbers in the given postal code.
 */
public class LockBoxNumberStep implements ValidateStep {

    public boolean validate(PostalCode pc, Address a, Address suggestion,
            ValidateState state) throws DatabaseException {
        if (!pc.containsLockBoxNumber(suggestion)) {
            if (pc.getLockBoxBagFromNumber().equals(pc.getLockBoxBagToNumber())) {
                suggestion.setLockBoxNumber(new Integer(pc.getLockBoxBagFromNumber()).toString());
            }
            state.incrementErrorCount("Lock box number should does not fall in the postal code lock box range.");
        }
        return false;
    }

}
