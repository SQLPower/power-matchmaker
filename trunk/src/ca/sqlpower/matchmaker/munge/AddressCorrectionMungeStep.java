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
import java.util.List;

import org.apache.log4j.Logger;

import ca.sqlpower.matchmaker.MatchMakerSession;
import ca.sqlpower.matchmaker.MatchMakerSessionContext;
import ca.sqlpower.matchmaker.address.Address;
import ca.sqlpower.matchmaker.address.AddressDatabase;
import ca.sqlpower.validation.ValidateResult;

public class AddressCorrectionMungeStep extends AbstractMungeStep {

	public static final String ADDRESS_CORRECTION_DATA_PATH = "AddressCorrectionDataPath";
	
	private MungeStepOutput<String> suite;
	private MungeStepOutput<Integer> streetNumber;
	private MungeStepOutput<String> streetNumberSuffix;
	private MungeStepOutput<String> street;
	private MungeStepOutput<String> streetType;
	private MungeStepOutput<String> streetDirection;
	private MungeStepOutput<String> municipalityName;
	private MungeStepOutput<String> provinceName;
	private MungeStepOutput<String> countryName;
	private MungeStepOutput<String> postalCode;

	private AddressDatabase addressDB;
	
	public AddressCorrectionMungeStep() {
		super("Address Correction", false);
		
		addChild(suite = new MungeStepOutput<String>("Suite", String.class));
		addChild(streetNumber = new MungeStepOutput<Integer>("Street Number", Integer.class));
		addChild(streetNumberSuffix = new MungeStepOutput<String>("Street Number Suffix", String.class));
		addChild(street = new MungeStepOutput<String>("Street", String.class));
		addChild(streetType = new MungeStepOutput<String>("Street Type", String.class));
		addChild(streetDirection = new MungeStepOutput<String>("Street Direction", String.class));
		addChild(municipalityName = new MungeStepOutput<String>("Municipality", String.class));
		addChild(provinceName = new MungeStepOutput<String>("Province", String.class));
		addChild(postalCode = new MungeStepOutput<String>("Postal/ZIP", String.class));
		addChild(countryName = new MungeStepOutput<String>("Country", String.class));
	
		InputDescriptor input0 = new InputDescriptor("Address Line 1", String.class);
		InputDescriptor input1 = new InputDescriptor("Address Line 2", String.class);
		InputDescriptor input2 = new InputDescriptor("Municipality", String.class);
		InputDescriptor input3 = new InputDescriptor("Province", String.class);
		InputDescriptor input4 = new InputDescriptor("Postal/ZIP", String.class);
		InputDescriptor input5 = new InputDescriptor("Country", String.class);

		super.addInput(input0);
		super.addInput(input1);
		super.addInput(input2);
		super.addInput(input3);
		super.addInput(input4);
		super.addInput(input5);
	}
	
	@Override
	public void doOpen(Logger log) throws Exception {
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

		String addressLine1 = (String)getMSOInputs().get(0).getData();
		String municipality = (String)getMSOInputs().get(2).getData();
		String province = (String)getMSOInputs().get(3).getData();
		String inPostalCode = (String)getMSOInputs().get(4).getData();
		String country = (String)getMSOInputs().get(5).getData();
		
		// nicely formatted 
		String addressString = addressLine1 + ", " + municipality + ", " + province + ", " + inPostalCode + ", " + country;
		logger.debug("Parsing Address: " + addressString);
		Address address = Address.parse(addressLine1, municipality, province, inPostalCode, country);
		
		List<ValidateResult> results = addressDB.validate(address);

		if (results.size() > 0) { 
			logger.debug("Address '" + addressString + "' was invalid with the following problem(s):");
		}
		
		for (ValidateResult result: results) {
			logger.debug("\tStatus:" + result.getStatus() + " Message: " + result.getMessage());
		}
		
		suite.setData(address.getSuite());
		streetNumber.setData(address.getStreetNumber());
		streetNumberSuffix.setData(address.getStreetNumberSuffix());
		street.setData(address.getStreet());
		streetType.setData(address.getStreetType());
		streetDirection.setData(address.getStreetDirection());
		municipalityName.setData(address.getMunicipality());
		provinceName.setData(address.getProvince());
		countryName.setData(address.getCountry());
		postalCode.setData(address.getPostalCode());
		
		return Boolean.TRUE;
	}
}