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

import com.sleepycat.je.DatabaseException;

import ca.sqlpower.matchmaker.address.Address;
import ca.sqlpower.matchmaker.address.PostalCode;

public interface ValidateStep {

    /**
     * Call this method to validate an address based on a postal code. If there
     * is a problem in the address then the suggestion may be updated to contain
     * a more correct address.
     * 
     * @param pc
     *            This postal code is a valid postal code from the Canada Post
     *            database. It will be used to find differences between a valid
     *            address and the given address.
     * @param a
     *            The address given to be validated.
     * @param suggestion
     *            This suggestion is either a copy of the address or a copy of
     *            the address modified by other steps. This suggestion may be
     *            modified to create a more valid address.
     * @param state
     *            The state of the validation process. This will include things
     *            like the error count and error messages.
     * @return True if the validation step should stop early because a
     *         suggestion or the address itself was found to be valid without
     *         needing to run additional steps.
     */
    boolean validate(PostalCode pc, Address a, Address suggestion, ValidateState state) throws DatabaseException;

}
