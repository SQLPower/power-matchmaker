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

package ca.sqlpower.matchmaker.address;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.address.Address.Type;
import ca.sqlpower.matchmaker.address.PostalCode.RecordType;
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityJoin;
import com.sleepycat.persist.ForwardCursor;

public class AddressValidator {
	
	private static final Logger logger = Logger.getLogger(AddressValidator.class);

    private final AddressDatabase db;
    private final Address address;
    
    /**
     * The validation results for address. This member will be null until
     * {@link #validateImpl()} is called, which will happen automatically
     * upon the first call to {@link #getResults()}.
     */
    private List<ValidateResult> results;

    /**
     * The list of suggested correct addresses that are similar to
     * {@link #address}. This member will be null until {@link #validateImpl()}
     * is called, which will happen automatically upon the first call to
     * {@link #getSuggestions()}.
     */
    private List<Address> suggestions;
    
    /**
     * If true the first suggestion in the suggestions list is a valid postal code
     * that is accurate for the given address. If false then either there is too many
     * conflicts between the data given in the address or the address is missing too
     * much information to accurately give a valid suggestion.
     */
    private boolean validSuggestion = true;

    /**
     * This value is used in generating suggestions. If true then the suggestion had to
     * modify the address in a way that was not an error but created a valid alternative.
     * This occurs in places where the parser has difficulty like additional information
     * coming after the delivery installation name (ie: RR 4 STN A 21 YONGE puts A 21 YONGE
     * as the delivery installation name).
     */
	private boolean reparsed;

    
    /**
     * 
     * @param db The database to use for address resolution
     * @param address The address to validate
     */
    public AddressValidator(AddressDatabase db, Address address) {
        if (db == null) throw new NullPointerException("Null address database");
        if (address == null) throw new NullPointerException("Null address");
        this.db = db;
        this.address = address;
    }
    
    private void validateImpl() throws DatabaseException {
        Address a = new Address(address);
        
        results = new ArrayList<ValidateResult>();
        suggestions = new ArrayList<Address>();
        
        // translate province/state names to official code (TODO)
        a.normalize();
        
        // translate municipality name to canonical name
        Municipality municipality = null;
        Set<Municipality> municipalities = db.findMunicipality(a.getMunicipality(), a.getProvince());
        if (municipalities.size() == 0) {
            results.add(ValidateResult.createValidateResult(
                    Status.FAIL, "Municipality \"" + a.getMunicipality() + "\" does not exist"));
        } else if (municipalities.size() > 1) {
            results.add(ValidateResult.createValidateResult(
                    Status.FAIL, "Municipality \"" + a.getMunicipality() + "\" is ambiguous (" + municipalities.size() + " matches)"));
        } else {
            municipality = municipalities.iterator().next();
        }
        
        // validate street
        
        if (a.getPostalCode() != null) {
            Set<PostalCode> pcSet = db.findPostalCode(a.getPostalCode());
            
            // verify province, municipality, street, type, direction, and street number
            if (pcSet.isEmpty()) {
                results.add(ValidateResult.createValidateResult(
                        Status.FAIL, "Invalid postal code: " + a.getPostalCode()));
                
                // This will trigger a postal code lookup in the next section
                a.setPostalCode(null);
                
            } else {
            	generateSuggestions(a, municipality, pcSet);
            }
            
        }
        
        if (a.getPostalCode() == null) {
            results.add(ValidateResult.createValidateResult(Status.FAIL, "Postal Code is missing"));
            
            // try to find unique postal code match TODO extract to public method
            EntityJoin<Long, PostalCode> join = new EntityJoin<Long, PostalCode>(db.postalCodePK);
            
            boolean allNulls = true;
            
            if (a.getProvince() != null) {
                join.addCondition(db.postalCodeProvince, a.getProvince());
                allNulls = false;
            }
            if (a.getMunicipality() != null) {
                join.addCondition(db.postalCodeMunicipality, a.getMunicipality());
                allNulls = false;
            }
            
            
            // TODO check how many fields match these criteria
            // (for example, if more than 1000 records match, just emit an error)
            
            List<RecordType> validRecordTypes = new ArrayList<RecordType>();
            if (a.getType() == Type.URBAN) {
            	if (a.getStreet() != null) {
            		join.addCondition(db.postalCodeStreet, a.getStreet());
            		allNulls = false;
            	}
            	validRecordTypes.add(RecordType.STREET);
            } else if (a.getType() == Type.RURAL) {
            	validRecordTypes.add(RecordType.ROUTE);
            } else if (a.getType() == Type.MIXED) {
            	validRecordTypes.add(RecordType.STREET_AND_ROUTE);
            } else if (a.getType() == Type.LOCK_BOX) {
            	validRecordTypes.add(RecordType.LOCK_BOX);
            } else if (a.getType() == Type.GD) {
            	validRecordTypes.add(RecordType.GENERAL_DELIVERY);
            }
            
            if (!allNulls) {
                ForwardCursor<PostalCode> matches = null;
                try {
                    matches = join.entities();
                    logger.debug("Checking address " + a);
                    Set<PostalCode> pcSet = new HashSet<PostalCode>();
                    for (PostalCode pc : matches) {
                        if (validRecordTypes.contains(pc.getRecordType()) && pc.containsAddress(a)) {
                            pcSet.add(pc);
                        }
                    }
                    generateSuggestions(a, municipality, pcSet);
                } finally {
                    if (matches != null) matches.close();
                }
            } else {
                results.add(ValidateResult.createValidateResult(Status.FAIL, "Address is too incomplete for lookup"));
            }
            
            if (a.getPostalCode() == null) {
                results.add(ValidateResult.createValidateResult(
                        Status.FAIL, "No matching postal code found for address"));
            }
        }
        
    }

