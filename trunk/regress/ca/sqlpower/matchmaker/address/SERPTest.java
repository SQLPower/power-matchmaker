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
import java.util.ArrayList;
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
    	String bdbPath = System.getProperty("ca.sqlpower.matchmaker.test.addressDB");
	    if (bdbPath == null) {
	        throw new RuntimeException(
	                "Please define the system property ca.sqlpower.matchmaker.test.addressDB" +
	                " to point to the directory where your BDB instance is which contains addresses");
	    }
        addressDB = new AddressDatabase(new File(bdbPath));
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
    	
    	int i = 0;
    	int maxRow = 16000;
    	int showLastRows = 1000;
    	int lastSuccessCount = 0;
    	int lastFailureCount = 0;
    	List<Double> successRates = new ArrayList<Double>();
    	boolean runAll = true;
    	while (runAll || i < maxRow) {
    		i++;
    		if (i % 100 == 0) {
    			logger.debug("Parsed " + i + " records");
    		}
    		if (i % 1000 == 0) {
    			successRates.add(((double) (numPassed-lastSuccessCount) / (double) ((numPassed-lastSuccessCount) + (numFailed-lastFailureCount)) * 100));
    			lastSuccessCount = numPassed;
    			lastFailureCount = numFailed;
    		}
    		line = in.readLine();
    		resultLine = result.readLine();
    		if (!runAll && i < maxRow - showLastRows) continue;
    		if (line == null) break;
    		if (firstLineRead) {
    			String streetAddress = line.substring(109,159).trim();
    			String municipality = line.substring(159, 189).trim();
    			String province = line.substring(189, 191).trim();
    			String postalCode = line.substring(214).trim();
    			
//    			logger.debug("Next address is '" + streetAddress + ", " + municipality + ", " + province + ", " + postalCode);
    			
    			address = Address.parse(streetAddress, municipality, province, postalCode, "CA", addressDB);
    			
    			AddressValidator validator = new AddressValidator(addressDB, address);
    			List<ValidateResult> results = validator.getResults();
    			List<Address> suggestedAddresses = validator.getSuggestions();
    			if (suggestedAddresses.size() > 0 && validator.isValidSuggestion()) {
    				address = suggestedAddresses.get(0);
    			}
    			
    			for (ValidateResult validateResult: results) {
//    				logger.debug(validateResult);
    			}
    			
    			String resultStreetAddress = resultLine.substring(109,159).trim();
    			String resultMunicipality = resultLine.substring(159, 189).trim();
    			String resultProvince = resultLine.substring(189, 191).trim();
    			String resultPostalCode = resultLine.substring(214, 220).trim();
    			
    			//XXX:not handling optional cases, they are currently being handled as valid
    			boolean optionalFailed = true;
    			if (resultLine.substring(220, 222).equals("CO")) {
    				logger.debug("Optional corrected address");
    				optionalFailed = validateAddress(streetAddress,
    						municipality, province, postalCode);
    			}

    			boolean failed = validateAddress(resultStreetAddress,
						resultMunicipality, resultProvince, resultPostalCode);
    			
    			if (resultLine.substring(220, 221).equals("V") && !validator.isSerpValid()) {
    				failed = true;
    				logger.debug("Expecting input to be valid but received suggestions.");
    				logger.debug("Errors were: " + validator.getResults());
    			}
    			if (resultLine.substring(220, 221).equals("N") && (validator.isValidSuggestion() || validator.isSerpValid())) {
    				failed = true;
    				if (validator.isSerpValid()) {
    					logger.debug("Expecting input to be invalid non-correctable but was marked valid.");
    				} else {
    					logger.debug("Expecting input to be invalid non-correctable but received suggestions.");
    				}
    			}
    			if (resultLine.substring(220, 221).equals("C") && ((!validator.isValidSuggestion() && validator.isSerpValid())
    					|| (resultLine.substring(221, 222).equals("O") && !validator.isSerpValid()))) {
    				failed = true;
    				if (validator.isSerpValid()) {
    					logger.debug("Expecting input to be invalid correctable but was marked valid.");
    				} else {
    					logger.debug("Expecting input to be invalid correctable but marked invalid.");
    				}
    			}
    			
    			if (failed && optionalFailed) {
    				logger.debug("Failed to parse '" + streetAddress + ", " + municipality + ", " + province + ", " + postalCode);
    				logger.debug("Error was on line " + i + ": currently " + ((double) numPassed / (double) (numPassed + numFailed) * 100) + "% passed");
    				logger.debug("");
    				numFailed++;
    			} else {
    				numPassed++;
    			}
    			
//    			logger.debug("");
    			
//    			assertEquals(resultStreetAddress, address.getStreetAddress());
//    			assertEquals(resultMunicipality, address.getMunicipality());
//    			assertEquals(resultProvince, address.getProvince());
//    			assertEquals(resultPostalCode, address.getPostalCode());
    		}
    		firstLineRead = true;
    	}
		successRates.add(((double) (numPassed-lastSuccessCount) / (double) ((numPassed-lastSuccessCount) + (numFailed-lastFailureCount)) * 100));
		lastSuccessCount = numPassed;
		lastFailureCount = numFailed;
    	
    	long time = System.currentTimeMillis() - before;
    	logger.debug("Test took " + time + " ms");
    	logger.debug("Number of tests passed: " + numPassed);
    	logger.debug("Number of tests failed: " + numFailed);
    	double passedPercent = (double) numPassed / (double) (numPassed + numFailed) * 100;
    	System.out.printf("Percentage of tests passed %.2f: ", passedPercent);
    	System.out.println("");
    	logger.debug("Percentage of tests passed per 1000 tests: " + successRates);
    	
    	in.close();
    	result.close();
    }

	private boolean validateAddress(String resultStreetAddress,
			String resultMunicipality, String resultProvince,
			String resultPostalCode) {
		boolean failed = false;
		
		final String add = address.getAddress();
		if (!resultStreetAddress.equals(add)) {
			logger.debug("Street Address is wrong: Expected '" + resultStreetAddress 
					+ "' Got '" + add + "'");
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
		return failed;
	}
}
