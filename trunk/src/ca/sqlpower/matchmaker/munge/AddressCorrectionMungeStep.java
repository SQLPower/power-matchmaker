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

import ca.sqlpower.matchmaker.address.Address;

public class AddressCorrectionMungeStep extends AbstractMungeStep {

	private MungeStepOutput<String> addressLine1;
	private MungeStepOutput<String> addressLine2;
	private MungeStepOutput<String> municipalityName;
	private MungeStepOutput<String> provinceName;
	private MungeStepOutput<String> countryName;
	private MungeStepOutput<String> postalCode;
	
	public AddressCorrectionMungeStep() {
		
		super("Address Correction", false);
		
		addChild(addressLine1 = new MungeStepOutput<String>("Address Line 1", String.class));
		addChild(addressLine2 = new MungeStepOutput<String>("Address Line 2", String.class));
		addChild(municipalityName = new MungeStepOutput<String>("City", String.class));
		addChild(provinceName = new MungeStepOutput<String>("Province", String.class));
		addChild(countryName = new MungeStepOutput<String>("Country", String.class));
		addChild(postalCode = new MungeStepOutput<String>("Postal/ZIP", String.class));
	
		InputDescriptor input0 = new InputDescriptor("Suite", String.class);
		InputDescriptor input1 = new InputDescriptor("Street Number", Integer.class);
		InputDescriptor input2 = new InputDescriptor("Street number Suffix", String.class);
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

//		AddressDatabase addressDB = new AddressDatabase(new File(""));
		
		return super.doCall();
	}
}