	/**
	 * This method will generate the suggestions for a given address.
	 * 
	 * @param a
	 *            An address to correct.
	 * @param municipality
	 *            A municipality taken from the address to correct. Allows
	 *            checking for older or alternate municipality names.
	 * @param pcSet
	 *            A set of postal codes to compare the address to and try to
	 *            generate suggestions from.
	 */
	private void generateSuggestions(Address a, Municipality municipality,
			Set<PostalCode> pcSet) {
		Map<Integer, Set<Address>> addressSuggestionsByError = new HashMap<Integer, Set<Address>>();
		List<ValidateResult> smallestErrorList = new ArrayList<ValidateResult>();
		boolean smallestErrorIsValid = true;
		int smallestErrorCount = Integer.MAX_VALUE;
		for (PostalCode pc : pcSet) {
			List<ValidateResult> errorList = new ArrayList<ValidateResult>();
			int errorCount = 0;
			boolean isValid = true;
			reparsed = false;
			Address suggestion = new Address(a);
			if (different(pc.getPostalCode(), a.getPostalCode())) {
				if (a.getPostalCode() != null) {
					isValid = false;
				}
				errorList.add(ValidateResult.createValidateResult(
						Status.FAIL, "Postal codes do not agree"));
				suggestion.setPostalCode(pc.getPostalCode());
				errorCount++;
			}
			if (!pc.getProvinceCode().equals(a.getProvince())) {
				if (a.getProvince() != null) {
					isValid = false;
				}
				errorList.add(ValidateResult.createValidateResult(
						Status.FAIL, "Province code does not agree with postal code"));
				suggestion.setProvince(pc.getProvinceCode());
				errorCount++;
			}
			if (different(pc.getMunicipalityName(), a.getMunicipality())) {
				if (municipality != null) {
					// it might be a valid alternate name
					if (!municipality.isNameAcceptable(a.getMunicipality(), a.getPostalCode())) {
						errorList.add(ValidateResult.createValidateResult(
								Status.FAIL, "Municipality is not a valid alternate within postal code"));
						suggestion.setMunicipality(municipality.getOfficialName());
						errorCount++;
					}
				} else {
					errorList.add(ValidateResult.createValidateResult(
							Status.FAIL, "Municipality does not agree with postal code"));
					suggestion.setMunicipality(pc.getMunicipalityName());
					errorCount++;
				}
			}
			if (pc.getRecordType() == RecordType.STREET || pc.getRecordType() == RecordType.STREET_AND_ROUTE) {
				if (suggestion.getType() == null) {
					if (pc.getRecordType() == RecordType.STREET) {
						suggestion.setType(Type.URBAN);
					} else if (pc.getRecordType() == RecordType.STREET_AND_ROUTE) {
						suggestion.setType(Type.MIXED);
						if (suggestion.isUrbanBeforeRural() == null) {
							suggestion.setUrbanBeforeRural(true);
						}
					}
				}
				if (different(pc.getStreetName(), a.getStreet())) {
					errorList.add(ValidateResult.createValidateResult(
							Status.FAIL, "Street name does not agree with postal code"));
					suggestion.setStreet(pc.getStreetName());
					errorCount++;
				}
				if (different(pc.getStreetTypeCode(), a.getStreetType())) {
					errorList.add(ValidateResult.createValidateResult(
							Status.FAIL, "Street type does not agree with postal code"));
					suggestion.setStreetType(pc.getStreetTypeCode());
					errorCount++;
				}
				if (a.isStreetTypePrefix() != isStreetTypePrefix(suggestion, pc)) {
					errorList.add(ValidateResult.createValidateResult(
							Status.FAIL, "Street type prefix does not agree with postal code"));
					suggestion.setStreetTypePrefix(isStreetTypePrefix(suggestion, pc));
					errorCount++;
				}
				if (different(pc.getStreetDirectionCode(), a.getStreetDirection())) {
					errorList.add(ValidateResult.createValidateResult(
							Status.FAIL, "Street direction does not agree with postal code"));
					suggestion.setStreetDirection(pc.getStreetDirectionCode());
					errorCount++;
				}
				if (a.getStreetNumber() == null) {
					isValid = false;
					errorList.add(ValidateResult.createValidateResult(
							Status.FAIL, "Street number missing from urban address."));
					errorCount++;
					logger.debug("Urban address missing street number is " + a.getAddress());
				} else if (pc.getStreetAddressFromNumber() != null && pc.getStreetAddressToNumber() != null &&
						(pc.getStreetAddressFromNumber() > a.getStreetNumber() || pc.getStreetAddressToNumber() < a.getStreetNumber())) {
					isValid = false;
					errorList.add(ValidateResult.createValidateResult(
							Status.FAIL, "Street number does not fall into the range of allowed street numbers for this postal code."));
					errorCount++;
				}
//            			Partial implementation of how street suffixs are supposed to be appended but is not consistent with the test data.
//            			if (pc.getStreetAddressFromNumber() == a.getStreetNumber() && pc.getStreetAddressNumberSuffixFromCode() != null) {
//            				if (a.getStreetNumberSuffix() == null) {
//            					errorList.add(ValidateResult.createValidateResult(
//                						Status.FAIL, "Street number suffix comes before the allowed street number suffixes for this postal code."));
//                				suggestion.setStreetNumberSuffix(pc.getStreetAddressNumberSuffixFromCode());
//                				errorCount++;
//            				} else {
//            					char pcSuffix = pc.getStreetAddressNumberSuffixFromCode().charAt(0);
//            					char aSuffix = a.getStreetNumberSuffix().charAt(0);
//            					if (!(pcSuffix >= 65 && (aSuffix >= pcSuffix || aSuffix == 49 || aSuffix == 50 || aSuffix == 51))) {
//            						errorList.add(ValidateResult.createValidateResult(
//                    						Status.FAIL, "Street number suffix comes before the allowed street number suffixes for this postal code."));
//                    				suggestion.setStreetNumberSuffix(pc.getStreetAddressNumberSuffixFromCode());
//                    				errorCount++;
//            					}
//            				}
//            			}
				if (a.getStreetNumber() != null) {
					if (pc.getStreetAddressSequenceType() == AddressSequenceType.ODD && a.getStreetNumber() % 2 == 0) {
						isValid = false;
						errorList.add(ValidateResult.createValidateResult(
								Status.FAIL, "Street number is even when it should be odd."));
						errorCount++;
					} else if (pc.getStreetAddressSequenceType() == AddressSequenceType.EVEN && a.getStreetNumber() % 2 == 1) {
						isValid = false;
						errorList.add(ValidateResult.createValidateResult(
								Status.FAIL, "Street number is odd when it should be even."));
						errorCount++;
					}
				}
			
				if (!a.isSuitePrefix() && a.getSuite() != null && !Address.isSuiteTypeExactMatch(a.getSuiteType())) {
					errorList.add(ValidateResult.createValidateResult(
							Status.FAIL, "Suite number should be a prefix if the suite type is invalid."));
					suggestion.setSuitePrefix(true);
					errorCount++;
				}
				if (a.getSuite() != null && a.getSuite().length() > 0 && a.getSuite().charAt(0) == '#') {
					errorList.add(ValidateResult.createValidateResult(
							Status.FAIL, "Suite numbers should not have # prepended."));
					suggestion.setSuite(a.getSuite().substring(1));
					errorCount++;
				}

				// TODO all the other fields
			}
			if (pc.getRecordType() == RecordType.GENERAL_DELIVERY) {
				suggestion.setType(Type.GD);
				if (!Address.isGeneralDeliveryExactMatch(a.getGeneralDeliveryName())) {
					if (Address.isGeneralDelivery(a.getGeneralDeliveryName()) && !a.getProvince().equals(AddressDatabase.QUEBEC_PROVINCE_CODE)
							&& different(a.getGeneralDeliveryName(), Address.GENERAL_DELIVERY_ENGLISH)) {
						errorList.add(ValidateResult.createValidateResult(
								Status.FAIL, "English general delivery name is incorrectly spelled and/or abbreviated."));
						suggestion.setGeneralDeliveryName(Address.GENERAL_DELIVERY_ENGLISH);
						errorCount++;
					} else if (Address.isGeneralDelivery(a.getGeneralDeliveryName()) && a.getProvince().equals(AddressDatabase.QUEBEC_PROVINCE_CODE)
							&& different(a.getGeneralDeliveryName(), Address.GENERAL_DELIVERY_FRENCH)) {
						errorList.add(ValidateResult.createValidateResult(
								Status.FAIL, "French general delivery name is incorrectly spelled and/or abbreviated."));
						suggestion.setGeneralDeliveryName(Address.GENERAL_DELIVERY_FRENCH);
						errorCount++;
					}
				}
				
				errorCount += correctDeliveryInstallation(a, pc, suggestion, errorList);
			}
			if (pc.getRecordType() == RecordType.LOCK_BOX) {
				suggestion.setType(Type.LOCK_BOX);
				if (!a.getProvince().equals(AddressDatabase.QUEBEC_PROVINCE_CODE) && different(a.getLockBoxType(), Address.LOCK_BOX_ENGLISH)) {
					errorList.add(ValidateResult.createValidateResult(
							Status.FAIL, "English lock box name is incorrectly spelled and/or abbreviated."));
					suggestion.setLockBoxType(Address.LOCK_BOX_ENGLISH);
					errorCount++;
				} else if (a.getProvince().equals(AddressDatabase.QUEBEC_PROVINCE_CODE) && different(a.getLockBoxType(), Address.LOCK_BOX_FRENCH)) {
					errorList.add(ValidateResult.createValidateResult(
							Status.FAIL, "French lock box name is incorrectly spelled and/or abbreviated."));
					suggestion.setLockBoxType(Address.LOCK_BOX_FRENCH);
					errorCount++;
				}
				if (a.getLockBoxNumber() != null && a.getLockBoxNumber().length() > 0 && a.getLockBoxNumber().charAt(0) == '#') {
					errorList.add(ValidateResult.createValidateResult(
							Status.FAIL, "Lock box number should not start with a #."));
					suggestion.setLockBoxNumber(a.getLockBoxNumber().substring(1));
					errorCount++;
				}
				
				errorCount += correctDeliveryInstallation(a, pc, suggestion, errorList);
			}
			if (pc.getRecordType() == RecordType.ROUTE || pc.getRecordType() == RecordType.STREET_AND_ROUTE) {
				if (suggestion.getType() == null) {
					if (pc.getRecordType() == RecordType.ROUTE) {
						suggestion.setType(Type.RURAL);
					} else if (pc.getRecordType() == RecordType.STREET_AND_ROUTE) {
						suggestion.setType(Type.MIXED);
						if (suggestion.isUrbanBeforeRural() == null) {
							suggestion.setUrbanBeforeRural(false);
						}
					}
				}
				if (!Address.RURAL_ROUTE_TYPES.contains(a.getRuralRouteType())) {
					errorList.add(ValidateResult.createValidateResult(
							Status.FAIL, "Invalid rural route type."));
					suggestion.setRuralRouteType(Address.getRuralRouteShortForm(a.getRuralRouteType()));
					errorCount++;
				}
				
				errorCount += correctDeliveryInstallation(a, pc, suggestion, errorList);
				
				if (a.getRuralRouteNumber() == null && pc.getRouteServiceNumber() != null && pc.getRouteServiceNumber().trim().length() > 0) {
					errorList.add(ValidateResult.createValidateResult(
							Status.FAIL, "Missing rural route number."));
					suggestion.setRuralRouteNumber(new Integer(pc.getRouteServiceNumber()).toString());
					errorCount++;
				}
				if (a.getRuralRouteNumber() != null && a.getRuralRouteNumber().length() > 0 && a.getRuralRouteNumber().charAt(0) == '#') {
					errorList.add(ValidateResult.createValidateResult(
							Status.FAIL, "Rural route number should not start with a #."));
					suggestion.setRuralRouteNumber(a.getRuralRouteNumber().substring(1));
					errorCount++;
				}
				if (different(suggestion.getRuralRouteNumber(), pc.getRouteServiceNumber()) && pc.getRouteServiceNumber() != null && pc.getRouteServiceNumber().trim().length() > 0) {
					errorList.add(ValidateResult.createValidateResult(
							Status.FAIL, "Incorrect rural route number."));
					suggestion.setRuralRouteNumber(new Integer(pc.getRouteServiceNumber()).toString());
					errorCount++;
				}
			}
			

			if (errorCount > 0 || reparsed) {
				Set<Address> addresses = addressSuggestionsByError.get(errorCount);
				if (addresses == null) {
					addresses = new HashSet<Address>();
					addresses.add(suggestion);
					addressSuggestionsByError.put(errorCount, addresses);
				} else {
					addresses.add(suggestion);
				}
			} else {
				suggestions.clear();
				return;
			}
			if (errorCount < smallestErrorCount) {
				smallestErrorList = errorList;
				smallestErrorCount = errorCount;
				smallestErrorIsValid = isValid;
			}
		}
		results.addAll(smallestErrorList);
		final ArrayList<Integer> errorCounts = new ArrayList<Integer>(addressSuggestionsByError.keySet());
		Collections.sort(errorCounts);
		//If there is only one suggestion at the lowest error count then that is a valid suggestion, otherwise invalid
		if (!smallestErrorIsValid || (addressSuggestionsByError.get(smallestErrorCount) != null && addressSuggestionsByError.get(smallestErrorCount).size() > 1)) {
			validSuggestion = false;
		}
		for (Integer errorCount : errorCounts) {
			for (Address suggestion : addressSuggestionsByError.get(errorCount)) {
				suggestions.add(suggestion);
			}
		}
	}
	
