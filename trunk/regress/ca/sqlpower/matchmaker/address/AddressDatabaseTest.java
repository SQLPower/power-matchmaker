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

import junit.framework.TestCase;
import ca.sqlpower.matchmaker.address.Address.Type;

public class AddressDatabaseTest extends TestCase {

    private Address address;
    private AddressDatabase addressDB;
    
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
    
    public void testReplaceIncorrectMunicipality() {
        // The record we're hoping to match: NS,ANTIGONISH,HILLCREST,ST,ANTIGONISH,B2G 1Z3
        address.setType(Type.URBAN);
        address.setProvince("NS");
        address.setMunicipality("ANITGINOSH"); // this is the incorrect municipality name
        address.setStreet("Hillcrest");
        address.setStreetType("St");
        address.setPostalCode("B2G 1Z3");
        address.resetChangeFlags();
        
        addressDB.correctMunicipality(address);
        
        assertTrue(address.isMunicipalityChanged());
        assertEquals("ANTIGONISH", address.getMunicipality());
    }

    public void testUnrecognizedMunicipality() throws Exception {
        // The record we're hoping to match: NS,ANTIGONISH,HILLCREST,ST,ANTIGONISH,B2G 1Z3
        address.setMunicipality("ANITGINOSH"); // this is the incorrect municipality name
        address.resetChangeFlags();
        
        addressDB.correctMunicipality(address);
        
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
        
        addressDB.correctMunicipality(address);
        
        assertFalse(address.isMunicipalityChanged());
        assertEquals("ANTIGONISH", address.getMunicipality());
    }
    
    public void testMunicipalityCrossProvince() {
        address.setType(Type.URBAN);
        address.setMunicipality("VICTORIA");
        address.setProvince("NS");
        address.resetChangeFlags();
        
        addressDB.correctMunicipality(address);
        
        assertTrue(address.isMunicipalityChanged());
        assertEquals("OXFORD", address.getMunicipality());
    }

}
