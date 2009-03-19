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

import junit.framework.TestCase;
import ca.sqlpower.matchmaker.address.PostalCode.RecordType;

public class AddressValidatorTest extends TestCase {
	
	/**
	 * Path pointing to the directory containing the address database.
	 */
	private static final String ADDRESS_DATABASE_PATH = "/Users/thomas/addressdb";
	private AddressDatabase addressDB;
	
    @Override
    protected void setUp() throws Exception {
        addressDB = new AddressDatabase(new File(ADDRESS_DATABASE_PATH));
    }
	
	/**
	 * Tests that if the street type is in Quebec it should
	 * come after a number
	 */
	public void testStreetTypeQCAfterNumber() throws Exception {
		Address a = new Address();
		a.setStreet("34");
		a.setStreetType("COUR");
		PostalCode pc = new PostalCode();
		pc.setProvinceCode("QC");
		
		AddressValidator validator = new AddressValidator(addressDB, a);
		assertFalse(validator.isStreetTypePrefix(a, pc));
	}
	
	/**
	 * Tests that if the street type is in Quebec it should
	 * come after a number followed by E (ie: 37E).
	 */
	public void testStreetTypeQCAfterNumberE() throws Exception {
		Address a = new Address();
		a.setStreet("34E");
		a.setStreetType("COUR");
		PostalCode pc = new PostalCode();
		pc.setProvinceCode("QC");
		
		AddressValidator validator = new AddressValidator(addressDB, a);
		assertFalse(validator.isStreetTypePrefix(a, pc));
	}
	
	/**
	 * Tests that if the street type is in Quebec it should
	 * come after a number followed by RE (ie: 37RE);
	 */
	public void testStreetTypeQCAfterNumberRE() throws Exception {
		Address a = new Address();
		a.setStreet("34RE");
		a.setStreetType("COUR");
		PostalCode pc = new PostalCode();
		pc.setProvinceCode("QC");
		
		AddressValidator validator = new AddressValidator(addressDB, a);
		assertFalse(validator.isStreetTypePrefix(a, pc));
	}
	
	/**
	 * Tests that if the street type is in Quebec it should
	 * come before a generic street name
	 */
	public void testStreetTypeQCBeforeString() throws Exception {
		Address a = new Address();
		a.setStreet("A STREET NAME");
		a.setStreetType("COUR");
		PostalCode pc = new PostalCode();
		pc.setProvinceCode("QC");
		
		AddressValidator validator = new AddressValidator(addressDB, a);
		assertTrue(validator.isStreetTypePrefix(a, pc));
	}
	
    /**
     * Test to ensure the validator can deal with well formed street information
     * missing a postal code.
     * 
     * Input: 1817 QUEEN E, TORONTO, ON,
     */
    public void testAddressWithNoPostalCode() throws Exception {
    	Address a = new Address();
    	a.setType(RecordType.STREET);
    	a.setStreetNumber(1817);
    	a.setStreet("QUEEN");
    	a.setStreetDirection("E");
    	a.setMunicipality("TORONTO");
    	a.setProvince("ON");
    	a.setCountry("CA");
    	
    	AddressValidator validator = new AddressValidator(addressDB, a);
    	List<Address> suggestions = validator.getSuggestions();
    	assertFalse(suggestions.isEmpty());
    	Address bestSuggestion = suggestions.get(0);
    	assertEquals("M4L3Z6", bestSuggestion.getPostalCode());
    	assertEquals("ST", bestSuggestion.getStreetType());
    }
    
    /**
     * Test to ensure the validator can deal with well formed street information
     * missing a postal code. and has additional information
     * 
     * Input: 1817 QUEEN 0, TORONTO, ON,
     */
    public void testAddressWithNoPostalCodeAndAddInfo() throws Exception {
    	Address a = new Address();
    	a.setStreetNumber(1817);
    	a.setType(RecordType.STREET);
    	a.setStreet("QUEEN 0");
    	a.setMunicipality("TORONTO");
    	a.setProvince("ON");
    	a.setCountry("CA");
    	
    	AddressValidator validator = new AddressValidator(addressDB, a);
    	List<Address> suggestions = validator.getSuggestions();
    	assertFalse(suggestions.isEmpty());
    	Address bestSuggestion = suggestions.get(0);
    	assertEquals("M4L3Z6", bestSuggestion.getPostalCode());
    	assertEquals("ST", bestSuggestion.getStreetType());
    }
    
    /**
     * Test to ensure the validator can deal with rural route with no station name.
     * 
     * Input: RR 4 STN, DUNDAS, ON, L9H5E4
     */
    public void testRuralRouteNoDIName() throws Exception {
    	Address a = new Address();
    	a.setRuralRouteType("RR");
    	a.setRuralRouteNumber("4");
    	a.setDeliveryInstallationType("STN");
    	a.setType(RecordType.ROUTE);
    	a.setMunicipality("DUNDAS");
    	a.setProvince("ON");
    	a.setCountry("CA");
    	
    	AddressValidator validator = new AddressValidator(addressDB, a);
    	List<Address> suggestions = validator.getSuggestions();
    	assertFalse(suggestions.isEmpty());
    	Address bestSuggestion = suggestions.get(0);
    	assertEquals("DUNDAS", bestSuggestion.getDeliveryInstallationName());
    }
    
}
