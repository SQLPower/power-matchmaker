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
import ca.sqlpower.matchmaker.address.AddressDatabase;
import ca.sqlpower.matchmaker.address.PostalCode;

/**
 * A collection of static methods used in validating an address.
 */
public class ValidateStepUtil {
    
    private ValidateStepUtil() {
        //Don't make an instance of this class
    }

    /**
     * Given an address that has a corrected street name and the postal code retrieved by
     * the addresses's postal code this method will decide if the street type should
     * be appended before or after the street name.
     * @param suggestion The suggested street address.
     * @param pc The postal code retrieved by the address being corrected.
     * @return True if the address street type should be placed before the street name, false otherwise.
     */
    public static boolean isStreetTypePrefix(AddressDatabase db, Address suggestion, PostalCode pc) {
    	if (!db.isStreetTypeFrench(suggestion.getStreetType())) {
    		return false;
    	}
    	if (suggestion.getStreetType() == null) {
    		return false;
    	}
    	String typeShortForm = db.getShortFormStreetType(suggestion.getStreetType());
    	if (typeShortForm == null) {
    		typeShortForm = suggestion.getStreetType();
    	}
    	//French people like to put the street type after the street name if it is numeric and is followed
    	//by 'e', 're' or if the street name is an ordinal number.
    	String street = suggestion.getStreet().split(" ")[0];
    	if (street != null) {
    		try {
    			Integer.parseInt(street);
    			return false;
    		} catch (NumberFormatException e) {
    			//street type goes in front still.
    		}
    		try {
    			if (street.length() > 1) {
    				Integer.parseInt(street.substring(0, street.length() - 1));
    				if (street.substring(street.length() - 1).equals("E")) {
    					return false;
    				}
    			}
    		} catch (NumberFormatException e) {
    			//street type goes in front still.
    		}
    		try {
    			if (street.length() > 2) {
    				Integer.parseInt(street.substring(0, street.length() - 2));
    				if (street.substring(street.length() - 2).equals("RE")) {
    					return false;
    				}
    			}
    		} catch (NumberFormatException e) {
    			//street type goes in front still.
    		}
    		try {
    			if (street.length() > 3) {
    				Integer.parseInt(street.substring(0, street.length() - 3));
    				if (street.substring(street.length() - 3).equals("IER")) {
    					return false;
    				}
    			}
    		} catch (NumberFormatException e) {
    			//street type goes in front still.
    		}
    	}
    	return true;
    }

    /**
     * Compares two strings case insensitively, also considering null to be equivalent
     * to the empty string. Leading and trailing whitespace is ignored.
     * 
     * @param s1 One string to compare
     * @param s2 The other string to compare
     * @return True iff strings s1 and s2 differ according to the rules outlined
     * above.
     */
    public static boolean different(String s1, String s2) {
        if (s1 == null) s1 = "";
        if (s2 == null) s2 = "";
        return !s1.trim().equalsIgnoreCase(s2.trim());
    }

    /**
     * Given an address to correct, a postal code to correct to and a partially validated suggestion,
     * this method will check the delivery installation information and update the suggestion accordingly.
     * If there are corrections to be made errors will be added to the given error list and the number
     * of errors will be returned.
     */
    public static void correctDeliveryInstallation(Address a, PostalCode pc, Address suggestion, ValidateState state) {
        if (pc.getDeliveryInstallationQualifierName() == null || pc.getDeliveryInstallationQualifierName().trim().length() == 0) return;
        
    	if (different(a.getDeliveryInstallationType(), pc.getDeliveryInstallationTypeDescription())) {
    		if (a.getDeliveryInstallationType() != null && pc.getDeliveryInstallationTypeDescription() != null &&
    				((a.getDeliveryInstallationType().equals("STN") && pc.getDeliveryInstallationTypeDescription().equals("SUCC"))
    						|| (a.getDeliveryInstallationType().equals("SUCC") && pc.getDeliveryInstallationTypeDescription().equals("STN")))) {
    			//no problem
    		} else {
    		    suggestion.setDeliveryInstallationType(pc.getDeliveryInstallationTypeDescription());
    		    state.incrementErrorCount("Invalid delivery installation type.");
    		}
    	}
    	
    	if (different(pc.getDeliveryInstallationQualifierName(), a.getDeliveryInstallationName())) {
    		if (a.getDeliveryInstallationName() != null) {
    			String diName = a.getDeliveryInstallationName().trim();
    			while (diName.length() > 0) {
    				if (!different(pc.getDeliveryInstallationQualifierName(), diName)) {
    					suggestion.setDeliveryInstallationName(diName);
    					suggestion.setAdditionalInformationSuffix(a.getDeliveryInstallationName().substring(diName.length()).trim());
    					state.setReparsed(true);
    				}
    				if (diName.lastIndexOf(' ') < 0) {
    					break;
    				}
    				diName = diName.substring(0, diName.lastIndexOf(' ')).trim();
    			}
    		}
    		if (!state.isReparsed()) {
    		    suggestion.setDeliveryInstallationName(pc.getDeliveryInstallationQualifierName());
    		    state.incrementErrorCount("Invalid delivery installation name.");
    		}
    	}
    }

}
