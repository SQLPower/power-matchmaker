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

package ca.sqlpower.matchmaker.munge;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.matchmaker.address.Address;
import ca.sqlpower.matchmaker.address.AddressDatabase;
import ca.sqlpower.matchmaker.address.AddressValidator;
import ca.sqlpower.validation.ValidateResult;

public class AddressCorrectionMungeStep extends AbstractMungeStep {

	private static Logger logger = Logger.getLogger(AddressCorrectionMungeStep.class);
	
	public static final String ADDRESS_CORRECTION_DATA_PATH = "AddressCorrectionDataPath";
	
	private AddressDatabase addressDB;
	
	private boolean addressValid;
	
	private MungeStep inputStep;
	
	public AddressCorrectionMungeStep() {
		super("Address Correction", false);
		
		addChild(new MungeStepOutput<String>("Address Line 1", String.class));
		addChild(new MungeStepOutput<String>("Address Line 2", String.class));
		addChild(new MungeStepOutput<String>("Suite", String.class));
		addChild(new MungeStepOutput<BigDecimal>("Street Number", BigDecimal.class));
		addChild(new MungeStepOutput<String>("Street Number Suffix", String.class));
		addChild(new MungeStepOutput<String>("Street", String.class));
		addChild(new MungeStepOutput<String>("Street Type", String.class));
		addChild(new MungeStepOutput<String>("Street Direction", String.class));
		addChild(new MungeStepOutput<String>("Municipality", String.class));
		addChild(new MungeStepOutput<String>("Province", String.class));
		addChild(new MungeStepOutput<String>("Country", String.class));
		addChild(new MungeStepOutput<String>("Postal/ZIP", String.class));
	
		InputDescriptor input0 = new InputDescriptor("Address Line 1", String.class);
		InputDescriptor input1 = new InputDescriptor("Address Line 2", String.class);
		InputDescriptor input2 = new InputDescriptor("Municipality", String.class);
		InputDescriptor input3 = new InputDescriptor("Province", String.class);
		InputDescriptor input4 = new InputDescriptor("Country", String.class);
		InputDescriptor input5 = new InputDescriptor("Postal/ZIP", String.class);

		super.addInput(input0);
		super.addInput(input1);
		super.addInput(input2);
		super.addInput(input3);
		super.addInput(input4);
		super.addInput(input5);
	}
	
	public void setInputStep(MungeStep inputStep) {
		this.inputStep = inputStep;
	}
	
	@Override
	public void doOpen(Logger logger) throws Exception {
		MatchMakerSession session = getSession();
		MatchMakerSessionContext context = session.getContext();
		setParameter(ADDRESS_CORRECTION_DATA_PATH, context.getAddressCorrectionDataPath());
		
		String addressCorrectionDataPath = getParameter(ADDRESS_CORRECTION_DATA_PATH);
		if (addressCorrectionDataPath == null || addressCorrectionDataPath.length() == 0) {
			throw new IllegalStateException("Address Correction Data Path is empty. Please set the path in User Preferences");
		}
		addressDB = new AddressDatabase(new File(addressCorrectionDataPath));
		
	}
	
	@Override
	public Boolean doCall() throws Exception {

		addressValid = false;
		
		MungeStepOutput addressLine1MSO = getMSOInputs().get(0);
		String addressLine1 = (addressLine1MSO != null) ? (String)addressLine1MSO.getData(): null;
		MungeStepOutput addressLine2MSO = getMSOInputs().get(1);
		String addressLine2 = (addressLine2MSO != null) ? (String)addressLine2MSO.getData() : null;
		MungeStepOutput municipalityMSO = getMSOInputs().get(2);
		String municipality = (municipalityMSO != null) ? (String)municipalityMSO.getData() : null;
		MungeStepOutput provinceMSO = getMSOInputs().get(3);
		String province = (provinceMSO != null) ? (String)provinceMSO.getData() : null;
		MungeStepOutput countryMSO = getMSOInputs().get(4);
		String country = (countryMSO != null) ? (String)countryMSO.getData() : null;
		MungeStepOutput postalCodeMSO = getMSOInputs().get(5);
		String inPostalCode = (postalCodeMSO != null) ? (String)postalCodeMSO.getData() : null;
		
		// nicely formatted 
		String addressString = addressLine1 + ", " + addressLine2 + ", " + municipality + ", " + province + ", " + inPostalCode + ", " + country;
		logger.debug("Parsing Address: " + addressString);
		Address address = Address.parse(addressLine1, municipality, province, inPostalCode, country);
		
		logger.debug("Address that was parsed:\n" + address.toString());
		
		AddressValidator validator = new AddressValidator(addressDB, address);
		List<ValidateResult> results = validator.getResults();

		if (results.size() > 0) { 
			logger.debug("Address '" + addressString + "' was invalid with the following problem(s):");
			
			for (ValidateResult result: results) {
				logger.debug("\tStatus:" + result.getStatus() + " Message: " + result.getMessage());
			}
		} else {
			addressValid = true;
		}
		
		logger.debug("Address after correction:\n" + address.toString());
		
		List<MungeStepOutput> outputs = getChildren(); 
		
		outputs.get(0).setData(address.getStreetAddress());
		outputs.get(1).setData(addressLine2);
		outputs.get(2).setData(address.getSuite());
		outputs.get(3).setData(address.getStreetNumber() != null ? BigDecimal.valueOf(address.getStreetNumber()) : null);
		outputs.get(4).setData(address.getStreetNumberSuffix());
		outputs.get(5).setData(address.getStreet());
		outputs.get(6).setData(address.getStreetType());
		outputs.get(7).setData(address.getStreetDirection());
		outputs.get(8).setData(address.getMunicipality());
		outputs.get(9).setData(address.getProvince());
		outputs.get(10).setData(address.getCountry());
		outputs.get(11).setData(address.getPostalCode());

		return Boolean.TRUE;
	}
	
	/**
	 * A package-private method that will return whether or not the current
	 * address being parsed in this step is valid. Note that the value is
	 * meaningless if there is no address currently being parsed in this step.
	 * Generally, the boolean value applies to the address data it received that
	 * last time the {@link #doCall()} method was called.
	 */
	boolean isAddressValid() {
		return addressValid;
	}
	
	MungeStep getInputStep() {
		return inputStep;
	}
}
