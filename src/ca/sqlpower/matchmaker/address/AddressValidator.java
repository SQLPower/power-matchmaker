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
import java.util.List;
import java.util.Set;

import ca.sqlpower.validation.Status;
import ca.sqlpower.validation.ValidateResult;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityJoin;
import com.sleepycat.persist.ForwardCursor;

public class AddressValidator {

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
            PostalCode pc = db.findPostalCode(a.getPostalCode());
            
            // verify province, municipality, street, type, direction, and street number
            if (pc == null) {
                results.add(ValidateResult.createValidateResult(
                        Status.FAIL, "Invalid postal code: " + a.getPostalCode()));
                
                // This will trigger a postal code lookup in the next section
                a.setPostalCode(null);
                
            } else {
                Address suggestion = new Address(a);
                suggestion.setPostalCode(pc.getPostalCode());
                boolean needSuggestion = false;
                if (!pc.getProvinceCode().equals(a.getProvince())) {
                    results.add(ValidateResult.createValidateResult(
                            Status.FAIL, "Province code does not agree with postal code"));
                    suggestion.setProvince(pc.getProvinceCode());
                    needSuggestion = true;
                }
                if (different(pc.getMunicipalityName(), a.getMunicipality())) {
                    if (municipality != null) {
                        // it might be a valid alternate name
                        if (!municipality.isNameAcceptable(a.getMunicipality(), a.getPostalCode())) {
                            results.add(ValidateResult.createValidateResult(
                                    Status.FAIL, "Municipality is not a valid alternate within postal code"));
                            suggestion.setMunicipality(municipality.getOfficialName());
                            needSuggestion = true;
                        }
                    } else {
                        results.add(ValidateResult.createValidateResult(
                                Status.FAIL, "Municipality does not agree with postal code"));
                        suggestion.setMunicipality(pc.getMunicipalityName());
                        needSuggestion = true;
                    }
                }
                if (different(pc.getStreetName(), a.getStreet())) {
                    results.add(ValidateResult.createValidateResult(
                            Status.FAIL, "Street name does not agree with postal code"));
                    suggestion.setStreet(pc.getStreetName());
                    needSuggestion = true;
                }
                if (different(pc.getStreetTypeCode(), a.getStreetType())) {
                    results.add(ValidateResult.createValidateResult(
                            Status.FAIL, "Street type does not agree with postal code"));
                    suggestion.setStreetType(pc.getStreetTypeCode());
                    needSuggestion = true;
                }
                if (different(pc.getStreetDirectionCode(), a.getStreetDirection())) {
                    results.add(ValidateResult.createValidateResult(
                            Status.FAIL, "Street direction does not agree with postal code"));
                    suggestion.setStreetDirection(pc.getStreetDirectionCode());
                    needSuggestion = true;
                }
                
                // TODO all the other fields
                
                if (needSuggestion) {
                    suggestions.add(suggestion);
                }
            }
            
        }
        
        if (a.getPostalCode() == null) {
            results.add(ValidateResult.createValidateResult(Status.FAIL, "Postal Code is missing"));
            
            // try to find unique postal code match TODO extract to public method
            EntityJoin<String, PostalCode> join = new EntityJoin<String, PostalCode>(db.postalCodePK);
            
            boolean allNulls = true;
            
            if (a.getProvince() != null) {
                join.addCondition(db.postalCodeProvince, a.getProvince());
                allNulls = false;
            }
            if (a.getMunicipality() != null) {
                join.addCondition(db.postalCodeMunicipality, a.getMunicipality());
                allNulls = false;
            }
            if (a.getStreet() != null) {
                join.addCondition(db.postalCodeStreet, a.getStreet());
                allNulls = false;
            }
            
            
            // TODO check how many fields match these criteria
            // (for example, if more than 1000 records match, just emit an error)
            
            if (!allNulls) {
                ForwardCursor<PostalCode> matches = null;
                try {
                    matches = join.entities();
                    for (PostalCode pc : matches) {
                        if (pc.containsAddress(a)) {
                            Address suggestion = new Address(a);
                            suggestion.setPostalCode(pc.getPostalCode());
                            suggestion.setProvince(pc.getProvinceCode());
                            suggestion.setStreet(pc.getStreetName());
                            suggestion.setStreetDirection(pc.getStreetDirectionCode());
                            suggestion.setStreetType(pc.getStreetTypeCode());
                            suggestions.add(suggestion);
                            // don't break--multiple matches can differ by street type and direction
                        }
                    }
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
     * Compares two strings case insensitively, and considers null to be equivalent
     * to the empty string. Leading and trailing whitespace is ignored.
     * 
     * @param s1 One string to compare
     * @param s2 The other string to compare
     * @return True iff strings s1 and s2 differ according to the rules outlined
     * above.
     */
    private static boolean different(String s1, String s2) {
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
}