	/**
	 * Given an address to correct, a postal code to correct to and a partially validated suggestion,
	 * this method will check the delivery installation information and update the suggestion accordingly.
	 * If there are corrections to be made errors will be added to the given error list and the number
	 * of errors will be returned.
	 */
	private int correctDeliveryInstallation(Address a, PostalCode pc, Address suggestion, List<ValidateResult> errorList) {
		int errorCount = 0;
		if (a.getDeliveryInstallationName() == null && a.getDeliveryInstallationType() == null) {
			return 0;
		}
		if (different(a.getDeliveryInstallationType(), pc.getDeliveryInstallationTypeDescription())) {
			if (a.getDeliveryInstallationType() != null && pc.getDeliveryInstallationTypeDescription() != null &&
					((a.getDeliveryInstallationType().equals("STN") && pc.getDeliveryInstallationTypeDescription().equals("SUCC"))
							|| (a.getDeliveryInstallationType().equals("SUCC") && pc.getDeliveryInstallationTypeDescription().equals("STN")))) {
				//no problem
			} else {
				errorList.add(ValidateResult.createValidateResult(
						Status.FAIL, "Invalid delivery installation type."));
				suggestion.setDeliveryInstallationType(pc.getDeliveryInstallationTypeDescription());
				errorCount++;
			}
		}
		
		if (different(pc.getDeliveryInstallationQualifierName(), a.getDeliveryInstallationName())) {
			if (a.getDeliveryInstallationName() != null) {
				String diName = a.getDeliveryInstallationName().trim();
				while (diName.length() > 0) {
					if (!different(pc.getDeliveryInstallationQualifierName(), diName)) {
						suggestion.setDeliveryInstallationName(diName);
						suggestion.setAdditionalInformationSuffix(a.getDeliveryInstallationName().substring(diName.length()).trim());
						reparsed = true;
					}
					if (diName.lastIndexOf(' ') < 0) {
						break;
					}
					diName = diName.substring(0, diName.lastIndexOf(' ')).trim();
				}
			}
			if (!reparsed) {
				errorList.add(ValidateResult.createValidateResult(
						Status.FAIL, "Invalid delivery installation name."));
				suggestion.setDeliveryInstallationName(pc.getDeliveryInstallationQualifierName());
				errorCount++;
			}
		}
		return errorCount;
	}

	/**
     * Given an address that has a corrected street name and the postal code retrieved by
     * the addresses's postal code this method will decide if the street type should
     * be appended before or after the street name.
     * @param suggestion The suggested street address.
     * @param pc The postal code retrieved by the address being corrected.
     * @return True if the address street type should be placed before the street name, false otherwise.
     */
    boolean isStreetTypePrefix(Address suggestion, PostalCode pc) {
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
     * Runs the validation process on {@link #address}. You don't have to call
     * this method explicitly--it will be called when you try to access the
     * validation results or suggestions. This method is public so you can call
     * it explicitly if you want the Address Database access to happen at a
     * predictable time.
     */
    public void validate() {
        try {
            validateImpl();
        } catch (DatabaseException e) {
            throw new RuntimeException(e);
        }
    }
    
    public List<ValidateResult> getResults() {
        if (results == null) {
            validate();
        }
        return Collections.unmodifiableList(results);
    }
    
    public List<Address> getSuggestions() {
        if (suggestions == null) {
            validate();
        }
        return Collections.unmodifiableList(suggestions);
    }
    
    public boolean isValidSuggestion() {
		return validSuggestion;
	}
}
