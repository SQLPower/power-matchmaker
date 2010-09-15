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
import ca.sqlpower.matchmaker.address.Municipality;
import ca.sqlpower.matchmaker.address.PostalCode;

public class MunicipalityNameStep implements ValidateStep {
    
    private final Municipality municipality;

    /**
     * @param municipality
     *            This is the canonical name of the municipality which may be
     *            different from the address the user is trying to validate.
     */
    public MunicipalityNameStep(Municipality municipality) {
        this.municipality = municipality;
    }

    public boolean validate(PostalCode pc, Address a, Address suggestion,
            ValidateState state) {
        if (ValidateStepUtil.different(pc.getMunicipalityName(), a.getMunicipality())) {
            if (municipality != null) {
                // it might be a valid alternate name
                if (!municipality.isNameAcceptable(a.getMunicipality(), a.getPostalCode())) {
                    suggestion.setMunicipality(municipality.getOfficialName());
                    state.incrementErrorCount("Municipality is not a valid alternate within postal code");
                }
            } else {
                suggestion.setMunicipality(pc.getMunicipalityName());
                state.incrementErrorCount("Municipality does not agree with postal code");
            }
        }
        return false;
    }

}
