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
import ca.sqlpower.matchmaker.address.AddressDatabase;
import ca.sqlpower.matchmaker.address.PostalCode;
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;

/**
 * Special case on D-33 of the SERP handbook. Any address that has a postal code
 * in the CPC database with a 0 in the second position is valid. We still need
 * to correct the province and municipality however.
 */
public class SERPRuralZeroPostalCodeStep implements ValidateStep {
    
    private static final Logger logger = Logger.getLogger(SERPRuralZeroPostalCodeStep.class);

    public boolean validate(PostalCode pc, Address a, Address suggestion,
            ValidateState state) {

        if (a.getPostalCode() != null && a.getPostalCode().length() == 6 && a.getPostalCode().charAt(1) == '0') {
            
            //Optional lock box name fix on rural routes
            if ("BOX".equals(a.getLockBoxType())) {
                if (!a.getProvince().equals(AddressDatabase.QUEBEC_PROVINCE_CODE) && ValidateStepUtil.different(a.getLockBoxType(), Address.LOCK_BOX_ENGLISH)) {
                    state.addError(ValidateResult.createValidateResult(
                            Status.FAIL, "English lock box name is incorrectly spelled and/or abbreviated."));
                    suggestion.setLockBoxType(Address.LOCK_BOX_ENGLISH);
                } else if (a.getProvince().equals(AddressDatabase.QUEBEC_PROVINCE_CODE) && ValidateStepUtil.different(a.getLockBoxType(), Address.LOCK_BOX_FRENCH)) {
                    state.addError(ValidateResult.createValidateResult(
                            Status.FAIL, "French lock box name is incorrectly spelled and/or abbreviated."));
                    suggestion.setLockBoxType(Address.LOCK_BOX_FRENCH);
                }
            }
            
            state.setValid(true);
            logger.debug("SERP valid because postal code has a 0 in the second position.");
            return true;
        }
        return false;

    }

}
