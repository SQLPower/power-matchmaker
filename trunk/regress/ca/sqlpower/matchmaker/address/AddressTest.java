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

import java.io.File;

import junit.framework.TestCase;
import ca.sqlpower.matchmaker.address.PostalCode.RecordType;

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
        sqlpWWHQ.setType(RecordType.STREET);
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
    	System.out.println("address with extra info " + address);
    	assertEquals(Integer.valueOf(56), address.getStreetNumber());
    	assertEquals("SAINT-JEAN-BAPTISTE 0", address.getStreet());
    	assertEquals("RUE", address.getStreetType());
    	assertEquals("RIMOUSKI", address.getMunicipality());
    	assertEquals("G5L4J3", address.getPostalCode());
    	assertEquals("QC", address.getProvince());
    	assertEquals("CA", address.getCountry());
    }
    
    /**
     * Test to ensure the parser can deal with street addresses 
     * with a suite suffixed and misspelled.
     * 
     * Input: 29 TORONTO ISLAND AIRPT SUITS 210, TORONTO, ON, M5V1A1
     */
    public void testAddressWithMisspelledSuite() throws Exception {
    	Address address = Address.parse("29 TORONTO ISLAND AIRPT SUITS 210", "TORONTO", "ON", "M5V1A1", "CA", addressDatabase);
    	assertEquals("210", address.getSuite());
    	assertFalse(address.isSuitePrefix());
    	assertEquals("SUITS", address.getSuiteType());
    	assertEquals(Integer.valueOf(29), address.getStreetNumber());
    	assertEquals("TORONTO ISLAND AIRPT", address.getStreet());
    	assertEquals("TORONTO", address.getMunicipality());
    	assertEquals("M5V1A1", address.getPostalCode());
    	assertEquals("ON", address.getProvince());
    	assertEquals("CA", address.getCountry());
    }
    
    /**
     * Test to ensure the parser can deal with street addresses 
     * with a suite suffixed containing a # sign and the street name
     * is a number.
     * 
     * Input: 11181 80 AVE UNIT #8410, DELTA, BC, V4C1W6
     */
    public void testAddressWithNumberSignSuite() throws Exception {
    	Address address = Address.parse("11181 80 AVE UNIT #8410", "DELTA", "BC", "V4C1W6", "CA", addressDatabase);
    	System.out.println(address.getAddress());
    	assertEquals(Integer.valueOf(11181), address.getStreetNumber());
    	assertEquals("80", address.getStreet());
    	assertEquals("AVE", address.getStreetType());
    	assertEquals("DELTA", address.getMunicipality());
    	assertEquals("V4C1W6", address.getPostalCode());
    	assertEquals("BC", address.getProvince());
    	assertEquals("CA", address.getCountry());
    	
    	assertEquals("#8410", address.getSuite());
    	assertFalse(address.isSuitePrefix());
    	assertEquals("UNIT", address.getSuiteType());
    }
    
    /**
     * Test to ensure the parser can deal with street addresses 
     * where the street name is a number, and there is a street number
     * and suite number.
     * 
     * Input: 3 9903 240 ST, MAPLE RIDGE, BC, V2W1G2
     */
    public void testAddressWithNumberStreetNameAndSuite() throws Exception {
    	Address address = Address.parse("3 9903 240 ST", "MAPLE RIDGE", "BC", "V2W1G2", "CA", addressDatabase);
    	System.out.println(address.getAddress());
    	assertEquals(Integer.valueOf(9903), address.getStreetNumber());
    	assertEquals("240", address.getStreet());
    	assertEquals("ST", address.getStreetType());
    	assertEquals("MAPLE RIDGE", address.getMunicipality());
    	assertEquals("V2W1G2", address.getPostalCode());
    	assertEquals("BC", address.getProvince());
    	assertEquals("CA", address.getCountry());
    	
    	assertEquals("3", address.getSuite());
    	assertTrue(address.isSuitePrefix());
    }
    
    /**
     * Simple test to prove we can parse a lock box.
     */
    public void testLockBox() throws Exception {
    	Address address = Address.parse("PO BOX #736 STN CENTRAL", "CHARLOTTETOWN", "PE", "C1A7L3", "CA", addressDatabase);
    	System.out.println("Lock box test " + address.getAddress());
    	assertEquals("PO BOX", address.getLockBoxType());
    	assertEquals("736", address.getLockBoxNumber());
    	assertEquals("STN", address.getDeliveryInstallationType());
    	assertEquals("CENTRAL", address.getDeliveryInstallationName());
    }
    
    /**
     * Test for parsing a rural route with no station name
     * 
     * INPUT: RR 4 STN, DUNDAS, ON, L9H5E4
     */
    public void testRuralRouteNoStation() throws Exception {
    	Address address = Address.parse("RR 4 STN", "DUNDAS", "ON", "L9H5E4", "CA", addressDatabase);
    	assertEquals("RR", address.getRuralRouteType());
    	assertEquals("4", address.getRuralRouteNumber());
    	assertEquals("STN", address.getDeliveryInstallationType());
    }
    
    
    /**
     * Simple test to prove we can parse a general delivery.
     */
    public void testGeneralDelivery() throws Exception {
    	Address address = Address.parse("GENERAL DELIVERY STN MAIN", "ST THOMAS", "ON", "N5P3T4", "CA", addressDatabase);
    	assertEquals("GENERAL DELIVERY", address.getGeneralDeliveryName());
    	assertEquals("STN", address.getDeliveryInstallationType());
    	assertEquals("MAIN", address.getDeliveryInstallationName());
    }
    
    /**
     * Test general delivery with misspelling and additional information
     * 
     * Input: GENERAL DEIVER SUCC BUCKINGHAM 3905 36TH AVE W, GATINEAU, QC, J8L1T7
     */
    public void testGeneralDeliveryAdditionalInfo() throws Exception {
    	Address address = Address.parse("GENERAL DEIVER SUCC BUCKINGHAM 3905 36TH AVE W", "GATINEAU", "QC", "J8L1T7", "CA", addressDatabase);
    	assertEquals("GENERAL DEIVER", address.getGeneralDeliveryName());
    	assertEquals("SUCC", address.getDeliveryInstallationType());
    	assertEquals("BUCKINGHAM 3905 36TH AVE W", address.getDeliveryInstallationName());
    }
}
