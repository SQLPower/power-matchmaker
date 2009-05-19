/*
 * Copyright (c) 2009, SQL Power Group Inc.
 *
 * This file is part of DQguru
 *
 * DQguru is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DQguru is distributed in the hope that it will be useful,
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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.address.LargeVolumeReceiver.LVRRecordType;
import ca.sqlpower.matchmaker.address.PostalCode.RecordType;
import ca.sqlpower.matchmaker.address.steps.GeneralDeliveryNameAndTypeStep;
import ca.sqlpower.matchmaker.address.steps.GeneralDeliveryNameStep;
import ca.sqlpower.matchmaker.address.steps.LockBoxNameAndTypeStep;
import ca.sqlpower.matchmaker.address.steps.LockBoxNumberContainsNumSignStep;
import ca.sqlpower.matchmaker.address.steps.LockBoxNumberStep;
import ca.sqlpower.matchmaker.address.steps.LockBoxTypeStep;
import ca.sqlpower.matchmaker.address.steps.MunicipalityNameStep;
import ca.sqlpower.matchmaker.address.steps.PostalCodeStep;
import ca.sqlpower.matchmaker.address.steps.ProvinceNameStep;
import ca.sqlpower.matchmaker.address.steps.RouteDINameAndTypeStep;
import ca.sqlpower.matchmaker.address.steps.RouteNumberContainsNumSignStep;
import ca.sqlpower.matchmaker.address.steps.RouteNumberStep;
import ca.sqlpower.matchmaker.address.steps.RouteTypeStep;
import ca.sqlpower.matchmaker.address.steps.SERPRuralZeroPostalCodeStep;
import ca.sqlpower.matchmaker.address.steps.StreetAddressNumberSuffixStep;
import ca.sqlpower.matchmaker.address.steps.StreetAndRouteForRouteTypeStep;
import ca.sqlpower.matchmaker.address.steps.StreetAndRouteForStreetTypeStep;
import ca.sqlpower.matchmaker.address.steps.StreetDirectionCodeStep;
import ca.sqlpower.matchmaker.address.steps.StreetNameStep;
import ca.sqlpower.matchmaker.address.steps.StreetNumberSequenceCodeStep;
import ca.sqlpower.matchmaker.address.steps.StreetNumberStep;
import ca.sqlpower.matchmaker.address.steps.StreetNumberSuffixSpacingStep;
import ca.sqlpower.matchmaker.address.steps.StreetTypeCodeStep;
import ca.sqlpower.matchmaker.address.steps.StreetTypePrefixStep;
import ca.sqlpower.matchmaker.address.steps.SuiteNumberMissingStep;
import ca.sqlpower.matchmaker.address.steps.SuiteNumberPrefixStep;
import ca.sqlpower.matchmaker.address.steps.SuiteNumberSignStep;
import ca.sqlpower.matchmaker.address.steps.ValidateState;
import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityJoin;
import com.sleepycat.persist.ForwardCursor;

public class AddressValidator {
	
	private static final Logger logger = Logger.getLogger(AddressValidator.class);
	
	/**
	 * This is a hard limit on the number of postal code entries found to correct an address to.
	 * This is an optimization for cases where too much information is missing from the address
	 * to make a reasonable suggestion. There is not likely to be more than 50 entries for a single
	 * postal code in the database. However, this limit is high enough that even if multiple postal
	 * codes are required to be processed to validate an address this value should be well over the
	 * number of postal code entries processed to guarantee the postal code is valid before stopping.
	 */
	private static final int MAX_POSTAL_CODES_PROCESSED = 1000;

    private final AddressDatabase db;
    private final Address address;
    
    /**
     * The validation results for address. This member will be null until
     * {@link #validateImpl()} is called, which will happen automatically
     * upon the first call to {@link #getResults()}.
     */
    private final List<ValidateResult> results;

    /**
     * The list of suggested correct addresses that are similar to
     * {@link #address}. This member will be null until {@link #validateImpl()}
     * is called, which will happen automatically upon the first call to
     * {@link #getSuggestions()}.
     */
    private final List<Address> suggestions;
    
    /**
     * Tracks if the address has been validated yet.
     */
    private boolean validated = false;
    
    /**
     * If true the first suggestion in the suggestions list is a valid postal code
     * that is accurate for the given address. If false then either there is too many
     * conflicts between the data given in the address or the address is missing too
     * much information to accurately give a valid suggestion.
     */
    private boolean validSuggestion = true;


	
	/**
	 * If true then this address is valid due to SERP standards. If false then the address
	 * is not valid and there may be a valid suggestion. Check validSuggestion to see if
	 * the first suggestion is a valid correction.
	 */
	private boolean serpValid = false;

    
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
        results = new ArrayList<ValidateResult>();
        suggestions = new ArrayList<Address>();
    }
    
    private void validateImpl() throws DatabaseException {
        Address a = new Address(getAddress());
        
        validated = true;

        results.clear();
        suggestions.clear();
        
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
        	
        	//Addresses containing a large volume receiver postal code type B, C, D, E, or F should
        	//not be modified from SERP handbook D-22.
        	if (db.containsLVRPostalCode(a.getPostalCode())) {
        		LargeVolumeReceiver lvr = db.findLargeVolumeReceiver(a.getPostalCode());
        		final LVRRecordType recordType = lvr.getLVRRecordType();
        		if (recordType == LVRRecordType.LVR_NAME_LOCK_BOX || recordType == LVRRecordType.LVR_NAME_STREET ||
        				recordType == LVRRecordType.GOVERNMENT_NAME_LOCK_BOX || recordType == LVRRecordType.GOVERNMENT_NAME_STREET ||
        				recordType == LVRRecordType.GENERAL_DELIVERY_NAME) {
        			serpValid = true;
        			logger.debug("SERP valid because the postal code is a large volume receiver.");
        			return;
        		}
        	}
        	
            Set<PostalCode> pcSet = db.findPostalCode(a.getPostalCode());
            List<PostalCode> pcList = new ArrayList<PostalCode>(pcSet);
            
            // verify province, municipality, street, type, direction, and street number
            if (pcSet.isEmpty()) {
                results.add(ValidateResult.createValidateResult(
                        Status.FAIL, "Invalid postal code: " + a.getPostalCode()));
                
                // This will trigger a postal code lookup in the next section
                a.setPostalCode(null);
                
            } else {
            	generateSuggestions(a, municipality, pcList);
            }
            
        }
        
        if (a.getPostalCode() == null) {
            results.add(ValidateResult.createValidateResult(Status.FAIL, "Postal Code is missing"));
            
            //If the address line 1 and 2 are missing along with the postal code this will return too many results to
            //reasonably process.
            if (a.getUnparsedAddressLine1() == null && a.getUnparsedAddressLine2() == null) {
            	return;
            }
            
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
            if (a.getType() != null) {
            	join.addCondition(db.postalCodeRecordType, a.getType().getRecordTypeCode());
            	allNulls = false;
            }
            
            
            // TODO check how many fields match these criteria
            // (for example, if more than 1000 records match, just emit an error)
            
            List<RecordType> validRecordTypes = new ArrayList<RecordType>();
            if (a.getType() != null) {
            	validRecordTypes.add(a.getType());
            }
            if (a.getType() == RecordType.STREET) {
            	if (a.getStreet() != null) {
            		join.addCondition(db.postalCodeStreet, a.getStreet());
            		allNulls = false;
            	}
            }
            
            if (!allNulls) {
                ForwardCursor<PostalCode> matches = null;
                try {
                    matches = join.entities();
                    logger.debug("Checking address " + a);
                    List<PostalCode> pcSet = new ArrayList<PostalCode>();
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
	 * <p>
	 * Package private for testing purposes.
	 * 
	 * @param a
	 *            An address to correct.
	 * @param municipality
	 *            A municipality taken from the address to correct. Allows
	 *            checking for older or alternate municipality names.
	 * @param pcList
	 *            A set of postal codes to compare the address to and try to
	 *            generate suggestions from.
	 * @throws DatabaseException 
	 */
	void generateSuggestions(Address a, Municipality municipality,
			List<PostalCode> pcList) throws DatabaseException {
		Map<Integer, List<Address>> addressSuggestionsByError = new HashMap<Integer, List<Address>>();
		List<ValidateResult> smallestErrorList = new ArrayList<ValidateResult>();
		boolean smallestErrorIsValid = true;
		int smallestErrorCount = Integer.MAX_VALUE;
		PostalCode smallestErrorPostalCode = null;
		//If set to true then the first postal code in the list is valid even if there 
		//are multiple postal codes with the same error count. The first postal code is valid due to some unique case.
		boolean validSpecialCase = false;
		
		Address bestSuggestion = null; 
		int postalCodesProcessed = 0;
		for (PostalCode pc : pcList) {
			if (postalCodesProcessed > MAX_POSTAL_CODES_PROCESSED) {
				break;
			}
			postalCodesProcessed++;

			Address suggestion = new Address(a);
			ValidateState state = new ValidateState();
			new PostalCodeStep().validate(pc, a, suggestion, state);
			new ProvinceNameStep().validate(pc, a, suggestion, state);
			new MunicipalityNameStep(municipality).validate(pc, a, suggestion, state);
			if (new SERPRuralZeroPostalCodeStep().validate(pc, a, suggestion, state)) {
			    suggestions.add(suggestion);
			    serpValid = true;
			    return;
			}
			
        	
			if ((pc.getRecordType() == RecordType.STREET || pc.getRecordType() == RecordType.STREET_AND_ROUTE) &&
					!(suggestion.getType() == RecordType.ROUTE && pc.getRecordType() == RecordType.STREET_AND_ROUTE)) {
				
			    new StreetAndRouteForStreetTypeStep().validate(pc, a, suggestion, state);
				new StreetNameStep().validate(pc, a, suggestion, state);
				new StreetNumberStep().validate(pc, a, suggestion, state);
				new StreetTypeCodeStep().validate(pc, a, suggestion, state);
				new StreetTypePrefixStep(db).validate(pc, a, suggestion, state);
				new StreetDirectionCodeStep().validate(pc, a, suggestion, state);
				new StreetNameStep().validate(pc, a, suggestion, state);
				new StreetAddressNumberSuffixStep().validate(pc, a, suggestion, state);
				new StreetNumberSuffixSpacingStep().validate(pc, a, suggestion, state);
				new StreetNumberSequenceCodeStep().validate(pc, a, suggestion, state);
				new SuiteNumberPrefixStep().validate(pc, a, suggestion, state);
				new SuiteNumberSignStep().validate(pc, a, suggestion, state);
				new SuiteNumberMissingStep().validate(pc, a, suggestion, state);
			}
			if (pc.getRecordType() == RecordType.GENERAL_DELIVERY) {
				if (suggestion.getType() != PostalCode.RecordType.GENERAL_DELIVERY) {
					state.incrementErrorCount("Address type does not match best suggestion.");
				}
				suggestion.setType(RecordType.GENERAL_DELIVERY);
				new GeneralDeliveryNameStep().validate(pc, a, suggestion, state);
				new GeneralDeliveryNameAndTypeStep(db).validate(pc, a, suggestion, state);
				
			}
			if (pc.getRecordType() == RecordType.LOCK_BOX) {
				if (suggestion.getType() != PostalCode.RecordType.LOCK_BOX) {
					state.incrementErrorCount("Address type does not match best suggestion.");
				}
				suggestion.setType(RecordType.LOCK_BOX);
				
				new LockBoxTypeStep().validate(pc, a, suggestion, state);
				new LockBoxNumberContainsNumSignStep().validate(pc, a, suggestion, state);
				new LockBoxNumberStep().validate(pc, a, suggestion, state);
				new LockBoxNameAndTypeStep(db).validate(pc, a, suggestion, state);
				
			}
			if ((pc.getRecordType() == RecordType.ROUTE || pc.getRecordType() == RecordType.STREET_AND_ROUTE) &&
					!(suggestion.getType() == RecordType.STREET && pc.getRecordType() == RecordType.STREET_AND_ROUTE)) {
				
				new StreetAndRouteForRouteTypeStep().validate(pc, a, suggestion, state);
				new RouteTypeStep().validate(pc, a, suggestion, state);
				new RouteDINameAndTypeStep(db).validate(pc, a, suggestion, state);
				new RouteNumberStep().validate(pc, a, suggestion, state);
				new RouteNumberContainsNumSignStep().validate(pc, a, suggestion, state);
				
			}
			

			if (state.isSuggestionExists() || state.isReparsed()) {
				//Special case: if multiple postal codes exist where one has a smaller street range
				//than the other (802-806 vs 802-812) and the address falls in both ranges, and the
				//streets differ by a type that the address is missing (BAYVIEW AVE vs BAYVIEW ST 
				//with address BAYVIEW) then the postal code with the smaller range should be taken
				//as more valid.
				boolean addToFrontOfList = false;
				if (smallestErrorPostalCode != null && postalCodeStreetRangesCoverAddress(a, pc, smallestErrorPostalCode) && 
						checkIfFirstPostalCodeStreetRangeInsideSecond(pc, smallestErrorPostalCode) &&
						checkIfStreetErrorMatchesBothPostalCodes(a, pc, smallestErrorPostalCode)) {
					if (state.getErrorCount() > smallestErrorCount) {
						state.setErrorCount(smallestErrorCount);
					}
					addToFrontOfList = true;
					validSpecialCase = true;
				} else if (smallestErrorPostalCode != null && postalCodeStreetRangesCoverAddress(a, pc, smallestErrorPostalCode) && 
						checkIfFirstPostalCodeStreetRangeInsideSecond(smallestErrorPostalCode, pc) &&
						checkIfStreetErrorMatchesBothPostalCodes(a, pc, smallestErrorPostalCode)) {
					validSpecialCase = true;
					List<Address> addresses = addressSuggestionsByError.get(smallestErrorCount);
					addresses.remove(bestSuggestion);
					addresses.add(0, bestSuggestion);
				} else if (smallestErrorPostalCode != null && postalCodeStreetRangesCoverAddress(a, pc, smallestErrorPostalCode) &&
						checkIfStreetErrorMatchesBothPostalCodes(a, pc, smallestErrorPostalCode) &&
						!checkIfFirstPostalCodeStreetRangeInsideSecond(smallestErrorPostalCode, pc)) {
					validSpecialCase = false;
				}
				
				List<Address> addresses = addressSuggestionsByError.get(state.getErrorCount());
				if (addresses == null) {
					addresses = new ArrayList<Address>();
					addresses.add(suggestion);
					addressSuggestionsByError.put(state.getErrorCount(), addresses);
				} else if (!addresses.contains(suggestion)) {
					if (addToFrontOfList) {
						addresses.add(0, suggestion);
					} else {
						addresses.add(suggestion);
					}
				}
				if (state.getErrorCount() < smallestErrorCount || addToFrontOfList) {
					smallestErrorPostalCode = pc;
					smallestErrorList = state.getErrorList();
					smallestErrorCount = state.getErrorCount();
					smallestErrorIsValid = state.isValid();
					bestSuggestion = suggestion;
				}
			} else {
				suggestions.clear();
				serpValid = true;
				logger.debug("SERP valid, clearing suggestions");
				return;
			}
		}
		serpValid = smallestErrorCount == 0;
		if (results.size() == 0) {
			serpValid = true;
		}
		results.addAll(smallestErrorList);
		final ArrayList<Integer> errorCounts = new ArrayList<Integer>(addressSuggestionsByError.keySet());
		Collections.sort(errorCounts);
		//If there is only one suggestion at the lowest error count then that is a valid suggestion, otherwise invalid
		if (!validSpecialCase && (!smallestErrorIsValid || (addressSuggestionsByError.get(smallestErrorCount) != null && addressSuggestionsByError.get(smallestErrorCount).size() > 1))) {
			validSuggestion = false;
			serpValid = false;
		}
		for (Integer errorCount : errorCounts) {
			for (Address suggestion : addressSuggestionsByError.get(errorCount)) {
				if (!suggestions.contains(suggestion)) {
					suggestions.add(suggestion);
				}
			}
		}
		validated = true;
		logger.debug("Displaying " + suggestions.size() + " suggestions");
	}
	
	/**
	 * There are specific errors an address could have that would allow it to match to two
	 * different postal codes. These errors are things like the postal codes differ by street
	 * type or direction and the direction is missing. This will return true if they differ
	 * by the valid error types and could be matched to both. An error count does not suffice
	 * as one could be different by municipality only while the other could be different by direction
	 * only.
	 */
	private boolean checkIfStreetErrorMatchesBothPostalCodes(Address a,
			PostalCode pc, PostalCode smallestErrorPostalCode) {
		if (a == null || pc == null || smallestErrorPostalCode == null) return false;
		if (pc.getRecordType() != smallestErrorPostalCode.getRecordType()) return false;
		if (!pc.getMunicipalityName().equals(smallestErrorPostalCode.getMunicipalityName())) return false;
		if (!pc.getPostalCode().equals(smallestErrorPostalCode.getPostalCode())) return false;
		if (!pc.getProvinceCode().equals(smallestErrorPostalCode.getProvinceCode())) return false;
		if (pc.getStreetName() == null || !pc.getStreetName().equals(smallestErrorPostalCode.getStreetName())) return false;
		if (!(a.getStreetType() == null || pc.getStreetTypeCode().equals(smallestErrorPostalCode.getStreetTypeCode()))) return false;
		if (!(a.getStreetDirection() == null || pc.getStreetDirectionCode().equals(smallestErrorPostalCode.getStreetDirectionCode()))) return false;
		return true;
	}

	/**
	 * Returns true if the address contains a street number and falls in the range
	 * of both postal codes passed in. 
	 */
	private boolean postalCodeStreetRangesCoverAddress(Address a,
			PostalCode pc, PostalCode smallestErrorPostalCode) {
		if (a == null || pc == null || smallestErrorPostalCode == null) return false;
		if (a.getStreetNumber() == null ||pc.getStreetAddressFromNumber() == null || pc.getStreetAddressToNumber() == null || smallestErrorPostalCode.getStreetAddressFromNumber() == null || smallestErrorPostalCode.getStreetAddressToNumber() == null) return false;
		if (pc.contains(a) && smallestErrorPostalCode.contains(a)) return true;
		return false;
	}

	/**
	 * Returns true if one of the postal code's range is inside the other. If they are
	 * the same then false will be returned. 
	 */
	private boolean checkIfFirstPostalCodeStreetRangeInsideSecond(PostalCode pc,
			PostalCode smallestErrorPostalCode) {
		if (pc == null || smallestErrorPostalCode == null) return false;
		if (smallestErrorPostalCode.contains(pc)) return true;
		return false;
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
        if (!validated) {
            validate();
        }
        return Collections.unmodifiableList(results);
    }
    
    public List<Address> getSuggestions() {
        if (!validated) {
            validate();
        }
        return Collections.unmodifiableList(suggestions);
    }
    
    /**
     * If true the first suggestion in the suggestions list is a valid postal code
     * that is accurate for the given address. If false then either there is too many
     * conflicts between the data given in the address or the address is missing too
     * much information to accurately give a valid suggestion.
     */
    public boolean isValidSuggestion() {
		return validSuggestion;
	}

	public boolean isSerpValid() {
		return serpValid;
	}

	public Address getAddress() {
		return address;
	}
	
	/**
	 * What is defined to be valid may depend on user preferences when switching between
	 * SERP certification, MDM conformance and normalizing. This will tell what is valid
	 * based on settings. At current it is simple but will grow.
	 */
	public boolean isAddressValid() {
		return getResults().size() == 0;
	}
}
