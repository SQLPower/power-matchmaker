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

public class AddressTest extends TestCase {

    /** SQLPower Word Wide Headquarters! */
    private Address sqlpWWHQ;
    
    private AddressDatabase addressDatabase;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
	    String bdbPath = System.getProperty("ca.sqlpower.matchmaker.test.addressDB");
	    if (bdbPath == null) {
	        throw new RuntimeException(
	                "Please define the system property ca.sqlpower.matchmaker.test.addressDB" +
	                " to point to the directory where your BDB instance is which contains addresses");
	    }
        addressDatabase = new AddressDatabase(new File(bdbPath));
        sqlpWWHQ = new Address();
        sqlpWWHQ.setCountry("CA");
        sqlpWWHQ.setMunicipality("NORTH YORK");
        sqlpWWHQ.setPostalCode("M2N6K1");
        sqlpWWHQ.setProvince("ON");
        sqlpWWHQ.setStreet("YONGE");
        sqlpWWHQ.setStreetNumber(4950);
        sqlpWWHQ.setStreetType("ST");
        sqlpWWHQ.setSuite("2110");
        sqlpWWHQ.setType(Type.URBAN);
        sqlpWWHQ.resetChangeFlags();
    }
    
    public void testMunicipalityChangedFlag() throws Exception {
        assertFalse(sqlpWWHQ.isMunicipalityChanged());
        sqlpWWHQ.setMunicipality("moocow");
        assertTrue(sqlpWWHQ.isMunicipalityChanged());
    }
    
    public void testResetChangeFlags() throws Exception {
        sqlpWWHQ.setMunicipality("moocow");

        sqlpWWHQ.resetChangeFlags();
        
        assertFalse(sqlpWWHQ.isMunicipalityChanged());
    }
    
    
    public void testParse() throws Exception {
    	Address address = Address.parse("4950 YONGE ST", "NORTH YORK", "ON", "M2N6K1", "CA", addressDatabase);
    	assertEquals(Integer.valueOf(4950), address.getStreetNumber());
    	assertEquals("YONGE", address.getStreet());
    	assertEquals("ST", address.getStreetType());
    	assertEquals("NORTH YORK", address.getMunicipality());
    	assertEquals("ON", address.getProvince());
    	assertEquals("M2N6K1", address.getPostalCode());
    	assertEquals("CA", address.getCountry());
    }

	/**
	 * The Address parser should be able to parse CIRCLE. When this test was
	 * made, the parse would not recognize it and result in all the street
	 * address fields coming up null. But if you change it to CIR, then it would
	 * work. Ideally it should work for both.
	 */
    public void testParseWithNonAbbreviatedStreetType() throws Exception {
    	Address address = Address.parse("1751 SANDHURST CIRCLE", "AGINCOURT", "ON", "", "CA", addressDatabase);
    	assertEquals(Integer.valueOf(1751), address.getStreetNumber());
    	assertEquals("SANDHURST", address.getStreet());
    	assertEquals("CIRCLE", address.getStreetType());
    	assertEquals("AGINCOURT", address.getMunicipality());
    	assertEquals("ON", address.getProvince());
    	assertEquals("CA", address.getCountry());
    }
    
    /**
	 * The parser should be able to properly extract the street number suffix.
	 */
    public void testParseWithStreetNumberSuffix() throws Exception {
    	Address address = Address.parse("15C DEVONRIDGE CRES", "SCARBOROUGH", "ON", "M1C5A5", "CA", addressDatabase);
    	assertEquals(Integer.valueOf(15), address.getStreetNumber());
    	assertEquals("C", address.getStreetNumberSuffix());
    	assertEquals("DEVONRIDGE", address.getStreet());
    	assertEquals("CRES", address.getStreetType());
    	assertEquals("SCARBOROUGH", address.getMunicipality());
    	assertEquals("M1C5A5", address.getPostalCode());
    	assertEquals("ON", address.getProvince());
    	assertEquals("CA", address.getCountry());
    }
    
    /**
     * In Quebec, the street type is actually put BEFORE the street name.
     * The parser should be able to handle this.
     * The Address tested here is: 4539 RUE BROOKLYN, TROIS-RIVIERES, QC, G8Y1C8
     */
    public void testParseQuebecStreetAddress() throws Exception {
    	Address address = Address.parse("4539 RUE BROOKLYN", "TROIS-RIVIERES", "QC", "G8Y1C8", "CA", addressDatabase);
    	assertEquals(Integer.valueOf(4539), address.getStreetNumber());
    	assertEquals("BROOKLYN", address.getStreet());
    	assertEquals("RUE", address.getStreetType());
    	assertEquals("TROIS-RIVIERES", address.getMunicipality());
    	assertEquals("G8Y1C8", address.getPostalCode());
    	assertEquals("QC", address.getProvince());
    	assertEquals("CA", address.getCountry());
    }
    
    /**
     * This address has a long and complicated street name that
     * also contains a street type (RIDGE) in the street name itself.
     *
     * The parser should be able to extract the full street name and
     * recognize the proper street type (which is CRT here, not RIDGE)
     * 
     * Test Address: 42 POPLAR RIDGE TRAILER CRT, DRAYTON VALLEY, AB, T7A1N4
     */
    public void testParseComplicatedStreetName() throws Exception {
    	Address address = Address.parse("42 POPLAR RIDGE TRAILER CRT", "DRAYTON VALLEY", "AB", "T7A1N4", "CA", addressDatabase);
    	assertEquals(Integer.valueOf(42), address.getStreetNumber());
    	assertEquals("POPLAR RIDGE TRAILER", address.getStreet());
    	assertEquals("CRT", address.getStreetType());
    	assertEquals("DRAYTON VALLEY", address.getMunicipality());
    	assertEquals("T7A1N4", address.getPostalCode());
    	assertEquals("AB", address.getProvince());
    	assertEquals("CA", address.getCountry());
    }
    
    /**
     * Test to ensure the parser can deal with street addresses 
     * containing an apostrophe.
     * 
     * Input: 1539 HALL'S RD, COURTENAY, BC, V9N5R8
     */
    public void testAddressWithApostrophe() throws Exception {
    	Address address = Address.parse("1539 HALL'S RD", "COURTENAY", "BC", "V9N5R8", "CA", addressDatabase);
    	assertEquals(Integer.valueOf(1539), address.getStreetNumber());
    	assertEquals("HALL'S", address.getStreet());
    	assertEquals("RD", address.getStreetType());
    	assertEquals("COURTENAY", address.getMunicipality());
    	assertEquals("V9N5R8", address.getPostalCode());
    	assertEquals("BC", address.getProvince());
    	assertEquals("CA", address.getCountry());
    }
    
    /**
     * Test to ensure the parser can deal with street addresses 
     * containing extra info.
     * 
     * Input: 56 RUE SAINT-JEAN-BAPTISTE O, RIMOUSKI, QC, G5L4J3
     */
    public void testAddressWithExtraInfo() throws Exception {
    	Address address = Address.parse("56 RUE SAINT-JEAN-BAPTISTE 0", "RIMOUSKI", "QC", "G5L4J3", "CA", addressDatabase);
    	assertEquals(Integer.valueOf(56), address.getStreetNumber());
    	assertEquals("SAINT-JEAN-BAPTISTE 0", address.getStreet());
    	assertEquals("RUE", address.getStreetType());
    	assertEquals("RIMOUSKI", address.getMunicipality());
    	assertEquals("G5L4J3", address.getPostalCode());
    	assertEquals("QC", address.getProvince());
    	assertEquals("CA", address.getCountry());
    }
    
    public void testGeneralDelivery() throws Exception {
    	Address address = Address.parse("GENERAL DELIVERY STN MAIN", "ST THOMAS", "ON", "N5P3T4", "CA", addressDatabase);
    	assertEquals("GENERAL DELIVERY", address.getGeneralDeliveryName());
    	assertEquals("STN", address.getDeliveryInstallationType());
    	assertEquals("MAIN", address.getDeliveryInstallationName());
    }
}
