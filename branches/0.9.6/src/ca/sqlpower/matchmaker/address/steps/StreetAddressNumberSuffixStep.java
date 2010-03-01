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
 * Checks if the street address number suffix falls in the allowed range of the
 * street address number suffix to and from codes.
 */
public class StreetAddressNumberSuffixStep implements ValidateStep {

    public boolean validate(PostalCode pc, Address a, Address suggestion,
            ValidateState state) {
        if (pc.getStreetAddressFromNumber().equals(a.getStreetNumber()) && pc.getStreetAddressNumberSuffixFromCode() != null  && pc.getStreetAddressNumberSuffixFromCode().trim().length() > 0) {
            if (a.getStreetNumberSuffix() == null) {
                suggestion.setStreetNumberSuffix(pc.getStreetAddressNumberSuffixFromCode());
                state.incrementErrorAndSetValidate("Street number suffix comes before the allowed street number suffixes for this postal code.", false);
            } else {
                char pcSuffix = pc.getStreetAddressNumberSuffixFromCode().charAt(0);
                char aSuffix = a.getStreetNumberSuffix().charAt(0);
                if (a.getStreetNumberSuffix().equals("1/4")) {
                    aSuffix = '1';
                } else if (a.getStreetNumberSuffix().equals("1/2")) {
                    aSuffix = '2';
                } else if (a.getStreetNumberSuffix().equals("3/4")) {
                    aSuffix = '3';
                }
                if ((pcSuffix >= 65 && (aSuffix < pcSuffix && aSuffix >= 65))
                        || (pcSuffix == 49 && aSuffix > 51)
                        || (pcSuffix == 50 && (aSuffix > 51 || aSuffix == 49))
                        || (pcSuffix == 51 && aSuffix != 51)) {
                    suggestion.setStreetNumberSuffix(pc.getStreetAddressNumberSuffixFromCode());
                    state.incrementErrorAndSetValidate("Street number suffix comes before the allowed street number suffixes for this postal code.", false);
                }
            }
        }
        if (pc.getStreetAddressToNumber().equals(a.getStreetNumber()) && pc.getStreetAddressNumberSuffixToCode() != null && pc.getStreetAddressNumberSuffixToCode().trim().length() > 0) {
            if (a.getStreetNumberSuffix() == null) {
                suggestion.setStreetNumberSuffix(pc.getStreetAddressNumberSuffixToCode());
                state.incrementErrorAndSetValidate("Street number suffix comes after the allowed street number suffixes for this postal code.", false);
            } else {
                char pcSuffix = pc.getStreetAddressNumberSuffixToCode().charAt(0);
                char aSuffix = a.getStreetNumberSuffix().charAt(0);
                if (a.getStreetNumberSuffix().equals("1/4")) {
                    aSuffix = '1';
                } else if (a.getStreetNumberSuffix().equals("1/2")) {
                    aSuffix = '2';
                } else if (a.getStreetNumberSuffix().equals("3/4")) {
                    aSuffix = '3';
                }
                if ((pcSuffix >= 65 && ((aSuffix > pcSuffix && aSuffix >= 65) || aSuffix == 49 || aSuffix == 50 || aSuffix == 51))
                        || (pcSuffix == 49 && (aSuffix == 50 || aSuffix == 51))
                        || (pcSuffix == 50 && aSuffix == 51)) {
                    suggestion.setStreetNumberSuffix(pc.getStreetAddressNumberSuffixToCode());
                    state.incrementErrorAndSetValidate("Street number suffix comes after the allowed street number suffixes for this postal code.", false);
                }
            }
        }
        return false;
    }

}
