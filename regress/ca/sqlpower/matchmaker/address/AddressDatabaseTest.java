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

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import ca.sqlpower.matchmaker.address.Address.Type;
import ca.sqlpower.validation.ValidateResult;

public class AddressDatabaseTest extends TestCase {

    private Address address;
    private AddressDatabase addressDB;

    /**
     * Searches the message string of each result in the given list for the
     * given regular expression. If any match is found, the assertion passes.
     * 
     * @param results
     *            The list of results to search through
     * @param regex
     *            The regular expression pattern to search for. This pattern is
     *            treated as case-insensitive, and does not have to match a
     *            whole message--just part of one.
     */
    private static void assertResultContains(List<ValidateResult> results, String regex) {
        if (!resultContains(results, regex)) {
            throw new AssertionFailedError(
                    "Pattern /"+regex+"/ was not found among the "
                    + results.size() + " results: " + results); 
        }
    }

    /**
     * Searches the message string of each result in the given list for the
     * given regular expression. If no match is found, the assertion passes.
     * 
     * @param results
     *            The list of results to search through
     * @param regex
     *            The regular expression pattern to search for. This pattern is
     *            treated as case-insensitive, and does not have to match a
     *            whole message--just part of one.
     */
    private static void assertResultDoesNotContain(List<ValidateResult> results, String regex) {
        if (resultContains(results, regex)) {
            throw new AssertionFailedError(
                    "Pattern /"+regex+"/ was found among the "
                    + results.size() + " results: " + results); 
        }
    }

    private static boolean resultContains(List<ValidateResult> results, String regex) {
        Pattern p = Pattern.compile(".*" + regex + ".*", Pattern.CASE_INSENSITIVE);
        for (ValidateResult result : results) {
            if (result.getMessage() != null && p.matcher(result.getMessage()).matches()) {
                return true;
            }
        }
        return false;
    }

    private List<ValidateResult> createValidateResults() {
        AddressValidator validator = new AddressValidator(addressDB, address);
        return validator.getResults();
    }
    
    @Override
    protected void setUp() throws Exception {
        addressDB = new AddressDatabase(new File("/Users/fuerth/addressdb"));
        
        address = new Address();
    }
    
    @Override
    protected void tearDown() throws Exception {
        addressDB.close();
        super.tearDown();
    }
    
    public void testNoticeInvalidPostalCode() {
        address.setPostalCode("1AAAAA"); // does not follow A1A1A1 pattern, so should never be valid
        
        List<ValidateResult> result = createValidateResults();
        
        assertResultContains(result, "invalid postal code");
    }
    
    public void testIncorrectMunicipality() {
        // The record we're hoping to match: NS,ANTIGONISH,HILLCREST,ST,ANTIGONISH,B2G 1Z3
        address.setType(Type.URBAN);
        address.setProvince("NS");
        address.setMunicipality("ANITGINOSH"); // this is the incorrect municipality name
        address.setStreet("Hillcrest");
        address.setStreetType("St");
        address.setPostalCode("B2G 1Z3");
        address.resetChangeFlags();
        
        List<ValidateResult> results = createValidateResults();
        
        assertResultContains(results, "ANITGINOSH.*does not exist");
        assertResultContains(results, "municipality.*does not agree");
    }

    public void testAddPostalCode() {
        // The record we're hoping to match: NS,ANTIGONISH,HILLCREST,ST,14..58,ANTIGONISH,B2G 1Z3
        address.setType(Type.URBAN);
        address.setProvince("NS");
        address.setMunicipality("ANTIGONISH");
        address.setStreetNumber(20);
        address.setStreet("Hillcrest");
        address.setStreetType("St");
        address.resetChangeFlags();
        
        List<ValidateResult> results = createValidateResults();
        
        System.out.println("addpostalcode: " + results);
        System.out.println(address);
        assertEquals("B2G1Z3", address.getPostalCode());
        assertResultContains(results, "added postal code");
    }
    
    public void testNoWarningForOfficialName() throws Exception {
        address.setType(Type.URBAN);
        address.setProvince("NS");
        address.setMunicipality("ANTIGONISH");
        
        List<ValidateResult> results = createValidateResults();

        assertResultDoesNotContain(results, "corrected municipality");
    }

    public void testUnrecognizedMunicipality() throws Exception {
        // The record we're hoping to match: NS,ANTIGONISH,HILLCREST,ST,ANTIGONISH,B2G 1Z3
        address.setMunicipality("ANITGINOSH"); // this is the incorrect municipality name
        address.resetChangeFlags();
        
        createValidateResults();
        
        assertFalse(address.isMunicipalityChanged());
    }
    
    public void testRecognizeValidMunicipality() {
        address.setType(Type.URBAN);
        address.setProvince("NS");
        address.setMunicipality("ANTIGONISH");
        address.setStreet("Hillcrest");
        address.setStreetType("St");
        address.setPostalCode("B2G 1Z3");
        address.resetChangeFlags();
        
        createValidateResults();
        
        assertFalse(address.isMunicipalityChanged());
        assertEquals("ANTIGONISH", address.getMunicipality());
    }
    
    public void testMunicipalityCrossProvince() {
        address.setType(Type.URBAN);
        address.setMunicipality("VICTORIA");
        address.setProvince("NS");
        address.resetChangeFlags();
        
        List<ValidateResult> results = createValidateResults();
        
        assertTrue(address.isMunicipalityChanged());
        assertEquals("OXFORD", address.getMunicipality());
        assertResultContains(results, "corrected municipality");
    }

    /**
     * This is just a valid address that we made a test for because it
     * happened to be causing problems at the time. There are other tests
     * for the specific problem we ran up against, but I'm leaving this
     * test here for variety.
     */
    public void testValidAddress() throws Exception {
        address.setStreetNumber(59);
        address.setStreet("BRAMLEY");
        address.setStreetType("ST");
        address.setStreetDirection("S");
        address.setMunicipality("PORT HOPE");
        address.setProvince("ON");
        address.setPostalCode("L1A3K3");
        address.setCountry("Canada");
        System.out.println(address);
        List<ValidateResult> results = createValidateResults();
        
        for (ValidateResult result: results) {
            System.out.println(result.toString());
        }
        
        assertEquals(0, results.size());
    }
}
