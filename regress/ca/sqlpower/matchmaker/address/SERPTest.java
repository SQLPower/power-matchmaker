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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

import junit.framework.TestCase;

import org.apache.log4j.Logger;

import ca.sqlpower.validation.ValidateResult;

/**
 * This test case takes a SERP self-test file and reads the input, parses it and
 * corrects it, and then compares the results to a SERP self-test result file.
 * 
 * NOTE: You have to provide the address database, test, and result files
 * yourself.
 */
public class SERPTest extends TestCase {

	private Logger logger = Logger.getLogger(SERPTest.class);
	
	/**
	 * Path pointing to the directory containing the address database.
	 */
	private static final String ADDRESS_DATABASE_PATH = "";
	
	/**
	 * The path to the SERP self test input file.
	 */
	private static final String TEST_FILE_PATH = "../matchmaker-addressparse/test_data/30843SCT";
	
	/**
	 * The path to the SERP self test result file.
	 */
	private static final String RESULT_FILE_PATH = "../matchmaker-addressparse/test_data/30843SCR";
	private AddressDatabase addressDB;
	private Address address;
	
    @Override
    protected void setUp() throws Exception {
        addressDB = new AddressDatabase(new File(ADDRESS_DATABASE_PATH));
        address = new Address();
    }
    
    @Override
    protected void tearDown() throws Exception {
        addressDB.close();
        super.tearDown();
    }

    public void testSERP() throws Exception {
    	File inputFile = new File (TEST_FILE_PATH);
    	File resultFile = new File (RESULT_FILE_PATH);
    	BufferedReader in = new BufferedReader(new FileReader(inputFile));
    	BufferedReader result = new BufferedReader(new FileReader(resultFile));

    	String line;
    	String resultLine;
    	boolean firstLineRead = false;
    	
    	int numPassed = 0;
    	int numFailed = 0;
    	long before = System.currentTimeMillis();
    	
    	while (true) {
    		line = in.readLine();
    		resultLine = result.readLine();
    		if (line == null) break;
    		if (firstLineRead) {
    			String streetAddress = line.substring(109,159).trim();
    			String municipality = line.substring(159, 189).trim();
    			String province = line.substring(189, 191).trim();
    			String postalCode = line.substring(214).trim();
    			
    			logger.debug("Next address is '" + streetAddress + ", " + municipality + ", " + province + ", " + postalCode);
    			
    			address = Address.parse(streetAddress, municipality, province, postalCode, "CA", addressDB);
    			
    			AddressValidator validator = new AddressValidator(addressDB, address);
    			List<ValidateResult> results = validator.getResults();
    			
    			for (ValidateResult validateResult: results) {
    				logger.debug(validateResult);
    			}
    			
    			String resultStreetAddress = resultLine.substring(109,159).trim();
    			String resultMunicipality = resultLine.substring(159, 189).trim();
    			String resultProvince = resultLine.substring(189, 191).trim();
    			String resultPostalCode = resultLine.substring(214, 220).trim();

    			boolean failed = false;
    			
    			final String add = address.getAddress();
				if (!resultStreetAddress.equals(add)) {
    				logger.debug("Street Address is wrong: Expected '" + resultStreetAddress 
    						+ "' Got '" + address.getAddress() + "'");
    				failed = true;
    			}
    			
    			if (!resultMunicipality.equals(address.getMunicipality())) {
    				logger.debug("Municipality is wrong: Expected '" + resultMunicipality 
    						+ "' Got '" + address.getMunicipality() + "'");
    				failed = true;
    			}
    			
    			if (!resultProvince.equals(address.getProvince())) {
    				logger.debug("Province is wrong: Expected '" + resultProvince 
    						+ "' Got '" + address.getProvince() + "'");
    				failed = true;
    			} 
    			
    			if (!resultPostalCode.equals(address.getPostalCode())) {
    				logger.debug("Postal Code is wrong: Expected '" + resultPostalCode 
    						+ "' Got '" + address.getPostalCode() + "'");
    				failed = true;
    			}
    			
    			if (failed) {
    				numFailed++;
    			} else {
    				numPassed++;
    			}
    			
    			logger.debug("");
    			
//    			assertEquals(resultStreetAddress, address.getStreetAddress());
//    			assertEquals(resultMunicipality, address.getMunicipality());
//    			assertEquals(resultProvince, address.getProvince());
//    			assertEquals(resultPostalCode, address.getPostalCode());
    		}
    		firstLineRead = true;
    	}
    	
    	long time = System.currentTimeMillis() - before;
    	logger.debug("Test took " + time + " ms");
    	logger.debug("Number of tests passed: " + numPassed);
    	logger.debug("Number of tests failed: " + numFailed);
    	double passedPercent = (double) numPassed / (double) (numPassed + numFailed) * 100;
    	System.out.printf("Percentage of tests passed %.2f: ", passedPercent);
    	
    	in.close();
    	result.close();
    }
}
