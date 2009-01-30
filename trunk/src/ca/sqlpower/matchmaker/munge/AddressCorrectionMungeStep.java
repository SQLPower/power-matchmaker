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
	
	public AddressCorrectionMungeStep() {
		super("Address Correction", false);
		
		addChild(suite = new MungeStepOutput<String>("Suite", String.class));
		addChild(streetNumber = new MungeStepOutput<Integer>("Street Number", Integer.class));
		addChild(streetNumberSuffix = new MungeStepOutput<String>("Street Number Suffix", String.class));
		addChild(street = new MungeStepOutput<String>("Street", String.class));
		addChild(streetType = new MungeStepOutput<String>("Street Type", String.class));
		addChild(streetType = new MungeStepOutput<String>("Street Direction", String.class));
		addChild(municipalityName = new MungeStepOutput<String>("Municipality", String.class));
		addChild(provinceName = new MungeStepOutput<String>("Province", String.class));
		addChild(postalCode = new MungeStepOutput<String>("Postal/ZIP", String.class));
		addChild(countryName = new MungeStepOutput<String>("Country", String.class));
	
		InputDescriptor input0 = new InputDescriptor("Suite", String.class);
		InputDescriptor input1 = new InputDescriptor("Street Number", Integer.class);
		InputDescriptor input2 = new InputDescriptor("Street Number Suffix", String.class);
		InputDescriptor input3 = new InputDescriptor("Street", String.class);
		InputDescriptor input4 = new InputDescriptor("Street Type", String.class);
		InputDescriptor input5 = new InputDescriptor("Street Direction", String.class);		
		InputDescriptor input6 = new InputDescriptor("Municipality", String.class);
		InputDescriptor input7 = new InputDescriptor("Province", String.class);
		InputDescriptor input8 = new InputDescriptor("Postal/ZIP", String.class);
		InputDescriptor input9 = new InputDescriptor("Country", String.class);

		super.addInput(input0);
		super.addInput(input1);
		super.addInput(input2);
		super.addInput(input3);
		super.addInput(input4);
		super.addInput(input5);
		super.addInput(input6);
		super.addInput(input7);
		super.addInput(input8);
		super.addInput(input9);
	}
	
	@Override
	public void doOpen(Logger log) throws Exception {
		MatchMakerSession session = getSession();
		MatchMakerSessionContext context = session.getContext();
		setParameter(ADDRESS_CORRECTION_DATA_PATH, context.getAddressCorrectionDataPath());
	}
	
	@Override
	public Boolean doCall() throws Exception {
		Address address = new Address();
		address.setSuite((String)getMSOInputs().get(0).getData());
		address.setStreetNumber((Integer)getMSOInputs().get(1).getData());
		address.setStreetNumberSuffix((String)getMSOInputs().get(2).getData());
		address.setStreet((String)getMSOInputs().get(3).getData());
		address.setStreetType((String)getMSOInputs().get(4).getData());
		address.setStreetDirection((String)getMSOInputs().get(5).getData());
		address.setMunicipality((String)getMSOInputs().get(6).getData());
		address.setProvince((String)getMSOInputs().get(7).getData());
		address.setPostalCode((String)getMSOInputs().get(8).getData());
		address.setCountry((String)getMSOInputs().get(9).getData());

		String addressCorrectionDataPath = getParameter(ADDRESS_CORRECTION_DATA_PATH);
		if (addressCorrectionDataPath == null || addressCorrectionDataPath.length() == 0) {
			throw new IllegalStateException("Address Correction Data Path is empty. Please set the path in User Preferences");
		}
		
		AddressDatabase addressDB = new AddressDatabase(new File(addressCorrectionDataPath));
		
		List<ValidateResult> results = addressDB.validate(address);
		
		if (results.size() > 0) {
			
		} else {
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
		}
		
		return Boolean.TRUE;
	}
}
